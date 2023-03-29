package com.kodego.diangca.ebrahim.laundryexpres

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.kodego.diangca.ebrahim.laundryexpres.adater.FragmentAdapter
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    lateinit var binding: ActivityMainBinding
    var fragmentAdapter = FragmentAdapter(supportFragmentManager, lifecycle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    private fun initComponent() {
        fragmentAdapter = FragmentAdapter(supportFragmentManager, lifecycle)
//        fragmentAdapter.addFragment(StartFragment(binding))
//        fragmentAdapter.addFragment(StartServiceFragment(binding))
//        fragmentAdapter.addFragment(StartGoFragment(this))

        with(binding.viewPager2) {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = fragmentAdapter


            TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            }.attach()

        }
        with(binding.tabLayout) {
            getTabAt(0)!!.setIcon(R.drawable.vector_home).text = "HOME"
            getTabAt(1)!!.setIcon(R.drawable.vector_services).text = "ORDERS"
            getTabAt(2)!!.setIcon(R.drawable.vector_register_login).text = "REGISTER NOW"
        }
    }


}