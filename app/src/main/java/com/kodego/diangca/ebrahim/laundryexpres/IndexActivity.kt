package com.kodego.diangca.ebrahim.laundryexpres

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer.DashboardCustomerFragment
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityIndexBinding

class IndexActivity : AppCompatActivity() {


    private lateinit var binding: ActivityIndexBinding
    private lateinit var mainFragment: MainFragment
    lateinit var mainFrame: FragmentTransaction

    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")

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
            firebaseDatabaseReference.child("users").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {

                        val getUserType =
                            snapshot.child(firebaseAuth.currentUser!!.uid).child("type")
                                .getValue(String::class.java).toString()

                        when (getUserType) {
                            "Partner" -> {

                            }
                            "Customer" -> {
                                Toast.makeText(
                                    applicationContext,
                                    "Hi ${
                                        snapshot.child(firebaseAuth.currentUser!!.uid)
                                            .child("firstname").getValue(String::class.java)
                                    }! Have a great day!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                mainFrame = supportFragmentManager.beginTransaction()
                                mainFrame.replace(R.id.mainFrame, DashboardCustomerFragment(this@IndexActivity))
                                mainFrame.commit()
                            }
                            "Rider" -> {

                            }

                        }

                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Either your username or password is Invalid! Please try again!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                    Toast.makeText(applicationContext, "${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }

            })
        }
    }
}