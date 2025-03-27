package com.example.common.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "road_name_addresses")
class RoadNameAddress(
    @Id
    @Column(name = "adr_mng_no", length = 26)
    val addressManagementNo: String? = null,  // 도로명주소관리번호 (PK1)

    @Column(name = "adm_cd", length = 10)
    var adminCode: String? = null,  // 법정동코드

    @Column(name = "ctp_kor_nm", length = 40)
    var cityProvinceName: String? = null,  // 시도명

    @Column(name = "sig_kor_nm", length = 40)
    var cityCountyName: String? = null,  // 시군구명

    @Column(name = "emd_kor_nm", length = 40)
    var townName: String? = null,  // 읍면동명

    @Column(name = "li_kor_nm", length = 40)
    var villageName: String? = null,  // 리명

    @Column(name = "und_yn", length = 1)
    var undergroundYn: String? = null,  // 지하여부

    @Column(name = "road_cd", length = 12)
    var roadCode: String? = null,  // 도로명코드

    @Column(name = "road_seq", length = 3)
    var roadSeq: String? = null,  // 도로명일련번호

    @Column(name = "road_id", length = 12)
    var roadId: String? = null,  // 도로명ID

    @Column(name = "rn_nm", length = 80)
    var roadName: String? = null,  // 도로명

    @Column(name = "buld_se_cd", length = 1)
    var buildingType: String? = null,  // 지하여부

    @Column(name = "buld_mnnm", length = 25)
    var buildingMainNo: String? = null,  // 건물본번

    @Column(name = "buld_slno", length = 25)
    var buildingSubNo: String? = null,  // 건물부번

    @Column(name = "adm_zone_cd", length = 10)
    var adminZoneCode: String? = null,  // 행정구역코드

    @Column(name = "adm_zone_nm", length = 40)
    var adminZoneName: String? = null,  // 행정구역명

    @Column(name = "bsi_zon_no", length = 5)
    var postalCode: String? = null,  // 우편번호

    @Column(name = "buld_nm", length = 200)
    var buildingName: String? = null,  // 건물명

    @Column(name = "effect_de", length = 8)
    var effectiveDate: String? = null,  // 효력발생일

    @Column(name = "chg_res_cd", length = 2)
    var changeReasonCode: String? = null,  // 이동사유코드

    @Column(name = "buld_nm_chg_res", length = 200)
    var buildingNameChangeReason: String? = null,  // 건물명변경사유

    @Column(name = "buld_nm_chg_hist", length = 1000)
    var buildingNameChangeHistory: String? = null,  // 건물명변경이력

    @Column(name = "detail_buld_nm", length = 200)
    var detailBuildingName: String? = null,  // 상세건물명

    @OneToMany(mappedBy = "roadNameAddress", cascade = [CascadeType.ALL], orphanRemoval = true)
    var entrances: MutableList<RoadNameAddressEntrance> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {
    constructor() : this(
        addressManagementNo = null,
        adminCode = null,
        cityProvinceName = null,
        cityCountyName = null,
        townName = null,
        villageName = null,
        undergroundYn = null,
        roadCode = null,
        roadSeq = null,
        roadId = null,
        roadName = null,
        buildingType = null,
        buildingMainNo = null,
        buildingSubNo = null,
        adminZoneCode = null,
        adminZoneName = null,
        postalCode = null,
        buildingName = null,
        effectiveDate = null,
        changeReasonCode = null,
        buildingNameChangeReason = null,
        buildingNameChangeHistory = null,
        detailBuildingName = null
    )
} 