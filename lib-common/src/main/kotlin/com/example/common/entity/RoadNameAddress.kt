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
    val addressManagementNo: String,  // 도로명주소관리번호 (PK1)

    @Column(name = "adm_cd", length = 10)
    var adminCode: String,  // 법정동코드

    @Column(name = "ctp_kor_nm", length = 40)
    var cityProvinceName: String,  // 시도명

    @Column(name = "sig_kor_nm", length = 40)
    var cityCountyName: String,  // 시군구명

    @Column(name = "emd_kor_nm", length = 40)
    var townName: String,  // 읍면동명

    @Column(name = "li_kor_nm", length = 40)
    var villageName: String? = null,  // 리명

    @Column(name = "road_cd", length = 12)
    var roadCode: String,  // 도로명코드 (PK2)

    @Column(name = "rn_nm", length = 80)
    var roadName: String,  // 도로명

    @Column(name = "buld_se_cd", length = 1)
    var buildingType: String,  // 지하여부 (PK3) - 0:지상, 1:지하, 2:공중, 3:수상

    @Column(name = "buld_mnnm", length = 5)
    var buildingMainNo: Int,  // 건물본번 (PK4)

    @Column(name = "buld_slno", length = 5)
    var buildingSubNo: Int,  // 건물부번 (PK5)

    @Column(name = "bsi_zon_no", length = 5)
    var postalCode: String,  // 기초구역번호 (우편번호)

    @Column(name = "effect_de", length = 8)
    var effectiveDate: String,  // 효력발생일

    @Column(name = "chg_res_cd", length = 2)
    var changeReasonCode: String? = null,  // 이동사유코드 (31:신규, 34:수정, 63:폐지)

    @OneToMany(mappedBy = "roadNameAddress", cascade = [CascadeType.ALL], orphanRemoval = true)
    var entrances: MutableList<RoadNameAddressEntrance> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) 