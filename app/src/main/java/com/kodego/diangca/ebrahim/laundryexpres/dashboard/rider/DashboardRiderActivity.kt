package com.kodego.diangca.ebrahim.laundryexpres.dashboard.rider

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kodego.diangca.ebrahim.laundryexpres.LoginActivity
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityDashboardRiderBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogLoadingBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardRiderAccountBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardRiderInboxBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardRiderNotificationBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Requirements
import com.kodego.diangca.ebrahim.laundryexpres.model.User

class DashboardRiderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardRiderBinding

    private lateinit var mainFrame: FragmentTransaction

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")


    private lateinit var dashboardHomeFragment: DashboardRiderHomeFragment
    private lateinit var dashboardRidesFragment: DashboardRiderRideFragment
    private lateinit var dashboardNotificationFragment: DashboardRiderNotificationFragment
    private lateinit var dashboardInboxFragment: DashboardRiderInboxFragment
    private lateinit var dashboardAccountFragment: DashboardRiderAccountFragment

    private var user: User? = null
    private var requirements: Requirements? = null

    private var pickUpDatetime: String? = null
    private var deliveryDatetime: String? = null

    private var bundle = Bundle()

    private lateinit var loadingBuilder: AlertDialog.Builder
    lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardRiderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }
    private fun initComponent() {
        Log.d("ON_ATTACH_DASHBOARD_RIDER", "ATTACHED RIDER DASHBOARD")
        retrieveUserDetails()

        dashboardHomeFragment = DashboardRiderHomeFragment(this)
        dashboardRidesFragment = DashboardRiderRideFragment(this)
        dashboardNotificationFragment = DashboardRiderNotificationFragment(this)
        dashboardInboxFragment = DashboardRiderInboxFragment(this)
        dashboardAccountFragment = DashboardRiderAccountFragment(this)


        binding.dashboardNav.setOnItemSelectedListener {
            navMenuOnItemSelectedListener(it)
        }
    }

    @JvmName("getShop1")
    fun setRequirements(requirements: Requirements) {
        this.requirements = requirements
    }

    @JvmName("getShop1")
    fun getRequirements(): Requirements {
        return requirements!!
    }

    fun setUser(user: User?) {
        this.user = user
    }

    fun getUser(): User? {
        return user!!
    }

    private fun retrieveUserDetails() {
        val databaseRef = firebaseDatabase.reference.child("users")
            .child(firebaseAuth.currentUser!!.uid)

        databaseRef.get().addOnCompleteListener { dataSnapshot ->
            if (dataSnapshot.isSuccessful) {
                user = dataSnapshot.result.getValue(User::class.java)
                if (user != null) {
                    Log.d("USER_DETAILS_FOUND", user.toString())
                    bundle = Bundle()
                    bundle.putParcelable("user", user)
                    dashboardHomeFragment.arguments = bundle
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(
                        R.id.fragmentRiderDashboard,
                        DashboardRiderHomeFragment(this@DashboardRiderActivity)
                    );
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                }
            } else {
                Log.d("USER_DETAILS_NOT_FOUND", "USER NOT FOUND")
            }
        }
    }

    private fun navMenuOnItemSelectedListener(it: MenuItem?): Boolean {
        if (it == null) {
            bundle = Bundle()
            bundle.putParcelable("user", user)
            dashboardHomeFragment.arguments = bundle
            mainFrame = supportFragmentManager.beginTransaction()
            mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardHomeFragment);
            mainFrame.addToBackStack(null);
            mainFrame.commit();
            return true
        } else {
            Log.d("MENU ITEM", "ID: ${it.itemId}  --------------")
            bundle = Bundle()
            when (it.itemId) {
                R.id.navRiderHome -> {
                    bundle.putParcelable("user", user)
                    dashboardHomeFragment.arguments = bundle
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentRiderDashboard, dashboardHomeFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }

                R.id.navRiderOrder -> {
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentRiderDashboard, dashboardRidesFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }

                R.id.navRiderUpdates -> {
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace( R.id.fragmentRiderDashboard, dashboardNotificationFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    binding.dashboardNav.visibility = View.VISIBLE
                    return true
                }

                R.id.navRiderInbox -> {
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentRiderDashboard, dashboardInboxFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }

                R.id.navCustomerAccount -> {
                    bundle.putParcelable("user", user)
                    dashboardAccountFragment.arguments = bundle
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentRiderDashboard, dashboardAccountFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }

                else -> {
                    bundle.putParcelable("user", user)
                    dashboardHomeFragment.arguments = bundle
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardHomeFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }
            }
        }
    }

    fun signOut() {
        var loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(Intent(loginIntent))
        finish()
    }

    fun showLoadingDialog() {
        val loadingBinding = DialogLoadingBinding.inflate(this.layoutInflater)
        loadingBuilder = AlertDialog.Builder(this)
        loadingBuilder.setCancelable(false)
        loadingBuilder.setView(loadingBinding.root)
        loadingDialog = loadingBuilder.create()
        if (loadingDialog.window != null) {
            loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        loadingDialog.show()
    }

    fun dismissLoadingDialog() {
        loadingDialog.dismiss()
    }




}