package com.example.common.repository

import com.example.common.entity.RoadNameAddress
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RoadNameAddressRepository : JpaRepository<RoadNameAddress, String> {
    fun findByRoadCodeAndBuilding(
        roadCode: String,
        buildingType: String,
        buildingMainNo: Int,
        buildingSubNo: Int
    ): RoadNameAddress?

    fun findByPostalCode(postalCode: String): List<RoadNameAddress>

    fun findByCityProvinceName(cityProvinceName: String): List<RoadNameAddress>

    fun findByCityProvinceAndCountyName(
        cityProvinceName: String,
        cityCountyName: String
    ): List<RoadNameAddress>

    @Query("SELECT DISTINCT r FROM RoadNameAddress r LEFT JOIN FETCH r.entrances")
    fun findAllWithEntrances(pageable: Pageable): Page<RoadNameAddress>
} 