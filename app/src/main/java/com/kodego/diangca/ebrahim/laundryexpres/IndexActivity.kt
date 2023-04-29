package com.kodego.diangca.ebrahim.laundryexpres

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer.DashboardCustomerActivity
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.partner.DashboardPartnerActivity
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.rider.DashboardRiderActivity
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityIndexBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogLoadingBinding
import com.kodego.diangca.ebrahim.laundryexpres.registration.RegisterCustomerActivity
import com.kodego.diangca.ebrahim.laundryexpres.registration.partner.RegisterPartnerActivity
import com.kodego.diangca.ebrahim.laundryexpres.registration.rider.RegisterRiderActivity
import com.squareup.picasso.Picasso


class IndexActivity : AppCompatActivity() {


    private lateinit var binding: ActivityIndexBinding
    private lateinit var mainFragment: MainFragment
    lateinit var mainFrame: FragmentTransaction

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var userType = "UNKNOWN"
    private var displayName: String? = null
    private var profileUri: Uri? = null

    fun getDatabaseReference(): DatabaseReference {
        return firebaseDatabaseReference
    }

    fun getFirebaseAuth(): FirebaseAuth {
        return firebaseAuth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponent()
    }

    private fun initComponent() {
        /* if (firebaseAuth.currentUser==null) {
             mainFragment = MainFragment(this)
             mainFrame = supportFragmentManager.beginTransaction()
             mainFrame.replace(R.id.mainFrame, mainFragment)
             mainFrame.commit()
         }*/
    }

    override fun onStart() {
        super.onStart()
        showLoadingDialog()
//        showProgressBar(true)
        Handler(Looper.getMainLooper()).postDelayed({
            if (FirebaseAuth.getInstance().currentUser!=null) {
                firebaseDatabaseReference.child("users").addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        dismissLoadingDialog()
//                        showProgressBar(false)
                        if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                            userType =
                                snapshot.child(firebaseAuth.currentUser!!.uid).child("type")
                                    .getValue(String::class.java).toString()

                            val isVerified: Boolean =
                                snapshot.child(firebaseAuth.currentUser!!.uid).child("verified")
                                    .getValue(Boolean::class.java)!!
                            if((userType!="Customer") && !isVerified) {
                                Log.d("SIGN_OUT_USER", "UNVERIFIED_ACCOUNT")
                                firebaseAuth.signOut()
                                showMain()
                                return
                            }
                            goToDashboard()
                        } else {
                            Log.d("SIGN_OUT_USER", "WITH_AUTH_BUT_NOT_REGISTERED")
                            firebaseAuth.signOut()
                            showMain()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        dismissLoadingDialog()
//                        showProgressBar(false)
                        Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                })
            } else {
                dismissLoadingDialog()
//            showProgressBar(false)
                showMain()
            }

        }, 3000) // 3000 is the delayed time in milliseconds.
    }

    private fun showMain() {
        mainFragment = MainFragment(this)
        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.mainFrame, mainFragment)
        mainFrame.commit()
    }


    private fun goToDashboard() {
//        showProgressBar(false)
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
            binding.imageView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.imageView.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
        }
    }

    fun replaceFragment(fragment: Fragment) {
        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.mainFrame, fragment);
        mainFrame.addToBackStack(null);
        mainFrame.commit();
    }

    fun showLogin() {
        Log.d("SHOW_LOGIN_ACTIVITY", "LOGIN ACTIVITY")
        startActivity(Intent(Intent(Intent(this, LoginActivity::class.java))))
        finish()
    }

    fun showCustomerRegister() {
        Log.d("SHOW_REGISTER_ACTIVITY", "REGISTER CUSTOMER ACTIVITY")
        startActivity(Intent(Intent(this, RegisterCustomerActivity::class.java)))
        finish()
    }

    fun showPartnershipRegister() {
        Log.d("SHOW_REGISTER_ACTIVITY", "REGISTER PARTNERSHIP ACTIVITY")
        startActivity(Intent(Intent(this, RegisterPartnerActivity::class.java)))
        finish()
    }

    fun showRiderRegister() {
        Log.d("SHOW_REGISTER_ACTIVITY", "REGISTER RIDER ACTIVITY")
        startActivity(Intent(Intent(this, RegisterRiderActivity::class.java)))
        finish()
    }

    private lateinit var loadingBuilder: AlertDialog.Builder
    private lateinit var loadingDialog: Dialog
    private fun showLoadingDialog() {
        val loadingBinding = DialogLoadingBinding.inflate(this.layoutInflater)

        firebaseAuth.currentUser?.let {
            for (profile in it.providerData) {
                displayName = profile.displayName
                profileUri = profile.photoUrl
            }
        }
        if (profileUri!=null) {
            Log.d("profilePic", "$profileUri")
            val profileView: ImageView = loadingBinding.imageView
            Picasso.with(applicationContext).load(profileUri).into(profileView);
        }

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