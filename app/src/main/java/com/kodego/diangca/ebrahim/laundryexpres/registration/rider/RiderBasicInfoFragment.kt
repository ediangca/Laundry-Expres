package com.kodego.diangca.ebrahim.laundryexpres.registration.rider

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentRiderBasicInfoBinding
import java.util.*

class RiderBasicInfoFragment(var registerRiderActivity: RegisterRiderActivity) : Fragment() {

    var _binding: FragmentRiderBasicInfoBinding? = null
    val binding get() = _binding!!

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val permissionId = 2
    private  var longtitude: Double = 0.0
    private  var latitude: Double = 0.0
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRiderBasicInfoBinding.inflate(layoutInflater, container, false)
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

        if(firebaseAuth.currentUser!=null){
            binding.passwordLayout.visibility = View.GONE
            binding.confirmPasswordLayout.visibility = View.GONE

            binding.email.setText(firebaseAuth.currentUser!!.email)
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(registerRiderActivity)

        if(firebaseAuth.currentUser == null) {
            binding.btnSubmit.text = "Next"
        }else{
            binding.btnSubmit.text = "Submit"
        }
        binding.btnSubmit.setOnClickListener {
            btnSubmitOnClickListener()
        }
        binding.btnLocation.setOnClickListener {
            btnLocationOnClickListener()
        }

    }
    private fun btnLocationOnClickListener() {
        getLocation()
    }

    private fun btnSubmitOnClickListener() {
        if(firebaseAuth.currentUser == null && binding.btnSubmit.text.toString().equals("Next", true)) {
            registerRiderActivity.nextTab()
        }else{
            registerUser()
        }
    }


    private fun registerUser() {

    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            registerRiderActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                registerRiderActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                registerRiderActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            registerRiderActivity,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(registerRiderActivity) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
                        binding.apply {
                            currentLocation.text = list[0].toString()
                            latitude = list[0].latitude
                            longtitude = list[0].longitude
                            address.setText(list[0].getAddressLine(0)?:"n/a")
                            city.setText(list[0].locality?:"n/a")
                            state.setText(list[0].adminArea?:"n/a")
                            zipCode.setText(list[0].postalCode?:"n/a")
                            country.setText(list[0].countryName?:"n/a")
                        }
                    }else{
                        Toast.makeText(registerRiderActivity, "Not Found Location", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(registerRiderActivity, "Please turn on location", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermissions()
        }
    }
}