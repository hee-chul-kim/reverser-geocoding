package com.example.daily.service

import com.example.common.entity.RoadNameAddress
import com.example.common.entity.RoadNameAddressEntrance
import com.example.common.repository.RoadNameAddressRepository
import com.example.common.repository.RoadNameAddressEntranceRepository
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.nio.file.Paths

@Service
class RoadNameAddressProcessor(
    private val roadNameAddressRepository: RoadNameAddressRepository,
    private val roadNameAddressEntranceRepository: RoadNameAddressEntranceRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val csvMapper = CsvMapper().apply { registerKotlinModule() }

    fun processMonthlyData(year: Int, month: String) {
        logger.info("Starting to process data for $year/$month")
        val baseDir = Paths.get("data", "$year$month")
        
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
                file.renameTo(processedFile)
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
                file.renameTo(processedFile)
            } catch (e: Exception) {
                logger.error("Error processing entrance coordinates file ${file.name}: ${e.message}", e)
            }
        }
    }

    @Transactional
    fun processAddressFile(filePath: String) {
        logger.info("Starting to process address file: $filePath")
        
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
            .addColumn("postalCode")             // 우편번호
            .addColumn("effectiveDate")          // 효력발생일
            .addColumn("changeReasonCode")       // 이동사유코드
            .build()
            .withHeader()
            .withColumnSeparator(',')

        try {
            val file = File(filePath)
            if (!file.exists()) {
                logger.error("File not found: $filePath")
                return
            }

            val iterator: MappingIterator<RoadNameAddressData> = csvMapper
                .readerFor(RoadNameAddressData::class.java)
                .with(schema)
                .readValues(file)

            var processed = 0
            iterator.forEach { data ->
                when (data.changeReasonCode) {
                    "31", "34" -> { // 신규 또는 수정
                        val address = roadNameAddressRepository.findById(data.addressManagementNo).orElse(null)
                        
                        if (address != null) {
                            // Update existing address
                            address.apply {
                                adminCode = data.adminCode
                                cityProvinceName = data.cityProvinceName
                                cityCountyName = data.cityCountyName
                                townName = data.townName
                                villageName = data.villageName
                                roadCode = data.roadCode
                                roadName = data.roadName
                                buildingType = data.buildingType
                                buildingMainNo = data.buildingMainNo
                                buildingSubNo = data.buildingSubNo
                                postalCode = data.postalCode
                                effectiveDate = data.effectiveDate
                                changeReasonCode = data.changeReasonCode
                            }
                            roadNameAddressRepository.save(address)
                        } else {
                            // Create new address
                            val newAddress = RoadNameAddress(
                                addressManagementNo = data.addressManagementNo,
                                adminCode = data.adminCode,
                                cityProvinceName = data.cityProvinceName,
                                cityCountyName = data.cityCountyName,
                                townName = data.townName,
                                villageName = data.villageName,
                                roadCode = data.roadCode,
                                roadName = data.roadName,
                                buildingType = data.buildingType,
                                buildingMainNo = data.buildingMainNo,
                                buildingSubNo = data.buildingSubNo,
                                postalCode = data.postalCode,
                                effectiveDate = data.effectiveDate,
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
                
                processed++
                if (processed % 1000 == 0) {
                    logger.info("Processed $processed records")
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
            .addColumn("entranceNo")             // 출입구일련번호
            .addColumn("addressManagementNo")    // 도로명주소관리번호
            .addColumn("entranceType")           // 출입구구분
            .addColumn("entranceCategory")       // 출입구유형
            .addColumn("longitude")              // 출입구좌표X
            .addColumn("latitude")               // 출입구좌표Y
            .build()
            .withHeader()
            .withColumnSeparator(',')

        try {
            val file = File(filePath)
            if (!file.exists()) {
                logger.error("File not found: $filePath")
                return
            }

            val iterator: MappingIterator<EntranceData> = csvMapper
                .readerFor(EntranceData::class.java)
                .with(schema)
                .readValues(file)

            var processed = 0
            iterator.forEach { data ->
                val address = roadNameAddressRepository.findById(data.addressManagementNo).orElse(null)
                if (address != null) {
                    val entrance = roadNameAddressEntranceRepository.findById(data.entranceNo).orElse(null)
                    
                    if (entrance != null) {
                        // Update existing entrance
                        entrance.apply {
                            roadNameAddress = address
                            entranceType = data.entranceType
                            entranceCategory = data.entranceCategory
                            longitude = data.longitude
                            latitude = data.latitude
                        }
                        roadNameAddressEntranceRepository.save(entrance)
                    } else {
                        // Create new entrance
                        val newEntrance = RoadNameAddressEntrance(
                            entranceNo = data.entranceNo,
                            roadNameAddress = address,
                            entranceType = data.entranceType,
                            entranceCategory = data.entranceCategory,
                            longitude = data.longitude,
                            latitude = data.latitude
                        )
                        roadNameAddressEntranceRepository.save(newEntrance)
                    }
                } else {
                    logger.warn("Address not found for entrance: ${data.entranceNo}, addressManagementNo: ${data.addressManagementNo}")
                }
                
                processed++
                if (processed % 1000 == 0) {
                    logger.info("Processed $processed records")
                }
            }
            logger.info("Completed processing $processed records")
        } catch (e: Exception) {
            logger.error("Error processing file: ${e.message}", e)
            throw e
        }
    }

    data class RoadNameAddressData(
        val addressManagementNo: String,
        val adminCode: String,
        val cityProvinceName: String,
        val cityCountyName: String,
        val townName: String,
        val villageName: String?,
        val roadCode: String,
        val roadName: String,
        val buildingType: String,
        val buildingMainNo: Int,
        val buildingSubNo: Int,
        val postalCode: String,
        val effectiveDate: String,
        val changeReasonCode: String?
    )

    data class EntranceData(
        val entranceNo: String,
        val addressManagementNo: String,
        val entranceType: String,
        val entranceCategory: String,
        val longitude: String,
        val latitude: String
    )
} 