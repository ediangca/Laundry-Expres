package com.kodego.diangca.ebrahim.laundryexpres.registration.partner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.TextView
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
    private var partnerBusinessInfoFragment = PartnerBusinessInfoFragment(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPartnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    private fun initComponent() {
        fragmentAdapter = FragmentAdapter(supportFragmentManager, lifecycle)
        fragmentAdapter.addFragment(partnerBasicInfoFragment) //0
        fragmentAdapter.addFragment(partnerBusinessInfoFragment) //1
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
        setTab(currentItem + 1)
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

    fun checkFields(): Boolean {
        val bindingBasicInfo = partnerBasicInfoFragment.binding
        val bindingBusinessInfo = partnerBusinessInfoFragment.binding

        val mobileNo = bindingBasicInfo.mobileNo.text.toString()
        val firstName = bindingBasicInfo.firstName.text.toString()
        val lastName = bindingBasicInfo.lastName.text.toString()
        val street = bindingBasicInfo.address.text.toString()
        val city = bindingBasicInfo.city.text.toString()
        val state = bindingBasicInfo.state.text.toString()
        val zipCode = bindingBasicInfo.zipCode.text.toString()
        val country = bindingBasicInfo.country.text.toString()
        val sex = bindingBasicInfo.sex.getItemAtPosition(bindingBasicInfo.sex.selectedItemPosition)
            .toString()
        val email = bindingBasicInfo.email.text.toString()
        val password = bindingBasicInfo.password.text.toString()
        val confirmPassword = bindingBasicInfo.confirmPassword.text.toString()

        bindingBasicInfo.passwordLayout.isPasswordVisibilityToggleEnabled = true
        bindingBasicInfo.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = true

        var validate1 = false
        var validate2 = false

            if (email.isEmpty() || mobileNo.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || bindingBasicInfo.sex.selectedItemPosition==0 || street.isEmpty() || city.isEmpty() || state.isEmpty() || zipCode.isEmpty() || country.isEmpty() ) {
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
                if (bindingBasicInfo.sex.selectedItemPosition==0) {
                    (bindingBasicInfo.sex.selectedView as TextView).error =
                        "Please select your sex."
                }
                if (street.isEmpty()) {
                    bindingBasicInfo.address.error = "Please enter your Street."
                }
                if (city.isEmpty()) {
                    bindingBasicInfo.city.error = "Please enter your City."
                }
                if (state.isEmpty()) {
                    bindingBasicInfo.state.error = "Please enter your State."
                }
                if (zipCode.isEmpty()) {
                    bindingBasicInfo.zipCode.error = "Please enter your Zip Code."
                }
                if (country.isEmpty()) {
                    bindingBasicInfo.country.error = "Please enter your Country."
                }
                if (email.isEmpty() || email.isEmailValid()) {
                    bindingBasicInfo.email.error = "Please enter an email or a valid email."
                }
                validate1 = true
            }

            if (firebaseAuth.currentUser==null) {
                if (password.isEmpty()) {
                    bindingBasicInfo.password.error = "Please enter your password."
                    bindingBasicInfo.passwordLayout.isPasswordVisibilityToggleEnabled = false
                    validate1 = true
                }
                if (confirmPassword.isEmpty()) {
                    bindingBasicInfo.confirmPassword.error = "Please enter your password."
                    bindingBasicInfo.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
                    validate1 = true
                }
                if (password.length < 6) {
                    bindingBasicInfo.password.error = "Password must be more than 6 characters."
                    bindingBasicInfo.passwordLayout.isPasswordVisibilityToggleEnabled = false
                    validate1 = true
                }
                if (password!=confirmPassword) {
                    bindingBasicInfo.confirmPassword.error = "Password not match."
                    bindingBasicInfo.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
                    validate1 = true
                }
            }


            val businessName = bindingBusinessInfo.businessName.text.toString()
            val businessLegalName = bindingBusinessInfo.businessLegalName.text.toString()
            val businessEmail = bindingBusinessInfo.businessEmail.text.toString()
            val businessPhone = bindingBusinessInfo.businessPhone.text.toString()

            val businessHoursMondayFrom = bindingBusinessInfo.fromMonday.text.toString()
            val businessHoursMondayTo = bindingBusinessInfo.toMonday.text.toString()
            val businessHoursTuesdayFrom = bindingBusinessInfo.fromTuesday.text.toString()
            val businessHoursTuesdayTo = bindingBusinessInfo.toTuesday.text.toString()
            val businessHoursWednesdayFrom = bindingBusinessInfo.fromWednesday.text.toString()
            val businessHoursWednesdayTo = bindingBusinessInfo.toWednesday.text.toString()
            val businessHoursThursdayFrom = bindingBusinessInfo.fromThursday.text.toString()
            val businessHoursThursdayTo = bindingBusinessInfo.toThursday.text.toString()
            val businessHoursFridayFrom = bindingBusinessInfo.fromFriday.text.toString()
            val businessHoursFridayTo = bindingBusinessInfo.toFriday.text.toString()
            val businessHoursSaturdayFrom = bindingBusinessInfo.fromSaturday.text.toString()
            val businessHoursSaturdayTo = bindingBusinessInfo.toSaturday.text.toString()
            val businessHoursSundayFrom = bindingBusinessInfo.fromSunday.text.toString()
            val businessHoursSundayTo = bindingBusinessInfo.toSunday.text.toString()
            val businessHoursHolidayFrom = bindingBusinessInfo.fromHoliday.text.toString()
            val businessHoursHolidayTo = bindingBusinessInfo.toHoliday.text.toString()

            val businessBankName = bindingBusinessInfo.bankName.text.toString()
            val businessBankAccountName = bindingBusinessInfo.bankAccNameHolder.text.toString()
            val businessBankAccNo = bindingBusinessInfo.bankAccNo.text.toString()
            val businessBankBIC = bindingBusinessInfo.bankBIC.text.toString()

            val businessSignatureUri = "E-Signature URI"

            if (businessName.isEmpty()) {

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

    fun saveInfoToFirebase() {

    }

}