package com.example.api.controller

import com.example.api.model.ReverseGeocodeResponse
import com.example.api.service.ReverseGeocodeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reverse-geocode")
@Validated
@Tag(name = "Reverse Geocoding", description = "좌표를 주소로 변환하는 API")
class ReverseGeocodeController(
    private val reverseGeocodeService: ReverseGeocodeService
) {
    @Operation(
        summary = "좌표를 주소로 변환",
        description = "위도와 경도를 입력받아 가장 가까운 도로명주소를 반환합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "주소를 찾을 수 없음")
        ]
    )
    @GetMapping
    fun reverseGeocode(
        @Parameter(description = "위도 (WGS84 좌표계: -90 ~ 90, TM 좌표계: 0 ~ 1000000)")
        @RequestParam
        @DecimalMin("-90.0")
        @DecimalMax("1000000.0")
        lat: Double,

        @Parameter(description = "경도 (WGS84 좌표계: -180 ~ 180, TM 좌표계: 0 ~ 1000000)")
        @RequestParam
        @DecimalMin("-180.0")
        @DecimalMax("1000000.0")
        lon: Double,

        @Parameter(description = "좌표계 (WGS84 또는 TM)", example = "WGS84")
        @RequestParam(defaultValue = "WGS84")
        coordType: String
    ): ResponseEntity<ReverseGeocodeResponse> {
        val response = reverseGeocodeService.reverseGeocode(lat, lon, coordType)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(response)
    }
} 