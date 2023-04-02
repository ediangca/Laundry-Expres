package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kodego.diangca.ebrahim.laundryexpres.IndexActivity
import com.kodego.diangca.ebrahim.laundryexpres.adater.FragmentAdapter
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardCustomerBinding

class DashboardCustomerFragment(var indexActivity: IndexActivity) : Fragment() {

    var _binding: FragmentDashboardCustomerBinding? = null
    val binding get() = _binding!!
    var fragmentAdapter = FragmentAdapter(indexActivity.supportFragmentManager, lifecycle)

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
    }

    private fun btnNextOnClickListener() {
    }
}