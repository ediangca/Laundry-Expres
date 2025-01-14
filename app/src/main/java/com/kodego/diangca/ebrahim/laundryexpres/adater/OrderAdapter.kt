package com.kodego.diangca.ebrahim.laundryexpres.adater

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer.DashboardCustomerActivity
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.partner.DashboardPartnerActivity
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ItemOrdersBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Order
import com.kodego.diangca.ebrahim.laundryexpres.model.Rates
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop

class OrderAdapter(var activity: Activity, var orderList: ArrayList<Order>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {


    private var dashboardCustomer: DashboardCustomerActivity? = null
    private var dashboardPartner: DashboardPartnerActivity? = null

    private var callBack: String? = null

//    @JvmName("setDashboardCustomer1")
//    fun setDashboardCustomer(dashboardCustomer: DashboardCustomerActivity){
//        this.dashboardCustomer = dashboardCustomer
//    }
//    fun setDashboardPartner(dashboardPartner: DashboardPartnerActivity){
//        this.dashboardPartner = dashboardPartner
//    }


    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): OrderAdapter.OrderViewHolder {
        val itemBinding =
            ItemOrdersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: OrderAdapter.OrderViewHolder, position: Int) {
        holder.bindShop(orderList[position])
    }

    fun setCallBack(callBack: String) {
        this.callBack = callBack
    }

    inner class OrderViewHolder(
        private val itemBinding: ItemOrdersBinding,
    ) :
        RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        var order = Order()
        @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
        fun bindShop(order: Order) {
            this.order = order
            with(itemBinding){
                orderNo.text = order.orderNo
                pickUpDatetimeLabel.text = order.pickUpDatetime
                deliveryDatetimeLabel.text = order.deliveryDatetime
                statusLabel.text = "BOOK ${order.status}"
//                "ALL", "FOR PICK-UP", "RECEIVED", "FOR DELIVERY", "COMPLETE", "CANCEL"
                when(order.status){
                    "FOR PICK-UP" ->{
                        statusLabel.setTextColor(activity.getColor(R.color.color_blue_1))
                    }
                    "RECEIVED" ->{
                        statusLabel.setTextColor(activity.getColor(R.color.color_blue_2))
                    }
                    "FOR DELIVERY" ->{
                        statusLabel.setTextColor(activity.getColor(R.color.color_blue_3))
                    }
                    "COMPLETE" ->{
                        statusLabel.setTextColor(activity.getColor(R.color.success))
                    }
                    "CANCEL" ->{
                        statusLabel.setTextColor(activity.getColor(R.color.danger))
                    }
                    else ->{
                        Toast.makeText(activity.applicationContext, "No Status stated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            itemBinding.btnDetails.setOnClickListener {
                btnSelectOnClickListener(itemBinding, adapterPosition, order)
            }
        }

        override fun onClick(view: View?) {
            Toast.makeText(
                activity,
                "${order.orderNo}",
                Toast.LENGTH_SHORT
            ).show()
        }

        private fun btnSelectOnClickListener(
            itemBinding: ItemOrdersBinding,
            positionAdapter: Int,
            order: Order) {

            when(activity){
                is DashboardCustomerActivity ->{
                    Log.d("ON_SHOW_DETAIL", "DASHBOARD CUSTOMER")
                    (activity as DashboardCustomerActivity).showOrderDetails(order, callBack!!)
                }
                is DashboardPartnerActivity ->{
                    Log.d("ON_SHOW_DETAIL", "DASHBOARD PARTNER")
                    (activity as DashboardPartnerActivity).showOrderDetails(order, callBack!!)
                }

            }
        }

    }
}