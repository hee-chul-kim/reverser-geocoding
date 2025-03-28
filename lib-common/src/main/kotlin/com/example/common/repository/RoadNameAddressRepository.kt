package com.example.common.repository

import com.example.common.entity.RoadNameAddress
import com.example.common.entity.RoadNameAddressId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RoadNameAddressRepository : JpaRepository<RoadNameAddress, RoadNameAddressId> {
    
    @Query("""
        SELECT DISTINCT r FROM RoadNameAddress r 
        LEFT JOIN FETCH r.entrance
    """)
    fun findAllWithEntrances(): List<RoadNameAddress>

    @Query("""
        SELECT DISTINCT r FROM RoadNameAddress r 
        LEFT JOIN FETCH r.entrance
    """, countQuery = "SELECT COUNT(DISTINCT r) FROM RoadNameAddress r")
    fun findAllWithEntrancesPaged(pageable: Pageable): Page<RoadNameAddress>

    @Query("""
        SELECT r FROM RoadNameAddress r 
        WHERE r.addressManagementNo = :addressManagementNo 
        AND r.roadNameCode = :roadNameCode 
        AND r.legalDongCode = :legalDongCode 
        AND r.buildingMainNo = :buildingMainNo 
        AND r.buildingSubNo = :buildingSubNo
    """)
    fun findByCompositeKey(
        @Param("addressManagementNo") addressManagementNo: String,
        @Param("roadNameCode") roadNameCode: String,
        @Param("legalDongCode") legalDongCode: String,
        @Param("buildingMainNo") buildingMainNo: Int,
        @Param("buildingSubNo") buildingSubNo: Int
    ): RoadNameAddress?

    @Query("SELECT r FROM RoadNameAddress r WHERE r.zipCode = :zipCode")
    fun findByZipCode(@Param("zipCode") zipCode: String): List<RoadNameAddress>

    @Query("SELECT r FROM RoadNameAddress r WHERE r.sidoName = :sidoName")
    fun findBySidoName(@Param("sidoName") sidoName: String): List<RoadNameAddress>

    @Query("""
        SELECT r FROM RoadNameAddress r 
        WHERE r.sidoName = :sidoName 
        AND r.sigunguName = :sigunguName
    """)
    fun findBySidoAndSigunguName(
        @Param("sidoName") sidoName: String,
        @Param("sigunguName") sigunguName: String
    ): List<RoadNameAddress>

    @Query("""
        SELECT r FROM RoadNameAddress r
        WHERE r.addressManagementNo = :addressManagementNo
    """)
    fun findByAddressManagementNo(@Param("addressManagementNo") addressManagementNo: String): RoadNameAddress?
} 