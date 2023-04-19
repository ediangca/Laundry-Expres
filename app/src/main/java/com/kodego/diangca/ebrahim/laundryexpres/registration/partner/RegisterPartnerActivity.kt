package com.kodego.diangca.ebrahim.laundryexpres.registration.partner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kodego.diangca.ebrahim.laundryexpres.LoginActivity
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.adater.FragmentAdapter
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityRegisterPartnerBinding
import java.util.regex.Pattern

class RegisterPartnerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterPartnerBinding

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")

    var fragmentAdapter = FragmentAdapter(supportFragmentManager, lifecycle)
    private var partnerBasicInfoFragment = PartnerBasicInfoFragment(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPartnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    private fun initComponent() {
        fragmentAdapter = FragmentAdapter(supportFragmentManager, lifecycle)
        fragmentAdapter.addFragment(partnerBasicInfoFragment) //0
        fragmentAdapter.addFragment(PartnerBusinessInfoFragment(this)) //1
        with(binding.viewPager2) {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = fragmentAdapter

            TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            }.attach()

        }

        with(binding.tabLayout) {
            getTabAt(0)!!.setIcon(R.drawable.vector_account).text = "PERSONAL"
            getTabAt(1)!!.setIcon(R.drawable.vector_laundry).text = "BUSINESS"
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

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    fun checkBasicInfoFields(): Boolean {
        val bindingBasicInfo = partnerBasicInfoFragment.binding

        val mobileNo = bindingBasicInfo.mobileNo.text.toString()
        val firstName = bindingBasicInfo.firstName.text.toString()
        val lastName = bindingBasicInfo.lastName.text.toString()
        val street = bindingBasicInfo.address.text.toString()
        val city = bindingBasicInfo.city.text.toString()
        val state = bindingBasicInfo.state.text.toString()
        val zipCode = bindingBasicInfo.zipCode.text.toString()
        val country = bindingBasicInfo.country.text.toString()
        val sex = bindingBasicInfo.sex.getItemAtPosition(bindingBasicInfo.sex.selectedItemPosition).toString()
        val email = bindingBasicInfo.email.text.toString()
        val password = bindingBasicInfo.password.text.toString()
        val confirmPassword = bindingBasicInfo.confirmPassword.text.toString()

        bindingBasicInfo.passwordLayout.isPasswordVisibilityToggleEnabled = true
        bindingBasicInfo.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = true

        var trap = false
        val currentItem = binding.viewPager2.currentItem

        if(currentItem == 1){ //Trap for ViewPager 1 (Basic Info)

            if (mobileNo.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || street.isEmpty() || city.isEmpty() || state.isEmpty() || zipCode.isEmpty() || country.isEmpty() || email.isEmpty()) {
                if (mobileNo.isEmpty()) {
                    bindingBasicInfo.mobileNo.error = "Please enter your Mobile No."
                }
                if (mobileNo.length!=13) {
                    bindingBasicInfo.mobileNo.error = "Please check length of Mobile No."
                }
                if (firstName.isEmpty()) {
                    bindingBasicInfo.firstName.error = "Please enter your Firstname."
                }
                if (lastName.isEmpty()) {
                    bindingBasicInfo.lastName.error = "Please enter your Lastname."
                }
                if (street.isEmpty()) {
                    bindingBasicInfo.address.error = "Please enter your Street."
                }
                if (city.isEmpty()) {
                    bindingBasicInfo.address.error = "Please enter your City."
                }
                if (state.isEmpty()) {
                    bindingBasicInfo.address.error = "Please enter your State."
                }
                if (zipCode.isEmpty()) {
                    bindingBasicInfo.address.error = "Please enter your Zip Code."
                }
                if (country.isEmpty()) {
                    bindingBasicInfo.address.error = "Please enter your Country."
                }
                if (email.isEmpty() || email.isEmailValid()) {
                    bindingBasicInfo.email.error = "Please enter an email or a valid email."
                }
                trap = true
            }

            if (firebaseAuth.currentUser==null) {
                if (password.isEmpty()) {
                    bindingBasicInfo.password.error = "Please enter your password."
                    bindingBasicInfo.passwordLayout.isPasswordVisibilityToggleEnabled = false
                    trap = true
                }
                if (confirmPassword.isEmpty()) {
                    bindingBasicInfo.confirmPassword.error = "Please enter your password."
                    bindingBasicInfo.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
                    trap = true
                }
                if (password.length < 6) {
                    bindingBasicInfo.password.error = "Password must be more than 6 characters."
                    bindingBasicInfo.passwordLayout.isPasswordVisibilityToggleEnabled = false
                    trap = true
                }
                if (password!=confirmPassword) {
                    bindingBasicInfo.confirmPassword.error = "Password not match."
                    bindingBasicInfo.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
                    trap = true
                }
            }

        }else {

        }
        return trap
    }

    fun saveInfoToFirebase() {

    }

}