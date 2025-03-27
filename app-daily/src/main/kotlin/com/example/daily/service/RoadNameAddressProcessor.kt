package com.example.daily.service

import com.example.common.entity.RoadNameAddress
import com.example.common.entity.RoadNameAddressEntrance
import com.example.common.repository.RoadNameAddressRepository
import com.example.common.repository.RoadNameAddressEntranceRepository
import com.example.daily.config.FileConfig
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Paths

@Service
open class RoadNameAddressProcessor(
    private val roadNameAddressRepository: RoadNameAddressRepository,
    private val roadNameAddressEntranceRepository: RoadNameAddressEntranceRepository,
    private val fileConfig: FileConfig
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

    @Transactional
    open fun processAddressFile(filePath: String) {
        logger.info("Starting to process address file: $filePath")
        
        val schema = CsvSchema.builder()
            .addColumn("addressManagementNo")         // 도로명주소관리번호
            .addColumn("adminCode")                   // 법정동코드
            .addColumn("cityProvinceName")            // 시도명
            .addColumn("cityCountyName")              // 시군구명
            .addColumn("townName")                    // 읍면동명
            .addColumn("villageName")                 // 리명
            .addColumn("undergroundYn")               // 지하여부
            .addColumn("roadCode")                    // 도로명코드
            .addColumn("roadSeq")                     // 도로명일련번호
            .addColumn("roadId")                      // 도로명ID
            .addColumn("roadName")                    // 도로명
            .addColumn("buildingType")                // 지하여부
            .addColumn("buildingMainNo")              // 건물본번
            .addColumn("buildingSubNo")               // 건물부번
            .addColumn("adminZoneCode")               // 행정구역코드
            .addColumn("adminZoneName")               // 행정구역명
            .addColumn("postalCode")                  // 우편번호
            .addColumn("buildingName")                // 건물명
            .addColumn("effectiveDate")               // 효력발생일
            .addColumn("changeReasonCode")            // 이동사유코드
            .addColumn("buildingNameChangeReason")    // 건물명변경사유
            .addColumn("buildingNameChangeHistory")   // 건물명변경이력
            .addColumn("detailBuildingName")          // 상세건물명
            .build()
            .withHeader()
            .withColumnSeparator('|')  // CSV 구분자를 '|'로 변경
            .withNullValue("")         // 빈 문자열을 null로 처리
            .withQuoteChar('"')        // 쌍따옴표를 quote 문자로 사용

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
                        break  // 루프를 완전히 종료
                    }

                    try {
                        if (data.addressManagementNo.isNullOrBlank()) {
                            logger.warn("Skipping record with null or blank addressManagementNo")
                            continue
                        }

                        when (data.changeReasonCode) {
                            "0", "31", "34" -> { // 신규 또는 수정
                                val address = roadNameAddressRepository.findById(data.addressManagementNo).orElse(null)
                                
                                if (address != null) {
                                    // Update existing address
                                    address.apply {
                                        adminCode = data.adminCode ?: adminCode
                                        cityProvinceName = data.cityProvinceName ?: cityProvinceName
                                        cityCountyName = data.cityCountyName ?: cityCountyName
                                        townName = data.townName ?: townName
                                        villageName = data.villageName
                                        roadCode = data.roadCode ?: roadCode
                                        roadName = data.roadName ?: roadName
                                        buildingType = data.buildingType ?: buildingType
                                        buildingMainNo = data.buildingMainNo ?: buildingMainNo
                                        buildingSubNo = data.buildingSubNo ?: buildingSubNo
                                        postalCode = data.postalCode ?: postalCode
                                        buildingName = data.buildingName ?: buildingName
                                        effectiveDate = data.effectiveDate ?: effectiveDate
                                        changeReasonCode = data.changeReasonCode
                                    }
                                    roadNameAddressRepository.save(address)
                                } else {
                                    // Create new address
                                    val newAddress = RoadNameAddress(
                                        addressManagementNo = data.addressManagementNo,
                                        adminCode = data.adminCode ?: "",
                                        cityProvinceName = data.cityProvinceName ?: "",
                                        cityCountyName = data.cityCountyName ?: "",
                                        townName = data.townName ?: "",
                                        villageName = data.villageName,
                                        roadCode = data.roadCode ?: "",
                                        roadName = data.roadName ?: "",
                                        buildingType = data.buildingType ?: "",
                                        buildingMainNo = data.buildingMainNo ?: "",
                                        buildingSubNo = data.buildingSubNo ?: "",
                                        postalCode = data.postalCode ?: "",
                                        buildingName = data.buildingName ?: "",
                                        effectiveDate = data.effectiveDate ?: "",
                                        changeReasonCode = data.changeReasonCode
                                    )
                                    roadNameAddressRepository.save(newAddress)
                                }
                            }
                            "63" -> { // 폐지
                                roadNameAddressRepository.findById(data.addressManagementNo).ifPresent { address ->
                                    roadNameAddressRepository.delete(address)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("Error processing address record: $data", e)
                    }
                    
                    processed++
                    if (processed % 10 == 0) {
                        logger.info("Processed $processed records")
                    }
                }
            }
            logger.info("Completed processing $processed records")
        } catch (e: Exception) {
            logger.error("Error processing file: ${e.message}", e)
            throw e
        }
    }

    @Transactional
    fun processEntranceFile(filePath: String) {
        logger.info("Starting to process entrance file: $filePath")
        
        val schema = CsvSchema.builder()
            .addColumn("addressManagementNo")    // 도로명주소관리번호
            .addColumn("adminCode")              // 법정동코드
            .addColumn("cityProvinceName")       // 시도명
            .addColumn("cityCountyName")         // 시군구명
            .addColumn("townName")               // 읍면동명
            .addColumn("villageName")            // 리명
            .addColumn("roadCode")               // 도로명코드
            .addColumn("roadName")               // 도로명
            .addColumn("buildingType")           // 지하여부
            .addColumn("buildingMainNo")         // 건물본번
            .addColumn("buildingSubNo")          // 건물부번
            .addColumn("entranceNo")             // 출입구일련번호
            .addColumn("changeDate")             // 변경일자
            .addColumn("blank")                  // 공백
            .addColumn("entranceManagementNo")   // 출입구관리번호
            .addColumn("entranceType")           // 출입구구분
            .addColumn("entranceCategory")       // 출입구유형
            .addColumn("longitude")              // X좌표
            .addColumn("latitude")               // Y좌표
            .build()
            .withHeader()
            .withColumnSeparator('|')           // 구분자를 '|'로 변경
            .withNullValue("")                  // 빈 문자열을 null로 처리
            .withQuoteChar('"')                 // 쌍따옴표를 quote 문자로 사용

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
                        break  // 루프를 완전히 종료
                    }

                    try {
                        if (data.addressManagementNo.isNullOrBlank() || data.entranceNo.isNullOrBlank()) {
                            logger.warn("Skipping record with null or blank addressManagementNo or entranceNo")
                            continue
                        }

                        // 좌표값 유효성 검사
                        if (!data.longitude.isNullOrBlank() && !data.latitude.isNullOrBlank()) {
                            try {
                                val lon = data.longitude.toDouble()
                                val lat = data.latitude.toDouble()
                                
                                val address = roadNameAddressRepository.findById(data.addressManagementNo).orElse(null)
                                if (address != null) {
                                    val entrance = roadNameAddressEntranceRepository.findById(data.entranceNo).orElse(null)
                                    
                                    if (entrance != null) {
                                        // Update existing entrance
                                        entrance.apply {
                                            roadNameAddress = address
                                            entranceType = data.entranceType
                                            entranceCategory = data.entranceCategory
                                            longitude = lon
                                            latitude = lat
                                        }
                                        roadNameAddressEntranceRepository.save(entrance)
                                    } else {
                                        // Create new entrance
                                        val newEntrance = RoadNameAddressEntrance(
                                            entranceNo = data.entranceNo,
                                            roadNameAddress = address,
                                            entranceType = data.entranceType ?: "",
                                            entranceCategory = data.entranceCategory ?: "",
                                            longitude = lon,
                                            latitude = lat
                                        )
                                        roadNameAddressEntranceRepository.save(newEntrance)
                                    }
                                } else {
                                    logger.warn("Address not found for entrance: ${data.entranceNo}, addressManagementNo: ${data.addressManagementNo}")
                                }
                            } catch (e: NumberFormatException) {
                                logger.warn("Invalid coordinates for entrance: ${data.entranceNo}, longitude: ${data.longitude}, latitude: ${data.latitude}")
                                continue
                            }
                        } else {
                            logger.warn("Missing coordinates for entrance: ${data.entranceNo}")
                            continue
                        }
                    } catch (e: Exception) {
                        logger.error("Error processing entrance record: $data", e)
                    }
                    
                    processed++
                    if (processed % 10 == 0) {
                        logger.info("Processed $processed records")
                    }
                }
            }
            logger.info("Completed processing $processed records")
        } catch (e: Exception) {
            logger.error("Error processing file: ${e.message}", e)
            throw e
        }
    }

    data class RoadNameAddressData(
        val addressManagementNo: String? = null,      // 도로명주소관리번호
        val adminCode: String? = null,                // 법정동코드
        val cityProvinceName: String? = null,         // 시도명
        val cityCountyName: String? = null,           // 시군구명
        val townName: String? = null,                 // 읍면동명
        val villageName: String? = null,              // 리명
        val undergroundYn: String? = null,            // 지하여부
        val roadCode: String? = null,                 // 도로명코드
        val roadSeq: String? = null,                  // 도로명일련번호
        val roadId: String? = null,                   // 도로명ID
        val roadName: String? = null,                 // 도로명
        val buildingType: String? = null,             // 지하여부
        val buildingMainNo: String? = null,           // 건물본번
        val buildingSubNo: String? = null,            // 건물부번
        val adminZoneCode: String? = null,            // 행정구역코드
        val adminZoneName: String? = null,            // 행정구역명
        val postalCode: String? = null,               // 우편번호
        val buildingName: String? = null,             // 건물명
        val effectiveDate: String? = null,            // 효력발생일
        val changeReasonCode: String? = null,         // 이동사유코드
        val buildingNameChangeReason: String? = null,  // 건물명변경사유
        val buildingNameChangeHistory: String? = null, // 건물명변경이력
        val detailBuildingName: String? = null        // 상세건물명
    )

    data class EntranceData(
        val addressManagementNo: String? = null,      // 도로명주소관리번호
        val adminCode: String? = null,                // 법정동코드
        val cityProvinceName: String? = null,         // 시도명
        val cityCountyName: String? = null,           // 시군구명
        val townName: String? = null,                 // 읍면동명
        val villageName: String? = null,              // 리명
        val roadCode: String? = null,                 // 도로명코드
        val roadName: String? = null,                 // 도로명
        val buildingType: String? = null,             // 지하여부
        val buildingMainNo: String? = null,           // 건물본번
        val buildingSubNo: String? = null,            // 건물부번
        val entranceNo: String? = null,               // 출입구일련번호
        val changeDate: String? = null,               // 변경일자
        val blank: String? = null,                    // 공백
        val entranceManagementNo: String? = null,     // 출입구관리번호
        val entranceType: String? = null,             // 출입구구분
        val entranceCategory: String? = null,         // 출입구유형
        val longitude: String? = null,                // X좌표
        val latitude: String? = null                  // Y좌표
    )
} 