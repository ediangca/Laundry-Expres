package com.kodego.diangca.ebrahim.laundryexpres.registration.rider

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.LoginActivity
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.adater.FragmentAdapter
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.rider.DashboardRiderActivity
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityRegisterRiderBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentRiderBasicInfoBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentRiderRequirementsBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Requirements
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop
import com.kodego.diangca.ebrahim.laundryexpres.model.User

class RegisterRiderActivity : AppCompatActivity() {


    private lateinit var binding: ActivityRegisterRiderBinding

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")

    var fragmentAdapter = FragmentAdapter(supportFragmentManager, lifecycle)

    private lateinit var riderBasicInfoFragment: RiderBasicInfoFragment
    public lateinit var riderRequirementsInfoFragment: RiderRequirementsFragment

    var mobileNo: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var street: String? = null
    var city: String? = null
    var state: String? = null
    var zipCode: String? = null
    var country: String? = null
    var sex: String? = null
    var email: String? = null
    var password: String? = null
    var confirmPassword: String? = null


    var ioePerson: String? = null
    var ioeRelationship: String? = null
    var ioePhone: String? = null
    var tinNo: String? = null
    var selfieImage: String? = null
    var nbiImage: String? = null
    var licenseImage: String? = null
    var orImage: String? = null
    var crImage: String? = null
    var vehicleImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterRiderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    private fun initComponent() {

        riderBasicInfoFragment = RiderBasicInfoFragment(this)
        riderRequirementsInfoFragment = RiderRequirementsFragment(this)

        fragmentAdapter = FragmentAdapter(supportFragmentManager, lifecycle)
        fragmentAdapter.addFragment(riderBasicInfoFragment) //0
        fragmentAdapter.addFragment(riderRequirementsInfoFragment) //1

        with(binding.viewPager2) {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = fragmentAdapter

            TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            }.attach()

        }

        with(binding.tabLayout) {
            getTabAt(0)!!.setIcon(R.drawable.vector_account).text = "PERSONAL"
            getTabAt(1)!!.setIcon(R.drawable.vector_courier).text = "OTHERS AND VEHICLE"
        }

        binding.viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                Log.e("Position", position.toString())
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("On Page Selected", position.toString())
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                Log.e("On Page Scroll", state.toString())
            }
        })

        binding.btnBack.setOnClickListener {
            btnBackOnClickListener()
        }
    }

    private fun btnBackOnClickListener() {
        startActivity(Intent(Intent(this, LoginActivity::class.java)))
        finish()
    }

    fun nextTab() {
        val currentItem = binding.viewPager2.currentItem
        binding.viewPager2.post {
            binding.viewPager2.setCurrentItem(currentItem + 1, true)
        }
    }

    fun goToDashboard() {
        startActivity(Intent(Intent(this, DashboardRiderActivity::class.java)))
        finish()
    }

    fun showProgressBar(visible: Boolean) {
        if (visible) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun validEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidAddress(address: String): Boolean {
        var addresses: List<Address>? = null
        var locality: String? = null
        val trap = false
        if (address.isNotEmpty()) {
            var geocoder = Geocoder(binding.root.context)
            try {
                addresses = geocoder.getFromLocationName(address, 1)
            } catch (e: Exception) {
                Log.d("SEARCH_GEO_LOCATION", "${e.message}")
            }

            if (addresses != null && addresses.isNotEmpty()) {
                locality = addresses[0].locality
                Log.d("SEARCH_GEO_LOCATION > $address", addresses[0].getAddressLine(0))
            } else {
                Log.d("CITY AVAILABILITY", "NO AVAILABLE FROM SELECTED > $address")
            }
        }
        return addresses!!.isEmpty()
    }

    @SuppressLint("SuspiciousIndentation")
    fun checkFields(): Boolean {

        var validate1 = false
        var validate2 = false

        if (binding.viewPager2.currentItem == 1) {
            val bindingBasicInfo = riderBasicInfoFragment.binding
            setDataBasicInfo(bindingBasicInfo)

            bindingBasicInfo.passwordLayout.isPasswordVisibilityToggleEnabled = true
            bindingBasicInfo.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = true

            if (email!!.isEmpty() || !validEmail(email!!) || mobileNo!!.isEmpty() || mobileNo!!.length != 13 || firstName!!.isEmpty() || lastName!!.isEmpty() || (bindingBasicInfo.sex.selectedItemPosition == 0) || street!!.isEmpty() || city!!.isEmpty() || state!!.isEmpty() || zipCode!!.isEmpty() || country!!.isEmpty()
                || isValidAddress(street!!)
                || isValidAddress(city!!)
                || isValidAddress(state!!)
                || isValidAddress(country!!)
            ) {

                if (email!!.isEmpty() || !validEmail(email!!)) {
                    bindingBasicInfo.email.error = "Please enter valid email or a valid email."
                }
                if (mobileNo!!.isEmpty()) {
                    bindingBasicInfo.mobileNo.error = "Please enter your Mobile No."
                }
                if (mobileNo!!.length != 13) {
                    bindingBasicInfo.mobileNo.error = "Please check length of Mobile No."
                }
                if (firstName!!.isEmpty()) {
                    bindingBasicInfo.firstName.error = "Please enter your Firstname."
                }
                if (lastName!!.isEmpty()) {
                    bindingBasicInfo.lastName.error = "Please enter your Lastname."
                }
                if (bindingBasicInfo.sex.selectedItemPosition == 0) {
                    (bindingBasicInfo.sex.selectedView as TextView).error =
                        "Please select your sex."
                }
                if (street!!.isEmpty()) {
                    bindingBasicInfo.address.error = "Please enter your Street."
                }
                if (city!!.isEmpty()) {
                    bindingBasicInfo.city.error = "Please enter your City."
                }
                if (state!!.isEmpty()) {
                    bindingBasicInfo.state.error = "Please enter your State."
                }
                if (zipCode!!.isEmpty()) {
                    bindingBasicInfo.zipCode.error = "Please enter your Zip Code."
                }
                if (country!!.isEmpty()) {
                    bindingBasicInfo.country.error = "Please enter your Country."
                }
                validate1 = true
            }

            if (firebaseAuth.currentUser == null) {
                if (password!!.isEmpty()) {
                    bindingBasicInfo.password.error = "Please enter your password."
                    bindingBasicInfo.passwordLayout.isPasswordVisibilityToggleEnabled = false
                    validate1 = true
                }
                if (confirmPassword!!.isEmpty()) {
                    bindingBasicInfo.confirmPassword.error = "Please enter your password."
                    bindingBasicInfo.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
                    validate1 = true
                }
                if (password!!.length < 6) {
                    bindingBasicInfo.password.error = "Password must be more than 6 characters."
                    bindingBasicInfo.passwordLayout.isPasswordVisibilityToggleEnabled = false
                    validate1 = true
                }
                if (password != confirmPassword) {
                    bindingBasicInfo.confirmPassword.error = "Password not match."
                    bindingBasicInfo.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
                    validate1 = true
                }
            }

        } else if (binding.viewPager2.currentItem == 1) {
            val bindingRequirementsInfo = riderRequirementsInfoFragment.getBinding()
            setDataBusinessInfo(bindingRequirementsInfo)
            val businessSignatureUri = "E-Signature URI"

            if (ioePerson!!.isEmpty() || ioeRelationship!!.isEmpty() || ioePhone!!.isEmpty() || tinNo!!.isEmpty() ||
                selfieImage!!.isEmpty() || nbiImage!!.isEmpty() || licenseImage!!.isEmpty() || orImage!!.isEmpty() ||
                crImage!!.isEmpty() || vehicleImage!!.isEmpty()
            ) {
                if (ioePerson!!.isEmpty()) {
                    bindingRequirementsInfo.emergencyPhone.error =
                        "Please enter the Person Name in case of Emergency."
                }
                if (ioeRelationship!!.isEmpty()) {
                    bindingRequirementsInfo.emergencyRelationship.error =
                        "Please enter the relationship to Person in case of Emergency."
                }
                if (ioePhone!!.isEmpty()) {
                    bindingRequirementsInfo.emergencyPhone.error =
                        "Please enter the contact No. of Person in case of Emergency."
                }
                if (tinNo!!.isEmpty()) {
                    bindingRequirementsInfo.tinNumber.error =
                        "Please enter the TIN No."
                }
                if (selfieImage!!.isEmpty()) {
                    bindingRequirementsInfo.selfieImageUri.error =
                        "Please capture a Selfie."
                }
                if (nbiImage!!.isEmpty()) {
                    bindingRequirementsInfo.nbiImageUri.error =
                        "Please capture or upload your NBI or Police clearance."
                }
                if (licenseImage!!.isEmpty()) {
                    bindingRequirementsInfo.licenseImageUri.error =
                        "Please capture or upload your license."
                }
                if (orImage!!.isEmpty()) {
                    bindingRequirementsInfo.orImageUri.error =
                        "Please capture or upload the OR of your vehicle."
                }
                if (crImage!!.isEmpty()) {
                    bindingRequirementsInfo.crImageUri.error =
                        "Please capture or upload the CR of your vehicle."
                }
                if (vehicleImage!!.isEmpty()) {
                    bindingRequirementsInfo.vehicleImageUri.error =
                        "Please capture or upload a picture of your vehicle."
                }
            }

            validate2 = true
        }
        if (validate1) {
            setTab(0)
        }

        return (validate1 or validate2)
    }

    private fun setTab(currentItem: Int) {
        binding.viewPager2.post {
            binding.viewPager2.setCurrentItem(currentItem, true)
        }
    }

    private fun setDataBasicInfo(bindingBasicInfo: FragmentRiderBasicInfoBinding) {
        mobileNo = bindingBasicInfo.mobileNo.text.toString()
        firstName = bindingBasicInfo.firstName.text.toString()
        lastName = bindingBasicInfo.lastName.text.toString()
        street = bindingBasicInfo.address.text.toString()
        city = bindingBasicInfo.city.text.toString()
        state = bindingBasicInfo.state.text.toString()
        zipCode = bindingBasicInfo.zipCode.text.toString()
        country = bindingBasicInfo.country.text.toString()
        sex =
            bindingBasicInfo.sex.getItemAtPosition(bindingBasicInfo.sex.selectedItemPosition)
                .toString()
        email = bindingBasicInfo.email.text.toString()
        password = bindingBasicInfo.password.text.toString()
        confirmPassword = bindingBasicInfo.confirmPassword.text.toString()
    }

    private fun setDataBusinessInfo(bindingRequirementsInfo: FragmentRiderRequirementsBinding) {
        ioePerson = bindingRequirementsInfo.emergencyName.text.toString()
        ioeRelationship = bindingRequirementsInfo.emergencyRelationship.text.toString()
        ioePhone = bindingRequirementsInfo.emergencyPhone.text.toString()
        tinNo = bindingRequirementsInfo.tinNumber.text.toString()
        selfieImage = bindingRequirementsInfo.selfieImageUri.toString()
        nbiImage = bindingRequirementsInfo.nbiImageUri.toString()
        licenseImage = bindingRequirementsInfo.licenseImageUri.toString()
        orImage = bindingRequirementsInfo.orImageUri.toString()
        crImage = bindingRequirementsInfo.crImageUri.toString()
        vehicleImage = bindingRequirementsInfo.vehicleImageUri.toString()
    }


    fun saveInfoToFirebase() {

        if (firebaseAuth.currentUser == null) {

            showProgressBar(true)
            firebaseAuth.createUserWithEmailAndPassword(email!!, password!!).addOnCompleteListener {
                if (it.isSuccessful) {
                    firebaseDatabaseReference.child("users")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                                    Toast.makeText(
                                        this@RegisterRiderActivity,
                                        "User is already Registered!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    saveUserInfo()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("ListenerForSingleValueEvent", "${it.exception!!.message}")
                            }

                        })
                } else {
                    showProgressBar(false)
                    Snackbar.make(
                        binding.root,
                        it.exception!!.message!!,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    Log.d("USER -> ListenerForSingleValueEvent", "${it.exception!!.message}")

                }
            }
        }

    }

    private fun saveUserInfo() {

        val bindingBasicInfo = riderBasicInfoFragment.binding
        setDataBasicInfo(bindingBasicInfo)

        val user = User(
            firebaseAuth.currentUser!!.uid,
            email,
            "Rider",
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


        val databaseRef = firebaseDatabase.reference.child("users")
            .child(firebaseAuth.currentUser!!.uid)
        databaseRef.setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("USER", "User has been successfully Registered!")
                /*Toast.makeText(
                    this@RegisterPartnerActivity,
                    "User has been successfully Registered!",
                    Toast.LENGTH_SHORT
                ).show()*/
                saveRiderInfo()
            } else {
                Log.d("USER -> addOnCompleteListener", task.exception!!.message!!)
            }

        }
    }

    private fun saveRiderInfo() {

        val bindingBusinessInfo = riderRequirementsInfoFragment.getBinding()
        setDataBusinessInfo(bindingBusinessInfo)

        val requirements = Requirements(
            firebaseAuth.currentUser!!.uid,
            ioePerson,
            ioeRelationship,
            ioePhone,
            tinNo,
            riderRequirementsInfoFragment.selfieImageUri.toString(),
            riderRequirementsInfoFragment.nbiImageUri.toString(),
            riderRequirementsInfoFragment.licenseImageUri.toString(),
            riderRequirementsInfoFragment.orImageUri.toString(),
            riderRequirementsInfoFragment.crImageUri.toString(),
            riderRequirementsInfoFragment.vehicleImageUri.toString(),
        )

        val imageMappings = mapOf(
            "profile" to requirements.selfieImage,
            "nbi" to requirements.nbiImage,
            "license" to requirements.licenseImage,
            "or" to requirements.orImage,
            "cr" to requirements.crImage,
            "vehicle" to requirements.vehicleImage
        )

        val databaseRef = firebaseDatabase.reference.child("requirements")
            .child(firebaseAuth.currentUser!!.uid)

// First, save requirements data to Firebase Database
        databaseRef.setValue(requirements).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Keep track of completed uploads
                var successCount = 0
                var totalImages = imageMappings.size

                imageMappings.forEach { (type, imageUri) ->
                    val filename = "$type.jpg"
                    val parsedUri = Uri.parse(imageUri)
                    val firebaseStorageReference = FirebaseStorage.getInstance()
                        .getReference("requirements/${firebaseAuth.currentUser!!.uid}/$filename")

                    // Upload each file
                    firebaseStorageReference.putFile(parsedUri)
                        .addOnSuccessListener {
                            successCount++
                            Log.d("SAVING_USER_$type", "SUCCESS SAVING $type $filename")

                            // If all uploads are done
                            if (successCount == totalImages) {
                                showProgressBar(false)
                                showAcknowledgmentDialog()
                            }
                        }
                        .addOnFailureListener {
                            Log.d(
                                "FAILED_SAVING_USER_$type",
                                "FAILED SAVING $type $filename > ${it.message}"
                            )
                        }
                }

            } else {
                Log.d("REQUIREMENTS -> addOnCompleteListener", task.exception!!.message!!)
            }
        }
    }

    fun showAcknowledgmentDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("ACKNOWLEDGEMENT RECEIPT")
        builder.setMessage(
            "This is to acknowledge receipt of your accomplished Application.\n" +
                    "You will receive a call from Laundry Express regarding orientation, rules & regulations, etc.\n" +
                    "We will notify you once your application is verified or if more documents are needed.\n" +
                    "Thank you!"
        )
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            firebaseAuth.signOut()
            btnBackOnClickListener()
        }
        builder.show()
    }
}