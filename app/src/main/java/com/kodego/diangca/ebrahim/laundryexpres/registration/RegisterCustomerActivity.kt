package com.kodego.diangca.ebrahim.laundryexpres.registration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.LoginActivity
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer.DashboardCustomerActivity
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityRegisterCustomerBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogLoadingBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import java.util.regex.Pattern

class RegisterCustomerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterCustomerBinding

    private lateinit var dialogLoadingBinding: DialogLoadingBinding

    private var userType: String = "Customer"

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    private fun initComponent() {
        binding.btnBack.setOnClickListener {
            btnBackOnClickListener()
        }
        binding.btnSubmit.setOnClickListener {
            btnSubmitOnClickListener()
        }
    }

    private fun btnBackOnClickListener() {
        startActivity(Intent(Intent(this, LoginActivity::class.java)))
        finish()
    }

    private fun btnSubmitOnClickListener() {
        val mobileNo = binding.mobileNo.text.toString()
        val firstName = binding.firstName.text.toString()
        val lastName = binding.lastName.text.toString()
        val address = binding.address.text.toString()
        val sex = ""
        val birthdate = ""
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()
        val datetimeCreated = ""
        val datetimeUpdated = ""

        binding.passwordLayout.isPasswordVisibilityToggleEnabled = true
        binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = true

        if (mobileNo.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || address.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            if (mobileNo.isEmpty()) {
                binding.mobileNo.error = "Please enter your Mobile No."
            }
            if (firstName.isEmpty()) {
                binding.firstName.error = "Please enter your Firstname."
            }
            if (lastName.isEmpty()) {
                binding.lastName.error = "Please enter your Lastname."
            }
            if (address.isEmpty()) {
                binding.address.error = "Please enter your Address."
            }
            if (email.isEmpty() || !isValidEmail(email)) {
                binding.email.error = "Please enter an email or a valid email."
            }
            if (password.isEmpty()) {
                binding.password.error = "Please enter your password."
                binding.passwordLayout.isPasswordVisibilityToggleEnabled = false
            }
            if (confirmPassword.isEmpty()) {
                binding.confirmPassword.error = "Please enter your password."
                binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
            }
            Toast.makeText(this, "Please check empty fields!", Toast.LENGTH_SHORT).show()
            return
        } else if (mobileNo.length!=13) {
            Toast.makeText(this, "Please check mobile no.!", Toast.LENGTH_SHORT).show()
            return
        } else if (password.length < 6) {
            binding.password.error = "Password must be more than 6 characters."
            binding.passwordLayout.isPasswordVisibilityToggleEnabled = false
            Toast.makeText(this,"Please enter password more than 6 characters!",Toast.LENGTH_SHORT).show()
        } else if (password!=confirmPassword) {
            binding.confirmPassword.error = "Unmatched password and confirm password."
            binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
            Toast.makeText(this,"Password not matched to confirm password. Please try again!",Toast.LENGTH_SHORT).show()
        } else {
            showProgressBar(true)
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    firebaseDatabaseReference.child("users")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                                    Toast.makeText(this@RegisterCustomerActivity,"User is already Registered!",Toast.LENGTH_SHORT).show()
                                } else {
                                    val databaseRef = firebaseDatabase.reference.child("users")
                                        .child(firebaseAuth.currentUser!!.uid)


                                    val user = User(
                                        firebaseAuth.currentUser!!.uid,
                                        email,
                                        userType,
                                        password,
                                        firstName,
                                        lastName,
                                        sex,
                                        birthdate,
                                        address,
                                        mobileNo,
                                        null,
                                        false,
                                        datetimeCreated,
                                        datetimeUpdated
                                    )

                                    databaseRef.setValue(user).addOnCompleteListener {task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(this@RegisterCustomerActivity,"User has been successfully Registered!",Toast.LENGTH_SHORT).show()
                                            clearField()
                                            goToDashboard()
                                        }
                                    }

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("ListenerForSingleValueEvent", "${it.exception!!.message}")
                            }

                        })
                } else {
                    Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun goToDashboard() {
        showProgressBar(false)
        startActivity(Intent(Intent(this, DashboardCustomerActivity::class.java)))
        finish()
    }

    private fun showProgressBar(visible: Boolean) {
        if(visible){
            binding.progressBar.visibility = View.VISIBLE
        }else{
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
        binding.password.text = null
    }


}