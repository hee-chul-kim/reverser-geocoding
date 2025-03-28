package com.example.common.util

import org.locationtech.proj4j.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CoordinateTransformer {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        // UTM-K 좌표 유효 범위
        private const val MIN_X = 900000.0
        private const val MAX_X = 1100000.0
        private const val MIN_Y = 1600000.0
        private const val MAX_Y = 2300000.0

        // 좌표계 변환기 초기화
        private val factory = CRSFactory()
        private val srcCrs = factory.createFromName("EPSG:5179") // UTM-K
        private val dstCrs = factory.createFromName("EPSG:4326") // WGS84
        private val transform = BasicCoordinateTransform(srcCrs, dstCrs)
    }

    /**
     * UTM-K 좌표를 WGS84 경위도 좌표로 변환합니다.
     * 
     * @param x UTM-K X 좌표 (단위: 미터)
     * @param y UTM-K Y 좌표 (단위: 미터)
     * @return WGS84 좌표 Pair (위도, 경도) (단위: 도)
     */
    fun transform(y: Double, x: Double): Pair<Double, Double> {
        try {
            // 좌표 범위 검증
            if (x !in MIN_X..MAX_X || y !in MIN_Y..MAX_Y) {
                logger.warn("좌표가 유효한 범위를 벗어났습니다: x=$x, y=$y")
                return Pair(0.0, 0.0)
            }

            // 좌표 변환
            val srcCoord = ProjCoordinate(x, y)
            val dstCoord = ProjCoordinate()
            transform.transform(srcCoord, dstCoord)

            val lat = dstCoord.y
            val lon = dstCoord.x

            // 좌표 범위 재검증
            if (lon < 124.0 || lon > 132.0 || lat < 33.0 || lat > 43.0) {
                logger.warn("변환된 좌표가 한반도 범위를 벗어났습니다: lon=$lon, lat=$lat")
                return Pair(0.0, 0.0)
            }

            logger.debug("좌표 변환 완료 - 입력(x,y): ($x, $y) -> 출력(lat,lon): ($lat, $lon)")
            return Pair(lat, lon)
        } catch (e: Exception) {
            logger.error("좌표 변환 실패 - x: $x, y: $y", e)
            return Pair(0.0, 0.0)
        }
    }

    /**
     * UTM-K 좌표를 WGS84 경위도 좌표로 변환합니다.
     * 
     * @param x UTM-K X 좌표 (단위: 미터)
     * @param y UTM-K Y 좌표 (단위: 미터)
     * @return WGS84 좌표 Pair (경도, 위도) (단위: 도)
     * @throws IllegalArgumentException 좌표가 유효한 범위를 벗어난 경우
     */
    fun transformToWGS84(x: Double, y: Double): Pair<Double, Double> {
        try {
            // 좌표 범위 검증
            require(x in MIN_X..MAX_X) { "X 좌표가 유효한 범위를 벗어났습니다: $x" }
            require(y in MIN_Y..MAX_Y) { "Y 좌표가 유효한 범위를 벗어났습니다: $y" }

            // 좌표 변환
            val srcCoord = ProjCoordinate(x, y)
            val dstCoord = ProjCoordinate()
            transform.transform(srcCoord, dstCoord)

            val lon = dstCoord.x
            val lat = dstCoord.y

            // 좌표 범위 재검증
            require(lon >= 124.0 && lon <= 132.0) { "변환된 경도가 한반도 범위를 벗어났습니다: $lon" }
            require(lat >= 33.0 && lat <= 43.0) { "변환된 위도가 한반도 범위를 벗어났습니다: $lat" }

            logger.debug("좌표 변환 완료 - 입력(x,y): ($x, $y) -> 출력(lon,lat): ($lon, $lat)")
            return Pair(lon, lat)
        } catch (e: Exception) {
            logger.error("좌표 변환 실패 - x: $x, y: $y", e)
            throw e
        }
    }
} 