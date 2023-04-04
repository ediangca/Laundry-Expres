package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.kodego.diangca.ebrahim.laundryexpres.IndexActivity
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardCustomerBinding

class DashboardCustomerFragment(var indexActivity: IndexActivity) : Fragment() {

    var _binding: FragmentDashboardCustomerBinding? = null
    val binding get() = _binding!!

    private lateinit var mainFrame: FragmentTransaction
    private lateinit var firebaseDatabaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    private var userName : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardCustomerBinding.inflate(layoutInflater, container, false)
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

        firebaseDatabaseReference = indexActivity.getDatabaseReference()
        firebaseAuth = indexActivity.getFirebaseAuth()

        firebaseDatabaseRecord()

        openFragment(DashboardHomeFragment(this))

        binding.dashboardNav.setOnItemSelectedListener {
            navMenuOnItemSelectedListener(it)
        }
    }

    private fun navMenuOnItemSelectedListener(it: MenuItem): Boolean {

        Log.d("MENU ITEM", "ID: ${it.itemId}")
        when (it.itemId) {
            R.id.navCustomerHome -> {
                openFragment(DashboardHomeFragment(this))
                return true
            }
            R.id.navCustomerOrder -> {
                openFragment(DashboardOrdersFragment(this))
                return true
            }
            R.id.navCustomerUpdates -> {
                openFragment(DashboardNotificationFragment(this))
                return true
            }
            R.id.navCustomerInbox -> {
                openFragment(DashboardInboxFragment(this))
                return true
            }
            R.id.navCustomerAccount -> {
                openFragment(DashboardAccountFragment(this))
                return true
            }

        }
        return false
    }

    private fun openFragment(fragment: Fragment) {
        mainFrame = indexActivity.supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.mainFrameCustomerDashboard, fragment);
        mainFrame.addToBackStack(null);
        mainFrame.commit();
    }

    private fun btnNextOnClickListener() {
    }

    fun getUserName(): String {
        return userName
    }


    private fun firebaseDatabaseRecord() {
        firebaseDatabaseReference.child("users").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {

                    val getUserType = snapshot.child(firebaseAuth.currentUser!!.uid).child("type").getValue(String::class.java).toString()
                    userName = "${snapshot.child(firebaseAuth.currentUser!!.uid).child("firstname").getValue(String::class.java)}"

                } else {
                    Toast.makeText(context, "Either your username or password is Invalid! Please try again!", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(context, "${error.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }
}