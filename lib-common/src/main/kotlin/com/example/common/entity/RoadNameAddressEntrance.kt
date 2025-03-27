package com.example.common.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "road_name_address_entrances")
class RoadNameAddressEntrance(
    @Id
    @Column(name = "ent_man_no", length = 10)
    val entranceNo: String,  // 출입구일련번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adr_mng_no")
    var roadNameAddress: RoadNameAddress,  // 도로명주소관리번호 (FK)

    @Column(name = "entrc_se", length = 2)
    var entranceType: String,  // 출입구구분 (RM: 주출입구)

    @Column(name = "entrc_ty", length = 2)
    var entranceCategory: String,  // 출입구 유형 (01: 공용, 02: 차량용)

    @Column(name = "entrc_point_x", length = 17)
    var longitude: String,  // 출입구좌표X

    @Column(name = "entrc_point_y", length = 17)
    var latitude: String,  // 출입구좌표Y

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) 