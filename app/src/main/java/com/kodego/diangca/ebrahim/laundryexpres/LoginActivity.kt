package com.kodego.diangca.ebrahim.laundryexpres

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogUserTypeBinding
import com.kodego.diangca.ebrahim.laundryexpres.registration.RegisterCustomerActivity
import com.kodego.diangca.ebrahim.laundryexpres.registration.partner.RegisterPartnerActivity
import com.kodego.diangca.ebrahim.laundryexpres.registration.rider.RegisterRiderActivity

class LoginActivity: AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var customDialogBinding: DialogUserTypeBinding

    private lateinit var client: GoogleSignInClient

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()


    private lateinit var credential: AuthCredential

    private var userType = "UNKNOWN"
    private lateinit var gmail:String

//    implementation 'com.google.firebase:firebase-database:20.1.0'

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var userBuilder: AlertDialog.Builder
    private lateinit var userDialogInterface: DialogInterface

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.btnGoogle.setOnClickListener {
            signIn()
        }

        binding.btnHome.setOnClickListener {
            btnHomeOnClickListener()
        }

        binding.btnSubmit.setOnClickListener {
            btnSubmitOnClickListener()
        }

        binding.btnRegister.setOnClickListener {
            btnRegisterOnClickListener()
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
                showProgressBar(true)
                Log.d("SIGN_IN_WITH_EMAIL_PASSWORD", it.toString())
                if (it.isSuccessful) {
                    checkFirebaseDatabaseRecord()
                } else {
                    Toast.makeText(
                        this,
                        "Either your username or password is Invalid! Please try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                    showProgressBar(false)
                }
            }

        }

    }

    private fun btnRegisterOnClickListener() {
        userBuilder =  AlertDialog.Builder(this)
//        userBuilder.setCancelable(false)
        userBuilder.setView(getUserTypeView("Register"))
        userBuilder.setCancelable(true)
      /*  userBuilder.setPositiveButton("Select",
            DialogInterface.OnClickListener { dialog, _ ->
                dialogInterfaceOnClickListener(dialog)
            })
        userBuilder.setNegativeButton("Select",
            DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
            })*/
        userBuilder.create()
        this.userDialogInterface = userBuilder.show()
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun signIn() {
        firebaseAuth = Firebase.auth
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 9001)
    }

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
                 Toast.makeText(this, "onActivityResult ${e.message}",Toast.LENGTH_SHORT).show()
                Log.d("onActivityResult", "Google sign in failed", e)
            }
        }
    }

    private fun setGmail(gmail: String) {
            this.gmail = gmail
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = firebaseAuth.currentUser
                    user?.email?.let { setGmail(it) }
//                    Toast.makeText(this, "GOOGLE_SIGN_IN_SUCCESS ${user!!.displayName}",Toast.LENGTH_SHORT).show()
                    Log.d("GOOGLE_SIGN_IN_SUCCESS", "${user!!.displayName}")
                    checkUserAccount()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "GOOGLE_SIGN_IN_FAIL ${task.exception!!.message}",Toast.LENGTH_SHORT).show()
                    Log.d("firebaseAuthWithGoogle", "signInWithCredential:failure", task.exception)
                }
            }
    }


    private fun checkUserAccount() {
        showProgressBar(true)
        Toast.makeText(this, "Checking Account...", Toast.LENGTH_SHORT).show()

        firebaseDatabaseReference.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                        this@LoginActivity.userType = snapshot.child(firebaseAuth.currentUser!!.uid).child("type")
                            .getValue(String::class.java).toString()
                        goToDashboard()
                    } else {
                        btnRegisterOnClickListener()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@LoginActivity,
                        "${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun checkFirebaseDatabaseRecord() {
        firebaseDatabaseReference.child("users").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {

                    Log.d("ForSingleValueEvent", firebaseAuth.currentUser!!.uid)

                    this@LoginActivity.userType = snapshot.child(firebaseAuth.currentUser!!.uid).child("type")
                        .getValue(String::class.java).toString()

                    goToDashboard()

                } else {
                    Log.d(
                        "NOT FOUND",
                        "Either your username or password is Invalid! Please try again!"
                    )
                    Toast.makeText(
                        this@LoginActivity,
                        "Either your username or password is Invalid! Please try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity, "${error.message}", Toast.LENGTH_SHORT).show()
                showProgressBar(false)
            }

        })
    }


    private fun getUserTypeView(title: String): View {
        customDialogBinding = DialogUserTypeBinding.inflate(this.layoutInflater)

        with(customDialogBinding) {
            titleView.text  = title
            userPartner.setOnClickListener {
                userType = "Partner"
                showProgressBar(true)
                showRegistrationActivity()
            }
            userRider.setOnClickListener {
                userType = "Rider"
                showProgressBar(true)
                showRegistrationActivity()
            }
            userCustomer.setOnClickListener {
                userType = "Customer"
                showProgressBar(true)
                showRegistrationActivity()
            }
        }
        return customDialogBinding.root
    }

    private fun showRegistrationActivity() {

        showProgressBar(false)
        userDialogInterface.dismiss()
        /*
        if(firebaseAuth.currentUser!=null){
            val intent = Intent(this, RegisterPersonalInfoActivity::class.java)
            intent.putExtra("email", gmail)
            intent.putExtra("userType", userType)
            startActivity(intent)
            finish()
        }
        */
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
        showProgressBar(false)
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

    private fun showProgressBar(visible: Boolean) {
        if (visible) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

}