package com.kodego.diangca.ebrahim.laundryexpres

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.kodego.diangca.ebrahim.laundryexpres.registration.RegisterCustomerActivity
import com.kodego.diangca.ebrahim.laundryexpres.registration.partner.RegisterPartnerActivity
import com.kodego.diangca.ebrahim.laundryexpres.registration.rider.RegisterRiderActivity

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

        if (firebaseAuth.currentUser==null) {
            mainFragment = MainFragment(this)
            mainFrame = supportFragmentManager.beginTransaction()
            mainFrame.replace(R.id.mainFrame, mainFragment)
            mainFrame.commit()
        } else {
            showProgressBar(true)
        }
    }
    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser!=null) {
            firebaseDatabaseReference.child("users").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                        userType =
                            snapshot.child(firebaseAuth.currentUser!!.uid).child("type")
                                .getValue(String::class.java).toString()
                        goToDashboard()
                    } else {
                        showProgressBar(false)
                        Log.d("SIGN_OUT_USER", "WITH_AUTH_BUT_NOT_REGISTERED")
                        firebaseAuth.signOut()
                        mainFragment = MainFragment(this@IndexActivity)
                        mainFrame = supportFragmentManager.beginTransaction()
                        mainFrame.replace(R.id.mainFrame, mainFragment)
                        mainFrame.commit()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showProgressBar(false)
                    Toast.makeText(applicationContext, "${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }

            })
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


}