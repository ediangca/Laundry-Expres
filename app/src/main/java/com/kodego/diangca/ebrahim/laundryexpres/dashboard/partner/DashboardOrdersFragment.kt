package com.kodego.diangca.ebrahim.laundryexpres.dashboard.partner

import android.R
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kodego.diangca.ebrahim.laundryexpres.adater.OrderAdapter
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardPartnerOrdersBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Order
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop

class DashboardOrdersFragment(var dashboardPartner: DashboardPartnerActivity) : Fragment() {

    private var _binding: FragmentDashboardPartnerOrdersBinding? = null
    private val binding get() = _binding!!

    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")

    private var shop: Shop? = null

    private var ordersList: ArrayList<Order> = ArrayList()
    private var orderAdapter = OrderAdapter(dashboardPartner, ordersList)

    private var callBack: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardPartnerOrdersBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun initComponent() {
        Log.d("ON_SHOW_ORDER", "ORDER FRAGMENT")

        val bundle = arguments
        if (bundle!=null) {
            shop = bundle.getParcelable<Shop>("shop")!!
            Log.d("ON_RESUME_FETCH_SHOP", shop.toString())
        } else {
            shop = dashboardPartner.getShop()
            Log.d("ON_FETCH_SHOP", shop.toString())
        }

        orderAdapter = OrderAdapter(dashboardPartner, ordersList)
//        orderAdapter.setDashboardPartner(dashboardPartner)
        orderAdapter.setCallBack("Order")
        binding.orderList.layoutManager = LinearLayoutManager(dashboardPartner)
        binding.orderList.adapter = orderAdapter
        binding.orderList.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            onPropertyStateChanged()
        }

        showOrders()

    }

    fun setCallBack(callBack: String?) {
        this.callBack = callBack
    }

    fun onPropertyStateChanged() {
        if (orderAdapter.itemCount <= 0) {
            binding.promptView.visibility = View.VISIBLE
        } else {
            binding.promptView.visibility = View.GONE
        }
    }
    private fun showOrders() {
        // Clear the list and show the progress bar
        ordersList.clear()
        binding.progressBar.visibility = View.VISIBLE

        // Firebase Database Reference to 'orders'
        val databaseReference = FirebaseDatabase.getInstance().reference.child("orders")

        databaseReference.get().addOnSuccessListener { snapshot ->
            // Iterate through each key under 'orders'

            Log.d("ON_FETCH_ORDER", snapshot.value.toString())

            for (userSnapshot in snapshot.children) {
                // Iterate through each order under the user ID
                for (orderSnapshot in userSnapshot.children) {
                    val shopID = orderSnapshot.child("shopID").getValue(String::class.java)

                    // Check if shopID matches the target value
                    if (shopID == shop?.uid) {
                        // Convert the snapshot into a HashMap or custom data class
                        val orderData = orderSnapshot.getValue(Order::class.java)
                        if (orderData != null) {
                            ordersList.add(orderData)
                        }
                    }
                }
            }

            // Hide progress bar and update your UI
            if(ordersList.isEmpty()){
                binding.promptView.visibility = View.VISIBLE
            }else{
                binding.progressBar.visibility = View.GONE
                // Notify your adapter or any recyclerView here
                orderAdapter.notifyDataSetChanged()
            }

        }.addOnFailureListener {
            // Handle Errors
            binding.progressBar.visibility = View.GONE
            binding.promptView.visibility = View.VISIBLE
            Toast.makeText(dashboardPartner, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }


}