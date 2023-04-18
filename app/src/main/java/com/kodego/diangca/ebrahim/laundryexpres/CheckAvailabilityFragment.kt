package com.kodego.diangca.ebrahim.laundryexpres

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentCheckAvailabilityBinding
import java.util.*

class CheckAvailabilityFragment(var mainFragment: MainFragment): Fragment() {

    private var _binding: FragmentCheckAvailabilityBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCheckAvailabilityBinding.inflate(layoutInflater, container, false)
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
            val mainFragment = MainFragment(mainFragment.indexActivity)
            mainFragment.setSelectedTab(2)
            mainFragment.indexActivity.replaceFragment(mainFragment)
        }
        binding.btnUseMap.setOnClickListener {
            mainFragment.indexActivity.replaceFragment(CheckMapFragment(mainFragment))
        }

        binding.btnCheckAvailability.setOnClickListener {
            btnCheckAvailabilityOnClickListener()
        }

        binding.btnLogin.setOnClickListener {
            btnLoginOnClickListener()
        }

        binding.btnRegister.setOnClickListener {
            btnRegisterOnClickListener()
        }
    }

    private fun btnLoginOnClickListener() {
        mainFragment.indexActivity.showLogin()
    }

    private fun btnRegisterOnClickListener() {
        mainFragment.indexActivity.showCustomerRegister()
    }

    private fun btnCheckAvailabilityOnClickListener() {
        val stringLocation = binding.searchLocation.text.toString()
        var addresses: List<Address>? = null
        if(stringLocation.isNotEmpty()){
            var geocoder = Geocoder(binding.root.context)
            try {
                addresses = geocoder.getFromLocationName(stringLocation, 1)
            }catch (e: Exception){
                Log.d("SEARCH_GEO_LOCATION", "${e.message}")
            }

            if(addresses!=null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val searchLocation = LatLng(address.latitude, address.longitude)
                getAddressDetails(searchLocation)
            }else{
                Toast.makeText(context, "No Location found! Please try again!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAddressDetails(currentLatLong: LatLng) {

        var geocoder = Geocoder(mainFragment.indexActivity, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(currentLatLong.latitude, currentLatLong.longitude, 1)


        if(addresses!=null && addresses.isNotEmpty()) {
            val address: String = addresses[0].getAddressLine(0)
            val city: String = addresses[0].locality
            val state: String = addresses[0].adminArea
            val zip: String = addresses[0].postalCode
            val country: String = addresses[0].countryName

            Toast.makeText(
                context,
                "Address: $address, $city, $state, $zip, $country",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}