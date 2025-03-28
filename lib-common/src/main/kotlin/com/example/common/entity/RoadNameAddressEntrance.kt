package com.example.common.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "road_name_address_entrances")
@IdClass(RoadNameAddressEntranceId::class)
class RoadNameAddressEntrance(
    @Column(name = "ent_man_no", length = 10, nullable = false)
    val entranceNo: String, // 출입구일련번호

    @Id
    @Column(name = "address_management_no", nullable = false)
    val addressManagementNo: String, // 도로명주소관리번호 (PK1)

    @Id
    @Column(name = "road_name_code", nullable = false)
    val roadNameCode: String, // 도로명코드 (PK2)

    @Id
    @Column(name = "is_basement", length = 1, nullable = false)
    val isBasement: String = "0", // 지하여부 (PK3) (0:지상, 1:지하, 2:공중, 3:수상)

    @Id
    @Column(name = "building_main_no", nullable = false)
    val buildingMainNo: Int, // 건물본번 (PK4)

    @Id
    @Column(name = "building_sub_no", nullable = false)
    val buildingSubNo: Int, // 건물부번 (PK5)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns(
        JoinColumn(name = "address_management_no", referencedColumnName = "address_management_no", insertable = false, updatable = false),
        JoinColumn(name = "road_name_code", referencedColumnName = "road_name_code", insertable = false, updatable = false),
        JoinColumn(name = "is_basement", referencedColumnName = "is_basement", insertable = false, updatable = false),
        JoinColumn(name = "building_main_no", referencedColumnName = "building_main_no", insertable = false, updatable = false),
        JoinColumn(name = "building_sub_no", referencedColumnName = "building_sub_no", insertable = false, updatable = false)
    )
    var roadNameAddress: RoadNameAddress? = null, // 도로명주소 (1:1)

    @Column(name = "entrc_se", length = 2)
    var entranceType: String? = null, // 출입구구분 (RM: 주출입구)

    @Column(name = "entrc_ty", length = 2)
    var entranceCategory: String? = null, // 출입구 유형 (01: 공용, 02: 차량용)

    @Column(name = "entrc_point_x", columnDefinition = "DOUBLE PRECISION")
    var longitude: Double? = null, // 출입구좌표X

    @Column(name = "entrc_point_y", columnDefinition = "DOUBLE PRECISION")
    var latitude: Double? = null, // 출입구좌표Y

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoadNameAddressEntrance

        if (addressManagementNo != other.addressManagementNo) return false
        if (roadNameCode != other.roadNameCode) return false
        if (isBasement != other.isBasement) return false
        if (buildingMainNo != other.buildingMainNo) return false
        if (buildingSubNo != other.buildingSubNo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = addressManagementNo.hashCode()
        result = 31 * result + roadNameCode.hashCode()
        result = 31 * result + isBasement.hashCode()
        result = 31 * result + buildingMainNo
        result = 31 * result + buildingSubNo
        return result
    }

    override fun toString(): String {
        return "RoadNameAddressEntrance(addressManagementNo='$addressManagementNo', " +
               "roadNameCode='$roadNameCode', " +
               "isBasement=$isBasement, " +
               "buildingMainNo=$buildingMainNo, " +
               "buildingSubNo=$buildingSubNo)"
    }
} 