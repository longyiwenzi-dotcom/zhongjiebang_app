package com.example.zhongjiebang

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.zhongjiebang.database.HouseDbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddHouseActivity : AppCompatActivity() {

    // 房源类型：true=出租，false=出售
    private var isRent = true

    // 装修情况
    private var selectedDecoration: String? = null

    // 类型按钮
    private lateinit var btnTypeRent: Button
    private lateinit var btnTypeSell: Button

    // 价格标签
    private lateinit var tvPriceLabel: TextView

    // 输入框
    private lateinit var etCommunity: EditText
    private lateinit var etAddress: EditText
    private lateinit var etHouseNumber: EditText
    private lateinit var etRoom: EditText
    private lateinit var etHall: EditText
    private lateinit var etBathroom: EditText
    private lateinit var etArea: EditText
    private lateinit var etFloor: EditText
    private lateinit var etTotalFloor: EditText
    private lateinit var etOrientation: EditText
    private lateinit var etPrice: EditText
    private lateinit var etContactName: EditText
    private lateinit var etContactPhone: EditText
    private lateinit var etDescription: EditText

    // 复选框
    private lateinit var cbHasElevator: CheckBox

    // 装修按钮
    private lateinit var btnDecorationFine: Button
    private lateinit var btnDecorationNormal: Button
    private lateinit var btnDecorationRough: Button

    // 保存按钮
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_house)

        // 设置标题栏
        supportActionBar?.title = "录入房源"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 初始化控件
        initViews()
        // 设置监听
        setupListeners()
    }

    private fun initViews() {
        btnTypeRent = findViewById(R.id.btn_type_rent)
        btnTypeSell = findViewById(R.id.btn_type_sell)
        tvPriceLabel = findViewById(R.id.tv_price_label)

        etCommunity = findViewById(R.id.et_community)
        etAddress = findViewById(R.id.et_address)
        etHouseNumber = findViewById(R.id.et_house_number)
        etRoom = findViewById(R.id.et_room)
        etHall = findViewById(R.id.et_hall)
        etBathroom = findViewById(R.id.et_bathroom)
        etArea = findViewById(R.id.et_area)
        etFloor = findViewById(R.id.et_floor)
        etTotalFloor = findViewById(R.id.et_total_floor)
        etOrientation = findViewById(R.id.et_orientation)
        etPrice = findViewById(R.id.et_price)
        etContactName = findViewById(R.id.et_contact_name)
        etContactPhone = findViewById(R.id.et_contact_phone)
        etDescription = findViewById(R.id.et_description)

        cbHasElevator = findViewById(R.id.cb_has_elevator)

        btnDecorationFine = findViewById(R.id.btn_decoration_fine)
        btnDecorationNormal = findViewById(R.id.btn_decoration_normal)
        btnDecorationRough = findViewById(R.id.btn_decoration_rough)

        btnSave = findViewById(R.id.btn_save)
    }

    private fun setupListeners() {
        // 出租按钮
        btnTypeRent.setOnClickListener {
            isRent = true
            updateTypeDisplay()
        }

        // 出售按钮
        btnTypeSell.setOnClickListener {
            isRent = false
            updateTypeDisplay()
        }

        // 装修按钮
        btnDecorationFine.setOnClickListener {
            selectDecoration("精装修")
        }
        btnDecorationNormal.setOnClickListener {
            selectDecoration("普通装修")
        }
        btnDecorationRough.setOnClickListener {
            selectDecoration("毛坯房")
        }

        // 保存按钮
        btnSave.setOnClickListener {
            saveHouse()
        }
    }

    private fun updateTypeDisplay() {
        if (isRent) {
            // 出租选中
            btnTypeRent.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange)
            btnTypeRent.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            btnTypeSell.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray_light)
            btnTypeSell.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
            tvPriceLabel.text = "租金（元/月）"
        } else {
            // 出售选中
            btnTypeSell.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange)
            btnTypeSell.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            btnTypeRent.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray_light)
            btnTypeRent.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
            tvPriceLabel.text = "售价（万元）"
        }
    }

    private fun selectDecoration(decoration: String) {
        selectedDecoration = decoration
        updateDecorationButtons()
    }

    private fun updateDecorationButtons() {
        val decorations = listOf(
            btnDecorationFine to "精装修",
            btnDecorationNormal to "普通装修",
            btnDecorationRough to "毛坯房"
        )

        for ((button, text) in decorations) {
            if (selectedDecoration == text) {
                button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange)
                button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            } else {
                button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray_light)
                button.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
            }
        }
    }

    private fun saveHouse() {
        // 简单验证
        val community = etCommunity.text.toString().trim()
        val priceStr = etPrice.text.toString().trim()
        val contactName = etContactName.text.toString().trim()
        val contactPhone = etContactPhone.text.toString().trim()

        if (community.isEmpty()) {
            Toast.makeText(this, "请输入小区名称", Toast.LENGTH_SHORT).show()
            return
        }
        if (priceStr.isEmpty()) {
            Toast.makeText(this, "请输入价格", Toast.LENGTH_SHORT).show()
            return
        }
        if (contactName.isEmpty()) {
            Toast.makeText(this, "请输入联系人姓名", Toast.LENGTH_SHORT).show()
            return
        }
        if (contactPhone.isEmpty()) {
            Toast.makeText(this, "请输入联系电话", Toast.LENGTH_SHORT).show()
            return
        }

        // 读取所有字段
        val address = etAddress.text.toString().trim()
        val houseNumber = etHouseNumber.text.toString().trim()
        val room = etRoom.text.toString().trim().toIntOrNull() ?: 0
        val hall = etHall.text.toString().trim().toIntOrNull() ?: 0
        val bathroom = etBathroom.text.toString().trim().toIntOrNull() ?: 0
        val area = etArea.text.toString().trim().toDoubleOrNull() ?: 0.0
        val floor = etFloor.text.toString().trim().toIntOrNull() ?: 0
        val totalFloor = etTotalFloor.text.toString().trim().toIntOrNull() ?: 0
        val hasElevator = cbHasElevator.isChecked
        val orientation = etOrientation.text.toString().trim()
        val price = priceStr.toIntOrNull() ?: 0
        val description = etDescription.text.toString().trim()
        val decoration = selectedDecoration ?: ""

        // 创建House对象
        val house = House(
            isRent = isRent,
            community = community,
            address = address,
            houseNumber = houseNumber,
            room = room,
            hall = hall,
            bathroom = bathroom,
            area = area,
            floor = floor,
            totalFloor = totalFloor,
            hasElevator = hasElevator,
            decoration = decoration,
            orientation = orientation,
            price = price,
            contactName = contactName,
            contactPhone = contactPhone,
            description = description
        )

        // 保存到数据库
        val dbHelper = HouseDbHelper(this)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.insertHouse(house)
            }
            Toast.makeText(this@AddHouseActivity, "房源保存成功！", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // 返回按钮
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
