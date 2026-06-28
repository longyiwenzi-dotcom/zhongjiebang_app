package com.example.zhongjiebang

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zhongjiebang.database.HouseDbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HouseListActivity : AppCompatActivity() {

    // 房源类型：true=出租，false=出售
    private var isRent = true

    // 类型切换按钮
    private lateinit var btnTypeSwitch: Button

    // 各个复选框
    private lateinit var cbCommunity: CheckBox
    private lateinit var cbPrice: CheckBox
    private lateinit var cbPaymentTerm: CheckBox
    private lateinit var cbRoom: CheckBox
    private lateinit var cbFloor: CheckBox
    private lateinit var cbElevator: CheckBox
    private lateinit var cbDecoration: CheckBox
    private lateinit var cbPurpose: CheckBox

    // 小区及街道输入框
    private lateinit var etCommunityName: EditText
    private lateinit var etStreet: EditText

    // 价格输入框
    private lateinit var etPriceMin: EditText
    private lateinit var etPriceMax: EditText

    // 楼层输入框
    private lateinit var etFloorMin: EditText
    private lateinit var etFloorMax: EditText

    // 房型按钮
    private lateinit var btnRoom1: Button
    private lateinit var btnRoom2: Button
    private lateinit var btnRoom3: Button
    private lateinit var btnRoom4: Button
    private lateinit var btnRoom5: Button
    private lateinit var btnRoom5Plus: Button

    // 装修按钮
    private lateinit var btnDecorationFine: Button
    private lateinit var btnDecorationNormal: Button
    private lateinit var btnDecorationRough: Button

    // 用途按钮
    private lateinit var btnPurposeHouse: Button
    private lateinit var btnPurposeShop: Button
    private lateinit var btnPurposeGarage: Button

    // 结款期限按钮
    private lateinit var layoutPaymentTerm: LinearLayout
    private lateinit var btnPay11: Button
    private lateinit var btnPay13: Button
    private lateinit var btnPayHalf: Button
    private lateinit var btnPayYear: Button

    // 记录选中的房型
    private val selectedRooms = mutableSetOf<Int>()
    // 记录选中的装修
    private val selectedDecorations = mutableSetOf<String>()
    // 记录选中的用途
    private val selectedPurposes = mutableSetOf<String>()
    // 记录选中的结款期限
    private val selectedPaymentTerms = mutableSetOf<String>()

    // 房源列表
    private lateinit var recyclerView: RecyclerView
    private lateinit var houseAdapter: HouseListAdapter
    private val houseList = mutableListOf<House>()
    // 从数据库加载的所有房源（原始数据，用于筛选）
    private val allHouses = mutableListOf<House>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house_list)

        // 获取从上一页传过来的类型
        isRent = intent.getBooleanExtra("isRent", true)

        // 设置标题栏
        supportActionBar?.title = if (isRent) "出租房源" else "出售房源"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 初始化控件
        initViews()
        // 更新类型显示
        updateTypeDisplay()
        // 设置所有监听
        setupListeners()
        // 从数据库加载房源
        loadHousesFromDb()
    }

    override fun onResume() {
        super.onResume()
        // 页面恢复时重新加载数据（比如从录入页面返回后）
        loadHousesFromDb()
    }

    private fun initViews() {
        btnTypeSwitch = findViewById(R.id.btn_type_switch)

        cbCommunity = findViewById(R.id.cb_community)
        cbPrice = findViewById(R.id.cb_price)
        cbPaymentTerm = findViewById(R.id.cb_payment_term)
        cbRoom = findViewById(R.id.cb_room)
        cbFloor = findViewById(R.id.cb_floor)
        cbElevator = findViewById(R.id.cb_elevator)
        cbDecoration = findViewById(R.id.cb_decoration)
        cbPurpose = findViewById(R.id.cb_purpose)

        etCommunityName = findViewById(R.id.et_community_name)
        etStreet = findViewById(R.id.et_street)
        etPriceMin = findViewById(R.id.et_price_min)
        etPriceMax = findViewById(R.id.et_price_max)
        etFloorMin = findViewById(R.id.et_floor_min)
        etFloorMax = findViewById(R.id.et_floor_max)

        btnRoom1 = findViewById(R.id.btn_room_1)
        btnRoom2 = findViewById(R.id.btn_room_2)
        btnRoom3 = findViewById(R.id.btn_room_3)
        btnRoom4 = findViewById(R.id.btn_room_4)
        btnRoom5 = findViewById(R.id.btn_room_5)
        btnRoom5Plus = findViewById(R.id.btn_room_5_plus)

        btnDecorationFine = findViewById(R.id.btn_decoration_fine)
        btnDecorationNormal = findViewById(R.id.btn_decoration_normal)
        btnDecorationRough = findViewById(R.id.btn_decoration_rough)

        btnPurposeHouse = findViewById(R.id.btn_purpose_house)
        btnPurposeShop = findViewById(R.id.btn_purpose_shop)
        btnPurposeGarage = findViewById(R.id.btn_purpose_garage)

        // 结款期限
        layoutPaymentTerm = findViewById(R.id.layout_payment_term)
        btnPay11 = findViewById(R.id.btn_pay_1_1)
        btnPay13 = findViewById(R.id.btn_pay_1_3)
        btnPayHalf = findViewById(R.id.btn_pay_half)
        btnPayYear = findViewById(R.id.btn_pay_year)

        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        houseAdapter = HouseListAdapter(houseList) { house ->
            // 跳转到房源详情页
            val intent = Intent(this, HouseDetailActivity::class.java)
            intent.putExtra("house", house)
            startActivity(intent)
        }
        recyclerView.adapter = houseAdapter
    }

    private fun setupListeners() {
        // 类型切换按钮
        btnTypeSwitch.setOnClickListener {
            isRent = !isRent
            updateTypeDisplay()
            // 重置所有筛选条件
            resetFilters()
            // 切换类型后从数据库加载数据
            loadHousesFromDb()
        }

        // 小区及街道复选框
        cbCommunity.setOnCheckedChangeListener { _, isChecked ->
            etCommunityName.isEnabled = isChecked
            etStreet.isEnabled = isChecked
            filterHouses()
        }

        // 价格复选框
        cbPrice.setOnCheckedChangeListener { _, isChecked ->
            etPriceMin.isEnabled = isChecked
            etPriceMax.isEnabled = isChecked
            filterHouses()
        }

        // 结款期限复选框
        cbPaymentTerm.setOnCheckedChangeListener { _, isChecked ->
            btnPay11.isEnabled = isChecked
            btnPay13.isEnabled = isChecked
            btnPayHalf.isEnabled = isChecked
            btnPayYear.isEnabled = isChecked
            if (!isChecked) {
                selectedPaymentTerms.clear()
                updatePaymentButtons()
            }
            filterHouses()
        }

        // 房型复选框
        cbRoom.setOnCheckedChangeListener { _, isChecked ->
            btnRoom1.isEnabled = isChecked
            btnRoom2.isEnabled = isChecked
            btnRoom3.isEnabled = isChecked
            btnRoom4.isEnabled = isChecked
            btnRoom5.isEnabled = isChecked
            btnRoom5Plus.isEnabled = isChecked
            if (!isChecked) {
                selectedRooms.clear()
                updateRoomButtons()
            }
            filterHouses()
        }

        // 楼层复选框
        cbFloor.setOnCheckedChangeListener { _, isChecked ->
            etFloorMin.isEnabled = isChecked
            etFloorMax.isEnabled = isChecked
            filterHouses()
        }

        // 装修复选框
        cbDecoration.setOnCheckedChangeListener { _, isChecked ->
            btnDecorationFine.isEnabled = isChecked
            btnDecorationNormal.isEnabled = isChecked
            btnDecorationRough.isEnabled = isChecked
            if (!isChecked) {
                selectedDecorations.clear()
                updateDecorationButtons()
            }
            filterHouses()
        }

        // 用途复选框
        cbPurpose.setOnCheckedChangeListener { _, isChecked ->
            btnPurposeHouse.isEnabled = isChecked
            btnPurposeShop.isEnabled = isChecked
            btnPurposeGarage.isEnabled = isChecked
            if (!isChecked) {
                selectedPurposes.clear()
                updatePurposeButtons()
            }
            // 用途字段暂时没在录入页做，先不筛选
        }

        // 房型按钮点击
        setupRoomButton(btnRoom1, 1)
        setupRoomButton(btnRoom2, 2)
        setupRoomButton(btnRoom3, 3)
        setupRoomButton(btnRoom4, 4)
        setupRoomButton(btnRoom5, 5)
        setupRoomButton(btnRoom5Plus, 6)

        // 装修按钮点击
        setupDecorationButton(btnDecorationFine, "精装修")
        setupDecorationButton(btnDecorationNormal, "普通装修")
        setupDecorationButton(btnDecorationRough, "毛坯房")

        // 用途按钮点击
        setupPurposeButton(btnPurposeHouse, "住宅")
        setupPurposeButton(btnPurposeShop, "商铺")
        setupPurposeButton(btnPurposeGarage, "车库")

        // 结款期限按钮点击
        setupPaymentButton(btnPay11, "押一付一")
        setupPaymentButton(btnPay13, "押一付三")
        setupPaymentButton(btnPayHalf, "半年付")
        setupPaymentButton(btnPayYear, "年付")

        // 输入框文字变化监听，实时筛选
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterHouses()
            }
        }
        etCommunityName.addTextChangedListener(textWatcher)
        etStreet.addTextChangedListener(textWatcher)
        etPriceMin.addTextChangedListener(textWatcher)
        etPriceMax.addTextChangedListener(textWatcher)
        etFloorMin.addTextChangedListener(textWatcher)
        etFloorMax.addTextChangedListener(textWatcher)

        // 电梯复选框
        cbElevator.setOnCheckedChangeListener { _, _ ->
            filterHouses()
        }
    }

    private fun setupRoomButton(button: Button, roomType: Int) {
        button.setOnClickListener {
            if (selectedRooms.contains(roomType)) {
                selectedRooms.remove(roomType)
            } else {
                selectedRooms.add(roomType)
            }
            updateRoomButtons()
            filterHouses()
        }
    }

    private fun setupDecorationButton(button: Button, decoration: String) {
        button.setOnClickListener {
            if (selectedDecorations.contains(decoration)) {
                selectedDecorations.remove(decoration)
            } else {
                selectedDecorations.add(decoration)
            }
            updateDecorationButtons()
            filterHouses()
        }
    }

    private fun setupPurposeButton(button: Button, purpose: String) {
        button.setOnClickListener {
            if (selectedPurposes.contains(purpose)) {
                selectedPurposes.remove(purpose)
            } else {
                selectedPurposes.add(purpose)
            }
            updatePurposeButtons()
            // 用途字段暂时没在录入页做，先不筛选
            // filterHouses()
        }
    }

    private fun setupPaymentButton(button: Button, payment: String) {
        button.setOnClickListener {
            if (selectedPaymentTerms.contains(payment)) {
                selectedPaymentTerms.remove(payment)
            } else {
                selectedPaymentTerms.add(payment)
            }
            updatePaymentButtons()
            filterHouses()
        }
    }

    private fun updateRoomButtons() {
        updateButtonSelected(btnRoom1, selectedRooms.contains(1))
        updateButtonSelected(btnRoom2, selectedRooms.contains(2))
        updateButtonSelected(btnRoom3, selectedRooms.contains(3))
        updateButtonSelected(btnRoom4, selectedRooms.contains(4))
        updateButtonSelected(btnRoom5, selectedRooms.contains(5))
        updateButtonSelected(btnRoom5Plus, selectedRooms.contains(6))
    }

    private fun updateDecorationButtons() {
        updateButtonSelected(btnDecorationFine, selectedDecorations.contains("精装修"))
        updateButtonSelected(btnDecorationNormal, selectedDecorations.contains("普通装修"))
        updateButtonSelected(btnDecorationRough, selectedDecorations.contains("毛坯房"))
    }

    private fun updatePurposeButtons() {
        updateButtonSelected(btnPurposeHouse, selectedPurposes.contains("住宅"))
        updateButtonSelected(btnPurposeShop, selectedPurposes.contains("商铺"))
        updateButtonSelected(btnPurposeGarage, selectedPurposes.contains("车库"))
    }

    private fun updatePaymentButtons() {
        updateButtonSelected(btnPay11, selectedPaymentTerms.contains("押一付一"))
        updateButtonSelected(btnPay13, selectedPaymentTerms.contains("押一付三"))
        updateButtonSelected(btnPayHalf, selectedPaymentTerms.contains("半年付"))
        updateButtonSelected(btnPayYear, selectedPaymentTerms.contains("年付"))
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

    private fun updateTypeDisplay() {
        if (isRent) {
            btnTypeSwitch.text = "出租房源"
            cbPrice.text = "租金"
            supportActionBar?.title = "出租房源"
            layoutPaymentTerm.visibility = LinearLayout.VISIBLE
        } else {
            btnTypeSwitch.text = "出售房源"
            cbPrice.text = "售价"
            supportActionBar?.title = "出售房源"
            layoutPaymentTerm.visibility = LinearLayout.GONE
        }
    }

    /**
     * 从数据库加载房源
     */
    private fun loadHousesFromDb() {
        val dbHelper = HouseDbHelper(this)
        lifecycleScope.launch {
            val houses = withContext(Dispatchers.IO) {
                dbHelper.getHousesByType(isRent)
            }
            allHouses.clear()
            allHouses.addAll(houses)
            // 加载完后根据当前条件筛选
            filterHouses()
        }
    }

    /**
     * 重置所有筛选条件
     */
    private fun resetFilters() {
        // 取消所有复选框
        cbCommunity.isChecked = false
        cbPrice.isChecked = false
        cbPaymentTerm.isChecked = false
        cbRoom.isChecked = false
        cbFloor.isChecked = false
        cbElevator.isChecked = false
        cbDecoration.isChecked = false
        cbPurpose.isChecked = false

        // 清空输入框
        etCommunityName.setText("")
        etStreet.setText("")
        etPriceMin.setText("")
        etPriceMax.setText("")
        etFloorMin.setText("")
        etFloorMax.setText("")

        // 清空选中的按钮
        selectedRooms.clear()
        selectedDecorations.clear()
        selectedPurposes.clear()
        selectedPaymentTerms.clear()
        updateRoomButtons()
        updateDecorationButtons()
        updatePurposeButtons()
        updatePaymentButtons()
    }

    /**
     * 根据条件筛选房源
     */
    private fun filterHouses() {
        val filtered = allHouses.filter { house ->
            // 1. 小区及街道筛选
            if (cbCommunity.isChecked) {
                val communityKeyword = etCommunityName.text.toString().trim()
                val streetKeyword = etStreet.text.toString().trim()
                val matchCommunity = communityKeyword.isEmpty() || 
                    house.community.contains(communityKeyword, ignoreCase = true)
                val matchStreet = streetKeyword.isEmpty() || 
                    house.address.contains(streetKeyword, ignoreCase = true)
                // 小区和街道是"或"的关系：匹配小区名或者匹配街道
                if (communityKeyword.isNotEmpty() || streetKeyword.isNotEmpty()) {
                    if (!matchCommunity && !matchStreet) {
                        return@filter false
                    }
                }
            }

            // 2. 价格筛选
            if (cbPrice.isChecked) {
                val minPrice = etPriceMin.text.toString().trim().toIntOrNull()
                val maxPrice = etPriceMax.text.toString().trim().toIntOrNull()
                if (minPrice != null && house.price < minPrice) {
                    return@filter false
                }
                if (maxPrice != null && house.price > maxPrice) {
                    return@filter false
                }
            }

            // 2.5 结款期限筛选（仅出租时）
            if (isRent && cbPaymentTerm.isChecked && selectedPaymentTerms.isNotEmpty()) {
                val housePayments = house.paymentTerm.split(",")
                val matchPayment = selectedPaymentTerms.any { selected ->
                    housePayments.contains(selected)
                }
                if (!matchPayment) {
                    return@filter false
                }
            }

            // 3. 房型筛选
            if (cbRoom.isChecked && selectedRooms.isNotEmpty()) {
                val matchRoom = selectedRooms.any { roomType ->
                    when (roomType) {
                        1 -> house.room == 1
                        2 -> house.room == 2
                        3 -> house.room == 3
                        4 -> house.room == 4
                        5 -> house.room == 5
                        6 -> house.room >= 5 // 五室以上
                        else -> false
                    }
                }
                if (!matchRoom) {
                    return@filter false
                }
            }

            // 4. 楼层筛选
            if (cbFloor.isChecked) {
                val minFloor = etFloorMin.text.toString().trim().toIntOrNull()
                val maxFloor = etFloorMax.text.toString().trim().toIntOrNull()
                if (minFloor != null && house.floor < minFloor) {
                    return@filter false
                }
                if (maxFloor != null && house.floor > maxFloor) {
                    return@filter false
                }
            }

            // 5. 电梯筛选
            if (cbElevator.isChecked && !house.hasElevator) {
                return@filter false
            }

            // 6. 装修筛选
            if (cbDecoration.isChecked && selectedDecorations.isNotEmpty()) {
                if (house.decoration !in selectedDecorations) {
                    return@filter false
                }
            }

            // 所有条件都满足
            true
        }

        houseList.clear()
        houseList.addAll(filtered)
        refreshList()
    }

    /**
     * 刷新列表
     */
    private fun refreshList() {
        houseAdapter.notifyDataSetChanged()
    }

    // 返回按钮
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
