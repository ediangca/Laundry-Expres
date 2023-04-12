package com.kodego.diangca.ebrahim.laundryexpres.registration

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.LoginActivity
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer.DashboardCustomerActivity
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityRegisterPersonalInfoBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogLoadingBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import java.util.*
import java.util.regex.Pattern

class RegisterPersonalInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterPersonalInfoBinding

    private lateinit var dialogLoadingBinding: DialogLoadingBinding

    private var userType: String = "UNKNOWN"

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val permissionId = 2

    private var longtitude: Double = 0.0
    private var latitude: Double = 0.0

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    private fun initComponent() {

        binding.email.setText(intent.getStringExtra("email").toString())
        userType = intent.getStringExtra("userType")!!

        Log.d("REGISTER_PERSONAL_INFO", "Register $userType -> Personal Info")

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnBack.setOnClickListener {
            btnBackOnClickListener()
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

    private fun btnBackOnClickListener() {
        startActivity(Intent(Intent(this, LoginActivity::class.java)))
        finish()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )==PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )==PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
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
        grantResults: IntArray,
    ) {
        if (requestCode==permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location!=null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
                        binding.apply {
                            currentLocation.text = list[0].toString()
                            latitude = list[0].latitude
                            longtitude = list[0].longitude
                            address.setText(list[0].getAddressLine(0) ?: "n/a")
                            city.setText(list[0].locality ?: "n/a")
                            state.setText(list[0].adminArea ?: "n/a")
                            zipCode.setText(list[0].postalCode ?: "n/a")
                            country.setText(list[0].countryName ?: "n/a")
                        }
                    } else {
                        Toast.makeText(this, "Not Found Location", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermissions()
        }
    }

    private fun btnSubmitOnClickListener() {
        val mobileNo = binding.mobileNo.text.toString()
        val firstName = binding.firstName.text.toString()
        val lastName = binding.lastName.text.toString()
        val street = binding.address.text.toString()
        val city = binding.city.text.toString()
        val state = binding.state.text.toString()
        val zipCode = binding.zipCode.text.toString()
        val country = binding.country.text.toString()
        val sex = binding.sex.getItemAtPosition(binding.sex.selectedItemPosition).toString()
        val email = binding.email.text.toString()

        if (mobileNo.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || street.isEmpty() || city.isEmpty() || state.isEmpty() || zipCode.isEmpty() || country.isEmpty() || email.isEmpty()) {
            if (mobileNo.isEmpty()) {
                binding.mobileNo.error = "Please enter your Mobile No."
            }
            if (firstName.isEmpty()) {
                binding.firstName.error = "Please enter your Firstname."
            }
            if (lastName.isEmpty()) {
                binding.lastName.error = "Please enter your Lastname."
            }
            if (street.isEmpty()) {
                binding.address.error = "Please enter your Street."
            }
            if (city.isEmpty()) {
                binding.address.error = "Please enter your City."
            }
            if (state.isEmpty()) {
                binding.address.error = "Please enter your State."
            }
            if (zipCode.isEmpty()) {
                binding.address.error = "Please enter your Zip Code."
            }
            if (country.isEmpty()) {
                binding.address.error = "Please enter your Country."
            }
            if (email.isEmpty() || !isValidEmail(email)) {
                binding.email.error = "Please enter an email or a valid email."
            }
            Toast.makeText(this, "Please check empty fields!", Toast.LENGTH_SHORT).show()
            return
        } else if (mobileNo.length!=13) {
            Toast.makeText(this, "Please check mobile no.!", Toast.LENGTH_SHORT).show()
            return
        } else {
            showProgressBar(true)

            firebaseDatabaseReference.child("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                            Toast.makeText(
                                this@RegisterPersonalInfoActivity,
                                "User is already Registered!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val databaseRef = firebaseDatabase.reference.child("users")
                                .child(firebaseAuth.currentUser!!.uid)


                            val user = User(
                                firebaseAuth.currentUser!!.uid,
                                email,
                                userType,
                                firstName,
                                lastName,
                                sex,
                                street,
                                city,
                                state,
                                zipCode,
                                country,
                                mobileNo,
                                null,
                                false,
                            )
                            user.printLOG()


                            databaseRef.setValue(user).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@RegisterPersonalInfoActivity,
                                        "User has been successfully Registered!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    clearField()
                                    goToDashboard()
                                }
                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("addListenerForSingleValueEvent -> onCancelled", error.message)
                    }

                })

        }
    }

    private fun goToDashboard() {

        showProgressBar(false)
        when (userType) {
            "Customer" -> {
                startActivity(Intent(Intent(this, DashboardCustomerActivity::class.java)))
                finish()
            }
            "Partner" -> {
                startActivity(Intent(Intent(this, DashboardCustomerActivity::class.java)))
                finish()

            }
            "Rider" -> {
                startActivity(Intent(Intent(this, DashboardCustomerActivity::class.java)))
                finish()
            }
            else -> {
                showProgressBar(false)
            }
        }
    }

    private fun showProgressBar(visible: Boolean) {
        if (visible) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    fun String.isEmailValid() =
        Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        ).matcher(this).matches()

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun clearField() {
        binding.mobileNo.text = null
        binding.firstName.text = null
        binding.lastName.text = null
        binding.email.text = null
    }


}