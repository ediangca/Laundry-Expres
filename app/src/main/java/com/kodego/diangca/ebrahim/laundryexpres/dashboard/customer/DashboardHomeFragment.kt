package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogSchedulePickerBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class DashboardHomeFragment(var dashboardCustomer: DashboardCustomerActivity) : Fragment() {

    private var _binding: FragmentDashboardHomeBinding? = null
    private val binding get() = _binding!!

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var displayName: String? = null

    private var selectedService: String? = null


    private lateinit var schedulePickerBuilder: AlertDialog.Builder
    private lateinit var schedulePickerDialogInterface: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardHomeBinding.inflate(layoutInflater, container, false)
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

        displayUserName()

        binding.btnServiceRegular.setOnClickListener {
            btnServiceRegularOnClickListener()
        }
        binding.btnServicePets.setOnClickListener {
            btnServicePetsOnClickListener()
        }
        binding.btnServiceDryClean.setOnClickListener {
            btnServiceDryCleanOnClickListener()
        }
        binding.btnServiceSneakers.setOnClickListener {
            btnServiceSneakersOnClickListener()
        }
        binding.editPickupLayout.setOnClickListener {
            setSchedule(binding.editPickup, "Pick-Up")
        }
        binding.editPickup.setOnClickListener {
            setSchedule(it, "Pick-Up")
        }
        binding.editDeliveryLayout.setOnClickListener {
            setSchedule( binding.editDelivery, "Delivery")
        }
        binding.editDelivery.setOnClickListener {
            setSchedule(it, "Delivery")
        }
        binding.btnBook.setOnClickListener {
            btnBookOnClickListener()
        }

    }

    private fun btnBookOnClickListener() {
        val pickUp = binding.editPickup.text.toString()
        val delivery = binding.editDelivery.text.toString()
        if(pickUp.isEmpty() || delivery.isEmpty()){

            if(pickUp.isEmpty()){
                binding.editPickup.error = "Please select pick-up schedule."
            }
            if(delivery.isEmpty()){
                binding.editDelivery.error = "Please select delivery schedule."
            }

            val builder = AlertDialog.Builder(dashboardCustomer)
            builder.setTitle("UNVERIFIED ACCOUNT")
            builder.setMessage("Please wait for your verification. We will notify you within 24-72hours. Thank you!")
            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            }
            return
        }
    }

    private fun btnServiceRegularOnClickListener() {
        setService("Regular Wash")
    }

    private fun btnServiceSneakersOnClickListener() {
        setService("Sneaker Wash")
    }

    private fun btnServiceDryCleanOnClickListener() {
        setService("Dry Clean")
    }

    private fun btnServicePetsOnClickListener() {
        setService("Pets Wash")
    }
    private fun setService(service: String) {
        this.selectedService = service
        binding.selectedService.text = selectedService
    }

    @SuppressLint("SetTextI18n")
    private fun setSchedule(view: View, type: String) {
        val textInputEditText: TextInputEditText = view as TextInputEditText
        val schedulePickerBinding = DialogSchedulePickerBinding.inflate(this.layoutInflater)

        var cal = Calendar.getInstance()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            with(schedulePickerBinding){
                dialogTitle.text = "$type Schedule"
                btnSelect.setOnClickListener {
                    calendarOnDateChangedListener(textInputEditText, calendar.year, calendar.month, calendar.dayOfMonth)
                    schedulePickerDialogInterface.dismiss()
                }
                btnCancel.setOnClickListener {
                    schedulePickerDialogInterface.dismiss()
                }
            }
            schedulePickerBuilder = AlertDialog.Builder(dashboardCustomer)
            schedulePickerBuilder.setCancelable(false)
            schedulePickerBuilder.setView(schedulePickerBinding.root)
            schedulePickerDialogInterface = schedulePickerBuilder.create()
            schedulePickerDialogInterface.show()
        }else{
            val dateSetListener = object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                       dayOfMonth: Int) {
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val myFormat = "MM/dd/yyyy" // mention the format you need
                    val sdf = SimpleDateFormat(myFormat, Locale.US)
                    textInputEditText.setText(sdf.format(cal.time))
                }
            }
            DatePickerDialog(dashboardCustomer,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun calendarOnDateChangedListener(
        textInputEditText: TextInputEditText,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int
    ) {
        textInputEditText.setText("$monthOfYear/$dayOfMonth/$year")
    }

    private fun btnDeliveryOnClickListener() {

    }


    private fun displayUserName() {
        firebaseAuth.currentUser?.let {
            for (profile in it.providerData){
                displayName = profile.displayName
            }
        }

        if(!displayName.isNullOrEmpty()){
            Log.d("displayUserName", "Hi ${displayName}, Good Day!")
            binding.titleView.text = "Hi ${displayName}, Good Day!"
        }else {
            firebaseDatabaseReference.child("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                            val firstname = snapshot.child(firebaseAuth.currentUser!!.uid)
                                .child("firstname").value.toString()
                            binding.titleView.text = "Hi $firstname, Good Day!"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            dashboardCustomer,
                            "${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

}