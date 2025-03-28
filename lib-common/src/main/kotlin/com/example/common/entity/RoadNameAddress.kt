package com.example.common.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.ZonedDateTime

@Entity
@Table(name = "road_name_addresses")
@IdClass(RoadNameAddressId::class)
class RoadNameAddress(
    @Id
    @Column(name = "address_management_no", nullable = false)
    val addressManagementNo: String, // 도로명주소관리번호 (PK1)

    @Column(name = "legal_dong_code")
    val legalDongCode: String, // 법정동코드

    @Column(name = "sido_name")
    val sidoName: String? = null, // 시도명

    @Column(name = "sigungu_name")
    val sigunguName: String? = null, // 시군구명

    @Column(name = "legal_emd_name")
    val legalEmdName: String? = null, // 법정읍면동명

    @Column(name = "legal_ri_name")
    val legalRiName: String? = null, // 법정리명

    @Column(name = "is_mountain")
    val isMountain: Boolean = false, // 산여부

    @Column(name = "jibun_main_no")
    val jibunMainNo: Int = 0, // 지번본번(번지)

    @Column(name = "jibun_sub_no")
    val jibunSubNo: Int = 0, // 지번부번(호)

    @Id
    @Column(name = "road_name_code", nullable = false)
    val roadNameCode: String, // 도로명코드 (PK2)

    @Column(name = "road_name")
    val roadName: String? = null, // 도로명

    @Id
    @Column(name = "is_basement", length = 1, nullable = false)
    val isBasement: String = "0", // 지하여부 (PK3) (0:지상, 1:지하, 2:공중, 3:수상)

    @Id
    @Column(name = "building_main_no", nullable = false)
    val buildingMainNo: Int, // 건물본번 (PK4)

    @Id
    @Column(name = "building_sub_no", nullable = false)
    val buildingSubNo: Int, // 건물부번 (PK5)

    @Column(name = "admin_dong_code")
    val adminDongCode: String? = null, // 행정동코드

    @Column(name = "admin_dong_name")
    val adminDongName: String? = null, // 행정동명

    @Column(name = "zip_code")
    val zipCode: String? = null, // 기초구역번호(우편번호)

    @Column(name = "previous_address")
    val previousAddress: String? = null, // 이전도로명주소

    @Column(name = "effective_date")
    val effectiveDate: String? = null, // 효력발생일

    @Column(name = "is_apartment")
    val isApartment: Boolean = false, // 공동주택구분

    @Column(name = "change_reason_code")
    val changeReasonCode: String? = null, // 이동사유코드

    @Column(name = "building_name")
    val buildingName: String? = null, // 건축물대장건물명

    @Column(name = "sigungu_building_name")
    val sigunguBuildingName: String? = null, // 시군구용건물명

    @Column(name = "note")
    val note: String? = null, // 비고

    @OneToOne(mappedBy = "roadNameAddress")
    var entrance: RoadNameAddressEntrance? = null, // 출입구 정보 (1:1)

    @Column(name = "created_at")
    val createdAt: ZonedDateTime = ZonedDateTime.now(), // 생성일시

    @Column(name = "updated_at")
    var updatedAt: ZonedDateTime = ZonedDateTime.now(), // 수정일시

    @Column(name = "deleted_at")
    var deletedAt: ZonedDateTime? = null, // 삭제일시

    @Version
    @Column(name = "version")
    var version: Int = 0 // 버전
) {
    // 연관관계 편의 메서드
    fun connectEntrance(entrance: RoadNameAddressEntrance?) {
        this.entrance = entrance
        entrance?.roadNameAddress = this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoadNameAddress

        if (addressManagementNo != other.addressManagementNo) return false
        if (roadNameCode != other.roadNameCode) return false
        if (buildingMainNo != other.buildingMainNo) return false
        if (buildingSubNo != other.buildingSubNo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = addressManagementNo.hashCode()
        result = 31 * result + roadNameCode.hashCode()
        result = 31 * result + buildingMainNo
        result = 31 * result + buildingSubNo
        return result
    }

    override fun toString(): String {
        return "RoadNameAddress(addressManagementNo='$addressManagementNo', " +
               "roadNameCode='$roadNameCode', " +
               "buildingMainNo=$buildingMainNo, " +
               "buildingSubNo=$buildingSubNo)"
    }
} 