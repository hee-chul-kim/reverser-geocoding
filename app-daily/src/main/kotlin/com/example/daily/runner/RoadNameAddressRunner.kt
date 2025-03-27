package com.example.daily.runner

import com.example.daily.job.RoadNameAddressJob
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * 도로명주소 데이터 초기화를 위한 ApplicationRunner
 * 실행 예시:
 * - 기본값 사용: java -jar app.jar
 * - 특정 연월 지정: java -jar app.jar --year=2024 --month=04
 */
@Component
class RoadNameAddressRunner(
    private val roadNameAddressJob: RoadNameAddressJob
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        // 기본값: 2025년 3월
        val year = args.getOptionValues("year")?.firstOrNull()?.toIntOrNull() ?: 2025
        val month = args.getOptionValues("month")?.firstOrNull() ?: "03"

        logger.info("도로명주소 데이터 초기화 시작 - 연도: $year, 월: $month")
        roadNameAddressJob.initData(year, month)
    }
} 