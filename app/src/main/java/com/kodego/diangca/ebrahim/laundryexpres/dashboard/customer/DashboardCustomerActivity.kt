package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kodego.diangca.ebrahim.laundryexpres.LoginActivity
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityDashboardCustomerBinding

class DashboardCustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardCustomerBinding

    private lateinit var mainFrame: FragmentTransaction

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }
    private fun initComponent() {

        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.mainFrameCustomerDashboard, DashboardHomeFragment(this));
        mainFrame.addToBackStack(null);
        mainFrame.commit();

        binding.dashboardNav.setOnItemSelectedListener {
            navMenuOnItemSelectedListener(it)
        }
    }

    private fun navMenuOnItemSelectedListener(it: MenuItem): Boolean {

        Log.d("MENU ITEM", "ID: ${it.itemId}")
        when (it.itemId) {
            R.id.navCustomerHome -> {
                mainFrame = supportFragmentManager.beginTransaction()
                mainFrame.replace(R.id.mainFrameCustomerDashboard, DashboardHomeFragment(this));
                mainFrame.addToBackStack(null);
                mainFrame.commit();
                return true
            }
            R.id.navCustomerOrder -> {
                mainFrame = supportFragmentManager.beginTransaction()
                mainFrame.replace(R.id.mainFrameCustomerDashboard, DashboardOrdersFragment(this));
                mainFrame.addToBackStack(null);
                mainFrame.commit();
                return true
            }
            R.id.navCustomerUpdates -> {
                mainFrame = supportFragmentManager.beginTransaction()
                mainFrame.replace(R.id.mainFrameCustomerDashboard, DashboardNotificationFragment(this));
                mainFrame.addToBackStack(null);
                mainFrame.commit();
                return true
            }
            R.id.navCustomerInbox -> {
                mainFrame = supportFragmentManager.beginTransaction()
                mainFrame.replace(R.id.mainFrameCustomerDashboard, DashboardInboxFragment(this));
                mainFrame.addToBackStack(null);
                mainFrame.commit();
                return true
            }
            R.id.navCustomerAccount -> {
                mainFrame = supportFragmentManager.beginTransaction()
                mainFrame.replace(R.id.mainFrameCustomerDashboard, DashboardAccountFragment(this));
                mainFrame.addToBackStack(null);
                mainFrame.commit();
                return true
            }

        }
        return false
    }

    fun signOut() {

        var loginIntent = Intent(this, LoginActivity::class.java)
        var bundle = Bundle()
        /* bundle.putString("positionApplied", positionApply)
         bundle.putDouble("desiredSalary", desiredSalary)
         bundle.putString(
             "dateAvailable",
             SimpleDateFormat("yyyy-MM-d").format(dateAvailable)
         )
         nextForm.putExtras(bundle)

         nextForm.putExtra("something", "Extra")*/

        startActivity(Intent(loginIntent))
        finish()

    }

}