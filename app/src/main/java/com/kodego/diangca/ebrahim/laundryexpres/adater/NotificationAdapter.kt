package com.kodego.diangca.ebrahim.laundryexpres.adater

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer.DashboardCustomerActivity
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.partner.DashboardPartnerActivity
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.rider.DashboardRiderActivity
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ItemNotificationBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Notification
import com.kodego.diangca.ebrahim.laundryexpres.model.Order

class NotificationAdapter(var activity: Activity, var notificationList: ArrayList<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private var dashboardCustomer: DashboardCustomerActivity? = null
    private var dashboardPartner: DashboardPartnerActivity? = null

    private var callBack: String? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapter.NotificationViewHolder {
        val itemBinding =
            ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(itemBinding)
    }

    override fun onBindViewHolder(
        holder: NotificationAdapter.NotificationViewHolder,
        position: Int
    ) {
        holder.bindNotification(notificationList[position])
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    inner class NotificationViewHolder(
        private val itemBinding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        var notification = Notification()

        fun bindNotification(notification: Notification) {

            this.notification = notification
            with(itemBinding) {
                orderNo.text = notification.orderNo
                statusLabel.text = notification.status


                val databaseReference = FirebaseDatabase.getInstance().getReference("orders/${notification.customerID}");

                databaseReference.child(notification.orderNo!!)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            val order = dataSnapshot.getValue(Order::class.java)


                            btnDetails.setOnClickListener {
                                btnSelectOnClickListener(itemBinding, adapterPosition, order!!)
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w("Monitor Notification", "loadPost:onCancelled", error.toException())
                            Toast.makeText(activity, error.message, Toast.LENGTH_SHORT).show()
                        }
                    });



            }

        }
        override fun onClick(v: View?) {

        }


        private fun btnSelectOnClickListener(
            itemBinding: ItemNotificationBinding,
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
                is DashboardRiderActivity ->{
                    Log.d("ON_SHOW_DETAIL", "DASHBOARD RIDER")
                    (activity as DashboardRiderActivity).showOrderDetails(order, callBack!!)
                }

            }
        }
    }

}