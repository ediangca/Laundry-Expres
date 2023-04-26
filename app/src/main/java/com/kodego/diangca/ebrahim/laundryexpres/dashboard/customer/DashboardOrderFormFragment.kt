package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardOrderFormBinding


class DashboardOrderFormFragment(var dashboardCustomer: DashboardCustomerActivity) : Fragment() {

    private var _binding: FragmentDashboardOrderFormBinding? = null
    private val binding get() = _binding!!

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardOrderFormBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }

    private fun initComponent() {

        binding.apply {
            btnBack.setOnClickListener {
                dashboardCustomer.showShopList()
            }
            btnServiceRegular.setOnClickListener {
                btnServiceRegularOnClickListener()
            }
            btnServicePets.setOnClickListener {
                btnServicePetsOnClickListener()
            }
            btnServiceDryClean.setOnClickListener {
                btnServiceDryCleanOnClickListener()
            }
            btnServiceSneakers.setOnClickListener {
                btnServiceSneakersOnClickListener()
            }
        }

    }

    private fun btnServiceRegularOnClickListener() {
        setVisibility(binding.linear2)
    }

    private fun btnServicePetsOnClickListener() {
        setVisibility(binding.linear3)
    }

    private fun btnServiceDryCleanOnClickListener() {
        setVisibility(binding.linear4)
    }

    private fun btnServiceSneakersOnClickListener() {
        setVisibility(binding.linear5)
    }

    fun setVisibility(linear: LinearLayoutCompat) {
        if (linear.visibility==View.GONE) {
            linear.visibility = View.VISIBLE
        } else {
            linear.visibility = View.GONE
        }
    }


}