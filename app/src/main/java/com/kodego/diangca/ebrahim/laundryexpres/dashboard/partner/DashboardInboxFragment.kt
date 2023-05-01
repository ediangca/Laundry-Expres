package com.kodego.diangca.ebrahim.laundryexpres.dashboard.partner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardPartnerInboxBinding

class DashboardInboxFragment(var dashboardPartner: DashboardPartnerActivity) : Fragment() {

    private var _binding: FragmentDashboardPartnerInboxBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardPartnerInboxBinding.inflate(layoutInflater, container, false)
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
}