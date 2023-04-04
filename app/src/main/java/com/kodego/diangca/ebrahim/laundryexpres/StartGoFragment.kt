package com.kodego.diangca.ebrahim.laundryexpres

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityMainBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentStartGoBinding
import com.kodego.diangca.ebrahim.laundryexpres.login.LoginFragment


class StartGoFragment(private var mainActivity: MainFragment) : Fragment() {

    private var _binding: FragmentStartGoBinding? = null
    private val binding get() = _binding!!
    private lateinit var main_login_binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStartGoBinding.inflate(layoutInflater, container, false)
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
        binding.btnBack.setOnClickListener {
            btnBackOnClickListener()
        }
        binding.btnLogin.setOnClickListener {
            btnLoginOnClickListener()
        }

        binding.btnCustomer.setOnClickListener {
            btnCustomerOnClickListener()
        }


    }

    private fun btnCustomerOnClickListener() {
        mainActivity.indexActivity.mainFrame = mainActivity.indexActivity.supportFragmentManager.beginTransaction()
        mainActivity.indexActivity.mainFrame.replace(R.id.mainFrame, CheckAvailabilityFragment(mainActivity));
        mainActivity.indexActivity.mainFrame.addToBackStack(null);
        mainActivity.indexActivity.mainFrame.commit();
    }

    private fun btnLoginOnClickListener() {

        mainActivity.indexActivity.mainFrame = mainActivity.indexActivity.supportFragmentManager.beginTransaction()
        mainActivity.indexActivity.mainFrame.replace(R.id.mainFrame, LoginFragment(mainActivity));
        mainActivity.indexActivity.mainFrame.addToBackStack(null);
        mainActivity.indexActivity.mainFrame.commit();
    }

    fun View?.removeSelf() {
        this ?: return
        val parentView = parent as? ViewGroup ?: return
        parentView.removeView(this)
    }

    private fun btnBackOnClickListener() {
        mainActivity.binding.viewPager2.currentItem = 1
    }

}