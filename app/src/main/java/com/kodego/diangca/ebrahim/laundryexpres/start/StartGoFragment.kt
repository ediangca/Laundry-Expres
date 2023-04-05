package com.kodego.diangca.ebrahim.laundryexpres.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kodego.diangca.ebrahim.laundryexpres.CheckAvailabilityFragment
import com.kodego.diangca.ebrahim.laundryexpres.MainFragment
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityMainBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentStartGoBinding


class StartGoFragment(private var mainFragment: MainFragment) : Fragment() {

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
        mainFragment.indexActivity.mainFrame = mainFragment.indexActivity.supportFragmentManager.beginTransaction()
        mainFragment.indexActivity.mainFrame.replace(R.id.mainFrame, CheckAvailabilityFragment(mainFragment));
        mainFragment.indexActivity.mainFrame.addToBackStack(null);
        mainFragment.indexActivity.mainFrame.commit();
    }

    private fun btnLoginOnClickListener() {
        mainFragment.indexActivity.showLogin()
    }

    fun View?.removeSelf() {
        this ?: return
        val parentView = parent as? ViewGroup ?: return
        parentView.removeView(this)
    }

    private fun btnBackOnClickListener() {
        mainFragment.binding.viewPager2.currentItem = 1
    }

}