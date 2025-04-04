package com.example.common.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.DistanceUnit
import co.elastic.clients.elasticsearch._types.GeoLocation
import co.elastic.clients.elasticsearch._types.SortOptions
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.GeoDistanceQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.*
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation
import co.elastic.clients.elasticsearch.indices.*
import co.elastic.clients.elasticsearch.indices.UpdateAliasesRequest
import com.example.common.config.ElasticsearchProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class ElasticsearchService(
    private val elasticsearchClient: ElasticsearchClient,
    private val properties: ElasticsearchProperties
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val MAX_RETRY_COUNT = 3
    private val RETRY_DELAY_MS = 1000L
    private val indexAlias = "address_geo"

    fun createAddressGeoIndex(indexName: String) {
        try {
            // 이미 존재하는 인덱스인지 확인
            if (elasticsearchClient.indices().exists { it.index(indexName) }.value()) {
                logger.warn("이미 존재하는 인덱스입니다: $indexName")
                return
            }

            val createIndexRequest = CreateIndexRequest.Builder()
                .index(indexName)
                .settings { s -> 
                    s.numberOfShards(properties.indexSettings.numberOfShards.toString())
                     .numberOfReplicas(properties.indexSettings.numberOfReplicas.toString())
                }
                .mappings { m ->
                    m.properties("fullAddress") { p -> p.text { t -> t } }
                     .properties("location") { p -> 
                         p.geoPoint { g -> g }
                     }
                }
                .build()

            elasticsearchClient.indices().create(createIndexRequest)
            logger.info("인덱스 생성 완료: $indexName")
        } catch (e: IOException) {
            logger.error("인덱스 생성 중 IO 오류 발생: $indexName", e)
            throw ElasticsearchOperationException("인덱스 생성 실패", e)
        } catch (e: Exception) {
            logger.error("인덱스 생성 실패: $indexName", e)
            throw ElasticsearchOperationException("인덱스 생성 실패", e)
        }
    }

    fun bulkInsertAddresses(indexName: String, documents: List<AddressGeoDocument>) {
        if (documents.isEmpty()) return

        var retryCount = 0
        var lastException: Exception? = null
        val failedDocuments = mutableListOf<AddressGeoDocument>()

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                val bulkOperations = documents.map { doc ->
                    BulkOperation.Builder()
                        .index { i ->
                            i.index(indexName)
                             .document(mapOf(
                                 "fullAddress" to doc.fullAddress,
                                 "location" to mapOf(
                                     "lat" to doc.latitude,
                                     "lon" to doc.longitude
                                 )
                             ))
                        }.build()
                }

                val bulkRequest = BulkRequest.Builder()
                    .operations(bulkOperations)
                    .build()
                
                val response = elasticsearchClient.bulk(bulkRequest)
                
                if (response.errors()) {
                    logger.error("벌크 입력 중 일부 문서 처리 실패")
                    response.items().forEachIndexed { index, item ->
                        val error = item.error()
                        if (error != null) {
                            logger.error("문서 처리 실패 - 인덱스: ${item.index()}, 이유: ${error.reason()}")
                            failedDocuments.add(documents[index])
                        }
                    }
                    // 실패한 문서가 있으면 재시도
                    if (failedDocuments.isNotEmpty()) {
                        throw BulkOperationException("일부 문서 처리 실패")
                    }
                }
                
                logger.info("벌크 입력 완료 - 처리된 문서 수: ${documents.size}")
                return
                
            } catch (e: Exception) {
                lastException = e
                retryCount++
                
                if (retryCount < MAX_RETRY_COUNT) {
                    logger.warn("벌크 입력 실패, ${retryCount}번째 재시도 예정 - 문서 수: ${documents.size}")
                    Thread.sleep(RETRY_DELAY_MS * retryCount)
                }
            }
        }

        // 모든 재시도 실패 후
        logger.error("최대 재시도 횟수 초과 - 문서 수: ${documents.size}", lastException)
        throw ElasticsearchOperationException("벌크 입력 실패", lastException)
    }

    fun updateAddressGeoAlias(newIndexName: String) {
        try {
            // 새 인덱스 존재 여부 확인
            if (!elasticsearchClient.indices().exists { it.index(newIndexName) }.value()) {
                throw IllegalStateException("새로운 인덱스가 존재하지 않습니다: $newIndexName")
            }

            // 기존 별칭이 가리키는 인덱스 찾기
            val getAliasResponse = elasticsearchClient.indices().getAlias { it.name("address_geo") }
            val oldIndices = getAliasResponse.result().keys
            val oldIndex = oldIndices.firstOrNull()
                ?: throw IllegalStateException("기존 별칭이 가리키는 인덱스가 없습니다.")

            // 별칭 업데이트 (원자적 작업)
            elasticsearchClient.indices().updateAliases(
                UpdateAliasesRequest.of { builder ->
                    builder
                        .actions { requests ->
                            requests.add { addBuilder ->
                                addBuilder
                                    .index(newIndexName)
                                    .alias("address_geo")
                            }
                        }.actions { requests ->
                            requests.remove { removeBuilder ->
                                removeBuilder
                                    .index(oldIndex)
                                    .alias("address_geo")
                            }
                        }
                },
            )
            logger.info("별칭 업데이트 완료 - 새 인덱스: $newIndexName")

            // 이전 인덱스 삭제
            oldIndices.forEach { oldIndex ->
                try {
                    elasticsearchClient.indices().delete(
                        DeleteIndexRequest.Builder().index(oldIndex).build()
                    )
                    logger.info("이전 인덱스 삭제 완료: $oldIndex")
                } catch (e: Exception) {
                    // 이전 인덱스 삭제 실패는 경고로 처리 (치명적이지 않음)
                    logger.warn("이전 인덱스 삭제 실패: $oldIndex", e)
                }
            }
        } catch (e: Exception) {
            logger.error("별칭 업데이트 실패 - 새 인덱스: $newIndexName", e)
            throw ElasticsearchOperationException("별칭 업데이트 실패", e)
        }
    }

    fun findNearestAddress(lat: Double, lon: Double): AddressSearchResult? {
        try {
            val geoQuery: Query = GeoDistanceQuery.of { g ->
                g.field("location")
                    .distance("1km")
                    .location(
                        GeoLocation.of { loc ->
                            loc.latlon { ll -> ll.lat(lat).lon(lon) }
                        }
                    )
            }._toQuery()

            // 정렬 조건 (_geo_distance)
            val geoSort = SortOptions.of { sort ->
                sort.geoDistance { gd ->
                    gd.field("location")
                        .location { loc -> loc.latlon { ll -> ll.lat(lat).lon(lon) } }
                        .order(SortOrder.Asc)
                        .unit(DistanceUnit.Meters)

                }
            }

            val response = elasticsearchClient.search(
                SearchRequest.Builder()
                    .index(indexAlias)
                    .query(geoQuery)
                    .sort(geoSort)
                    .size(1)
                    .build(),
                AddressSearchResult::class.java
            )

            return response.hits().hits().firstOrNull()?.source()?.apply {
                // 검색 결과와 입력 좌표 사이의 거리 계산
                distance = calculateDistance(lat, lon, this.latitude ?: 0.0, this.longitude ?: 0.0)
            }
        } catch (e: Exception) {
            logger.error("주소 검색 중 오류 발생", e)
            return null
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 // 지구 반경 (미터)
        val φ1 = lat1 * Math.PI / 180
        val φ2 = lat2 * Math.PI / 180
        val Δφ = (lat2 - lat1) * Math.PI / 180
        val Δλ = (lon2 - lon1) * Math.PI / 180

        val a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                Math.cos(φ1) * Math.cos(φ2) *
                Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return r * c
    }
}

data class AddressGeoDocument(
    val fullAddress: String,
    val latitude: Double?,
    val longitude: Double?
)

open class AddressSearchResult() {
    var fullAddress: String? = null
    val latitude: Double?
        get() = location?.lat
    val longitude: Double?
        get() = location?.lon
    var location: Location? = null
    var distance: Double? = null
}

open class Location() {
    val lat: Double = 0.0
    val lon: Double = 0.0
}

class ElasticsearchOperationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class BulkOperationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) 