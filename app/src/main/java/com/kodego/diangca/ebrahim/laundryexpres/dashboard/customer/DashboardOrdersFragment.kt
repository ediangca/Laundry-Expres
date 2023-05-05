package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.R
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardOrdersBinding

class DashboardOrdersFragment(var dashboardCustomer: DashboardCustomerActivity) : Fragment(), AdapterView.OnItemSelectedListener {

    private var _binding: FragmentDashboardOrdersBinding? = null
    private val binding get() = _binding!!


    private var status = arrayOf("ALL", "FOR PICK-UP", "RECEIVED", "FOR DELIVERY", "COMPLETE", "CANCEL")
    val NEW_SPINNER_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardOrdersBinding.inflate(layoutInflater, container, false)
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
        var aa = ArrayAdapter(dashboardCustomer, R.layout.simple_spinner_item, status)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        with(binding.spinnerOrderStatus)
        {
            adapter = aa
            setSelection(0, false)
            onItemSelectedListener = this@DashboardOrdersFragment
            prompt = "Select Order Status"
            gravity = Gravity.CENTER

        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (view?.id) {
            1 -> showToast(message = "Spinner 2 Position:${position} and language: ${status[position]}")
            else -> {
                showToast(message = "Spinner 1 Position:${position} and language: ${status[position]}")
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        showToast(message = "Nothing selected")
    }

    private fun showToast(context: Context = dashboardCustomer.applicationContext, message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, message, duration).show()
    }
}