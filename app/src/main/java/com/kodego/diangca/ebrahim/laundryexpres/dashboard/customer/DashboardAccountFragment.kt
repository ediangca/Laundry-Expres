package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardAccountBinding
import com.kodego.diangca.ebrahim.laundryexpres.login.LoginFragment


class DashboardAccountFragment(var dashboardCustomerFragment: DashboardCustomerFragment) : Fragment() {

    private var _binding: FragmentDashboardAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userName = dashboardCustomerFragment.getUserName()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardAccountBinding.inflate(layoutInflater, container, false)
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


        binding.Title.text = userName

        firebaseAuth = dashboardCustomerFragment.indexActivity.getFirebaseAuth()

        binding.btnLogout.setOnClickListener {
            btnLogoutOnClickListener()
        }
    }

    private fun btnLogoutOnClickListener() {
        firebaseAuth.signOut()
        dashboardCustomerFragment.indexActivity.mainFrame = dashboardCustomerFragment.indexActivity.supportFragmentManager.beginTransaction()
        dashboardCustomerFragment.indexActivity.mainFrame.replace(R.id.mainFrame, LoginFragment(dashboardCustomerFragment.indexActivity.getMainFragment()));
        dashboardCustomerFragment.indexActivity.mainFrame.addToBackStack(null);
        dashboardCustomerFragment.indexActivity.mainFrame.commit();
    }

}