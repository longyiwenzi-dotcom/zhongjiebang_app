package com.example.zhongjiebang.database

import com.example.zhongjiebang.House

/**
 * HouseEntity 转 House
 */
fun HouseEntity.toHouse(): House {
    return House(
        id = this.id,
        isRent = this.isRent,
        community = this.community,
        address = this.address,
        houseNumber = this.houseNumber,
        room = this.room,
        hall = this.hall,
        bathroom = this.bathroom,
        area = this.area,
        floor = this.floor,
        totalFloor = this.totalFloor,
        hasElevator = this.hasElevator,
        decoration = this.decoration,
        orientation = this.orientation,
        price = this.price,
        contactName = this.contactName,
        contactPhone = this.contactPhone,
        description = this.description,
        createTime = this.createTime
    )
}

/**
 * House 转 HouseEntity
 */
fun House.toEntity(): HouseEntity {
    return HouseEntity(
        id = this.id,
        isRent = this.isRent,
        community = this.community,
        address = this.address,
        houseNumber = this.houseNumber,
        room = this.room,
        hall = this.hall,
        bathroom = this.bathroom,
        area = this.area,
        floor = this.floor,
        totalFloor = this.totalFloor,
        hasElevator = this.hasElevator,
        decoration = this.decoration,
        orientation = this.orientation,
        price = this.price,
        contactName = this.contactName,
        contactPhone = this.contactPhone,
        description = this.description,
        createTime = this.createTime
    )
}
