package com.example.common.repository

import com.example.common.entity.RoadNameAddressEntrance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RoadNameAddressEntranceRepository : JpaRepository<RoadNameAddressEntrance, String> {
    
    @Query("SELECT e FROM RoadNameAddressEntrance e WHERE e.roadNameAddress.addressManagementNo = :addressManagementNo")
    fun findByAddressManagementNo(addressManagementNo: String): List<RoadNameAddressEntrance>

    @Query("SELECT e FROM RoadNameAddressEntrance e WHERE e.entranceType = :entranceType")
    fun findByEntranceType(entranceType: String): List<RoadNameAddressEntrance>
} 