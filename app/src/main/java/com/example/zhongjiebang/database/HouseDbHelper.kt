package com.example.zhongjiebang.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.zhongjiebang.House

/**
 * 原生SQLite数据库帮助类，零依赖
 */
class HouseDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "zhongjiebang.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_HOUSES = "houses"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 创建房源表
        val createTable = """
            CREATE TABLE $TABLE_HOUSES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                isRent INTEGER NOT NULL,
                community TEXT NOT NULL,
                address TEXT,
                houseNumber TEXT,
                room INTEGER DEFAULT 0,
                hall INTEGER DEFAULT 0,
                bathroom INTEGER DEFAULT 0,
                area REAL DEFAULT 0,
                floor INTEGER DEFAULT 0,
                totalFloor INTEGER DEFAULT 0,
                hasElevator INTEGER DEFAULT 0,
                decoration TEXT,
                orientation TEXT,
                price INTEGER DEFAULT 0,
                contactName TEXT,
                contactPhone TEXT,
                description TEXT,
                createTime INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 开发阶段直接删表重建
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HOUSES")
        onCreate(db)
    }

    /**
     * 插入房源，返回id
     */
    fun insertHouse(house: House): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("isRent", if (house.isRent) 1 else 0)
            put("community", house.community)
            put("address", house.address)
            put("houseNumber", house.houseNumber)
            put("room", house.room)
            put("hall", house.hall)
            put("bathroom", house.bathroom)
            put("area", house.area)
            put("floor", house.floor)
            put("totalFloor", house.totalFloor)
            put("hasElevator", if (house.hasElevator) 1 else 0)
            put("decoration", house.decoration)
            put("orientation", house.orientation)
            put("price", house.price)
            put("contactName", house.contactName)
            put("contactPhone", house.contactPhone)
            put("description", house.description)
            put("createTime", System.currentTimeMillis())
        }
        return db.insert(TABLE_HOUSES, null, values)
    }

    /**
     * 根据类型查询房源（出租/出售），按时间倒序
     */
    fun getHousesByType(isRent: Boolean): List<House> {
        val houses = mutableListOf<House>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_HOUSES,
            null,
            "isRent = ?",
            arrayOf(if (isRent) "1" else "0"),
            null,
            null,
            "createTime DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                houses.add(cursorToHouse(it))
            }
        }
        return houses
    }

    /**
     * 根据id查询房源
     */
    fun getHouseById(id: Int): House? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_HOUSES,
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToHouse(it)
            }
        }
        return null
    }

    /**
     * 把Cursor转成House对象
     */
    private fun cursorToHouse(cursor: Cursor): House {
        return House(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            isRent = cursor.getInt(cursor.getColumnIndexOrThrow("isRent")) == 1,
            community = cursor.getString(cursor.getColumnIndexOrThrow("community")) ?: "",
            address = cursor.getString(cursor.getColumnIndexOrThrow("address")) ?: "",
            houseNumber = cursor.getString(cursor.getColumnIndexOrThrow("houseNumber")) ?: "",
            room = cursor.getInt(cursor.getColumnIndexOrThrow("room")),
            hall = cursor.getInt(cursor.getColumnIndexOrThrow("hall")),
            bathroom = cursor.getInt(cursor.getColumnIndexOrThrow("bathroom")),
            area = cursor.getDouble(cursor.getColumnIndexOrThrow("area")),
            floor = cursor.getInt(cursor.getColumnIndexOrThrow("floor")),
            totalFloor = cursor.getInt(cursor.getColumnIndexOrThrow("totalFloor")),
            hasElevator = cursor.getInt(cursor.getColumnIndexOrThrow("hasElevator")) == 1,
            decoration = cursor.getString(cursor.getColumnIndexOrThrow("decoration")) ?: "",
            orientation = cursor.getString(cursor.getColumnIndexOrThrow("orientation")) ?: "",
            price = cursor.getInt(cursor.getColumnIndexOrThrow("price")),
            contactName = cursor.getString(cursor.getColumnIndexOrThrow("contactName")) ?: "",
            contactPhone = cursor.getString(cursor.getColumnIndexOrThrow("contactPhone")) ?: "",
            description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: "",
            createTime = cursor.getLong(cursor.getColumnIndexOrThrow("createTime"))
        )
    }
}
