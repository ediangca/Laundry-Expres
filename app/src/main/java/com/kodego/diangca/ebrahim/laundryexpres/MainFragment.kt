package com.kodego.diangca.ebrahim.laundryexpres

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.kodego.diangca.ebrahim.laundryexpres.adater.FragmentAdapter
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentMainBinding
import com.kodego.diangca.ebrahim.laundryexpres.start.StartFragment
import com.kodego.diangca.ebrahim.laundryexpres.start.StartGoFragment
import com.kodego.diangca.ebrahim.laundryexpres.start.StartServiceFragment

class MainFragment(var indexActivity: IndexActivity) : Fragment() {

    var _binding: FragmentMainBinding? = null
    val binding get() = _binding!!
    var fragmentAdapter = FragmentAdapter(indexActivity.supportFragmentManager, lifecycle)

    private var currentItem = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
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
        fragmentAdapter =  FragmentAdapter(indexActivity.supportFragmentManager, lifecycle)
        fragmentAdapter.addFragment(StartFragment(_binding!!)) //0
        fragmentAdapter.addFragment(StartServiceFragment(_binding!!)) //1
        fragmentAdapter.addFragment(StartGoFragment(this)) //2

        with(binding.viewPager2) {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = fragmentAdapter

            TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            }.attach()

        }

        with(binding.tabLayout) {
            getTabAt(0)!!.setIcon(R.drawable.vector_home).text = "HOME"
            getTabAt(1)!!.setIcon(R.drawable.vector_services).text = "SERVICES"
            getTabAt(2)!!.setIcon(R.drawable.vector_register_login).text = "START"
        }



    }

    private fun btnNextOnClickListener() {
    }

    fun setSelectedTab(item: Int) {
        this.currentItem = item
    }

    override fun onStart() {
        super.onStart()
        Log.d("ON_START", "START HOME FRAGMENT $currentItem")
        binding.viewPager2.post {
            binding.viewPager2.setCurrentItem(currentItem, true)
        }
//        Handler().postDelayed({
//            binding.viewPager2.setCurrentItem(currentItem, true)
//        }, 50)

    }
}