package com.example.zhongjiebang.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * 房源数据访问对象
 */
@Dao
interface HouseDao {

    /**
     * 插入房源
     */
    @Insert
    suspend fun insert(house: HouseEntity): Long

    /**
     * 根据类型查询所有房源（出租/出售），按创建时间倒序
     */
    @Query("SELECT * FROM houses WHERE isRent = :isRent ORDER BY createTime DESC")
    suspend fun getHousesByType(isRent: Boolean): List<HouseEntity>

    /**
     * 根据ID查询房源
     */
    @Query("SELECT * FROM houses WHERE id = :id")
    suspend fun getHouseById(id: Int): HouseEntity?

    /**
     * 查询所有房源
     */
    @Query("SELECT * FROM houses ORDER BY createTime DESC")
    suspend fun getAllHouses(): List<HouseEntity>

    /**
     * 删除房源
     */
    @Query("DELETE FROM houses WHERE id = :id")
    suspend fun deleteById(id: Int)
}
