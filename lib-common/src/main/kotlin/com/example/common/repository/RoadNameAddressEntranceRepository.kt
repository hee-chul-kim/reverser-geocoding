package com.example.common.repository

import com.example.common.entity.RoadNameAddressEntrance
import com.example.common.entity.RoadNameAddressEntranceId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RoadNameAddressEntranceRepository : JpaRepository<RoadNameAddressEntrance, RoadNameAddressEntranceId> {
    
    @Query("""
        SELECT e FROM RoadNameAddressEntrance e 
        WHERE e.addressManagementNo = :addressManagementNo
    """)
    fun findByAddressManagementNo(@Param("addressManagementNo") addressManagementNo: String): List<RoadNameAddressEntrance>

    @Query("""
        SELECT e FROM RoadNameAddressEntrance e 
        WHERE e.entranceType = :entranceType
    """)
    fun findByEntranceType(@Param("entranceType") entranceType: String): List<RoadNameAddressEntrance>

    @Query("""
        SELECT e FROM RoadNameAddressEntrance e 
        WHERE e.addressManagementNo = :addressManagementNo 
        AND e.roadNameCode = :roadNameCode 
        AND e.isBasement = :isBasement 
        AND e.buildingMainNo = :buildingMainNo 
        AND e.buildingSubNo = :buildingSubNo
    """)
    fun findByCompositeKey(
        @Param("addressManagementNo") addressManagementNo: String,
        @Param("roadNameCode") roadNameCode: String,
        @Param("isBasement") isBasement: String,
        @Param("buildingMainNo") buildingMainNo: Int,
        @Param("buildingSubNo") buildingSubNo: Int
    ): RoadNameAddressEntrance?
} 