package com.example.zhongjiebang

import java.io.Serializable

/**
 * 房源数据模型
 */
data class House(
    val id: Int = 0,
    val isRent: Boolean = true,        // true=出租，false=出售
    val community: String = "",        // 小区名称
    val address: String = "",          // 街道地址
    val houseNumber: String = "",      // 门牌号
    val room: Int = 0,                 // 室
    val hall: Int = 0,                 // 厅
    val bathroom: Int = 0,             // 卫
    val area: Double = 0.0,            // 面积（㎡）
    val floor: Int = 0,                // 所在楼层
    val totalFloor: Int = 0,           // 总楼层
    val hasElevator: Boolean = false,  // 是否有电梯
    val decoration: String = "",       // 装修情况
    val orientation: String = "",      // 朝向
    val price: Int = 0,                // 价格（出租：元/月，出售：万元）
    val contactName: String = "",      // 联系人
    val contactPhone: String = "",     // 联系电话
    val description: String = "",      // 房源描述
    val createTime: Long = System.currentTimeMillis() // 创建时间
) : Serializable {
    /**
     * 获取房型描述
     */
    fun getRoomDesc(): String {
        return "${room}室${hall}厅${bathroom}卫"
    }

    /**
     * 获取楼层描述
     */
    fun getFloorDesc(): String {
        return "${floor}/${totalFloor}层"
    }

    /**
     * 获取价格描述
     */
    fun getPriceDesc(): String {
        return if (isRent) {
            "${price}元/月"
        } else {
            "${price}万"
        }
    }
}
