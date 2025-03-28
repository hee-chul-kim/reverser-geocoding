package com.example.api.service

import com.example.api.model.ReverseGeocodeResponse
import com.example.api.model.ReverseGeocodeResponse.AddressInfo
import com.example.api.model.ReverseGeocodeResponse.Coordinates
import com.example.common.service.ElasticsearchService
import com.example.common.util.CoordinateTransformer
import org.springframework.stereotype.Service

@Service
class ReverseGeocodeService(
    private val elasticsearchService: ElasticsearchService,
    private val coordinateTransformer: CoordinateTransformer
) {
    fun reverseGeocode(lat: Double, lon: Double, coordType: String = "WGS84"): ReverseGeocodeResponse? {
        // 좌표계 변환이 필요한 경우
        val (searchLat, searchLon) = when (coordType.uppercase()) {
            "WGS84" -> lat to lon
            "TM" -> {
                val transformed = coordinateTransformer.transform(lon, lat)
                transformed
            }
            else -> throw IllegalArgumentException("Unsupported coordinate type: $coordType")
        }

        // Elasticsearch에서 가장 가까운 주소 검색
        val result = elasticsearchService.findNearestAddress(searchLat, searchLon)
            ?: return null

        return ReverseGeocodeResponse(
            addressInfo = AddressInfo(
                fullAddress = result.fullAddress ?: "",
                city_do = "",  // 현재 데이터에 포함되지 않음
                gu_gun = "",   // 현재 데이터에 포함되지 않음
                ri = null,     // 현재 데이터에 포함되지 않음
                roadName = "", // 현재 데이터에 포함되지 않음
                buildingIndex = "", // 현재 데이터에 포함되지 않음
                buildingName = null, // 현재 데이터에 포함되지 않음
                mappingDistance = result.distance?.toInt() ?: 0,
                roadCode = null, // 현재 데이터에 포함되지 않음
                bunji = null    // 현재 데이터에 포함되지 않음
            ),
            roadCoord = Coordinates(
                latitude = result.latitude ?: 0.0,
                longitude = result.longitude ?: 0.0
            )
        )
    }
} 