package com.kodego.diangca.ebrahim.laundryexpres

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentMainBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentStartBinding

class StartFragment(private var mainActivity: FragmentMainBinding) : Fragment() {

    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStartBinding.inflate(layoutInflater, container, false)
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

        _binding!!.btnNext.setOnClickListener {
            Log.d("TEST NEXT BUTTON", "EFFECT")
            btnNextOnClickListener()
        }
    }

    private fun btnNextOnClickListener() {
        mainActivity.viewPager2.currentItem = 1
    }
}