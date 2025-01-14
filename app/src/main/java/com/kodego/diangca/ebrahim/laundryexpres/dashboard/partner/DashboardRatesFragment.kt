package com.kodego.diangca.ebrahim.laundryexpres.dashboard.partner

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardPartnerRatesBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Rates
import java.text.SimpleDateFormat
import java.util.*


class DashboardRatesFragment(var dashboardPartner: DashboardPartnerActivity) : Fragment() {

    private var _binding: FragmentDashboardPartnerRatesBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseStorageRef: StorageReference
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")


    private var rates: Rates? = null

    private var uid: String? = null
    private var regular: Boolean? = null
    private var pets: Boolean? = null
    private var dry: Boolean? = null
    private var sneakers: Boolean? = null
    private var ratesUnit: String? = null
    private var regularWhiteMaxKg: Int? = null
    private var regularWhiteRate: Double? = null
    private var regularColorMaxKg: Int? = null
    private var regularColorRate: Double? = null
    private var regularComforterRate: Double? = null
    private var regularOthersMaxKg: Int? = null
    private var regularOthersRate: Double? = null
    private var petsWhiteMaxKg: Int? = null
    private var petsWhiteRate: Double? = null
    private var petsColorMaxKg: Int? = null
    private var petsColorRate: Double? = null
    private var dryWhiteMaxKg: Int? = null
    private var dryWhiteRate: Double? = null
    private var dryColorMaxKg: Int? = null
    private var dryColorRate: Double? = null
    private var sneakerOrdinaryRate: Double? = null
    private var sneakerBootsRate: Double? = null
    private var datetimeCreated: String? = null
    private var datetimeUpdated: String? = null
    private var pickUpFee: Double? = null
    private var deliveryFee: Double? = null

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardPartnerRatesBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }

    private fun initComponent() {

        val bundle = arguments
        if (bundle!=null) {
            rates = bundle.getParcelable<Rates>("rates")!!
            Log.d("ON_RESUME_FETCH_USER", rates.toString())
        } else {
            rates = dashboardPartner.getRates()
        }
        retrieveRates(rates)

        binding.apply {
            btnServiceRegular.setOnClickListener {
                btnServiceRegularOnClickListener()
            }
            btnServicePets.setOnClickListener {
                btnServicePetsOnClickListener()
            }
            btnServiceDryClean.setOnClickListener {
                btnServiceDryCleanOnClickListener()
            }
            btnServiceSneakers.setOnClickListener {
                btnServiceSneakersOnClickListener()
            }
            btnRatesSubmit.setOnClickListener {
                btnRatesSubmitOnClickListener()
            }
        }
    }

    private fun btnServiceRegularOnClickListener() {
        setVisibility(binding.linear2, binding.btnServiceRegular)
    }

    private fun btnServicePetsOnClickListener() {
        setVisibility(binding.linear3, binding.btnServicePets)
    }

    private fun btnServiceDryCleanOnClickListener() {
        setVisibility(binding.linear4, binding.btnServiceDryClean)
    }

    private fun btnServiceSneakersOnClickListener() {
        setVisibility(binding.linear5, binding.btnServiceSneakers)
    }

    private fun setVisibility(linear: LinearLayoutCompat, view: View) {

        val button = view as AppCompatButton

        if (linear.visibility==View.GONE) {
            binding.textView.error = null
            linear.visibility = View.VISIBLE
            button.background =
                ContextCompat.getDrawable(dashboardPartner, R.drawable.button_secondary)
        } else {
            linear.visibility = View.GONE
            button.background = ContextCompat.getDrawable(dashboardPartner, R.drawable.button_white)
        }
    }


    @SuppressLint("SimpleDateFormat")
    private fun getDataFromFields() {
        uid = dashboardPartner.getUser()!!.uid

        binding.apply {
            regular = linear2.visibility==View.VISIBLE
            pets = linear3.visibility==View.VISIBLE
            dry = linear4.visibility==View.VISIBLE
            sneakers = linear5.visibility==View.VISIBLE
        }
        ratesUnit = "Php"

        regularWhiteMaxKg = zeroIfEmpty(binding.regularWhiteKg.text.toString()).toInt()
        Log.d("regularWhiteMaxKg", "$regularColorMaxKg")
        regularWhiteRate = zeroIfEmpty(binding.regularWhiteRate.text.toString()).toDouble()
        regularColorMaxKg = zeroIfEmpty(binding.regularColorKg.text.toString()).toInt()
        regularColorRate = zeroIfEmpty(binding.regularColorRate.text.toString()).toDouble()
        regularComforterRate = zeroIfEmpty(binding.regularComforterRate.text.toString()).toDouble()
        regularOthersMaxKg = zeroIfEmpty(binding.regularOthersKg.text.toString()).toInt()
        regularOthersRate = zeroIfEmpty(binding.regularOthersRate.text.toString()).toDouble()

        petsWhiteMaxKg = zeroIfEmpty(binding.petsWhiteKg.text.toString()).toInt()
        petsWhiteRate = zeroIfEmpty(binding.petsWhiteRate.text.toString()).toDouble()
        petsColorMaxKg = zeroIfEmpty(binding.petsColorKg.text.toString()).toInt()
        petsColorRate = zeroIfEmpty(binding.petsColorRate.text.toString()).toDouble()

        dryWhiteMaxKg = zeroIfEmpty(binding.dryWhiteKg.text.toString()).toInt()
        dryWhiteRate = zeroIfEmpty(binding.dryWhiteRate.text.toString()).toDouble()
        dryColorMaxKg = zeroIfEmpty(binding.dryColorKg.text.toString()).toInt()
        dryColorRate = zeroIfEmpty(binding.dryColorRate.text.toString()).toDouble()

        sneakerOrdinaryRate = zeroIfEmpty(binding.sneakersOrdinaryRate.text.toString()).toDouble()
        sneakerBootsRate = zeroIfEmpty(binding.sneakersBootsRate.text.toString()).toDouble()

        pickUpFee = zeroIfEmpty(binding.pickUpRate.text.toString()).toDouble()
        deliveryFee = zeroIfEmpty(binding.deliveryRate.text.toString()).toDouble()

        if (rates==null) {
            datetimeCreated = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date())
        }
        datetimeUpdated = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date())
    }

    private fun zeroIfEmpty(text: String): String {
        return if (text.isEmpty()) {
            "0"
        } else {
            text
        }
    }

    private fun btnRatesSubmitOnClickListener() {
        dashboardPartner.showLoadingDialog()
        getDataFromFields()
        if (errorFields()) {
            dashboardPartner.dismissLoadingDialog()
            Toast.makeText(
                context,
                "Please check error field(s)!",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            saveRates()
        }
    }

    private fun errorFields(): Boolean {
        var invalid = false

        if (!regular!! && !pets!! && !dry!! && !sneakers!!) {
            invalid = true
            Toast.makeText(
                context,
                "You must have at least 1 service to be saved.",
                Toast.LENGTH_LONG
            ).show()
            binding.textView.error = "Select at least 1 service."
        } else {
            binding.apply {
                if (regular!!) {
                    if (regularWhiteKg.text!!.isEmpty()) {
                        regularWhiteKg.error = "Please enter value."
                        invalid = true
                    }
                    if (regularWhiteRate.text!!.isEmpty()) {
                        regularWhiteRate.error = "Please enter value."
                        invalid = true
                    }
                    if (regularColorKg.text!!.isEmpty()) {
                        regularColorKg.error = "Please enter value."
                        invalid = true
                    }
                    if (regularWhiteRate.text!!.isEmpty()) {
                        regularWhiteRate.error = "Please enter value."
                        invalid = true
                    }
                    if (regularComforterRate.text!!.isEmpty()) {
                        regularComforterRate.error = "Please enter value."
                        invalid = true
                    }
                    if (regularOthersKg.text!!.isEmpty()) {
                        regularOthersKg.error = "Please enter value."
                        invalid = true
                    }
                    if (regularOthersRate.text!!.isEmpty()) {
                        regularOthersRate.error = "Please enter value."
                        invalid = true
                    }
                }
                if (pets!!) {
                    if (petsWhiteKg.text!!.isEmpty()) {
                        petsWhiteKg.error = "Please enter value."
                        invalid = true
                    }
                    if (petsWhiteRate.text!!.isEmpty()) {
                        petsWhiteRate.error = "Please enter value."
                        invalid = true
                    }
                    if (petsColorKg.text!!.isEmpty()) {
                        petsColorKg.error = "Please enter value."
                        invalid = true
                    }
                    if (petsColorRate.text!!.isEmpty()) {
                        petsColorRate.error = "Please enter value."
                        invalid = true
                    }
                }
                if (dry!!) {
                    if (dryWhiteKg.text!!.isEmpty()) {
                        dryWhiteKg.error = "Please enter value."
                        invalid = true
                    }
                    if (dryWhiteRate.text!!.isEmpty()) {
                        dryWhiteRate.error = "Please enter value."
                        invalid = true
                    }
                    if (dryColorKg.text!!.isEmpty()) {
                        dryColorKg.error = "Please enter value."
                        invalid = true
                    }
                    if (dryColorRate.text!!.isEmpty()) {
                        dryColorRate.error = "Please enter value."
                        invalid = true
                    }
                }
                if (sneakers!!) {
                    if (sneakersOrdinaryRate.text!!.isEmpty()) {
                        sneakersOrdinaryRate.error = "Please enter value."
                        invalid = true
                    }
                    if (sneakersBootsRate.text!!.isEmpty()) {
                        sneakersBootsRate.error = "Please enter value."
                        invalid = true
                    }
                }
                if (pickUpRate.text!!.isEmpty()) {
                    pickUpRate.error = "Please enter value."
                    invalid = true
                }
                if (deliveryRate.text!!.isEmpty()) {
                    deliveryRate.error = "Please enter value."
                    invalid = true
                }
            }
        }

        return invalid
    }

    private fun saveRates() {

        rates = Rates(
            uid,
            regular,
            pets,
            dry,
            sneakers,
            ratesUnit,
            regularWhiteMaxKg,
            regularWhiteRate,
            regularColorMaxKg,
            regularColorRate,
            regularComforterRate,
            regularOthersMaxKg,
            regularOthersRate,
            petsWhiteMaxKg,
            petsWhiteRate,
            petsColorMaxKg,
            petsColorRate,
            dryWhiteMaxKg,
            dryWhiteRate,
            dryColorMaxKg,
            dryColorRate,
            sneakerOrdinaryRate,
            sneakerBootsRate,
            pickUpFee,
            deliveryFee,
            datetimeCreated,
            datetimeUpdated
        )
        val databaseRef = firebaseDatabase.reference.child("rates")
            .child(firebaseAuth.currentUser!!.uid)
        databaseRef.setValue(rates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                dashboardPartner.dismissLoadingDialog()
                if(rates!=null){
                    Toast.makeText(context, "Services Rates has been success updated!", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(context, "Services Rates has been success saved!", Toast.LENGTH_LONG).show()
                }
                dashboardPartner.setRates(rates!!)
                clearSelectedServices()
                retrieveRates(rates)
            } else {
                dashboardPartner.dismissLoadingDialog()
                Log.d("SAVE_UPDATE_RATES -> addOnCompleteListener", task.exception!!.message!!)
            }

        }
    }

    private fun clearSelectedServices() {
        binding.apply {
            linear2.visibility = View.GONE
            linear3.visibility = View.GONE
            linear4.visibility = View.GONE
            linear5.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun retrieveRates(rates: Rates?) {
        binding.apply {
            if (rates!=null) {
                btnRatesSubmit.text = "UPDATE"
                btnRatesSubmit.visibility = View.VISIBLE
                if (rates.regular!!) {
                    Log.d("SERVICE_ACTIVATE", "REGULAR")
                    regular = true
                    setVisibility(linear2, btnServiceRegular)
                }
                if (rates.pets!!) {
                    Log.d("SERVICE_ACTIVATE", "PETS")
                    pets = true
                    setVisibility(linear3, btnServicePets)
                }
                if (rates.dry!!) {
                    Log.d("SERVICE_ACTIVATE", "DRY CLEAN")
                    dry = true
                    setVisibility(linear4, btnServiceDryClean)
                }
                if (rates.sneakers!!) {
                    Log.d("SERVICE_ACTIVATE", "SNEAKERS")
                    sneakers = true
                    setVisibility(linear5, btnServiceSneakers)
                }

                if(rates.regularWhiteMaxKg!! <= 0){
                    binding.regularWhiteKg.text = null
                }else{
                    binding.regularWhiteKg.setText("${rates.regularWhiteMaxKg}")
                }

                setNullIfLessThanEqualZero("${rates.regularWhiteMaxKg}", binding.regularWhiteKg)
                setNullIfLessThanEqualZero("${rates.regularWhiteRate}",binding.regularWhiteRate)
                setNullIfLessThanEqualZero("${rates.regularColorMaxKg}", binding.regularColorKg)
                setNullIfLessThanEqualZero("${rates.regularColorRate}", binding.regularColorRate)
                setNullIfLessThanEqualZero("${rates.regularComforterRate}", binding.regularComforterRate)
                setNullIfLessThanEqualZero("${rates.regularOthersMaxKg}", binding.regularOthersKg)
                setNullIfLessThanEqualZero("${rates.regularOthersRate}", binding.regularOthersRate)

                setNullIfLessThanEqualZero("${rates.petsWhiteMaxKg}", binding.petsWhiteKg)
                setNullIfLessThanEqualZero("${rates.petsWhiteRate}", binding.petsWhiteRate)
                setNullIfLessThanEqualZero("${rates.petsColorMaxKg}", binding.petsColorKg)
                setNullIfLessThanEqualZero("${rates.petsColorRate}", binding.petsColorRate)

                setNullIfLessThanEqualZero("${rates.dryWhiteMaxKg}", binding.dryWhiteKg)
                setNullIfLessThanEqualZero("${rates.dryWhiteRate}", binding.dryWhiteRate)
                setNullIfLessThanEqualZero("${rates.dryColorMaxKg}", binding.dryColorKg)
                setNullIfLessThanEqualZero("${rates.dryColorRate}", binding.dryColorRate)

                setNullIfLessThanEqualZero("${rates.sneakerOrdinaryRate}", binding.sneakersOrdinaryRate)
                setNullIfLessThanEqualZero("${rates.sneakerBootsRate}", binding.sneakersBootsRate)

                setNullIfLessThanEqualZero("${rates.pickUpFee}", binding.pickUpRate)
                setNullIfLessThanEqualZero("${rates.deliveryFee}", binding.deliveryRate)

                datetimeCreated = rates.datetimeCreated

            }
        }

    }

    private fun setNullIfLessThanEqualZero(value: String, textInputEditText: TextInputEditText) {
        if(value.toDouble() <= 0){
            textInputEditText.text = null
        }else{
            textInputEditText.setText(value)
        }

    }

}