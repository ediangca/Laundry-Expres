package com.kodego.diangca.ebrahim.laundryexpres.adater

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ItemShopBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Order

class OrderAdapter(var activity: Activity, var orderList: ArrayList<Order>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {


    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): OrderAdapter.OrderViewHolder {
        val itemBinding =
            ItemShopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: OrderAdapter.OrderViewHolder, position: Int) {
        holder.bindShop(orderList[position])
    }

    inner class OrderViewHolder(
        private val itemBinding: ItemShopBinding,
    ) :
        RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        var order = Order()
        fun bindShop(order: Order) {
            this.order = order
            with(itemBinding){

            }
            itemBinding.btnSelect.setOnClickListener {
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
            itemBinding: ItemShopBinding,
            positionAdapter: Int,
            order: Order) {

        }

    }
}