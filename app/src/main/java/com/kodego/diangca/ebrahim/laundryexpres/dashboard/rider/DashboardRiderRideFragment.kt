package com.kodego.diangca.ebrahim.laundryexpres.dashboard.rider

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kodego.diangca.ebrahim.laundryexpres.adater.OrderAdapter
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardRiderOrdersBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Order
import com.kodego.diangca.ebrahim.laundryexpres.model.Requirements
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardRiderRideFragment(var dashboardRider: DashboardRiderActivity) : Fragment() {


    private var _binding: FragmentDashboardRiderOrdersBinding? = null
    private val binding get() = _binding!!

    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var status =
        arrayOf(
            "ALL",
//            "PENDING", //AFTER BOOKING
//            "FOR PICK-UP", // TO RELEASE LAUNDRY BY CUSTOMER TO PICK-UP BY RIDER TO DELIVER TO SHOP
            "TO PICK-UP", // AFTER ACCEPT BY RIDE
            "IN TRANSIT", // AFTER PICK-UP TRANSIT LAUNDRY TO SHOP
            "ON PROCESS", // LAUNDRY ACCEPTED BY SHOP
            "FOR DELIVERY", // TO RELEASE LAUNDRY BY SHOP WILL PICK-UP BY RIDER TO DELIVER TO CUSTOMER
            "TO DELIVER", // AFTER PICK-UP RIDER FROM SHOP
            "COMPLETE", // RECEIVE FROM CUSTOMER
//            "CANCEL" // CANCEL BY CUSTOMER
        )

    private var requirements: Requirements? = null

    private var ordersList: ArrayList<Order> = ArrayList()
    private var orderAdapter = OrderAdapter(dashboardRider, ordersList)

    private var callBack: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardRiderOrdersBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initComponent() {
        var statusAdapter = ArrayAdapter(dashboardRider, R.layout.simple_spinner_item, status)
        statusAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        with(binding.spinnerOrderStatus)
        {
            adapter = statusAdapter
            setSelection(0, true)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem =
                        parent.getItemAtPosition(position).toString() // Get the selected item
                    onPropertyStateChanged(selectedItem) // Pass the selected item
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Optionally handle the case where no item is selected

                }
            }
            prompt = "Select Order Status"
            gravity = Gravity.CENTER
        }

        Log.d("ON_SHOW_ORDER", "ORDER FRAGMENT")

        val bundle = arguments
        if (bundle != null) {
            requirements = bundle.getParcelable<Requirements>("requirements")!!
            Log.d("ON_RESUME_FETCH_REQUIREMENTS", requirements.toString())
        } else {
            requirements = dashboardRider.getRequirements()
            Log.d("ON_FETCH_SHOP_REQUIREMENTS", requirements.toString())
        }

        orderAdapter = OrderAdapter(dashboardRider, ordersList)
        orderAdapter.setCallBack("Order")
        binding.orderList.layoutManager = LinearLayoutManager(dashboardRider)
        binding.orderList.adapter = orderAdapter

        showOrders("ALL")

    }

    fun onPropertyStateChanged(selectedItem: String) {
        if (orderAdapter.itemCount <= 0) {
            binding.promptView.visibility = View.VISIBLE
        } else {
            binding.promptView.visibility = View.GONE
        }

        showOrders(selectedItem);
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun showOrders(status: String) {
        // Clear the list and show the progress bar
        ordersList.clear()
        dashboardRider.showLoadingDialog()
        Log.d("SHOW ORDER STATUS", status)

        // Firebase Database Reference to 'orders'
        val databaseReference = FirebaseDatabase.getInstance().reference.child("orders")

        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val ordersToAdd = mutableListOf<Order>()

                snapshot.children.forEach { userSnapshot ->
                    userSnapshot.children.forEach { orderSnapshot ->
                        val riderID = orderSnapshot.child("rid").getValue(String::class.java)
                        val orderStatus = orderSnapshot.child("status").getValue(String::class.java)
                        val orderData = orderSnapshot.getValue(Order::class.java)

                        if (orderData != null && riderID == requirements?.uid) {
                            when (status) {
                                // If "All" is selected, disregard status and add all orders for this shop
                                "ALL" -> {
                                    ordersToAdd.add(orderData)
                                }
                                // Otherwise, filter based on status
                                else -> {
                                    if (orderStatus.equals(status, true)) {
                                        ordersToAdd.add(orderData)
                                    }
                                }
                            }
                        }
                    }
                }

                // Add the filtered orders and update UI
                ordersList.addAll(ordersToAdd)
                orderAdapter.notifyDataSetChanged()
                sortAndNotify(status)
            } else {
                sortAndNotify(status)
            }
        }.addOnFailureListener { exception ->
            // Handle database errors
            sortAndNotify(status)
            Toast.makeText(dashboardRider, "Error: ${exception.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun sortAndNotify(status: String) {
        // Update prompt text based on the status
        binding.promptView.text = if (ordersList.isEmpty()) {
            if (status == "all") "No Ride yet!" else "No $status Ride"
        } else {
            // Sort the orders by descending pickUpDatetime
            ordersList.sortByDescending { parseDatetime(it.pickUpDatetime) }
            binding.promptView.visibility = View.GONE
            orderAdapter.notifyDataSetChanged()
            dashboardRider.dismissLoadingDialog()
            return // Exit early as there's no need to continue if ordersList is not empty
        }

        dashboardRider.dismissLoadingDialog()
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