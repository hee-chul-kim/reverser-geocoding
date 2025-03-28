package com.example.daily.service

import com.example.common.entity.RoadNameAddress
import com.example.common.entity.RoadNameAddressId
import com.example.common.entity.RoadNameAddressEntrance
import com.example.common.entity.RoadNameAddressEntranceId
import com.example.common.repository.RoadNameAddressRepository
import com.example.common.repository.RoadNameAddressEntranceRepository
import com.example.common.util.CoordinateTransformer
import com.example.daily.config.FileConfig
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Paths
import java.time.ZonedDateTime

@Service
open class RoadNameAddressProcessor(
    private val roadNameAddressRepository: RoadNameAddressRepository,
    private val roadNameAddressEntranceRepository: RoadNameAddressEntranceRepository,
    private val fileConfig: FileConfig,
    private val coordinateTransformer: CoordinateTransformer
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val csvMapper = CsvMapper().apply { registerKotlinModule() }
    private val charset = Charset.forName("EUC-KR")

    fun processMonthlyData(year: Int, month: String) {
        logger.info("Starting to process data for $year/$month")
        val baseDir = Paths.get(fileConfig.baseDir, "$year$month")
        
        // 도로명 주소 파일 처리
        processRoadAddressFiles(baseDir.resolve("road"))
        
        // 출입구 좌표 파일 처리
        processEntranceFiles(baseDir.resolve("coords"))
        
        logger.info("Completed processing data for $year/$month")
    }

    private fun processRoadAddressFiles(roadDir: java.nio.file.Path) {
        val dir = roadDir.toFile()
        if (!dir.exists() || !dir.isDirectory) {
            logger.warn("Road name address directory not found: ${dir.absolutePath}")
            return
        }

        dir.listFiles()?.filter { it.isFile && it.extension == "txt" }?.forEach { file ->
            try {
                logger.info("Processing road name address file: ${file.name}")
                processAddressFile(file.absolutePath)
                // 처리 완료된 파일은 .processed 확장자 추가
                val processedFile = File(file.absolutePath + ".processed")
                // TODO - 테스트할 때는 제외
                // file.renameTo(processedFile)
            } catch (e: Exception) {
                logger.error("Error processing road name address file ${file.name}: ${e.message}", e)
            }
        }
    }

    private fun processEntranceFiles(coordsDir: java.nio.file.Path) {
        val dir = coordsDir.toFile()
        if (!dir.exists() || !dir.isDirectory) {
            logger.warn("Entrance coordinates directory not found: ${dir.absolutePath}")
            return
        }

        dir.listFiles()?.filter { it.isFile && it.extension == "txt" }?.forEach { file ->
            try {
                logger.info("Processing entrance coordinates file: ${file.name}")
                processEntranceFile(file.absolutePath)
                // 처리 완료된 파일은 .processed 확장자 추가
                val processedFile = File(file.absolutePath + ".processed")
                // TODO - 테스트할 때는 제외
                //file.renameTo(processedFile)
            } catch (e: Exception) {
                logger.error("Error processing entrance coordinates file ${file.name}: ${e.message}", e)
            }
        }
    }

    open fun processAddressFile(filePath: String) {
        logger.info("Starting to process address file: $filePath")
        
        val schema = CsvSchema.builder()
            .addColumn("addressManagementNo")         // 1. 도로명주소관리번호
            .addColumn("legalDongCode")              // 2. 법정동코드
            .addColumn("sidoName")                   // 3. 시도명
            .addColumn("sigunguName")                // 4. 시군구명
            .addColumn("legalEmdName")               // 5. 법정읍면동명
            .addColumn("legalRiName")                // 6. 법정리명
            .addColumn("isMountain")                 // 7. 산여부
            .addColumn("jibunMainNo")                // 8. 지번본번(번지)
            .addColumn("jibunSubNo")                 // 9. 지번부번(호)
            .addColumn("roadNameCode")               // 10. 도로명코드
            .addColumn("roadName")                   // 11. 도로명
            .addColumn("isBasement")                 // 12. 지하여부
            .addColumn("buildingMainNo")             // 13. 건물본번
            .addColumn("buildingSubNo")              // 14. 건물부번
            .addColumn("adminDongCode")              // 15. 행정동코드
            .addColumn("adminDongName")              // 16. 행정동명
            .addColumn("zipCode")                    // 17. 기초구역번호(우편번호)
            .addColumn("previousAddress")            // 18. 이전도로명주소
            .addColumn("effectiveDate")              // 19. 효력발생일
            .addColumn("isApartment")                // 20. 공동주택구분
            .addColumn("changeReasonCode")           // 21. 이동사유코드
            .addColumn("buildingName")               // 22. 건축물대장건물명
            .addColumn("sigunguBuildingName")        // 23. 시군구용건물명
            .addColumn("note")                       // 24. 비고
            .build()
            .withHeader()
            .withColumnSeparator('|')
            .withNullValue("")
            .withQuoteChar('"')

        try {
            val file = File(filePath)
            if (!file.exists()) {
                logger.error("File not found: $filePath")
                return
            }

            val reader = InputStreamReader(FileInputStream(file), charset)
            val iterator: MappingIterator<RoadNameAddressData> = csvMapper
                .readerFor(RoadNameAddressData::class.java)
                .with(schema)
                .readValues(reader)

            var processed = 0
            val maxRecords = 100  // TODO - 처리할 최대 레코드 수 제한

            iterator.use { iter ->
                for (data in iter) {
                    if (processed >= maxRecords) {
                        logger.info("Reached maximum record limit of $maxRecords")
                        break
                    }

                    try {
                        if (data.addressManagementNo.isNullOrBlank()) {
                            logger.warn("Skipping record with null or blank addressManagementNo")
                            continue
                        }

                        val id = RoadNameAddressId(
                            addressManagementNo = data.addressManagementNo,
                            roadNameCode = data.roadNameCode ?: "",
                            isBasement = data.isBasement ?: "0",
                            buildingMainNo = data.buildingMainNo?.toIntOrNull() ?: 0,
                            buildingSubNo = data.buildingSubNo?.toIntOrNull() ?: 0
                        )

                        when (data.changeReasonCode) {
                            null, "31", "34", "63" -> { // 신규, 변경, 폐지
                                val existingAddress = roadNameAddressRepository.findById(id).orElse(null)
                                
                                when (data.changeReasonCode) {
                                    null, "31", "34" -> { // 신규 또는 변경
                                        if (existingAddress != null) {
                                            // Update existing address
                                            val updatedAddress = RoadNameAddress(
                                                addressManagementNo = id.addressManagementNo,
                                                legalDongCode = data.legalDongCode ?: "",
                                                sidoName = data.sidoName,
                                                sigunguName = data.sigunguName,
                                                legalEmdName = data.legalEmdName,
                                                legalRiName = data.legalRiName,
                                                isMountain = data.isMountain?.equals("1") ?: false,
                                                jibunMainNo = data.jibunMainNo?.toIntOrNull() ?: 0,
                                                jibunSubNo = data.jibunSubNo?.toIntOrNull() ?: 0,
                                                roadNameCode = id.roadNameCode,
                                                roadName = data.roadName,
                                                isBasement = id.isBasement,
                                                buildingMainNo = id.buildingMainNo,
                                                buildingSubNo = id.buildingSubNo,
                                                adminDongCode = data.adminDongCode,
                                                adminDongName = data.adminDongName,
                                                zipCode = data.zipCode,
                                                previousAddress = data.previousAddress,
                                                effectiveDate = data.effectiveDate,
                                                isApartment = data.isApartment?.equals("1") ?: false,
                                                changeReasonCode = data.changeReasonCode,
                                                buildingName = data.buildingName,
                                                sigunguBuildingName = data.sigunguBuildingName,
                                                note = data.note,
                                                entrance = existingAddress.entrance,
                                                createdAt = existingAddress.createdAt,
                                                updatedAt = ZonedDateTime.now(),
                                                deletedAt = null,
                                                version = existingAddress.version
                                            )
                                            roadNameAddressRepository.save(updatedAddress)
                                            logger.info("Updated address: ${id.addressManagementNo}")
                                        } else {
                                            // Create new address
                                            val newAddress = RoadNameAddress(
                                                addressManagementNo = id.addressManagementNo,
                                                legalDongCode = data.legalDongCode ?: "",
                                                sidoName = data.sidoName,
                                                sigunguName = data.sigunguName,
                                                legalEmdName = data.legalEmdName,
                                                legalRiName = data.legalRiName,
                                                isMountain = data.isMountain?.equals("1") ?: false,
                                                jibunMainNo = data.jibunMainNo?.toIntOrNull() ?: 0,
                                                jibunSubNo = data.jibunSubNo?.toIntOrNull() ?: 0,
                                                roadNameCode = id.roadNameCode,
                                                roadName = data.roadName,
                                                isBasement = id.isBasement,
                                                buildingMainNo = id.buildingMainNo,
                                                buildingSubNo = id.buildingSubNo,
                                                adminDongCode = data.adminDongCode,
                                                adminDongName = data.adminDongName,
                                                zipCode = data.zipCode,
                                                previousAddress = data.previousAddress,
                                                effectiveDate = data.effectiveDate,
                                                isApartment = data.isApartment?.equals("1") ?: false,
                                                changeReasonCode = data.changeReasonCode,
                                                buildingName = data.buildingName,
                                                sigunguBuildingName = data.sigunguBuildingName,
                                                note = data.note
                                            )
                                            roadNameAddressRepository.save(newAddress)
                                            logger.info("Created new address: ${id.addressManagementNo}")
                                        }
                                    }
                                    "63" -> { // 폐지
                                        if (existingAddress != null) {
                                            existingAddress.deletedAt = ZonedDateTime.now()
                                            roadNameAddressRepository.save(existingAddress)
                                            logger.info("Marked address as deleted: ${id.addressManagementNo}")
                                        }
                                    }
                                }
                            }
                            else -> {
                                logger.debug("Skipping record with change reason code: ${data.changeReasonCode}")
                            }
                        }
                        processed++
                    } catch (e: Exception) {
                        logger.error("Error processing record: ${e.message}", e)
                    }
                }
            }
            logger.info("Processed $processed records from $filePath")
        } catch (e: Exception) {
            logger.error("Error processing file $filePath: ${e.message}", e)
            throw e
        }
    }

    open fun processEntranceFile(filePath: String) {
        logger.info("Starting to process entrance file: $filePath")
        
        val schema = CsvSchema.builder()
            .addColumn("addressManagementNo")         // 1. 도로명주소관리번호
            .addColumn("legalDongCode")              // 2. 법정동코드
            .addColumn("sidoName")                   // 3. 시도명
            .addColumn("sigunguName")                // 4. 시군구명
            .addColumn("legalEmdName")               // 5. 법정읍면동명
            .addColumn("legalRiName")                // 6. 법정리명
            .addColumn("roadNameCode")               // 7. 도로명코드
            .addColumn("roadName")                   // 8. 도로명
            .addColumn("isBasement")                 // 9. 지하여부
            .addColumn("buildingMainNo")             // 10. 건물본번
            .addColumn("buildingSubNo")              // 11. 건물부번
            .addColumn("zipCode")                    // 12. 기초구역번호
            .addColumn("effectiveDate")              // 13. 효력발생일
            .addColumn("changeReasonCode")           // 14. 이동사유코드
            .addColumn("entranceNo")                 // 15. 출입구일련번호
            .addColumn("entranceType")               // 16. 출입구구분
            .addColumn("entranceCategory")           // 17. 출입구 유형
            .addColumn("longitude")                  // 18. 출입구좌표X
            .addColumn("latitude")                   // 19. 출입구좌표Y
            .build()
            .withHeader()
            .withColumnSeparator('|')
            .withNullValue("")
            .withQuoteChar('"')

        try {
            val file = File(filePath)
            if (!file.exists()) {
                logger.error("File not found: $filePath")
                return
            }

            val reader = InputStreamReader(FileInputStream(file), charset)
            val iterator: MappingIterator<EntranceData> = csvMapper
                .readerFor(EntranceData::class.java)
                .with(schema)
                .readValues(reader)

            var processed = 0
            val maxRecords = 100  // TODO - 처리할 최대 레코드 수 제한

            iterator.use { iter ->
                for (data in iter) {
                    if (processed >= maxRecords) {
                        logger.info("Reached maximum record limit of $maxRecords")
                        break
                    }

                    try {
                        if (data.addressManagementNo.isNullOrBlank()) {
                            logger.warn("Skipping record with null or blank addressManagementNo")
                            continue
                        }

                        // 좌표 변환
                        val (transformedLat, transformedLon) = coordinateTransformer.transform(
                            data.latitude?.toDoubleOrNull() ?: 0.0,
                            data.longitude?.toDoubleOrNull() ?: 0.0
                        )

                        // 출입구 정보 생성 또는 업데이트
                        val entranceId = RoadNameAddressEntranceId(
                            addressManagementNo = data.addressManagementNo,
                            roadNameCode = data.roadNameCode ?: "",
                            isBasement = data.isBasement ?: "0",
                            buildingMainNo = data.buildingMainNo?.toIntOrNull() ?: 0,
                            buildingSubNo = data.buildingSubNo?.toIntOrNull() ?: 0
                        )

                        val entrance = RoadNameAddressEntrance(
                            entranceNo = data.entranceNo ?: "1",
                            addressManagementNo = entranceId.addressManagementNo,
                            roadNameCode = entranceId.roadNameCode,
                            isBasement = entranceId.isBasement,
                            buildingMainNo = entranceId.buildingMainNo,
                            buildingSubNo = entranceId.buildingSubNo,
                            entranceType = data.entranceType ?: "RM",
                            entranceCategory = data.entranceCategory ?: "01",
                            longitude = transformedLon,
                            latitude = transformedLat
                        )

                        roadNameAddressEntranceRepository.save(entrance)
                        processed++
                    } catch (e: Exception) {
                        logger.error("Error processing record: ${e.message}", e)
                    }
                }
            }
            logger.info("Processed $processed records from $filePath")
        } catch (e: Exception) {
            logger.error("Error processing file $filePath: ${e.message}", e)
            throw e
        }
    }

    data class RoadNameAddressData(
        val addressManagementNo: String? = null,      // 1. 도로명주소관리번호
        val legalDongCode: String? = null,            // 2. 법정동코드
        val sidoName: String? = null,                 // 3. 시도명
        val sigunguName: String? = null,              // 4. 시군구명
        val legalEmdName: String? = null,             // 5. 법정읍면동명
        val legalRiName: String? = null,              // 6. 법정리명
        val isMountain: String? = null,               // 7. 산여부
        val jibunMainNo: String? = null,              // 8. 지번본번(번지)
        val jibunSubNo: String? = null,               // 9. 지번부번(호)
        val roadNameCode: String? = null,             // 10. 도로명코드
        val roadName: String? = null,                 // 11. 도로명
        val isBasement: String? = null,               // 12. 지하여부
        val buildingMainNo: String? = null,           // 13. 건물본번
        val buildingSubNo: String? = null,            // 14. 건물부번
        val adminDongCode: String? = null,            // 15. 행정동코드
        val adminDongName: String? = null,            // 16. 행정동명
        val zipCode: String? = null,                  // 17. 기초구역번호(우편번호)
        val previousAddress: String? = null,          // 18. 이전도로명주소
        val effectiveDate: String? = null,            // 19. 효력발생일
        val isApartment: String? = null,              // 20. 공동주택구분
        val changeReasonCode: String? = null,         // 21. 이동사유코드
        val buildingName: String? = null,             // 22. 건축물대장건물명
        val sigunguBuildingName: String? = null,      // 23. 시군구용건물명
        val note: String? = null                      // 24. 비고
    )

    data class EntranceData(
        val addressManagementNo: String? = null,      // 1. 도로명주소관리번호
        val legalDongCode: String? = null,            // 2. 법정동코드
        val sidoName: String? = null,                 // 3. 시도명
        val sigunguName: String? = null,              // 4. 시군구명
        val legalEmdName: String? = null,             // 5. 법정읍면동명
        val legalRiName: String? = null,              // 6. 법정리명
        val roadNameCode: String? = null,             // 7. 도로명코드
        val roadName: String? = null,                 // 8. 도로명
        val isBasement: String? = null,               // 9. 지하여부
        val buildingMainNo: String? = null,           // 10. 건물본번
        val buildingSubNo: String? = null,            // 11. 건물부번
        val zipCode: String? = null,                  // 12. 기초구역번호
        val effectiveDate: String? = null,            // 13. 효력발생일
        val changeReasonCode: String? = null,         // 14. 이동사유코드
        val entranceNo: String? = null,               // 15. 출입구일련번호
        val entranceType: String? = null,             // 16. 출입구구분
        val entranceCategory: String? = null,         // 17. 출입구 유형
        val longitude: String? = null,                // 18. 출입구좌표X
        val latitude: String? = null                  // 19. 출입구좌표Y
    )
} 