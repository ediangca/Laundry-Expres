package com.kodego.diangca.ebrahim.laundryexpres.registration.partner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnAttachStateChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kodego.diangca.ebrahim.laundryexpres.LoginActivity
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.adater.FragmentAdapter
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityRegisterPartnerBinding

class RegisterPartnerActivity : AppCompatActivity(){

    private lateinit var binding: ActivityRegisterPartnerBinding

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")

    var fragmentAdapter = FragmentAdapter(supportFragmentManager, lifecycle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPartnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    private fun initComponent() {
        fragmentAdapter =  FragmentAdapter(supportFragmentManager, lifecycle)
        if(firebaseAuth.currentUser == null) {
            fragmentAdapter.addFragment(PartnerBusinessInfoFragment(this)) //0
            fragmentAdapter.addFragment(PartnerBusinessInfoFragment(this)) //1
        }else{
            fragmentAdapter.addFragment(PartnerBusinessInfoFragment(this)) //1
        }
        with(binding.viewPager2) {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = fragmentAdapter

            TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            }.attach()

        }
        binding.viewPager2.addOnAttachStateChangeListener(object : OnAttachStateChangeListener{
            override fun onViewAttachedToWindow(v: View) {

                println( "View onViewAttachedToWindow ${v.id}")

            }

            override fun onViewDetachedFromWindow(v: View) {

                println( "View onViewDetachedFromWindow ${v.id}")
            }

        })
        binding.viewPager2.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            println(
                "View onViewDetachedFromWindow $v, $left, $top, $right, $bottom, $oldLeft, $oldTop, $oldRight, $oldBottom"
            )
        }

        with(binding.tabLayout) {

            if(firebaseAuth.currentUser == null) {
                getTabAt(0)!!.setIcon(R.drawable.vector_account).text = "PERSONAL"
                getTabAt(1)!!.setIcon(R.drawable.vector_laundry).text = "BUSINESS"
            }else{
                getTabAt(0)!!.setIcon(R.drawable.vector_laundry).text = "BUSINESS"
            }
        }


        binding.btnBack.setOnClickListener {
            btnBackOnClickListener()
        }
    }

    private fun btnBackOnClickListener() {
        startActivity(Intent(Intent(this, LoginActivity::class.java)))
        finish()
    }

}