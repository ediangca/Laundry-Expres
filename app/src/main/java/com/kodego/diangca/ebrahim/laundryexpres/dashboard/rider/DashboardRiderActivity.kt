package com.kodego.diangca.ebrahim.laundryexpres.dashboard.rider

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kodego.diangca.ebrahim.laundryexpres.LoginActivity
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer.DashboardOrderDetailsFragment
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityDashboardRiderBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogLoadingBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardRiderAccountBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardRiderInboxBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardRiderNotificationBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Order
import com.kodego.diangca.ebrahim.laundryexpres.model.Requirements
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import java.util.Locale

class DashboardRiderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardRiderBinding

    private lateinit var mainFrame: FragmentTransaction

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")


    private lateinit var dashboardHomeFragment: DashboardRiderHomeFragment
    private lateinit var dashboardRidesFragment: DashboardRiderRideFragment
    private lateinit var dashboardNotificationFragment: DashboardRiderNotificationFragment
    private lateinit var dashboardInboxFragment: DashboardRiderInboxFragment
    private lateinit var dashboardAccountFragment: DashboardRiderAccountFragment

    private lateinit var dashboardOrderDetailsFragment: DashboardOrderDetailsFragment

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var user: User? = null
    private var requirements: Requirements? = null

    private var pickUpDatetime: String? = null
    private var deliveryDatetime: String? = null

    private var bundle = Bundle()

    private lateinit var loadingBuilder: AlertDialog.Builder
    lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardRiderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }
    private fun initComponent() {
        Log.d("ON_ATTACH_DASHBOARD_RIDER", "ATTACHED RIDER DASHBOARD")
        retrieveUserDetails()

        dashboardHomeFragment = DashboardRiderHomeFragment(this)
        dashboardRidesFragment = DashboardRiderRideFragment(this)
        dashboardNotificationFragment = DashboardRiderNotificationFragment(this)
        dashboardInboxFragment = DashboardRiderInboxFragment(this)
        dashboardAccountFragment = DashboardRiderAccountFragment(this)

        binding.dashboardNav.setOnItemSelectedListener {
            navMenuOnItemSelectedListener(it)
        }
    }

    @JvmName("getRequirements1")
    fun setRequirements(requirements: Requirements) {
        this.requirements = requirements
    }

    @JvmName("getRequirements1")
    fun getRequirements(): Requirements {
        return requirements!!
    }

    fun setUser(user: User?) {
        this.user = user
    }

    fun getUser(): User? {
        return user!!
    }

    private fun retrieveUserDetails() {
        val databaseRef = firebaseDatabase.reference.child("users")
            .child(firebaseAuth.currentUser!!.uid)

        databaseRef.get().addOnCompleteListener { dataSnapshot ->
            if (dataSnapshot.isSuccessful) {
                user = dataSnapshot.result.getValue(User::class.java)
                if (user != null) {
                    retrieveRequirements()
                    Log.d("USER_DETAILS_FOUND", user.toString())
                    bundle = Bundle()
                    bundle.putParcelable("user", user)
                    dashboardHomeFragment.arguments = bundle
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(
                        R.id.fragmentRiderDashboard,
                        DashboardRiderHomeFragment(this@DashboardRiderActivity)
                    );
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    checkLocationPermission()
                }
            } else {
                Log.d("USER_DETAILS_NOT_FOUND", "USER NOT FOUND")
            }
        }
    }

    private fun retrieveRequirements(){
        val databaseRef = firebaseDatabase.reference.child("requirements")
            .child(firebaseAuth.currentUser!!.uid)

        databaseRef.get().addOnCompleteListener { dataSnapshot ->
            if (dataSnapshot.isSuccessful) {
                requirements = dataSnapshot.result.getValue(Requirements::class.java)
            } else {
                Log.d("REQUIREMENTS_DETAILS_NOT_FOUND", "REQUIREMENTS NOT FOUND")
            }
        }
    }

    private fun navMenuOnItemSelectedListener(it: MenuItem?): Boolean {
        if (it == null) {
            bundle = Bundle()
            bundle.putParcelable("user", user)
            dashboardHomeFragment.arguments = bundle
            mainFrame = supportFragmentManager.beginTransaction()
            mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardHomeFragment);
            mainFrame.addToBackStack(null);
            mainFrame.commit();
            return true
        } else {
            Log.d("MENU ITEM", "ID: ${it.itemId}  --------------")
            bundle = Bundle()
            when (it.itemId) {
                R.id.navRiderHome -> {
                    bundle.putParcelable("user", user)
                    dashboardHomeFragment.arguments = bundle
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentRiderDashboard, dashboardHomeFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }

                R.id.navRiderOrder -> {
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentRiderDashboard, dashboardRidesFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }

                R.id.navRiderUpdates -> {
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace( R.id.fragmentRiderDashboard, dashboardNotificationFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    binding.dashboardNav.visibility = View.VISIBLE
                    return true
                }

                R.id.navRiderInbox -> {
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentRiderDashboard, dashboardInboxFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }

                R.id.navRiderAccount -> {
                    bundle.putParcelable("user", user)
                    dashboardAccountFragment.arguments = bundle
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentRiderDashboard, dashboardAccountFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }

                else -> {
                    bundle.putParcelable("user", user)
                    dashboardHomeFragment.arguments = bundle
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentRiderDashboard, dashboardHomeFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }
            }
        }
    }

    fun signOut() {
        Log.d("STATUS RIDER", "${user!!.uid!!} OFFLINE")
        val riderStatusRef = firebaseDatabase.getReference("status").child(user!!.uid!!)
        riderStatusRef.child("status").setValue("offline")
        var loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(Intent(loginIntent))
        finish()
    }

    fun showLoadingDialog() {
        val loadingBinding = DialogLoadingBinding.inflate(this.layoutInflater)
        loadingBuilder = AlertDialog.Builder(this)
        loadingBuilder.setCancelable(false)
        loadingBuilder.setView(loadingBinding.root)
        loadingDialog = loadingBuilder.create()
        if (loadingDialog.window != null) {
            loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        loadingDialog.show()
    }

    fun dismissLoadingDialog() {
        loadingDialog.dismiss()
    }

    fun showOrderDetails(order: Order, callBack: String) {

        order.printLOG()
        bundle = Bundle()
        bundle.putString("user", "rider")
        bundle.putParcelable("order", order)
        dashboardOrderDetailsFragment = DashboardOrderDetailsFragment(this)
        dashboardOrderDetailsFragment.setCallBack(callBack)
        dashboardOrderDetailsFragment.arguments = bundle
        mainFrame = supportFragmentManager.beginTransaction()
        Log.d("ON_SHOW_DETAIL", "FRAGMENT ORDER")
        mainFrame.replace(R.id.fragmentRiderDashboard, dashboardOrderDetailsFragment);
        mainFrame.addToBackStack(null);
        mainFrame.commit()
    }

    fun showrRides() {
        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.fragmentRiderDashboard, dashboardRidesFragment);
        mainFrame.addToBackStack(null);
        mainFrame.commit();
    }


    private fun checkLocationPermission() {
        // Check if permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if it's not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            // Permission granted, fetch location
            getDeviceLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, call the location fetching method
                Log.d("Permissions", "Permission Granted, access location.")
                getDeviceLocation()
            } else {
                // Permission denied
                Log.e("Permissions", "Permission denied, can't access location.")
            }
        }
    }

    private fun getDeviceLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check if the permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request for permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener(this, OnSuccessListener { location: Location? ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    Log.d("Device Location", "Lat: $lat, Lng: $lng")


                    val riderStatusRef = firebaseDatabase.getReference("riders").child(user!!.uid!!)
                    riderStatusRef.child("lat").setValue(lat)
                    riderStatusRef.child("lng").setValue(lng)

                    // Use lat and lng for your locale setting
                    setLocaleBasedOnLocation(lat, lng)
                } else {
                    Log.e("Device Location", "Location not available")
                }
            })
    }
    private fun setLocaleBasedOnLocation(lat: Double, lng: Double) {
        val geocoder = Geocoder(this)
        val addresses = geocoder.getFromLocation(lat, lng, 1)

        if (addresses != null && addresses.isNotEmpty()) {
            val countryCode = addresses[0].countryCode // Get the country code
            val locale = getLocaleFromCountryCode(countryCode)
            Locale.setDefault(locale)

            // Optionally, you can refresh the UI with the new language if needed
            Log.d("Device Locale", "Locale set to: $locale")
        }
    }

    private fun getLocaleFromCountryCode(countryCode: String): Locale {
        return when (countryCode) {
            "US" -> Locale("en", "US")
            "PH" -> Locale("tl", "PH") // Filipino for Philippines
            "FR" -> Locale("fr", "FR")
            // Add other cases as needed
            else -> Locale.getDefault()
        }
    }




}