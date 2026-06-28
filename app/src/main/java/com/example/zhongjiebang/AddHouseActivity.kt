package com.example.zhongjiebang

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
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

    // 选中的朝向
    private val selectedOrientations = mutableSetOf<String>()

    // 选中的结款方式
    private val selectedPaymentTerms = mutableSetOf<String>()

    // 类型按钮
    private lateinit var btnTypeRent: Button
    private lateinit var btnTypeSell: Button

    // 价格标签
    private lateinit var tvPriceLabel: TextView

    // 结款期限布局（出租时显示）
    private lateinit var layoutPaymentTerm: LinearLayout

    // 所属街道布局（默认隐藏）
    private lateinit var layoutStreet: LinearLayout
    private lateinit var cbNoCommunity: CheckBox

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
    private lateinit var cbHasElevator: CheckBox
    private lateinit var etPrice: EditText
    private lateinit var etContactName: EditText
    private lateinit var etContactPhone: EditText

    // 装修按钮
    private lateinit var btnDecorationFine: Button
    private lateinit var btnDecorationNormal: Button
    private lateinit var btnDecorationRough: Button

    // 朝向按钮
    private lateinit var btnOrientationN: Button
    private lateinit var btnOrientationNE: Button
    private lateinit var btnOrientationE: Button
    private lateinit var btnOrientationSE: Button
    private lateinit var btnOrientationS: Button
    private lateinit var btnOrientationSW: Button
    private lateinit var btnOrientationW: Button
    private lateinit var btnOrientationNW: Button

    // 结款方式按钮
    private lateinit var btnPay11: Button
    private lateinit var btnPay13: Button
    private lateinit var btnPayHalf: Button
    private lateinit var btnPayYear: Button

    // 保存按钮
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_house)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "录入房源"

        initViews()
        initClickListeners()
    }

    private fun initViews() {
        // 类型按钮
        btnTypeRent = findViewById(R.id.btn_type_rent)
        btnTypeSell = findViewById(R.id.btn_type_sell)
        tvPriceLabel = findViewById(R.id.tv_price_label)
        layoutPaymentTerm = findViewById(R.id.layout_payment_term)

        // 街道相关
        layoutStreet = findViewById(R.id.layout_street)
        cbNoCommunity = findViewById(R.id.cb_no_community)

        // 输入框
        etCommunity = findViewById(R.id.et_community)
        etAddress = findViewById(R.id.et_address)
        etHouseNumber = findViewById(R.id.et_house_number)
        etRoom = findViewById(R.id.et_room)
        etHall = findViewById(R.id.et_hall)
        etBathroom = findViewById(R.id.et_bathroom)
        etArea = findViewById(R.id.et_area)
        etFloor = findViewById(R.id.et_floor)
        etTotalFloor = findViewById(R.id.et_total_floor)
        cbHasElevator = findViewById(R.id.cb_has_elevator)
        etPrice = findViewById(R.id.et_price)
        etContactName = findViewById(R.id.et_contact_name)
        etContactPhone = findViewById(R.id.et_contact_phone)

        // 装修按钮
        btnDecorationFine = findViewById(R.id.btn_decoration_fine)
        btnDecorationNormal = findViewById(R.id.btn_decoration_normal)
        btnDecorationRough = findViewById(R.id.btn_decoration_rough)

        // 朝向按钮
        btnOrientationN = findViewById(R.id.btn_orientation_n)
        btnOrientationNE = findViewById(R.id.btn_orientation_ne)
        btnOrientationE = findViewById(R.id.btn_orientation_e)
        btnOrientationSE = findViewById(R.id.btn_orientation_se)
        btnOrientationS = findViewById(R.id.btn_orientation_s)
        btnOrientationSW = findViewById(R.id.btn_orientation_sw)
        btnOrientationW = findViewById(R.id.btn_orientation_w)
        btnOrientationNW = findViewById(R.id.btn_orientation_nw)

        // 结款按钮
        btnPay11 = findViewById(R.id.btn_pay_1_1)
        btnPay13 = findViewById(R.id.btn_pay_1_3)
        btnPayHalf = findViewById(R.id.btn_pay_half)
        btnPayYear = findViewById(R.id.btn_pay_year)

        // 保存按钮
        btnSave = findViewById(R.id.btn_save)
    }

    private fun initClickListeners() {
        // 类型切换
        btnTypeRent.setOnClickListener {
            isRent = true
            updateTypeUI()
        }
        btnTypeSell.setOnClickListener {
            isRent = false
            updateTypeUI()
        }

        // 没有小区复选框
        cbNoCommunity.setOnCheckedChangeListener { _, isChecked ->
            layoutStreet.visibility = if (isChecked) LinearLayout.VISIBLE else LinearLayout.GONE
        }

        // 装修按钮
        btnDecorationFine.setOnClickListener { selectDecoration("精装修", it as Button) }
        btnDecorationNormal.setOnClickListener { selectDecoration("普通装修", it as Button) }
        btnDecorationRough.setOnClickListener { selectDecoration("毛坯房", it as Button) }

        // 朝向按钮
        setupOrientationButton(btnOrientationN, "北")
        setupOrientationButton(btnOrientationNE, "东北")
        setupOrientationButton(btnOrientationE, "东")
        setupOrientationButton(btnOrientationSE, "东南")
        setupOrientationButton(btnOrientationS, "南")
        setupOrientationButton(btnOrientationSW, "西南")
        setupOrientationButton(btnOrientationW, "西")
        setupOrientationButton(btnOrientationNW, "西北")

        // 结款方式按钮
        setupPaymentButton(btnPay11, "押一付一")
        setupPaymentButton(btnPay13, "押一付三")
        setupPaymentButton(btnPayHalf, "半年付")
        setupPaymentButton(btnPayYear, "年付")

        // 保存按钮
        btnSave.setOnClickListener { saveHouse() }
    }

    private fun updateTypeUI() {
        if (isRent) {
            btnTypeRent.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange)
            btnTypeRent.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            btnTypeSell.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray_light)
            btnTypeSell.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
            tvPriceLabel.text = "租金（元/月）"
            layoutPaymentTerm.visibility = LinearLayout.VISIBLE
        } else {
            btnTypeSell.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange)
            btnTypeSell.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            btnTypeRent.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray_light)
            btnTypeRent.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
            tvPriceLabel.text = "售价（万元）"
            layoutPaymentTerm.visibility = LinearLayout.GONE
            selectedPaymentTerms.clear()
            resetPaymentButtons()
        }
    }

    private fun selectDecoration(decoration: String, button: Button) {
        // 单选，先重置所有按钮
        resetDecorationButtons()
        selectedDecoration = decoration
        updateButtonSelected(button, true)
    }

    private fun resetDecorationButtons() {
        updateButtonSelected(btnDecorationFine, false)
        updateButtonSelected(btnDecorationNormal, false)
        updateButtonSelected(btnDecorationRough, false)
    }

    private fun setupOrientationButton(button: Button, value: String) {
        button.setOnClickListener {
            if (selectedOrientations.contains(value)) {
                selectedOrientations.remove(value)
                updateButtonSelected(button, false)
            } else {
                selectedOrientations.add(value)
                updateButtonSelected(button, true)
            }
        }
    }

    private fun setupPaymentButton(button: Button, value: String) {
        button.setOnClickListener {
            if (selectedPaymentTerms.contains(value)) {
                selectedPaymentTerms.remove(value)
                updateButtonSelected(button, false)
            } else {
                selectedPaymentTerms.add(value)
                updateButtonSelected(button, true)
            }
        }
    }

    private fun resetPaymentButtons() {
        updateButtonSelected(btnPay11, false)
        updateButtonSelected(btnPay13, false)
        updateButtonSelected(btnPayHalf, false)
        updateButtonSelected(btnPayYear, false)
    }

    private fun updateButtonSelected(button: Button, isSelected: Boolean) {
        if (isSelected) {
            button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange)
            button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray_light)
            button.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        }
    }

    private fun saveHouse() {
        // 简单验证
        val community = etCommunity.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val priceStr = etPrice.text.toString().trim()
        val contactName = etContactName.text.toString().trim()
        val contactPhone = etContactPhone.text.toString().trim()

        // 如果没选"没有小区"，则小区名称必填
        if (!cbNoCommunity.isChecked && community.isEmpty()) {
            Toast.makeText(this, "请输入小区名称，或勾选\"没有小区\"", Toast.LENGTH_SHORT).show()
            return
        }
        // 如果选了"没有小区"，则街道必填
        if (cbNoCommunity.isChecked && address.isEmpty()) {
            Toast.makeText(this, "请输入所属街道", Toast.LENGTH_SHORT).show()
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
        val houseNumber = etHouseNumber.text.toString().trim()
        val room = etRoom.text.toString().trim().toIntOrNull() ?: 0
        val hall = etHall.text.toString().trim().toIntOrNull() ?: 0
        val bathroom = etBathroom.text.toString().trim().toIntOrNull() ?: 0
        val area = etArea.text.toString().trim().toDoubleOrNull() ?: 0.0
        val floor = etFloor.text.toString().trim().toIntOrNull() ?: 0
        val totalFloor = etTotalFloor.text.toString().trim().toIntOrNull() ?: 0
        val hasElevator = cbHasElevator.isChecked
        val price = priceStr.toIntOrNull() ?: 0
        val decoration = selectedDecoration ?: ""
        val orientation = selectedOrientations.joinToString(",")
        val paymentTerm = if (isRent) selectedPaymentTerms.joinToString(",") else ""

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
            paymentTerm = paymentTerm,
            contactName = contactName,
            contactPhone = contactPhone
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
