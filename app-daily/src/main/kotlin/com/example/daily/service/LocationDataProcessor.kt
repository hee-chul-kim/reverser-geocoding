package com.example.daily.service

import com.example.common.entity.Location
import com.example.common.repository.LocationRepository
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File

@Service
class LocationDataProcessor(
    private val locationRepository: LocationRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val csvMapper = CsvMapper().apply { registerKotlinModule() }

    @Transactional
    fun processDataFile(filePath: String) {
        logger.info("Starting to process file: $filePath")
        
        val schema = CsvSchema.builder()
            .addColumn("latitude")
            .addColumn("longitude")
            .addColumn("address")
            .addColumn("adminArea1")
            .addColumn("adminArea2")
            .addColumn("adminArea3")
            .addColumn("postalCode")
            .build()
            .withHeader()
            .withColumnSeparator(',')

        try {
            val file = File(filePath)
            if (!file.exists()) {
                logger.error("File not found: $filePath")
                return
            }

            val iterator: MappingIterator<LocationData> = csvMapper
                .readerFor(LocationData::class.java)
                .with(schema)
                .readValues(file)

            var processed = 0
            iterator.forEach { data ->
                val existingLocation = locationRepository.findByLatitudeAndLongitude(data.latitude, data.longitude)
                
                if (existingLocation != null) {
                    // Update existing location
                    existingLocation.apply {
                        address = data.address
                        adminArea1 = data.adminArea1
                        adminArea2 = data.adminArea2
                        adminArea3 = data.adminArea3
                        postalCode = data.postalCode
                    }
                    locationRepository.save(existingLocation)
                } else {
                    // Create new location
                    val location = Location(
                        latitude = data.latitude,
                        longitude = data.longitude,
                        address = data.address,
                        adminArea1 = data.adminArea1,
                        adminArea2 = data.adminArea2,
                        adminArea3 = data.adminArea3,
                        postalCode = data.postalCode
                    )
                    locationRepository.save(location)
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

    data class LocationData(
        val latitude: Double,
        val longitude: Double,
        val address: String,
        val adminArea1: String?,
        val adminArea2: String?,
        val adminArea3: String?,
        val postalCode: String?
    )
} 