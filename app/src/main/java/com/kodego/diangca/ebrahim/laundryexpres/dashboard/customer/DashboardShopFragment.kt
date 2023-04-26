package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.adater.ShopAdapter
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardShopBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop
import java.util.*

class DashboardShopFragment(var dashboardCustomer: DashboardCustomerActivity) : Fragment() {

    private var _binding: FragmentDashboardShopBinding? = null
    private val binding get() = _binding!!

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val permissionId = 2
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var shopArrayList : ArrayList<Shop> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardShopBinding.inflate(layoutInflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }

    private fun initComponent() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(dashboardCustomer)

        firebaseDatabaseReference.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                        val address: String = snapshot.child(firebaseAuth.currentUser!!.uid).child("address")
                            .getValue(String::class.java)!!
                        binding.address.setText(address)
                    } else {
                        Toast.makeText(
                            dashboardCustomer,
                            "No Address",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        dashboardCustomer,
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        showShop()

        binding.btnBack.setOnClickListener {
            btnBankOnClickListener()
        }

        binding.btnCurrentLocation.setOnClickListener {
            btnCurrentLocationOnClickListener()
        }
    }

    private fun btnCurrentLocationOnClickListener() {
        setLocation()
    }

    private fun btnBankOnClickListener() {
        dashboardCustomer.showHome()
    }

    private fun showShop() {
        shopArrayList.clear()
        firebaseDatabaseReference.child("shop").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    for (postSnapshot in dataSnapshot.children) {
                        val shop = postSnapshot.getValue(Shop::class.java)

                        if(shop!=null) {
                            val shopCity = getCity(shop.businessAddress!!) //if null -> n/a
                            Log.d("SHOP_CITY", shopCity)
                            val customerCity = getCity(binding.address.text.toString()) //if null -> n/a
                            Log.d("CUSTOMER_CITY", customerCity)

                            if (shopCity==customerCity) {
                                shopArrayList.add(shop)
                            }
                        }
                    }

                    Log.d("SHOP", "shopArrayList.size -> ${shopArrayList.size}")
                    if(shopArrayList.size > 0){
                        Log.d("SHOP", shopArrayList[0].toString())
                    }
                    setShopAdapter()

                }else{
                    Toast.makeText(
                        dashboardCustomer,"Sorry! No Available Laundry Shop found in your area.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("addValueEventListener", "loadPost:onCancelled", error.toException())

                Toast.makeText(
                    dashboardCustomer,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setShopAdapter() {
        val shopAdapter = ShopAdapter(this, shopArrayList)
        binding.shopList.layoutManager = LinearLayoutManager(dashboardCustomer)
        binding.shopList.adapter = shopAdapter
        shopAdapter.notifyDataSetChanged()
    }

    private fun getCity(address: String): String {
        var addresses : List<Address>? = null
        var locality : String? = null

        if(address.isNotEmpty()){
            var geocoder = Geocoder(binding.root.context)
            try {
                addresses = geocoder.getFromLocationName(address, 1)
            }catch (e: Exception){
                Log.d("SEARCH_GEO_LOCATION", "${e.message}")
            }

            if(addresses!=null && addresses.isNotEmpty()) {
                locality = addresses[0].locality
            }else{
                Log.d("CITY AVAILABILITY", "NO AVAILABLE FROM SELECTED CITY")
            }
        }
        return locality?: "n/a"
    }


    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                dashboardCustomer,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                dashboardCustomer,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            dashboardCustomer.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun setLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(dashboardCustomer) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
                        if(list.isNotEmpty()){
                            binding.apply {
                                address.setText(list[0].getAddressLine(0)?:"n/a")
                                val latitude = list[0].latitude
                                val longitude = list[0].longitude
                                val city = list[0].locality?:"n/a"
                                val state = list[0].adminArea?:"n/a"
                                val zipCode = list[0].postalCode?:"n/a"
                                val country = list[0].countryName?:"n/a"

                                Log.d("GET_CURRENT_ADDRESS", "$latitude $longitude $city $state $zipCode $country")
                                showShop()
                            }
                        }
                    }else{
                        Toast.makeText(dashboardCustomer, "Not Found Location", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(dashboardCustomer, "Please turn on location", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            dashboardCustomer,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }


}