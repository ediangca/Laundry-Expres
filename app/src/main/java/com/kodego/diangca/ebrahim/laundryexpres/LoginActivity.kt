package com.kodego.diangca.ebrahim.laundryexpres

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer.DashboardCustomerActivity
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.partner.DashboardPartnerActivity
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.rider.DashboardRiderActivity
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityLoginBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogForgotPasswordBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogLoadingBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogUserTypeBinding
import com.kodego.diangca.ebrahim.laundryexpres.registration.RegisterCustomerActivity
import com.kodego.diangca.ebrahim.laundryexpres.registration.partner.RegisterPartnerActivity
import com.kodego.diangca.ebrahim.laundryexpres.registration.rider.RegisterRiderActivity


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dialogUserTypeBinding: DialogUserTypeBinding
    private lateinit var dialogForgotPasswordBinding: DialogForgotPasswordBinding

    private lateinit var client: GoogleSignInClient

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()


    private lateinit var credential: AuthCredential

    private var userType = "UNKNOWN"
    private lateinit var gmail: String

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var loadingBuilder: AlertDialog.Builder
    private lateinit var loadingDialog: Dialog

    private lateinit var userBuilder: AlertDialog.Builder
    private lateinit var userDialogInterface: DialogInterface


    private lateinit var dialogForgotPassword: Dialog


    companion object {
        private const val TAG = "GoogleActivity"

        //9001
        private const val RC_SIGN_IN = 9001

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    private fun initComponent() {

        firebaseAuth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestIdToken(BuildConfig.WEB_CLIENT_ID)
            .requestEmail()
            .build()


        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.btnGoogle.setOnClickListener {
            signIn()
        }

        binding.btnHome.setOnClickListener {
            btnHomeOnClickListener()
        }

        binding.btnForgotPass.setOnClickListener {
            btnForgotPassOnClickListener()
        }
        binding.btnSubmit.setOnClickListener {
            btnSubmitOnClickListener()
        }

        binding.btnRegister.setOnClickListener {
            btnRegisterOnClickListener()
        }


    }

    private fun btnForgotPassOnClickListener() {

        dialogForgotPasswordBinding = DialogForgotPasswordBinding.inflate(this.layoutInflater)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogForgotPasswordBinding.root)
        builder.setCancelable(false)
        dialogForgotPassword = builder.create()
        with(dialogForgotPasswordBinding) {
            btnReset.setOnClickListener {
                checkEmail(email)
            }
            btnCancel.setOnClickListener {
                dialogForgotPassword.dismiss()
            }

        }
        /*if(dialogForgotPassword.window != null){
            dialogForgotPassword.window!!.setBackgroundDrawable(ColorDrawable(0))
        }*/
        dialogForgotPassword.show()
    }

    private fun checkEmail(input: TextInputEditText) {
        val email: String = input.text.toString()
        if (email.isEmpty()) {
            input.error = "Please enter an email."
            Toast.makeText(this, "Please enter an email.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input.error = "Please enter a valid email."
            Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_SHORT).show()
            return
        }
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                dialogForgotPassword.dismiss()
//                Toast.makeText(this, "Please check your email. Find it to your spam if not in your inbox.", Toast.LENGTH_SHORT).show()
                val builder = AlertDialog.Builder(this)
                builder.setCancelable(false)
                builder.setTitle("RESET PASSWORD SENT!")
                builder.setMessage("Please check your email. \nFind it to your spam if not seen in your inbox.\n Thank you!")

                builder.setNegativeButton(android.R.string.yes) { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    // ...

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode==RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
//                Toast.makeText(this, "GOOGLE_SIGN_IN_SUCCESS ${account.id}",Toast.LENGTH_SHORT).show()
                Log.d("onActivityResult", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "ApiException ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("onActivityResult", "Google sign in failed", e)
            }
        }
    }

    private fun btnHomeOnClickListener() {
        var loginIntent = Intent(this, IndexActivity::class.java)
        startActivity(Intent(loginIntent))
        finish()
    }

    private fun btnSubmitOnClickListener() {

        val username = binding.username.text.toString()
        val password = binding.password.text.toString()

        binding.passwordLayout.isPasswordVisibilityToggleEnabled = true

        if (username.isEmpty() || password.isEmpty()) {
            if (username.isEmpty()) {
                binding.username.error = "Please enter username/email."
            }
            if (password.isEmpty()) {
                binding.passwordLayout.isPasswordVisibilityToggleEnabled = false
                binding.password.error = "Please enter a password."
            }
            if (!isValidEmail(username)) {
                binding.username.error = "Please enter an email or a valid email."
            }
            Toast.makeText(this, "Please check following error(s)!", Toast.LENGTH_SHORT).show()
            return
        } else {
            firebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener {
                Log.d("SIGN_IN_WITH_EMAIL_PASSWORD", it.toString())
                if (it.isSuccessful) {
                    checkUserAccount()
                } else {
                    Toast.makeText(
                        this,
                        "Either your username or password is Invalid! Please try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

    }

    private fun btnRegisterOnClickListener() {
        userBuilder = AlertDialog.Builder(this)
        userBuilder.setCancelable(true)
        userBuilder.setView(getUserTypeView("Register"))
        userBuilder.create()
        this.userDialogInterface = userBuilder.show()
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setGmail(gmail: String) {
        this.gmail = gmail
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { _ ->
                // Sign in success
                Log.d(TAG, "firebaseAuthWithGoogle: LoggedIN")
                // Sign in success, update UI with the signed-in user's information
                val user = firebaseAuth.currentUser
                if (user!=null) {
                    val uid = user.uid
                    val email = user.email

//                    if(authResult.additionalUserInfo!!.isNewUser){} //Check if LoggedIn User is new
                    setGmail(email!!)
                    /*Toast.makeText(
                        this,
                        "GOOGLE_SIGN_IN_SUCCESS: ${user.displayName}",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    Log.d(TAG, "GOOGLE_SIGN_IN_SUCCESS: ${user.displayName}")

                    checkUserAccount()
                }

            }
            .addOnFailureListener { authResult ->

                // If sign in fails, display a message to the user.
                Toast.makeText(
                    this,
                    "GOOGLE_SIGN_IN_FAIL ${authResult.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG, "firebaseAuthWithGoogle:failure ${authResult.message}")

            }
    }

    private fun checkUserAccount() {
        showLoadingDialog()
        Toast.makeText(this, "Checking Account...", Toast.LENGTH_SHORT).show()
        firebaseDatabaseReference.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                        this@LoginActivity.userType =
                            snapshot.child(firebaseAuth.currentUser!!.uid).child("type")
                                .getValue(String::class.java).toString()
                        val isVerified: Boolean =
                            snapshot.child(firebaseAuth.currentUser!!.uid).child("verified")
                                .getValue(Boolean::class.java)!!
                        checkIfVerified(isVerified)
                    } else {
                        dismissLoadingDialog()
                        btnRegisterOnClickListener()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@LoginActivity,
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /* private fun checkFirebaseDatabaseRecord() {
         firebaseDatabaseReference.child("users").addListenerForSingleValueEvent(object :
             ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                     Log.d("ForSingleValueEvent", firebaseAuth.currentUser!!.uid)
                     this@LoginActivity.userType =
                         snapshot.child(firebaseAuth.currentUser!!.uid).child("type")
                             .getValue(String::class.java).toString()
                     val isVerified: Boolean = snapshot.child(firebaseAuth.currentUser!!.uid).child("verified")
                         .getValue(Boolean::class.java)!!
                     checkIfVerified(isVerified)


                 } else {
                     showProgressBar(false)
                     *//*Log.d(
                        "ACCOUNT",
                        "Either your username or password is Invalid! Please try again!"
                    )
                    Toast.makeText(
                        this@LoginActivity,
                        "Either your username or password is Invalid! Please try again!",
                        Toast.LENGTH_SHORT
                    ).show()*//*
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showProgressBar(false)
                Toast.makeText(this@LoginActivity, "${error.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }*/

    private fun checkIfVerified(isVerified: Boolean) {
        Handler(Looper.getMainLooper()).postDelayed({
            dismissLoadingDialog()
            if((userType!="Customer") && !isVerified) {
                Log.d(
                    "ACCOUNT",
                    "Unverified Account! Please wait for your verification. We will notify you within 24-72hours."
                )
                var verifyDialog: Dialog = Dialog(this)
                val builder = AlertDialog.Builder(this)
                builder.setCancelable(false)
                builder.setTitle("UNVERIFIED ACCOUNT")
                builder.setMessage("Please wait for your verification. We will notify you within 24-72hours. \nThank you!")
                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                    firebaseAuth.signOut()
                    binding.username.text = null
                    binding.password.text = null
                    verifyDialog.dismiss()
                }
                verifyDialog = builder.create()
                if(verifyDialog.window != null){
                    verifyDialog.window!!.setBackgroundDrawableResource(R.color.color_light_3)
                }
                verifyDialog.show()
            }else {
                goToDashboard()
            }
        }, 3000) // 3000 is the delayed time in milliseconds.
    }


    private fun getUserTypeView(title: String): View {
        dialogUserTypeBinding = DialogUserTypeBinding.inflate(this.layoutInflater)

        with(dialogUserTypeBinding) {
            userDisplayName.text = title
            userPartner.setOnClickListener {
                userType = "Partner"
                showRegistrationActivity()
            }
            userRider.setOnClickListener {
                userType = "Rider"
                showRegistrationActivity()
            }
            userCustomer.setOnClickListener {
                userType = "Customer"
                showRegistrationActivity()
            }
        }
        return dialogUserTypeBinding.root
    }

    private fun showRegistrationActivity() {
        when (userType) {
            "Customer" -> {
                startActivity((Intent(this, RegisterCustomerActivity::class.java)))
                finish()
            }
            "Partner" -> {
                startActivity((Intent(this, RegisterPartnerActivity::class.java)))
                finish()
            }
            "Rider" -> {
                startActivity((Intent(this, RegisterRiderActivity::class.java)))
                finish()
            }
            else -> {

            }
        }

    }

    private fun goToDashboard() {
        when (userType) {
            "Customer" -> {
                startActivity((Intent(this, DashboardCustomerActivity::class.java)))
                finish()
            }
            "Partner" -> {
                startActivity((Intent(this, DashboardPartnerActivity::class.java)))
                finish()
            }
            "Rider" -> {
                startActivity((Intent(this, DashboardRiderActivity::class.java)))
                finish()
            }
            else -> {

            }
        }
    }

    private fun showLoadingDialog() {
        val loadingBinding = DialogLoadingBinding.inflate(this.layoutInflater)
        loadingBuilder = AlertDialog.Builder(this)
        loadingBuilder.setCancelable(false)
        loadingBuilder.setView(loadingBinding.root)
        loadingBuilder.create()
        loadingDialog = loadingBuilder.create()
        if (loadingDialog.window!=null) {
            loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        loadingDialog.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog.dismiss()
    }

}