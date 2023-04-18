package com.kodego.diangca.ebrahim.laundryexpres

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentCheckMapBinding
import java.util.*


class CheckMapFragment(var mainActivity: MainFragment) : Fragment() {

    private var _binding: FragmentCheckMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapFragment: SupportMapFragment

    private lateinit var mainFrame: FragmentTransaction
    private lateinit var googleMap: GoogleMap

    private var currentLocation = LatLng(7.445660, 125.805809)
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var currentLatLong: LatLng


    companion object{
        private const val LOCATION_REQUEST_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCheckMapBinding.inflate(layoutInflater, container, false)
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity.indexActivity)

        mapFragment = SupportMapFragment()
        mapFragment.getMapAsync(OnMapReadyCallback {

            mapFragmentOnMapReadyCallback(it)
        })


        mainFrame = mainActivity.indexActivity.supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.mapFragment, mapFragment)
        mainFrame.commit()

        binding.btnBack.setOnClickListener {
            mainActivity.indexActivity.mainFrame =
                mainActivity.indexActivity.supportFragmentManager.beginTransaction()
            mainActivity.indexActivity.mainFrame.replace(
                R.id.mainFrame,
                CheckAvailabilityFragment(mainActivity)
            );
            mainActivity.indexActivity.mainFrame.addToBackStack(null);
            mainActivity.indexActivity.mainFrame.commit();
        }

        binding.btnProceed.setOnClickListener {

            btnProceedOnClickListener()
        }

        binding.btnSearch.setOnClickListener {
            btnSearchOnClickListener()
        }

    }

    private fun btnSearchOnClickListener() {
        val stringLocation = binding.searchLocation.text.toString()
        var addresses: List<Address>? = null
        if(!stringLocation.isEmpty()){
            var geocoder = Geocoder(binding.root.context)
            try {
                addresses = geocoder.getFromLocationName(stringLocation, 1)

            }catch (e: Exception){
                Log.d("SEARCH_GEO_LOCATION", "${e.message}")
            }
            if(addresses!=null && !addresses.isEmpty()) {
                val address = addresses!![0]
                val searchLocation = LatLng(address.latitude, address.longitude)
                placeMarkerOnMap(searchLocation)
            }else{
                Toast.makeText(context, "No Location found! Please try again!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun btnProceedOnClickListener() {


        getAddressDetails()
        /*
        mainActivity.indexAc  tivity.mainFrame = mainActivity.indexActivity.supportFragmentManager.beginTransaction()
        mainActivity.indexActivity.mainFrame.replace(R.id.mainFrame,
            CheckAvailabilityFragment(mainActivity)
        );
        mainActivity.indexActivity.mainFrame.addToBackStack(null);
        mainActivity.indexActivity.mainFrame.commit();*/
    }


    private fun mapFragmentOnMapReadyCallback(gMap: GoogleMap) {

        googleMap = gMap
        googleMap.uiSettings.isZoomControlsEnabled = true
//        googleMap.addMarker(MarkerOptions().position(currentLocation).title("My Location"))
//        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10f))

        setUpMap()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(binding.root.context,Manifest.permission.ACCESS_FINE_LOCATION)
            !=PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(binding.root.context,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(mainActivity.indexActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)

            return
        }
        googleMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(mainActivity.indexActivity) { location ->
                if(location != null){
                    lastLocation = location
                    currentLatLong = LatLng(location.latitude, location.longitude)
                    placeMarkerOnMap(currentLatLong)

                }
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        this.currentLatLong = currentLatLong
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(currentLatLong).title("$currentLatLong"))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 10f))

        getAddressDetails()

    }

    private fun getAddressDetails() {

        var geocoder = Geocoder(mainActivity.indexActivity, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(currentLatLong.latitude, currentLatLong.longitude, 1)


        val address: String = addresses!![0].getAddressLine(0)?:""
        val city: String = addresses!![0].locality?:""
        val state: String = addresses!![0].adminArea?:""
        val zip: String = addresses!![0].postalCode?:""
        val country: String = addresses!![0].countryName?:""

        Toast.makeText(context, "Address: $address, $city, $state, $zip, $country", Toast.LENGTH_LONG).show()
    }


}