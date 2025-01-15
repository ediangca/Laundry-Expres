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
import com.kodego.diangca.ebrahim.laundryexpres.model.Order
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop
import com.kodego.diangca.ebrahim.laundryexpres.model.User

class DashboardRiderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardRiderBinding

    private lateinit var mainFrame: FragmentTransaction

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")



    private var user: User? = null
    private var shop: Shop? = null

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