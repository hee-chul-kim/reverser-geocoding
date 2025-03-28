package com.example.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "역지오코딩 응답")
data class ReverseGeocodeResponse(
    @Schema(description = "주소 정보")
    val addressInfo: AddressInfo,
    
    @Schema(description = "도로 좌표")
    val roadCoord: Coordinates
) {
    @Schema(description = "주소 상세 정보")
    data class AddressInfo(
        @Schema(description = "전체 주소")
        val fullAddress: String,
        
        @Schema(description = "시/도")
        val city_do: String,
        
        @Schema(description = "구/군")
        val gu_gun: String,
        
        @Schema(description = "리 (없을 수 있음)")
        val ri: String?,
        
        @Schema(description = "도로명")
        val roadName: String,
        
        @Schema(description = "건물번호 (본번-부번)")
        val buildingIndex: String,
        
        @Schema(description = "건물명 (없을 수 있음)")
        val buildingName: String?,
        
        @Schema(description = "입력 좌표와의 거리 (미터)")
        val mappingDistance: Int,
        
        @Schema(description = "도로명코드")
        val roadCode: String?,
        
        @Schema(description = "지번 (없을 수 있음)")
        val bunji: String?
    )

    @Schema(description = "좌표 정보")
    data class Coordinates(
        @Schema(description = "위도")
        val latitude: Double,
        
        @Schema(description = "경도")
        val longitude: Double
    )
} 