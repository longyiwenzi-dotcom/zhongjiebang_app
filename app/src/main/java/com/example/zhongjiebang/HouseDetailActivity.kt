package com.example.zhongjiebang

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HouseDetailActivity : AppCompatActivity() {

    private lateinit var house: House

    // 控件
    private lateinit var tvPrice: TextView
    private lateinit var tvTypeTag: TextView
    private lateinit var tvCommunity: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvRoom: TextView
    private lateinit var tvArea: TextView
    private lateinit var tvFloor: TextView
    private lateinit var tvOrientation: TextView
    private lateinit var tvDecoration: TextView
    private lateinit var tvElevator: TextView
    private lateinit var tvHouseNumber: TextView
    private lateinit var layoutPaymentTerm: LinearLayout
    private lateinit var dividerPayment: View
    private lateinit var tvPaymentTerm: TextView
    private lateinit var tvContactName: TextView
    private lateinit var tvContactPhone: TextView
    private lateinit var btnCall: Button
    private lateinit var btnFollow: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house_detail)

        // 设置标题栏
        supportActionBar?.title = "房源详情"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 获取传递过来的房源数据
        house = intent.getSerializableExtra("house") as House

        // 初始化控件
        initViews()
        // 显示数据
        showHouseData()
        // 设置监听
        setupListeners()
    }

    private fun initViews() {
        tvPrice = findViewById(R.id.tv_price)
        tvTypeTag = findViewById(R.id.tv_type_tag)
        tvCommunity = findViewById(R.id.tv_community)
        tvAddress = findViewById(R.id.tv_address)
        tvRoom = findViewById(R.id.tv_room)
        tvArea = findViewById(R.id.tv_area)
        tvFloor = findViewById(R.id.tv_floor)
        tvOrientation = findViewById(R.id.tv_orientation)
        tvDecoration = findViewById(R.id.tv_decoration)
        tvElevator = findViewById(R.id.tv_elevator)
        tvHouseNumber = findViewById(R.id.tv_house_number)
        layoutPaymentTerm = findViewById(R.id.layout_payment_term_detail)
        dividerPayment = findViewById(R.id.divider_payment)
        tvPaymentTerm = findViewById(R.id.tv_payment_term)
        tvContactName = findViewById(R.id.tv_contact_name)
        tvContactPhone = findViewById(R.id.tv_contact_phone)
        btnCall = findViewById(R.id.btn_call)
        btnFollow = findViewById(R.id.btn_follow)
    }

    private fun showHouseData() {
        // 价格和类型标签
        tvPrice.text = house.getPriceDesc()
        tvTypeTag.text = if (house.isRent) "出租" else "出售"

        // 基本信息
        tvCommunity.text = house.community
        tvAddress.text = house.address
        tvRoom.text = house.getRoomDesc()
        tvArea.text = "${house.area}㎡"
        tvFloor.text = house.getFloorDesc()

        // 房屋详情
        tvOrientation.text = house.getOrientationDesc()
        tvDecoration.text = house.decoration.ifEmpty { "未填写" }
        tvElevator.text = if (house.hasElevator) "有" else "无"
        tvHouseNumber.text = house.houseNumber.ifEmpty { "未填写" }

        // 结款期限（仅出租且有值时显示）
        if (house.isRent && house.paymentTerm.isNotEmpty()) {
            layoutPaymentTerm.visibility = View.VISIBLE
            dividerPayment.visibility = View.VISIBLE
            tvPaymentTerm.text = house.paymentTerm
        } else {
            layoutPaymentTerm.visibility = View.GONE
            dividerPayment.visibility = View.GONE
        }

        // 联系人
        tvContactName.text = house.contactName
        tvContactPhone.text = house.contactPhone
    }

    private fun setupListeners() {
        // 拨打电话
        btnCall.setOnClickListener {
            if (house.contactPhone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${house.contactPhone}")
                startActivity(intent)
            } else {
                Toast.makeText(this, "没有电话号码", Toast.LENGTH_SHORT).show()
            }
        }

        // 添加跟进记录
        btnFollow.setOnClickListener {
            Toast.makeText(this, "跟进记录功能开发中...", Toast.LENGTH_SHORT).show()
            // TODO: 跳转到添加跟进记录页面
        }
    }

    // 返回按钮
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
