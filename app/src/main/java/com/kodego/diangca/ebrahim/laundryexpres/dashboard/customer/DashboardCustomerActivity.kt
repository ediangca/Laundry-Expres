package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.content.Intent
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
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityDashboardCustomerBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop

class DashboardCustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardCustomerBinding

    private lateinit var mainFrame: FragmentTransaction

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")


    private lateinit var dashboardHomeFragment: DashboardHomeFragment
    private lateinit var dashboardOrdersFragment: DashboardOrdersFragment
    private lateinit var dashboardNotificationFragment: DashboardNotificationFragment
    private lateinit var dashboardInboxFragment: DashboardInboxFragment
    private lateinit var dashboardAccountFragment: DashboardAccountFragment

    private lateinit var dashboardShopFragment: DashboardShopFragment
    private lateinit var dashboardOrderFormFragment: DashboardOrderFormFragment

    lateinit var shop: Shop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    private fun initComponent() {

        dashboardShopFragment = DashboardShopFragment(this)

        dashboardHomeFragment = DashboardHomeFragment(this)
        dashboardOrdersFragment = DashboardOrdersFragment(this)
        dashboardNotificationFragment = DashboardNotificationFragment(this)
        dashboardInboxFragment = DashboardInboxFragment(this)
        dashboardAccountFragment = DashboardAccountFragment(this)
        dashboardOrderFormFragment = DashboardOrderFormFragment(this)

        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.fragmentCustomerDashboard, DashboardHomeFragment(this));
        mainFrame.addToBackStack(null);
        mainFrame.commit();

        binding.dashboardNav.setOnItemSelectedListener {
            navMenuOnItemSelectedListener(it)
        }
    }

    private fun navMenuOnItemSelectedListener(it: MenuItem?): Boolean {

//        binding.fragmentCustomerDashboard.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0)
        if (it==null) {
            mainFrame = supportFragmentManager.beginTransaction()
            mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardHomeFragment);
            mainFrame.addToBackStack(null);
            mainFrame.commit();
            return true
        } else {
            Log.d("MENU ITEM", "ID: ${it!!.itemId}")
            when (it.itemId) {
                R.id.navCustomerHome -> {
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardHomeFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }
                R.id.navCustomerOrder -> {
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardOrdersFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }
                R.id.navCustomerUpdates -> {
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(
                        R.id.fragmentCustomerDashboard,
                        dashboardNotificationFragment
                    );
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    binding.dashboardNav.visibility = View.VISIBLE
                    return true
                }
                R.id.navCustomerInbox -> {
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardInboxFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }
                R.id.navCustomerAccount -> {
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardAccountFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }

                else -> {
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardHomeFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                    return true
                }
            }
        }
        return false
    }


    fun showHome() {
        binding.dashboardNav.visibility = View.VISIBLE
        navMenuOnItemSelectedListener(null)
    }

    fun showShopList() {
        binding.dashboardNav.visibility = View.GONE
//        binding.fragmentCustomerDashboard.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.MATCH_PARENT)
        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardShopFragment);
        mainFrame.addToBackStack(null);
        mainFrame.commit();
    }
    fun showOrderForm(shop: Shop) {
        this.shop = shop
        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardOrderFormFragment);
        mainFrame.addToBackStack(null);
        mainFrame.commit();
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