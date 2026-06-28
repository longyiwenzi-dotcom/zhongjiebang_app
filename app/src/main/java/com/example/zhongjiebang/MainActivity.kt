package com.example.zhongjiebang

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.zhongjiebang.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbar)

        // 设置标题
        supportActionBar?.title = "中介帮"

        // 找到按钮
        val btnAddHouse = findViewById<Button>(R.id.btn_add_house)
        val btnSearchHouse = findViewById<Button>(R.id.btn_search_house)

        // 录入房源按钮点击事件
        btnAddHouse.setOnClickListener {
            // 跳转到录入房源页面
            val intent = Intent(this, AddHouseActivity::class.java)
            startActivity(intent)
        }

        // 查询房源按钮点击事件
        btnSearchHouse.setOnClickListener {
            // 跳转到查询房源页面
            val intent = Intent(this, HouseSearchActivity::class.java)
            startActivity(intent)
        }
    }
}
