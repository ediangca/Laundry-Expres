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

    private var address: String? = null
    private var shopArrayList: ArrayList<Shop> = ArrayList()
    private var shopAdapter = ShopAdapter(this, shopArrayList)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardShopBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }

    override fun onResume() {
        super.onResume()
        Log.d("SHOP_LIST_ON_RESUME", "RESUME SHOP LIST")
        if (address!=null) {
            binding.address.setText(address)
            showShop()
            Log.d("SHOP", "shopArrayList.size -> ${shopArrayList.size}")
            if (shopArrayList.size > 0) {
                Log.d("SHOP", shopArrayList[0].toString())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("SHOP_LIST_ON_PAUSE", "PAUSE SHOP LIST")
        if (binding.address.text.toString().isNotEmpty()) {
            address = binding.address.text.toString()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("SHOP_LIST_ON_ATTACH", "ON ATTACH SHOP LIST")
        firebaseDatabaseReference.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                        address = snapshot.child(firebaseAuth.currentUser!!.uid).child("address")
                            .getValue(String::class.java)!!
                        binding.address.setText(address!!)
                        Log.d("USER_ADDRESS", address!!)
                        showShop()
                    } else {
                        Toast.makeText(
                            dashboardCustomer,
                            "No Address",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("USER_ADDRESS", error.message)
                    Toast.makeText(
                        dashboardCustomer,
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

    }

    override fun onDetach() {
        super.onDetach()
        Log.d("SHOP_LIST_ON_DETACH", "ON DETACH SHOP LIST")
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initComponent() {
        shopArrayList.clear()

        shopAdapter = ShopAdapter(this, shopArrayList)
        binding.shopList.layoutManager = LinearLayoutManager(dashboardCustomer)
        binding.shopList.adapter = shopAdapter
        binding.shopList.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            onPropertyStateChanged()
        }
        shopAdapter.notifyDataSetChanged()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(dashboardCustomer)

        binding.btnBack.setOnClickListener {
            btnBankOnClickListener()
        }

        binding.btnCurrentLocation.setOnClickListener {
            btnCurrentLocationOnClickListener()
        }

    }

    private fun btnCurrentLocationOnClickListener() {
        if (!dashboardCustomer.loadingDialog.isShowing) {
            setLocation()
        } else {
            Toast.makeText(
                context,
                "Please while retrieving available shop to your registered address.",
                Toast.LENGTH_SHORT
            )
        }
    }

    private fun btnBankOnClickListener() {
        dashboardCustomer.showHome()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showShop() {
        shopArrayList.clear()

        dashboardCustomer.showLoadingDialog()
        firebaseDatabaseReference.child("shop").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for ((index, postSnapshot) in dataSnapshot.children.withIndex()) {
                        val shop = postSnapshot.getValue(Shop::class.java)
                        if (shop!=null) {
                            val shopCity = getCity(shop.businessAddress!!) //if null -> n/a
                            Log.d("SHOP_CITY ${shop.uid}", shopCity)
                            val customerCity = getCity(address!!) //if null -> n/a
                            Log.d("CUSTOMER_CITY", customerCity)


                            if (shopCity==customerCity) {
                                Log.d("SHOP_RATES", "CHECK SHOP RATES ${shop.uid}")

                                //Check if the shop had added Rates
                                firebaseDatabase.reference.child("rates")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.hasChild(shop.uid!!)) {
                                                shopArrayList.add(shop)
                                                Log.d(
                                                    "SHOP_RATES",
                                                    "RATES FOUND @ ${shop.uid} -> ${shop.businessName}"
                                                )
                                                shopAdapter.notifyDataSetChanged()

                                            } else {
                                                Log.d(
                                                    "SHOP_RATES",
                                                    " RATES NOT FOUND"
                                                )
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Log.d("SHOP_RATES", error.message)
                                        }
                                    })
                            }

                        }
//                        Toast.makeText(context, "Retrieving Index $index out of ${dataSnapshot.childrenCount-1}", Toast.LENGTH_LONG).show()
                        if (index >= (dataSnapshot.childrenCount - 1)) {
                            dashboardCustomer.dismissLoadingDialog()
                        }
                    }
                } else {
                    Log.d("SHOP_ON_DATA_CHANGE", "NO SHOP YET AVAILABLE IN RECORD")
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


    private fun getCity(address: String): String {
        var addresses: List<Address>? = null
        var locality: String? = null

        if (address.isNotEmpty()) {
            var geocoder = Geocoder(binding.root.context)
            try {
                addresses = geocoder.getFromLocationName(address, 1)
            } catch (e: Exception) {
                Log.d("SEARCH_GEO_LOCATION", "${e.message}")
            }

            if (addresses!=null && addresses.isNotEmpty()) {
                locality = addresses[0].locality
            } else {
                Log.d("CITY AVAILABILITY", "NO AVAILABLE FROM SELECTED CITY")
            }
        }
        return locality ?: "n/a"
    }


    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                dashboardCustomer,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )==PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                dashboardCustomer,
                Manifest.permission.ACCESS_FINE_LOCATION
            )==PackageManager.PERMISSION_GRANTED
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
                    if (location!=null) {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
                        if (list.isNotEmpty()) {
                            this.address = list[0].getAddressLine(0)
                            binding.apply {
                                address.setText(list[0].getAddressLine(0) ?: "n/a")
                                val latitude = list[0].latitude
                                val longitude = list[0].longitude
                                val city = list[0].locality ?: "n/a"
                                val state = list[0].adminArea ?: "n/a"
                                val zipCode = list[0].postalCode ?: "n/a"
                                val country = list[0].countryName ?: "n/a"

                                Log.d(
                                    "GET_CURRENT_ADDRESS",
                                    "$latitude $longitude $city $state $zipCode $country"
                                )
                                showShop()
                            }
                        }
                    } else {
                        Toast.makeText(dashboardCustomer, "Not Found Location", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            } else {
                Toast.makeText(dashboardCustomer, "Please turn on location", Toast.LENGTH_LONG)
                    .show()
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

    fun onPropertyStateChanged() {
        if (shopAdapter.itemCount <= 0) {
            binding.promptView.visibility = View.VISIBLE
        } else {
            binding.promptView.visibility = View.GONE
        }
    }


}