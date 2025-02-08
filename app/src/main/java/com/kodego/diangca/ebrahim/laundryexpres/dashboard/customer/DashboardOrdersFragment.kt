package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.adater.OrderAdapter
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardOrdersBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Order
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardOrdersFragment(var dashboardCustomer: DashboardCustomerActivity) : Fragment() {

    private var _binding: FragmentDashboardOrdersBinding? = null
    private val binding get() = _binding!!

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var status =
        arrayOf(
            "ALL",
            "PENDING", //AFTER BOOKING
            "FOR PICK-UP", // AFTER ACCEPT BY LAUNDRY
            "TO PICK-UP", // AFTER ACCEPT BY RIDE
            "IN TRANSIT", // AFTER PICK-UP TRANSIT LAUNDRY TO SHOP
            "ON PROCESS", // LAUNDRY ACCEPTED BY SHOP
            "FOR DELIVERY", // TO RELEASE LAUNDRY BY SHOP
            "TO DELIVER", // AFTER PICK-UP RIDER FROM SHOP
            "COMPLETE", // RECEIVE FROM CUSTOMER
            "CANCEL" // CANCEL BY CUSTOMER
        )

    private var uid: String? = null
    private var shopId: String? = null

    private var shop: Shop? = null

    private var ordersList: ArrayList<Order> = ArrayList()
    private var orderAdapter = OrderAdapter(dashboardCustomer, ordersList)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardOrdersBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initComponent() {

        uid = firebaseAuth.currentUser!!.uid
        var statusAdapter = ArrayAdapter(dashboardCustomer, R.layout.simple_spinner_item, status)
        statusAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)


        with(binding.spinnerOrderStatus)
        {
            adapter = statusAdapter
            setSelection(0, false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem =
                        parent.getItemAtPosition(position).toString() // Get the selected item
                    showOrders(selectedItem);

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Optionally handle the case where no item is selected

                }
            }
            prompt = "Select Order Status"
            gravity = Gravity.CENTER
        }

        ordersList.clear()

        orderAdapter = OrderAdapter(dashboardCustomer, ordersList)
//        orderAdapter.setDashboardCustomer(dashboardCustomer)
        orderAdapter.setCallBack("Order")
        binding.orderList.layoutManager = LinearLayoutManager(dashboardCustomer)
        binding.orderList.adapter = orderAdapter
        showOrders("ALL")
    }

    fun onPropertyStateChanged(selectedItem: String) {
        showOrders(selectedItem);
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showOrders(status: String) {
        ordersList.clear()
        dashboardCustomer.showLoadingDialog()
        Log.d("SHOW ORDER STATUS", status)

        // Fetch orders for the user
        firebaseDatabaseReference.child("orders/$uid")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        dashboardCustomer.dismissLoadingDialog()
                        updateUI()
                        return
                    }

                    // Loop through orders
                    val ordersToAdd = mutableListOf<Order>()
                    val children = dataSnapshot.children.toList()
                    children.forEachIndexed { index, postSnapshot ->
                        val order = postSnapshot.getValue(Order::class.java)
                        if (order != null) {
//                            checkShopRates(order)
                            when (status) {
                                // If "All" is selected, disregard status and add all orders for this shop
                                "ALL" -> {
                                    ordersToAdd.add(order)
                                }
                                // Otherwise, filter based on status
                                else -> {
                                    if (order.status.equals(status, true)) {
                                        ordersToAdd.add(order)
                                    }
                                }
                            }
                        }

                        // Check if it's the last item
                        if (index == children.lastIndex) {
                            ordersList.addAll(ordersToAdd)
                            orderAdapter.notifyDataSetChanged()
                            sortAndNotify(status)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("addValueEventListener", "loadPost:onCancelled", error.toException())
                    dashboardCustomer.dismissLoadingDialog()
                    binding.promptView.visibility = View.VISIBLE
                    Toast.makeText(dashboardCustomer, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Function to check shop rates and log information
    private fun checkShopRates(order: Order) {
        firebaseDatabase.reference.child("shop")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(order.shopID ?: "")) {
                        shop = snapshot.child(order.shopID!!).getValue(Shop::class.java)
                        shop?.let {
                            Log.d(
                                "SHOP",
                                "SHOP FOUND @ ${it.uid} -> ${it.businessName}"
                            )
                        }
                    } else {
                        Log.d("SHOP", "SHOP NOT FOUND")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("SHOP_ERROR", error.message)
                }
            })
    }

    // Sort and notify adapter
    @SuppressLint("SetTextI18n")
    private fun sortAndNotify(status: String) {
        with(binding) {
            when (status) {
                "ALL" -> promptView.text = "No Booking yet!"
                else -> promptView.text = "No $status Booking"
            }
            if (ordersList.isEmpty()) {
                promptView.visibility = View.VISIBLE
            } else {
                // Sort ordersList by descending pickUpDatetime
                ordersList.sortByDescending { order ->
                    parseDatetime(order.pickUpDatetime)
                }
                promptView.visibility = View.GONE
            }
            dashboardCustomer.dismissLoadingDialog()
        }
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

    // Update UI on empty ordersList
    private fun updateUI() {
        if (ordersList.isEmpty()) {
            binding.promptView.visibility = View.VISIBLE
        } else {
            binding.promptView.visibility = View.GONE
            orderAdapter.notifyDataSetChanged()
        }
    }


    fun onPropertyStateChanged() {
        if (orderAdapter.itemCount <= 0) {
            binding.promptView.visibility = View.VISIBLE
        } else {
            binding.promptView.visibility = View.GONE
        }
    }

}