package com.example.common.repository

import com.example.common.entity.RoadNameAddress
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RoadNameAddressRepository : JpaRepository<RoadNameAddress, String> {
    
    @Query("""
        SELECT r FROM RoadNameAddress r 
        WHERE r.roadCode = :roadCode 
        AND r.buildingType = :buildingType 
        AND r.buildingMainNo = :buildingMainNo 
        AND r.buildingSubNo = :buildingSubNo
    """)
    fun findByRoadCodeAndBuilding(
        roadCode: String,
        buildingType: String,
        buildingMainNo: Int,
        buildingSubNo: Int
    ): RoadNameAddress?

    @Query("SELECT r FROM RoadNameAddress r WHERE r.postalCode = :postalCode")
    fun findByPostalCode(postalCode: String): List<RoadNameAddress>

    @Query("SELECT r FROM RoadNameAddress r WHERE r.cityProvinceName = :cityProvinceName")
    fun findByCityProvinceName(cityProvinceName: String): List<RoadNameAddress>

    @Query("""
        SELECT r FROM RoadNameAddress r 
        WHERE r.cityProvinceName = :cityProvinceName 
        AND r.cityCountyName = :cityCountyName
    """)
    fun findByCityProvinceAndCountyName(
        cityProvinceName: String,
        cityCountyName: String
    ): List<RoadNameAddress>
} 