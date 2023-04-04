package com.kodego.diangca.ebrahim.laundryexpres.login

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kodego.diangca.ebrahim.laundryexpres.MainFragment
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer.DashboardCustomerFragment
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogLoadingBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentRegisterUserBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import java.util.regex.Pattern

class RegisterUserFragment(var mainFragment: MainFragment) : Fragment() {

    private var _binding: FragmentRegisterUserBinding? = null
    private val binding get() = _binding!!

    private lateinit var dialogLoadingBinding: DialogLoadingBinding

    private var userType: String = "UNKNOWN"

    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun setUserType(userType: String) {
        this.userType = userType
    }

    fun getDatabaseReference(): DatabaseReference {
        return firebaseDatabase.getReference("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterUserBinding.inflate(layoutInflater, container, false)
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
        binding.btnBack.setOnClickListener {
            btnBackOnClickListener()
        }
        binding.btnSubmit.setOnClickListener {
            btnSubmitOnClickListener()
        }
    }

    private fun btnBackOnClickListener() {
        mainFragment.indexActivity.mainFrame =
            mainFragment.indexActivity.supportFragmentManager.beginTransaction()
        mainFragment.indexActivity.mainFrame.replace(R.id.mainFrame, LoginFragment(mainFragment));
        mainFragment.indexActivity.mainFrame.addToBackStack(null);
        mainFragment.indexActivity.mainFrame.commit();
    }

    private fun btnSubmitOnClickListener() {
        val mobileNo = binding.mobileNo.text.toString()
        val firstName = binding.firstName.text.toString()
        val lastName = binding.lastName.text.toString()
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()

        binding.passwordLayout.isPasswordVisibilityToggleEnabled = true
        binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = true

        if (mobileNo.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            if (mobileNo.isEmpty()) {
                binding.mobileNo.error = "Please enter your Mobile No."
            }
            if (firstName.isEmpty()) {
                binding.firstName.error = "Please enter your Firstname."
            }
            if (lastName.isEmpty()) {
                binding.lastName.error = "Please enter your Lastname."
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
            Toast.makeText(context, "Please check empty fields!", Toast.LENGTH_SHORT).show()
            return
        } else if (mobileNo.length!=13) {
            Toast.makeText(context, "Please check mobile no.!", Toast.LENGTH_SHORT).show()
            return
        } else if (password.length < 6) {
            binding.password.error = "Password must be more than 6 characters."
            binding.passwordLayout.isPasswordVisibilityToggleEnabled = false
            Toast.makeText(
                context,
                "Please enter password more than 6 characters!",
                Toast.LENGTH_SHORT
            ).show()
        } else if (password!=confirmPassword) {
            binding.confirmPassword.error = "Unmatched password and confirm password."
            binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
            Toast.makeText(
                context,
                "Password not matched to confirm password. Please try again!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            showProgressBar(true)
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    firebaseDatabaseReference.child("users")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                                    Toast.makeText(
                                        context,
                                        "User is already Registered!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val databaseRef = firebaseDatabase.reference.child("users")
                                        .child(firebaseAuth.currentUser!!.uid)


                                    val user = User(
                                        firebaseAuth.currentUser!!.uid,
                                        firstName,
                                        lastName,
                                        email,
                                        userType,
                                        password,
                                        mobileNo,
                                        false,
                                        null
                                    )

/*
                        val uid = firebaseAuth.currentUser!!.uid
                        firebaseDatabaseReference.child("users").child(uid).child("type").setValue("customer")
                        firebaseDatabaseReference.child("users").child(uid).child("firstname").setValue(firstName)
                        firebaseDatabaseReference.child("users").child(uid).child("lastname").setValue(lastName)
                        firebaseDatabaseReference.child("users").child(uid).child("email").setValue(email)
                        firebaseDatabaseReference.child("users").child(uid).child("password").setValue(password)
                        firebaseDatabaseReference.child("users").child(uid).child("phone").setValue(mobileNo)
                        firebaseDatabaseReference.child("users").child(uid).child("isVerified").setValue(false)
                        firebaseDatabaseReference.child("users").child(uid).child("profile").setValue(null)
*/

                                    databaseRef.setValue(user).addOnCompleteListener {
                                        if (it.isSuccessful) {

                                            Toast.makeText(
                                                context,
                                                "User has been successfully Registered!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            clearField()

                                            when (userType) {
                                                "Customer" -> {
                                                    mainFragment.indexActivity.mainFrame = mainFragment.indexActivity.supportFragmentManager.beginTransaction()
                                                    mainFragment.indexActivity.mainFrame.replace(R.id.mainFrame,DashboardCustomerFragment(mainFragment.indexActivity))
                                                    mainFragment.indexActivity.mainFrame.commit()

                                                    showProgressBar(false)
                                                }
                                                "Partner" -> {


                                                }
                                                "Rider" -> {


                                                }
                                            }
                                        }
                                    }

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("ListenerForSingleValueEvent", "${it.exception!!.message}")
                            }

                        })
                } else {
                    Toast.makeText(
                        mainFragment.indexActivity,
                        it.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


        }

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

    private fun loadingDialog(): Dialog {
        return activity?.let {
            dialogLoadingBinding = DialogLoadingBinding.inflate(this.layoutInflater)

            with(dialogLoadingBinding) {
            }

            val builder = AlertDialog.Builder(it)
            with(builder) {
                setView(dialogLoadingBinding.root)
                builder.create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}