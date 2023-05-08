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

class DashboardOrdersFragment(var dashboardCustomer: DashboardCustomerActivity) : Fragment(),
    AdapterView.OnItemSelectedListener {

    private var _binding: FragmentDashboardOrdersBinding? = null
    private val binding get() = _binding!!

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()


    private var status =
        arrayOf("ALL", "FOR PICK-UP", "RECEIVED", "FOR DELIVERY", "COMPLETE", "CANCEL")

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
            onItemSelectedListener = this@DashboardOrdersFragment
            prompt = "Select Order Status"
            gravity = Gravity.CENTER
        }

        ordersList.clear()

        orderAdapter = OrderAdapter(dashboardCustomer, ordersList)
        orderAdapter.setDashboardCustomer(dashboardCustomer)
        orderAdapter.setCallBack("Order")
        binding.orderList.layoutManager = LinearLayoutManager(dashboardCustomer)
        binding.orderList.adapter = orderAdapter
        binding.orderList.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            onPropertyStateChanged()
        }
        showOrders()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//        showToast(message = "Spinner 2 Position:${position} and language: ${status[position]}")

        showOrders(status[position])

    }

    private fun showOrders(orderStatus: String) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        showToast(message = "Nothing selected")
    }

    private fun showToast(
        context: Context = dashboardCustomer.applicationContext,
        message: String,
        duration: Int = Toast.LENGTH_LONG,
    ) {
        Toast.makeText(context, message, duration).show()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun showOrders() {
        ordersList.clear()

        dashboardCustomer.showLoadingDialog()
        firebaseDatabaseReference.child("orders/$uid")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for ((index, postSnapshot) in dataSnapshot.children.withIndex()) {
                            val order = postSnapshot.getValue(Order::class.java)
                            if (order!=null) {
                                Log.d("SHOP_RATES", "CHECK SHOP RATES ${order.orderNo}")

                                //Check if the shop had added Rates
                                firebaseDatabase.reference.child("shop")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.hasChild(order.shopID!!)) {
                                                shop = snapshot.getValue(Shop::class.java)
                                                Log.d(
                                                    "SHOP",
                                                    "SHOP FOUND @ ${shop!!.uid} -> ${shop!!.businessName}"
                                                )

                                            } else {
                                                Log.d(
                                                    "SHOP","SHOP NOT FOUND"
                                                )
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Log.d("SHOP_ERROR", error.message)
                                        }
                                    })
                                ordersList.add(order)
                                orderAdapter.notifyDataSetChanged()
                            }
                            if (index >= (dataSnapshot.childrenCount - 1)) {
                                dashboardCustomer.dismissLoadingDialog()
                            }
                        }
                    } else {
                        Log.d("ORDER_ON_DATA_CHANGE", "NO ORDER YET AVAILABLE IN RECORD")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Getting Post failed, log a message
                    dashboardCustomer.dismissLoadingDialog()
                    Log.w("addValueEventListener", "loadPost:onCancelled", error.toException())
                    Toast.makeText(
                        dashboardCustomer,
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    fun onPropertyStateChanged() {
        if (orderAdapter.itemCount <= 0) {
            binding.promptView.visibility = View.VISIBLE
        } else {
            binding.promptView.visibility = View.GONE
        }
    }

}