package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

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
import com.google.firebase.database.*
import com.kodego.diangca.ebrahim.laundryexpres.LoginActivity
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityDashboardCustomerBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogLoadingBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Order
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop
import com.kodego.diangca.ebrahim.laundryexpres.model.User

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
    private lateinit var dashboardOrderDetailsFragment: DashboardOrderDetailsFragment

    private var user: User? = null
    private var shop: Shop? = null

    private var pickUpDatetime: String? = null
    private var deliveryDatetime: String? = null

    private var bundle = Bundle()

    private lateinit var loadingBuilder: AlertDialog.Builder
    lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    private fun initComponent() {

        Log.d("ON_ATTACH_DASHBOARD_CUSTOMER", "ATTACHED CUSTOMER DASHBOARD")
        retrieveUserDetails()

        dashboardHomeFragment = DashboardHomeFragment(this)
        dashboardOrdersFragment = DashboardOrdersFragment(this)
        dashboardNotificationFragment = DashboardNotificationFragment(this)
        dashboardInboxFragment = DashboardInboxFragment(this)
        dashboardAccountFragment = DashboardAccountFragment(this)
        dashboardOrderFormFragment = DashboardOrderFormFragment(this)

        dashboardShopFragment = DashboardShopFragment(this)

        binding.dashboardNav.setOnItemSelectedListener {
            navMenuOnItemSelectedListener(it)
        }
    }

    @JvmName("getShop1")
    fun setShop(shop: Shop) {
        this.shop = shop
    }

    @JvmName("getShop1")
    fun getShop(): Shop {
        return shop!!
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
                        R.id.fragmentCustomerDashboard,
                        DashboardHomeFragment(this@DashboardCustomerActivity)
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
                R.id.navCustomerHome -> {
                    bundle.putParcelable("user", user)
                    dashboardHomeFragment.arguments = bundle
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
                    bundle.putParcelable("user", user)
                    dashboardAccountFragment.arguments = bundle
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardAccountFragment);
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


    fun showHome() {
        binding.dashboardNav.visibility = View.VISIBLE
        navMenuOnItemSelectedListener(null)
    }

    fun resumeShopList() {
        binding.dashboardNav.visibility = View.GONE
        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardShopFragment);
        mainFrame.addToBackStack(null);
        mainFrame.commit();
    }


    //
    fun showShopList(pickUpDatetime: String, deliveryDatetime: String) {
        binding.dashboardNav.visibility = View.GONE
        this.pickUpDatetime = pickUpDatetime
        this.deliveryDatetime = deliveryDatetime
        dashboardShopFragment = DashboardShopFragment(this)
        dashboardShopFragment.arguments = bundle
        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardShopFragment);
        mainFrame.addToBackStack(null);
        mainFrame.commit();
    }

    fun showOrderForm(shop: Shop) {
        setShop(shop)
        bundle = Bundle()
        bundle.putParcelable("shop", shop)
        bundle.putString("pickUpDatetime", pickUpDatetime)
        bundle.putString("deliveryDatetime", pickUpDatetime)
        dashboardOrderFormFragment = DashboardOrderFormFragment(this)
        dashboardOrderFormFragment.arguments = bundle
        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardOrderFormFragment);
        mainFrame.addToBackStack(null);
        mainFrame.commit();
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

    fun showOrder() {
        binding.dashboardNav.visibility = View.VISIBLE
        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardOrdersFragment);
        mainFrame.addToBackStack(null);
        mainFrame.commit();
    }

    fun showOrderDetails(order: Order, callBack: String) {
        order.printLOG()
        bundle = Bundle()
        bundle.putString("user", "customer")
        bundle.putParcelable("order", order)
        dashboardOrderDetailsFragment = DashboardOrderDetailsFragment(this)
        dashboardOrderDetailsFragment.setCallBack(callBack)
        dashboardOrderDetailsFragment.arguments = bundle
        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.fragmentCustomerDashboard, dashboardOrderDetailsFragment);
        mainFrame.addToBackStack(null);
        mainFrame.commit()
    }


}