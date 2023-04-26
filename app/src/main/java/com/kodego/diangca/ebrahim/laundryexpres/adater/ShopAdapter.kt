package com.kodego.diangca.ebrahim.laundryexpres.adater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer.DashboardShopFragment
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ItemShopBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop

class ShopAdapter(var dashboardShopFragment: DashboardShopFragment, var shopList: ArrayList<Shop>) :
    RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {


    override fun getItemCount(): Int {
        return shopList.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ShopAdapter.ShopViewHolder {
        val itemBinding =
            ItemShopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShopViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ShopAdapter.ShopViewHolder, position: Int) {
        holder.bindShop(shopList[position])
    }

    inner class ShopViewHolder(
        private val itemBinding: ItemShopBinding,
    ) :
        RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        var shop = Shop()
        fun bindShop(shop: Shop) {
            this.shop = shop
            with(itemBinding){

                shopName.text = shop.businessName
                shopAddress.text = shop.businessAddress

//            shopPic.setImageBitmap()
            }
            itemBinding.btnSelect.setOnClickListener {
                btnSelectOnClickListener(itemBinding, adapterPosition, shop)
            }
        }

        override fun onClick(view: View?) {
            Toast.makeText(
                dashboardShopFragment.dashboardCustomer,
                "${shop.businessName}",
                Toast.LENGTH_SHORT
            ).show()
        }

        private fun btnSelectOnClickListener(
            itemBinding: ItemShopBinding,
            positionAdapter: Int,
            shop: Shop,
        ) {
            dashboardShopFragment.dashboardCustomer.showOrderForm(shop)
        }

    }
}