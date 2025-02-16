package com.kodego.diangca.ebrahim.laundryexpres.dashboard.partner

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.adater.OrderAdapter
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardPartnerHomeBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.CustomerRequest
import com.kodego.diangca.ebrahim.laundryexpres.model.Notification
import com.kodego.diangca.ebrahim.laundryexpres.model.Order
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardHomeFragment(var dashboardPartner: DashboardPartnerActivity) : Fragment() {

    private var _binding: FragmentDashboardPartnerHomeBinding? = null
    private val binding get() = _binding!!

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var user: User? = null
    private var displayName: String? = null
    private var profileUri: Uri? = null

    val formatter = SimpleDateFormat("M/d/yyyy hh:mm a", Locale.ENGLISH)
    val currentDate = System.currentTimeMillis()

    private var ordersList: ArrayList<Order> = ArrayList()
    private var orderAdapter = OrderAdapter(dashboardPartner, ordersList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardPartnerHomeBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
    }

    private fun initComponent() {

        orderAdapter = OrderAdapter(dashboardPartner, ordersList)
        orderAdapter.setCallBack("Home")
        binding.orderList.layoutManager = LinearLayoutManager(dashboardPartner)
        binding.orderList.adapter = orderAdapter
        showOrders("ALL")

    }


    private fun showOrders(status: String) {
        // Clear the list and show the progress bar
        ordersList.clear()
        Log.d("SHOW ORDER STATUS", status)

        // Firebase Database Reference to 'orders'
        val databaseReference = FirebaseDatabase.getInstance().reference.child("orders")

        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val ordersToAdd = mutableListOf<Order>()

                snapshot.children.forEach { userSnapshot ->
                    userSnapshot.children.forEach { orderSnapshot ->
                        val shopID = orderSnapshot.child("shopID").getValue(String::class.java)
                        val orderStatus = orderSnapshot.child("status").getValue(String::class.java)
                        val orderData = orderSnapshot.getValue(Order::class.java)

                        if (orderData != null && shopID == firebaseAuth.currentUser!!.uid) {

                            ordersToAdd.add(orderData)

                        }
                    }
                }

                // Add the filtered orders and update UI
                orderAdapter.hideSchedule(true)
                ordersList.addAll(ordersToAdd)
                orderAdapter.notifyDataSetChanged()
                sortAndNotify(status)
            } else {
                sortAndNotify(status)
            }
        }.addOnFailureListener { exception ->
            // Handle database errors
            sortAndNotify(status)
            Toast.makeText(dashboardPartner, "Error: ${exception.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun sortAndNotify(status: String) {
        with(binding) {
            when (status) {
                "ALL" -> promptView.text = "No Pending Booking"
                else -> promptView.text = "No $status Booking"
            }
            if (ordersList.isEmpty()) {
                promptView.visibility = View.VISIBLE
            } else {
                // Sort ordersList by descending pickUpDatetime
                val sortedOrders =
                    ordersList.sortedByDescending { parseDatetime(it.pickUpDatetime) }
                        .take(5)

//                 Update the adapter's dataset instead of modifying ordersList directly
                orderAdapter.updateList(ArrayList(sortedOrders))

//                ordersList.sortByDescending { order ->
//                    parseDatetime(order.pickUpDatetime)
//                }

                promptView.visibility = View.GONE
                orderAdapter.notifyDataSetChanged()
            }
        }
        monitorNotification()
    }

    private fun monitorNotification() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        val notificationRef = firebaseDatabaseReference.child("notification")

        notificationRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                val unreadNotifications =
                    mutableListOf<Notification>() // Store matched notifications

                for (childSnapshot in snapshot.children) {
                    val notification = childSnapshot.getValue(Notification::class.java)

                    Log.d("Monitor Notifications", notification.toString())

                    if (notification != null &&
                        notification.shopID == dashboardPartner.getShop()!!.uid &&
                        notification.sunread
                    ) { // Matches shopID and unread

                        unreadNotifications.add(notification)
                    }
                }

                Log.d("Notifications", "Unread Notifications Count: ${unreadNotifications.size}")
                // Log the count of unread notifications
                if (unreadNotifications.size > 0) {
                    binding.notificationBadge.visibility = View.VISIBLE
                    binding.notificationBadge.text = unreadNotifications.size.toString()
                }else{
                    binding.notificationBadge.visibility = View.GONE
                }

                // Handle the unread notifications array (e.g., update UI, trigger alert, etc.)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching notifications: ${error.message}")
            }
        })
    }

    // Helper to parse datetime string
    private fun parseDatetime(datetime: String?): Long? {
        return try {
            datetime?.let {
                SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault()).parse(it)?.time
            }
        } catch (e: Exception) {
            Log.e("DATETIME_PARSE", "Error parsing date: $datetime", e)
            null
        }
    }


}