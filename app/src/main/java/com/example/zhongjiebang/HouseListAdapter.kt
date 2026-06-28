package com.example.zhongjiebang

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * 房源列表 Adapter
 */
class HouseListAdapter(
    private val houseList: List<House>,
    private val onItemClick: ((House) -> Unit)? = null
) : RecyclerView.Adapter<HouseListAdapter.HouseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_house, parent, false)
        return HouseViewHolder(view)
    }

    override fun onBindViewHolder(holder: HouseViewHolder, position: Int) {
        val house = houseList[position]
        holder.bind(house)
    }

    override fun getItemCount(): Int = houseList.size

    inner class HouseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCommunity: TextView = itemView.findViewById(R.id.tv_community)
        private val tvRoomArea: TextView = itemView.findViewById(R.id.tv_room_area)
        private val tvFloorOrientation: TextView = itemView.findViewById(R.id.tv_floor_orientation)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_price)
        private val tvPaymentTerm: TextView = itemView.findViewById(R.id.tv_payment_term)
        private val tvAddress: TextView = itemView.findViewById(R.id.tv_address)

        fun bind(house: House) {
            // 小区名称
            tvCommunity.text = house.community

            // 房型 + 面积
            tvRoomArea.text = "${house.getRoomDesc()} | ${house.area}㎡"

            // 楼层 + 朝向 + 装修
            val floorOrientation = "${house.getFloorDesc()} | ${house.getOrientationDesc()} | ${house.decoration}"
            tvFloorOrientation.text = floorOrientation

            // 价格
            tvPrice.text = house.getPriceDesc()

            // 结款期限（仅出租且有值时显示）
            if (house.isRent && house.paymentTerm.isNotEmpty()) {
                tvPaymentTerm.visibility = View.VISIBLE
                tvPaymentTerm.text = house.paymentTerm.split(",").firstOrNull() ?: ""
            } else {
                tvPaymentTerm.visibility = View.GONE
            }

            // 地址/街道
            tvAddress.text = if (house.address.isNotEmpty()) house.address else house.houseNumber

            // 点击事件
            itemView.setOnClickListener {
                onItemClick?.invoke(house)
            }
        }
    }
}
