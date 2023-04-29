package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardOrderFormBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop


class DashboardOrderFormFragment(var dashboardCustomer: DashboardCustomerActivity) : Fragment() {

    private var _binding: FragmentDashboardOrderFormBinding? = null
    private val binding get() = _binding!!

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()


    private lateinit var shop: Shop
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

//        shop = dashboardCustomer.getShop()
        val bundle = this.arguments
        if (bundle!=null) {
            val shopFromBundle = bundle.getParcelable<Shop>("shop")!!
            binding.shopName.text = shopFromBundle.businessName
            binding.shopAddress.text = shopFromBundle.businessAddress
            shopFromBundle.apply {
                Log.d("FETCH_SHOP", shop.toString())

            }
        }

        binding.apply {
            btnBack.setOnClickListener {
                dashboardCustomer.resumeShopList()
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
        setVisibility(binding.linear2, binding.btnServiceRegular)
    }

    private fun btnServicePetsOnClickListener() {
        setVisibility(binding.linear3, binding.btnServicePets)
    }

    private fun btnServiceDryCleanOnClickListener() {
        setVisibility(binding.linear4, binding.btnServiceDryClean)
    }

    private fun btnServiceSneakersOnClickListener() {
        setVisibility(binding.linear5, binding.btnServiceSneakers)
    }

    private fun setVisibility(linear: LinearLayoutCompat, view: View) {

        val button = view as AppCompatButton

        if (linear.visibility==View.GONE) {
            linear.visibility = View.VISIBLE
            button.background = ContextCompat.getDrawable(dashboardCustomer,R.drawable.button_secondary)
        } else {
            linear.visibility = View.GONE
            button.background = ContextCompat.getDrawable(dashboardCustomer,R.drawable.button_white)
        }
    }


}

