package com.kodego.diangca.ebrahim.laundryexpres.dashboard.rider

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardRiderOrdersBinding

class DashboardRiderRideFragment(var dashboardRider: DashboardRiderActivity) : Fragment() {


    private var _binding: FragmentDashboardRiderOrdersBinding? = null
    private val binding get() = _binding!!

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

}