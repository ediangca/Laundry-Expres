package com.kodego.diangca.ebrahim.laundryexpres.registration.partner

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
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
import com.google.firebase.database.*
import com.kodego.diangca.ebrahim.laundryexpres.LoginActivity
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.adater.FragmentAdapter
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityRegisterPartnerBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.BusinessInfo
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import java.util.regex.Pattern

class RegisterPartnerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterPartnerBinding

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")

    var fragmentAdapter = FragmentAdapter(supportFragmentManager, lifecycle)
    private lateinit var partnerBasicInfoFragment: PartnerBasicInfoFragment
    private lateinit var partnerBusinessInfoFragment: PartnerBusinessInfoFragment


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

    var businessName: String? = null
    var businessLegalName: String? = null
    var businessEmail: String? = null
    var businessPhone: String? = null

    var businessHoursMondayFrom: String? = null
    var businessHoursMondayTo: String? = null
    var businessHoursTuesdayFrom: String? = null
    var businessHoursTuesdayTo: String? = null
    var businessHoursWednesdayFrom: String? = null
    var businessHoursWednesdayTo: String? = null
    var businessHoursThursdayFrom: String? = null
    var businessHoursThursdayTo: String? = null
    var businessHoursFridayFrom: String? = null
    var businessHoursFridayTo: String? = null
    var businessHoursSaturdayFrom: String? = null
    var businessHoursSaturdayTo: String? = null
    var businessHoursSundayFrom: String? = null
    var businessHoursSundayTo: String? = null
    var businessHoursHolidayFrom: String? = null
    var businessHoursHolidayTo: String? = null

    var businessBankName: String? = null
    var businessBankAccountName: String? = null
    var businessBankAccNo: String? = null
    var businessBankBIC: String? = null
    var businessBankProofImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPartnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    private fun initComponent() {
        partnerBasicInfoFragment = PartnerBasicInfoFragment(this)
        partnerBusinessInfoFragment = PartnerBusinessInfoFragment(this)

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

    @SuppressLint("SuspiciousIndentation")
    fun checkFields(): Boolean {

        var validate1 = false
        var validate2 = false

        if (binding.viewPager2.currentItem==1) {
            val bindingBasicInfo = partnerBasicInfoFragment.getBinding()

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

            bindingBasicInfo.passwordLayout.isPasswordVisibilityToggleEnabled = true
            bindingBasicInfo.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = true

            if (email!!.isEmpty() || mobileNo!!.isEmpty() || firstName!!.isEmpty() || lastName!!.isEmpty() || (bindingBasicInfo.sex.selectedItemPosition==0) || street!!.isEmpty() || city!!.isEmpty() || state!!.isEmpty() || zipCode!!.isEmpty() || country!!.isEmpty()) {
                if (mobileNo!!.isEmpty()) {
                    bindingBasicInfo.mobileNo.error = "Please enter your Mobile No."
                }
                if (mobileNo!!.length!=13) {
                    bindingBasicInfo.mobileNo.error = "Please check length of Mobile No."
                }
                if (firstName!!.isEmpty()) {
                    bindingBasicInfo.firstName.error = "Please enter your Firstname."
                }
                if (lastName!!.isEmpty()) {
                    bindingBasicInfo.lastName.error = "Please enter your Lastname."
                }
                if (bindingBasicInfo.sex.selectedItemPosition==0) {
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
                if (email!!.isEmpty() || email!!.isEmailValid()) {
                    bindingBasicInfo.email.error = "Please enter an email or a valid email."
                }
                validate1 = true
            }

            if (firebaseAuth.currentUser==null) {
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
                if (password!=confirmPassword) {
                    bindingBasicInfo.confirmPassword.error = "Password not match."
                    bindingBasicInfo.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
                    validate1 = true
                }
            }

        } else if (binding.viewPager2.currentItem==1) {
            val bindingBusinessInfo = partnerBusinessInfoFragment.binding

            businessName = bindingBusinessInfo.businessName.text.toString()
            businessLegalName = bindingBusinessInfo.businessLegalName.text.toString()
            businessEmail = bindingBusinessInfo.businessEmail.text.toString()
            businessPhone = bindingBusinessInfo.businessPhone.text.toString()

            businessHoursMondayFrom = bindingBusinessInfo.fromMonday.text.toString()
            businessHoursMondayTo = bindingBusinessInfo.toMonday.text.toString()
            businessHoursTuesdayFrom = bindingBusinessInfo.fromTuesday.text.toString()
            businessHoursTuesdayTo = bindingBusinessInfo.toTuesday.text.toString()
            businessHoursWednesdayFrom = bindingBusinessInfo.fromWednesday.text.toString()
            businessHoursWednesdayTo = bindingBusinessInfo.toWednesday.text.toString()
            businessHoursThursdayFrom = bindingBusinessInfo.fromThursday.text.toString()
            businessHoursThursdayTo = bindingBusinessInfo.toThursday.text.toString()
            businessHoursFridayFrom = bindingBusinessInfo.fromFriday.text.toString()
            businessHoursFridayTo = bindingBusinessInfo.toFriday.text.toString()
            businessHoursSaturdayFrom = bindingBusinessInfo.fromSaturday.text.toString()
            businessHoursSaturdayTo = bindingBusinessInfo.toSaturday.text.toString()
            businessHoursSundayFrom = bindingBusinessInfo.fromSunday.text.toString()
            businessHoursSundayTo = bindingBusinessInfo.toSunday.text.toString()
            businessHoursHolidayFrom = bindingBusinessInfo.fromHoliday.text.toString()
            businessHoursHolidayTo = bindingBusinessInfo.toHoliday.text.toString()

            businessBankName = bindingBusinessInfo.bankName.text.toString()
            businessBankAccountName = bindingBusinessInfo.bankAccNameHolder.text.toString()
            businessBankAccNo = bindingBusinessInfo.bankAccNo.text.toString()
            businessBankBIC = bindingBusinessInfo.bankBIC.text.toString()

            val businessSignatureUri = "E-Signature URI"

            if (businessName!!.isEmpty() || businessLegalName!!.isEmpty() || businessEmail!!.isEmpty() || businessPhone!!.isEmpty() ||
                businessBankName!!.isEmpty() || businessBankAccountName!!.isEmpty() || businessBankAccNo!!.isEmpty() || businessBankBIC!!.isEmpty()
            ) {
                if (businessName!!.isEmpty()) {
                    bindingBusinessInfo.businessName.error = "Please enter your Business Name."
                }
                if (businessLegalName!!.isEmpty()) {
                    bindingBusinessInfo.businessLegalName.error =
                        "Please enter your Business Legal Name."
                }
                if (businessEmail!!.isEmpty()) {
                    bindingBusinessInfo.businessEmail.error = "Please enter your Business Email."
                }
                if (businessPhone!!.isEmpty()) {
                    bindingBusinessInfo.businessPhone.error = "Please enter your Business Phone."
                }

                if (businessBankName!!.isEmpty()) {
                    bindingBusinessInfo.bankName.error = "Please enter your Bank Name."
                }
                if (businessBankAccountName!!.isEmpty()) {
                    bindingBusinessInfo.bankAccNameHolder.error =
                        "Please enter your Bank Account Name."
                }
                if (businessBankBIC!!.isEmpty()) {
                    bindingBusinessInfo.bankBIC.error = "Please enter your Bank Account BIC."
                }
                validate2 = true
            }
            if (businessHoursMondayFrom!!.isNotEmpty()) {
                if (businessHoursMondayTo!!.isEmpty()) {
                    bindingBusinessInfo.toMonday.error = "Please enter Monday to Time."
                }
            } else if (businessHoursMondayTo!!.isNotEmpty()) {
                if (businessHoursMondayFrom!!.isEmpty()) {
                    bindingBusinessInfo.fromMonday.error = "Please enter Monday from Time."
                }
            }

            if (businessHoursTuesdayFrom!!.isNotEmpty()) {
                if (businessHoursTuesdayTo!!.isEmpty()) {
                    bindingBusinessInfo.toTuesday.error = "Please enter Tuesday to Time."
                }
            } else if (businessHoursTuesdayTo!!.isNotEmpty()) {
                if (businessHoursTuesdayFrom!!.isEmpty()) {
                    bindingBusinessInfo.fromTuesday.error = "Please enter Tuesday from Time."
                }
            }

            if (businessHoursWednesdayFrom!!.isNotEmpty()) {
                if (businessHoursWednesdayTo!!.isEmpty()) {
                    bindingBusinessInfo.toWednesday.error = "Please enter Wednesday to Time."
                }
            } else if (businessHoursWednesdayTo!!.isNotEmpty()) {
                if (businessHoursWednesdayFrom!!.isEmpty()) {
                    bindingBusinessInfo.fromWednesday.error = "Please enter Wednesday from Time."
                }
            }

            if (businessHoursThursdayFrom!!.isNotEmpty()) {
                if (businessHoursThursdayTo!!.isEmpty()) {
                    bindingBusinessInfo.toThursday.error = "Please enter Thursday to Time."
                }
            } else if (businessHoursThursdayTo!!.isNotEmpty()) {
                if (businessHoursThursdayFrom!!.isEmpty()) {
                    bindingBusinessInfo.fromThursday.error = "Please enter Thursday from Time."
                }
            }

            if (businessHoursFridayFrom!!.isNotEmpty()) {
                if (businessHoursFridayTo!!.isEmpty()) {
                    bindingBusinessInfo.toFriday.error = "Please enter Friday to Time."
                }
            } else if (businessHoursFridayTo!!.isNotEmpty()) {
                if (businessHoursFridayFrom!!.isEmpty()) {
                    bindingBusinessInfo.fromFriday.error = "Please enter Friday from Time."
                }
            }

            if (businessHoursSaturdayFrom!!.isNotEmpty()) {
                if (businessHoursSaturdayTo!!.isEmpty()) {
                    bindingBusinessInfo.toSaturday.error = "Please enter Saturday to Time."
                }
            } else if (businessHoursSaturdayTo!!.isNotEmpty()) {
                if (businessHoursSaturdayFrom!!.isEmpty()) {
                    bindingBusinessInfo.fromSaturday.error = "Please enter Saturday from Time."
                }
            }

            if (businessHoursSundayFrom!!.isNotEmpty()) {
                if (businessHoursSundayTo!!.isEmpty()) {
                    bindingBusinessInfo.toSunday.error = "Please enter Sunday to Time."
                }
            } else if (businessHoursSundayTo!!.isNotEmpty()) {
                if (businessHoursSundayFrom!!.isEmpty()) {
                    bindingBusinessInfo.fromSunday.error = "Please enter Sunday from Time."
                }
            }

            if (businessHoursHolidayFrom!!.isNotEmpty()) {
                if (businessHoursHolidayTo!!.isEmpty()) {
                    bindingBusinessInfo.toHoliday.error = "Please enter Holiday to Time."
                }
            } else if (businessHoursHolidayTo!!.isNotEmpty()) {
                if (businessHoursHolidayFrom!!.isEmpty()) {
                    bindingBusinessInfo.fromHoliday.error = "Please enter Holiday from Time."
                }
            }
        }


        if (validate1) {
            setTab(0)
        }

        return (validate1 or validate2)
    }

    fun showProgressBar(visible: Boolean) {
        if (visible) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun setTab(currentItem: Int) {
        binding.viewPager2.post {
            binding.viewPager2.setCurrentItem(currentItem, true)
        }
    }

    fun saveInfoToFirebase() {
        if (firebaseAuth.currentUser==null) {
            showProgressBar(true)
            firebaseAuth.createUserWithEmailAndPassword(email!!, password!!).addOnCompleteListener {
                if (it.isSuccessful) {
                    firebaseDatabaseReference.child("users")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                                    Toast.makeText(
                                        this@RegisterPartnerActivity,
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
                        "User email already existing",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    Log.d("USER -> ListenerForSingleValueEvent", "${it.exception!!.message}")

                }
            }
        }

    }

    private fun saveUserInfo() {
        val databaseRef = firebaseDatabase.reference.child("users")
            .child(firebaseAuth.currentUser!!.uid)

        val user = User(
            firebaseAuth.currentUser!!.uid,
            email,
            "Partner",
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
        //user.printLOG()

        databaseRef.setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("USER", "User has been successfully Registered!")
                /*Toast.makeText(
                    this@RegisterPartnerActivity,
                    "User has been successfully Registered!",
                    Toast.LENGTH_SHORT
                ).show()*/
                saveBusinessInfo()
            } else {
                Log.d("USER -> addOnCompleteListener", task.exception!!.message!!)
            }

        }
    }

    private fun saveBusinessInfo() {
        val databaseRef = firebaseDatabase.reference.child("business")
            .child(firebaseAuth.currentUser!!.uid)

        val businessInfo = BusinessInfo(
            firebaseAuth.currentUser!!.uid,
            businessName,
            businessLegalName,
            businessEmail,
            businessPhone,
            partnerBusinessInfoFragment.businessImageBytes,
            businessHoursMondayFrom,
            businessHoursMondayTo ,
            businessHoursTuesdayFrom ?: "Closed",
            businessHoursTuesdayTo ?: "Closed",
            businessHoursWednesdayFrom ?: "Closed",
            businessHoursWednesdayTo ?: "Closed",
            businessHoursThursdayFrom ?: "Closed",
            businessHoursThursdayTo ?: "Closed",
            businessHoursFridayFrom ?: "Closed",
            businessHoursFridayTo ?: "Closed",
            businessHoursSaturdayFrom ?: "Closed",
            businessHoursSaturdayTo ?: "Closed",
            businessHoursSundayFrom ?: "Closed",
            businessHoursSundayTo ?: "Closed",
            businessHoursHolidayFrom ?: "Closed",
            businessHoursHolidayTo ?: "Closed",
            businessBankName,
            businessBankAccountName,
            businessBankAccNo,
            businessBankBIC,
            partnerBusinessInfoFragment.bankImageBytes,
        )
        //businessInfo.printLOG()

        databaseRef.setValue(businessInfo).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showProgressBar(false)
                Log.d("BUSINESS", "User has been successfully Registered!")
                val builder = AlertDialog.Builder(this)
                builder.setCancelable(false)
                builder.setTitle("ACKNOWLEDGEMENT RECEIPT")
                builder.setMessage("This is to acknowledge receipt of your accomplished Application. \n" +
                        "You will receive a call from Laundry Express regarding Fees/ Charges, etc. \n" +
                        "We will also send you a notification once your application is verified or if there is anything else you need to submit. \n Thank you!")

                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                    firebaseAuth.signOut()
                    btnBackOnClickListener()
                }
                builder.show()

            } else {
                Log.d("BUSINESS -> addOnCompleteListener", task.exception!!.message!!)
            }

        }
    }

}