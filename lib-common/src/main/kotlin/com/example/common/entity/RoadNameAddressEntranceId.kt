package com.example.common.entity

import java.io.Serializable

data class RoadNameAddressEntranceId(
    val addressManagementNo: String = "", // 도로명주소관리번호 (PK1)
    val roadNameCode: String = "",        // 도로명코드 (PK2)
    val isBasement: String = "0",         // 지하여부 (PK3) (0:지상, 1:지하, 2:공중, 3:수상)
    val buildingMainNo: Int = 0,          // 건물본번 (PK4)
    val buildingSubNo: Int = 0            // 건물부번 (PK5)
) : Serializable 