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
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityIndexBinding
import com.kodego.diangca.ebrahim.laundryexpres.registration.RegisterCustomerActivity

class IndexActivity : AppCompatActivity() {


    private lateinit var binding: ActivityIndexBinding
    private lateinit var mainFragment: MainFragment
    lateinit var mainFrame: FragmentTransaction

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getMainFragment(): MainFragment {
        return mainFragment
    }
    fun getFirebaseDatabase(): FirebaseDatabase {
        return firebaseDatabase;
    }

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

        mainFragment = MainFragment(this)
        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.mainFrame, mainFragment)
        mainFrame.commit()
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser!=null) {
            showProgressBar(true)
            firebaseDatabaseReference.child("users").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                        val userType =
                            snapshot.child(firebaseAuth.currentUser!!.uid).child("type")
                                .getValue(String::class.java).toString()
                        goToDashboard(userType)
                    } else {
                        showProgressBar(false)
                        Toast.makeText(
                            applicationContext,
                            "Either your username or password is Invalid! Please try again!",
                            Toast.LENGTH_SHORT
                        ).show()

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

    private fun goToDashboard(userType: String) {
        when (userType) {
            "Customer" -> {
                showProgressBar(false)
                startActivity((Intent(this, DashboardCustomerActivity::class.java)))
                finish()
            }
            "Partner" -> {
                showProgressBar(false)
                //Checking if Verified

            }
            "Rider" -> {
                showProgressBar(true)
            }
            else -> {

                showProgressBar(true)
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

    fun replaceFragment( fragment: Fragment) {

        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.mainFrame, fragment);
        mainFrame.addToBackStack(null);
        mainFrame.commit();
    }

    fun showLogin() {
        Log.d("SHOW_LOGIN", "LOGIN ACTIVITY")
        startActivity(Intent(Intent(Intent(this, LoginActivity::class.java))))
        finish()
    }

    fun showRegister() {
        Log.d("SHOW_REGISTER", "REGISTER ACTIVITY")
        startActivity(Intent(Intent(this, RegisterCustomerActivity::class.java)))
        finish()
    }

}