package com.example.daily.job

import com.example.daily.service.RoadNameAddressProcessor
import com.example.common.repository.RoadNameAddressRepository
import com.example.common.repository.RoadNameAddressEntranceRepository
import com.example.common.service.ElasticsearchService
import com.example.common.service.AddressGeoDocument
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RoadNameAddressJob(
    private val roadNameAddressProcessor: RoadNameAddressProcessor,
    private val roadNameAddressRepository: RoadNameAddressRepository,
    private val roadNameAddressEntranceRepository: RoadNameAddressEntranceRepository,
    private val elasticsearchService: ElasticsearchService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val BULK_SIZE = 2000

    fun initData(year: Int, month: String) {
        logger.info("도로명주소 데이터 초기화 시작 - 연도: $year, 월: $month")
        
        if (!isValidInput(year, month)) {
            logger.error("잘못된 입력값 - 연도: $year, 월: $month")
            throw IllegalArgumentException("잘못된 입력값: 연도는 숫자여야 하고, 월은 2자리 숫자여야 합니다 (예: 03)")
        }

        try {
            // Step 1: 기존 데이터 전체 삭제
            logger.info("Step 1: 기존 데이터 삭제 시작")
            clearExistingData()
            logger.info("기존 데이터 삭제 완료")

            // Step 2: 새로운 데이터 처리
            logger.info("Step 2: 새로운 데이터 처리 시작")
            roadNameAddressProcessor.processMonthlyData(year, month)
            logger.info("도로명주소 데이터 초기화 완료 - 연도: $year, 월: $month")

            // Step 3: Elasticsearch 인덱스 생성
            val epochTimestamp = Instant.now().epochSecond
            val newIndexName = "address_geo_$epochTimestamp"
            logger.info("Step 3: Elasticsearch 인덱스 생성 시작 - $newIndexName")
            elasticsearchService.createAddressGeoIndex(newIndexName)
            logger.info("Elasticsearch 인덱스 생성 완료")

            // Step 4: 데이터 Elasticsearch에 벌크 입력
            logger.info("Step 4: Elasticsearch 데이터 입력 시작")
            bulkInsertToElasticsearch(newIndexName)
            logger.info("Elasticsearch 데이터 입력 완료")

            // Step 5: 별칭 설정
            logger.info("Step 5: Elasticsearch 별칭 설정 시작")
            elasticsearchService.updateAddressGeoAlias(newIndexName)
            logger.info("Elasticsearch 별칭 설정 완료")

        } catch (e: Exception) {
            logger.error("도로명주소 데이터 초기화 실패 - 연도: $year, 월: $month, 오류: ${e.message}", e)
            throw e
        }
    }

    private fun bulkInsertToElasticsearch(indexName: String) {
        var page = 0
        var processedCount = 0
        
        while (true) {
            val addresses = roadNameAddressRepository.findAllWithEntrances(PageRequest.of(page, BULK_SIZE))
            if (!addresses.hasContent()) break

            val documents = addresses.content.flatMap { address ->
                address.entrances.map { entrance ->
                    AddressGeoDocument(
                        fullAddress = buildFullAddress(address),
                        latitude = entrance.latitude.toDouble(),
                        longitude = entrance.longitude.toDouble()
                    )
                }
            }

            elasticsearchService.bulkInsertAddresses(indexName, documents)
            processedCount += documents.size
            logger.info("처리된 문서 수: $processedCount")
            
            page++
        }
    }

    private fun buildFullAddress(address: RoadNameAddress): String {
        return buildString {
            append(address.cityProvinceName)
            append(" ")
            append(address.cityCountyName)
            append(" ")
            if (!address.townName.isNullOrBlank()) {
                append(address.townName)
                append(" ")
            }
            if (!address.villageName.isNullOrBlank()) {
                append(address.villageName)
                append(" ")
            }
            append(address.roadName)
            append(" ")
            append(address.buildingMainNo)
            if (address.buildingSubNo > 0) {
                append("-")
                append(address.buildingSubNo)
            }
        }
    }

    private fun clearExistingData() {
        try {
            logger.info("도로명주소 출입구 테이블 데이터 삭제 시작")
            roadNameAddressEntranceRepository.deleteAll()
            logger.info("도로명주소 출입구 테이블 데이터 삭제 완료")
            
            logger.info("도로명주소 테이블 데이터 삭제 시작")
            roadNameAddressRepository.deleteAll()
            logger.info("도로명주소 테이블 데이터 삭제 완료")
        } catch (e: Exception) {
            logger.error("데이터 삭제 중 오류 발생: ${e.message}", e)
            throw e
        }
    }

    private fun isValidInput(year: Int, month: String): Boolean {
        return year > 0 && month.length == 2 && month.matches(Regex("\\d{2}"))
    }
} 