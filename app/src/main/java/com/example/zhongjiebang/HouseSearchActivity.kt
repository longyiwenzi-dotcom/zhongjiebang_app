package com.example.zhongjiebang

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HouseSearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house_search)

        // 设置标题栏
        supportActionBar?.title = "查询房源"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 显示返回按钮

        // 找到按钮
        val btnRentHouse = findViewById<Button>(R.id.btn_rent_house)
        val btnSellHouse = findViewById<Button>(R.id.btn_sell_house)

        // 查询出租房源按钮点击事件
        btnRentHouse.setOnClickListener {
            // 跳转到房源列表页面，传递出租类型
            val intent = Intent(this, HouseListActivity::class.java)
            intent.putExtra("isRent", true)
            startActivity(intent)
        }

        // 查询出售房源按钮点击事件
        btnSellHouse.setOnClickListener {
            // 跳转到房源列表页面，传递出售类型
            val intent = Intent(this, HouseListActivity::class.java)
            intent.putExtra("isRent", false)
            startActivity(intent)
        }
    }

    // 返回按钮点击事件
    override fun onSupportNavigateUp(): Boolean {
        finish() // 关闭当前页面，回到上一页
        return true
    }
}
