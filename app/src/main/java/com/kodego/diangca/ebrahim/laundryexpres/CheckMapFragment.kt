package com.kodego.diangca.ebrahim.laundryexpres

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogShopAvailabilityBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentCheckMapBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop
import java.util.*


class CheckMapFragment(var mainFragment: MainFragment) : Fragment() {

    private var _binding: FragmentCheckMapBinding? = null
    private val binding get() = _binding!!

    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    private lateinit var mapFragment: SupportMapFragment

    private lateinit var mainFrame: FragmentTransaction
    private lateinit var googleMap: GoogleMap

    private var currentLocation = LatLng(7.445660, 125.805809)
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var currentLatLong: LatLng

    private lateinit var promptBuilder: AlertDialog.Builder
    private lateinit var promptDialog: Dialog

    private var shopArrayList: ArrayList<Shop> = ArrayList()
    private var addresses: List<Address>? = null
    private var customerCity: String = ""


    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCheckMapBinding.inflate(layoutInflater, container, false)
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
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(mainFragment.indexActivity)

        mapFragment = SupportMapFragment()
        mapFragment.getMapAsync(OnMapReadyCallback {
            mapFragmentOnMapReadyCallback(it)
        })

        mainFrame = mainFragment.indexActivity.supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.mapFragment, mapFragment)
        mainFrame.commit()

        binding.btnBack.setOnClickListener {
            mainFragment.indexActivity.mainFrame =
                mainFragment.indexActivity.supportFragmentManager.beginTransaction()
            mainFragment.indexActivity.mainFrame.replace(
                R.id.mainFrame,
                CheckAvailabilityFragment(mainFragment)
            );
            mainFragment.indexActivity.mainFrame.addToBackStack(null);
            mainFragment.indexActivity.mainFrame.commit();
        }

        binding.btnProceed.setOnClickListener {
            btnProceedOnClickListener()
        }

        binding.btnSearch.setOnClickListener {
            btnSearchOnClickListener()
        }

    }

    private fun btnSearchOnClickListener() {
        val stringLocation = binding.searchLocation.text.toString()
        addresses = null
        if (!stringLocation.isEmpty()) {
            var geocoder = Geocoder(binding.root.context)
            try {
                addresses = geocoder.getFromLocationName(stringLocation, 1)

            } catch (e: Exception) {
                Log.d("SEARCH_GEO_LOCATION", "${e.message}")
            }
            if (addresses!=null) {
                if (addresses!!.isNotEmpty()) {
                    val address = addresses!![0]
                    customerCity = addresses!![0].locality ?: ""
                    val searchLocation = LatLng(address.latitude, address.longitude)
                    placeMarkerOnMap(searchLocation)
                } else {
                    Toast.makeText(
                        context,
                        "Can't Find Address! Please try again!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    private fun btnProceedOnClickListener() {
        showShop()
    }


    private fun mapFragmentOnMapReadyCallback(gMap: GoogleMap) {

        googleMap = gMap
        googleMap.uiSettings.isZoomControlsEnabled = true
//        googleMap.addMarker(MarkerOptions().position(currentLocation).title("My Location"))
//        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10f))

        setUpMap()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                binding.root.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            !=PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                binding.root.context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )!=PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                mainFragment.indexActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )

            return
        }
        googleMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(mainFragment.indexActivity) { location ->
            if (location!=null) {
                lastLocation = location
                currentLatLong = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLong)

            }
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        this.currentLatLong = currentLatLong
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(currentLatLong).title("You're Here!"))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 10f))

        getAddressDetails()

    }

    private fun getAddressDetails() {

        var geocoder = Geocoder(mainFragment.indexActivity, Locale.getDefault())
        addresses = geocoder.getFromLocation(currentLatLong.latitude, currentLatLong.longitude, 1)

        if (addresses!=null) {
            if (addresses!!.isNotEmpty()) {
                val address: String = addresses!![0].getAddressLine(0) ?: ""
                customerCity = addresses!![0].locality ?: ""
                val state: String = addresses!![0].adminArea ?: ""
                val zip: String = addresses!![0].postalCode ?: ""
                val country: String = addresses!![0].countryName ?: ""
            }
        }
    }

    private fun showShop() {
        if (addresses.isNullOrEmpty() && customerCity.isEmpty()) {
            Toast.makeText(
                context,
                "Can't Find Address! Please try again!",
                Toast.LENGTH_SHORT
            )
                .show()
            return
        }
        shopArrayList.clear()
        mainFragment.indexActivity.showLoadingDialog()

        Handler(Looper.getMainLooper()).postDelayed({
            mainFragment.indexActivity.dismissLoadingDialog()
            firebaseDatabaseReference.child("shop")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (postSnapshot in dataSnapshot.children) {
                                val shop = postSnapshot.getValue(Shop::class.java)

                                if (shop!=null) {
                                    val shopCity = getCity(shop.businessAddress!!) //if null -> n/a
                                    Log.d("SHOP_CITY ${shop.uid}", shopCity)
                                    Log.d("CUSTOMER_CITY", customerCity)
                                    if (shopCity==customerCity) {
                                        shopArrayList.add(shop)
                                    }
                                }
                            }

                            Log.d("SHOP", "shopArrayList.size -> ${shopArrayList.size}")
                            if (shopArrayList.size > 0) {
                                Log.d("SHOP", shopArrayList[0].toString())
                                showProceedDialog()
                            } else {
                                showRequestDialog()
                            }

                        } else {
                            Log.d("SHOP_ON_DATA_CHANGE", "NO SHOP YET AVAILABLE IN RECORD")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Getting Post failed, log a message
                        Log.w("addValueEventListener", "loadPost:onCancelled", error.toException())
                        Toast.makeText(
                            context,
                            "loadPost:onCancelled > ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }, 3000) // 3000 is the delayed time in milliseconds.
    }

    private fun showRequestDialog() {

        val dialogShopAvailabilityBinding =
            DialogShopAvailabilityBinding.inflate(this.layoutInflater)
        dialogShopAvailabilityBinding.btnRequest.setOnClickListener {
            firebaseDatabaseReference.child("request_address")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var address =
                            "${addresses!![0].countryName}_${addresses!![0].adminArea}_${addresses!![0].locality}"
                        if (!snapshot.hasChild(address)) {
                            val databaseRef = firebaseDatabase.reference.child("request_address")
                                .child(address)

                            address = addresses!![0].getAddressLine(0)

                            databaseRef.setValue(address).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    promptDialog.dismiss()
                                    showMessageDialog("REQUEST SENT!", "We will look for a laundry shop \nin your area soon. \nThank you!")
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Can't send request > ${task.exception}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    promptDialog.dismiss()
                                }
                            }

                        } else {
                            promptDialog.dismiss()
                            showMessageDialog(
                                "MESSAGE", "You're area has been already requested \n" +
                                        "and we're working on it right now. \n" +
                                        "Thank you!"
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("ERROR_SENDING_REQUEST", error.message)
                    }
                })
        }
        promptBuilder = AlertDialog.Builder(context)
        promptBuilder.setView(dialogShopAvailabilityBinding.root)
        promptDialog = promptBuilder.create()
        if (promptDialog.window!=null) {
            promptDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        promptDialog.show()
    }

    private fun showMessageDialog(title: String, message: String) {
        var promptDialog = Dialog(mainFragment.indexActivity)
        val promptBuilder = AlertDialog.Builder(context)
        promptBuilder.setTitle(title)
        promptBuilder.setMessage(message)
        promptBuilder.setNegativeButton("Okay") { _, _ ->
            promptDialog.dismiss()
        }
        promptDialog = promptBuilder.create()
        if (promptDialog.window!=null) {
            promptDialog.window!!.setBackgroundDrawableResource(R.color.color_light_3)
        }
        promptDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showProceedDialog() {

        val dialogShopAvailabilityBinding =
            DialogShopAvailabilityBinding.inflate(this.layoutInflater)
        dialogShopAvailabilityBinding.apply {
            heading.text = "AVAILABLE"
            subHeading.text = "We have available Laundry Shop partner in your area!"
            btnRequest.text = "BOOK NOW!"
        }
        dialogShopAvailabilityBinding.btnRequest.setOnClickListener {
            mainFragment.indexActivity.showLogin()
            promptDialog.dismiss()
        }
        promptBuilder = AlertDialog.Builder(context)
        promptBuilder.setView(dialogShopAvailabilityBinding.root)
        promptBuilder.create()
        promptDialog = promptBuilder.create()
        if (promptDialog.window!=null) {
            promptDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        promptDialog.show()
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

}