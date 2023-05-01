package com.kodego.diangca.ebrahim.laundryexpres

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogShopAvailabilityBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentCheckAvailabilityBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop
import java.util.*

class CheckAvailabilityFragment(var mainFragment: MainFragment): Fragment() {

    private var _binding: FragmentCheckAvailabilityBinding? = null
    private val binding get() = _binding!!

    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    private lateinit var promptBuilder: AlertDialog.Builder
    private lateinit var promptDialog: Dialog

    private var shopArrayList: ArrayList<Shop> = ArrayList()
    private var addresses: List<Address>? = null
    private var customerCity: String = ""
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
        addresses = null
        if(stringLocation.isNotEmpty()){
            var geocoder = Geocoder(binding.root.context)
            try {
                addresses = geocoder.getFromLocationName(stringLocation, 1)
            }catch (e: Exception){
                Log.d("SEARCH_GEO_LOCATION", "${e.message}")
            }

            if(addresses!=null && addresses!!.isNotEmpty()) {
                val address = addresses!![0]
                val searchLocation = LatLng(address.latitude, address.longitude)
                getAddressDetails(searchLocation)
            }else{
                Toast.makeText(context, "No Location found! Please try again!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAddressDetails(currentLatLong: LatLng) {

        var geocoder = Geocoder(mainFragment.indexActivity, Locale.getDefault())
        addresses = geocoder.getFromLocation(currentLatLong.latitude, currentLatLong.longitude, 1)


        if(addresses!=null && addresses!!.isNotEmpty()) {
            val address: String = addresses!![0].getAddressLine(0) ?: ""
            customerCity = addresses!![0].locality ?: ""
            val state: String = addresses!![0].adminArea ?: ""
            val zip: String = addresses!![0].postalCode ?: ""
            val country: String = addresses!![0].countryName ?: ""

            Toast.makeText(
                context,
                "Address: $address, $customerCity, $state, $zip, $country",
                Toast.LENGTH_LONG
            ).show()
            showShop()
        }
    }


    private fun showShop() {
        if (addresses.isNullOrEmpty() && customerCity.isEmpty()) {
            Toast.makeText(
                context,
                "Can't Find Address! Please try again!",
                Toast.LENGTH_SHORT
            )
                .show()
            return
        }
        shopArrayList.clear()
        mainFragment.indexActivity.showLoadingDialog()

        Handler(Looper.getMainLooper()).postDelayed({
            mainFragment.indexActivity.dismissLoadingDialog()
            firebaseDatabaseReference.child("shop")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (postSnapshot in dataSnapshot.children) {
                                val shop = postSnapshot.getValue(Shop::class.java)

                                if (shop!=null) {
                                    val shopCity = getCity(shop.businessAddress!!) //if null -> n/a
                                    Log.d("SHOP_CITY ${shop.uid}", shopCity)
                                    Log.d("CUSTOMER_CITY", customerCity)
                                    if (shopCity==customerCity) {
                                        shopArrayList.add(shop)
                                    }
                                }
                            }

                            Log.d("SHOP", "shopArrayList.size -> ${shopArrayList.size}")
                            if (shopArrayList.size > 0) {
                                Log.d("SHOP", shopArrayList[0].toString())
                                showProceedDialog()
                            } else {
                                showRequestDialog()
                            }

                        } else {
                            Log.d("SHOP_ON_DATA_CHANGE", "NO SHOP YET AVAILABLE IN RECORD")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Getting Post failed, log a message
                        Log.w("addValueEventListener", "loadPost:onCancelled", error.toException())
                        Toast.makeText(
                            context,
                            "loadPost:onCancelled > ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }, 3000) // 3000 is the delayed time in milliseconds.
    }

    private fun showRequestDialog() {

        val dialogShopAvailabilityBinding =
            DialogShopAvailabilityBinding.inflate(this.layoutInflater)
        dialogShopAvailabilityBinding.btnRequest.setOnClickListener {
            firebaseDatabaseReference.child("request_address")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var address =
                            "${addresses!![0].countryName}_${addresses!![0].adminArea}_${addresses!![0].locality}"
                        if (!snapshot.hasChild(address)) {
                            val databaseRef = firebaseDatabase.reference.child("request_address")
                                .child(address)

                            address = addresses!![0].getAddressLine(0)

                            databaseRef.setValue(address).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    promptDialog.dismiss()
                                    showMessageDialog("REQUEST SENT!", "We will look for a laundry shop \nin your area soon. \nThank you!")
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Can't send request > ${task.exception}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    promptDialog.dismiss()
                                }
                            }

                        } else {
                            promptDialog.dismiss()
                            showMessageDialog(
                                "MESSAGE", "You're area has been already requested \n" +
                                        "and we're working on it right now. \n" +
                                        "Thank you!"
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("ERROR_SENDING_REQUEST", error.message)
                    }
                })
        }
        promptBuilder = AlertDialog.Builder(context)
        promptBuilder.setView(dialogShopAvailabilityBinding.root)
        promptDialog = promptBuilder.create()
        if (promptDialog.window!=null) {
            promptDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        promptDialog.show()
    }

    private fun showMessageDialog(title: String, message: String) {
        var promptDialog = Dialog(mainFragment.indexActivity)
        val promptBuilder = AlertDialog.Builder(context)
        promptBuilder.setTitle(title)
        promptBuilder.setMessage(message)
        promptBuilder.setNegativeButton("Okay") { _, _ ->
            promptDialog.dismiss()
        }
        promptDialog = promptBuilder.create()
        if (promptDialog.window!=null) {
            promptDialog.window!!.setBackgroundDrawableResource(R.color.color_light_3)
        }
        promptDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showProceedDialog() {

        val dialogShopAvailabilityBinding =
            DialogShopAvailabilityBinding.inflate(this.layoutInflater)
        dialogShopAvailabilityBinding.apply {
            heading.text = "AVAILABLE"
            subHeading.text = "We have available Laundry Shop partner in your area!"
            btnRequest.text = "BOOK NOW!"
        }
        dialogShopAvailabilityBinding.btnRequest.setOnClickListener {
            mainFragment.indexActivity.showLogin()
            promptDialog.dismiss()
        }
        promptBuilder = AlertDialog.Builder(context)
        promptBuilder.setView(dialogShopAvailabilityBinding.root)
        promptBuilder.create()
        promptDialog = promptBuilder.create()
        if (promptDialog.window!=null) {
            promptDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        promptDialog.show()
    }

    private fun getCity(address: String): String {
        var addresses: List<Address>? = null
        var locality: String? = null

        if (address.isNotEmpty()) {
            var geocoder = Geocoder(binding.root.context)
            try {
                addresses = geocoder.getFromLocationName(address, 1)
            } catch (e: Exception) {
                Log.d("SEARCH_GEO_LOCATION", "${e.message}")
            }

            if (addresses!=null && addresses.isNotEmpty()) {
                locality = addresses[0].locality
            } else {
                Log.d("CITY AVAILABILITY", "NO AVAILABLE FROM SELECTED CITY")
            }
        }
        return locality ?: "n/a"
    }

}