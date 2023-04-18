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
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentRiderBasicInfoBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import java.util.*

class RiderBasicInfoFragment(private var registerRiderActivity: RegisterRiderActivity) : Fragment() {

    private var bindingRider: FragmentRiderBasicInfoBinding? = null
    val binding get() = bindingRider!!

//    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var userType: String = "Rider"
    private val permissionId = 2
    private  var longitude: Double = 0.0
    private  var latitude: Double = 0.0
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        bindingRider = FragmentRiderBasicInfoBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }


    @SuppressLint("SetTextI18n")
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


    @Suppress("DEPRECATION")
    private fun registerUser() {
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
        val password = binding.password.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()

        binding.passwordLayout.isPasswordVisibilityToggleEnabled = true
        binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = true

        var trap = false

        if (mobileNo.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || street.isEmpty() || city.isEmpty() || state.isEmpty() || zipCode.isEmpty() || country.isEmpty() || email.isEmpty()) {
            if (mobileNo.isEmpty()) {
                binding.mobileNo.error = "Please enter your Mobile No."
            }
            if (mobileNo.length!=13) {
                binding.mobileNo.error = "Please check length of Mobile No."
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
            Toast.makeText(registerRiderActivity, "Please check empty fields!", Toast.LENGTH_SHORT).show()
            return
        }
        if (firebaseAuth.currentUser==null) {
            if (password.isEmpty()) {
                binding.password.error = "Please enter your password."
                binding.passwordLayout.isPasswordVisibilityToggleEnabled = false
                trap =  true
            }
            if (confirmPassword.isEmpty()) {
                binding.confirmPassword.error = "Please enter your password."
                binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
                trap =  true
            }
            if (password.length < 6) {
                binding.password.error = "Password must be more than 6 characters."
                binding.passwordLayout.isPasswordVisibilityToggleEnabled = false
                trap =  true
            }
            if (password!=confirmPassword) {
                binding.confirmPassword.error = "Password not match."
                binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
                trap =  true
            }
            if(trap){
                Toast.makeText(registerRiderActivity, "Please check error field(s)!", Toast.LENGTH_SHORT).show()
                return
            }
            registerRiderActivity.showProgressBar(true)
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    firebaseDatabaseReference.child("users")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                                    Toast.makeText(
                                        registerRiderActivity,
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
                                                registerRiderActivity,
                                                "User has been successfully Registered!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            registerRiderActivity.showProgressBar(false)
                                            registerRiderActivity.goToDashboard()
                                        }else{
                                            registerRiderActivity.showProgressBar(false)
                                            Toast.makeText(registerRiderActivity, "${task.exception!!.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("ListenerForSingleValueEvent", "${it.exception!!.message}")
                            }

                        })
                } else {
                    registerRiderActivity.showProgressBar(false)
                    Snackbar.make(binding.root, "User email already existing", Snackbar.LENGTH_SHORT).show()
                    Log.d("ListenerForSingleValueEvent", "${it.exception!!.message}")
                }
            }

        } else {
            firebaseDatabaseReference.child("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                            Toast.makeText(
                                registerRiderActivity,
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
                                        registerRiderActivity,
                                        "User has been successfully Registered!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    registerRiderActivity.showProgressBar(false)
                                    registerRiderActivity.goToDashboard()
                                }else{
                                    registerRiderActivity.showProgressBar(false)
                                    Toast.makeText(registerRiderActivity, "${task.exception!!.message}", Toast.LENGTH_SHORT).show()
                                }
                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        registerRiderActivity.showProgressBar(false)
                        Toast.makeText(registerRiderActivity, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
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
/*
    @Deprecated("Deprecated in Java")
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
    }*/

    @Suppress("DEPRECATION")
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
                            longitude = list[0].longitude
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