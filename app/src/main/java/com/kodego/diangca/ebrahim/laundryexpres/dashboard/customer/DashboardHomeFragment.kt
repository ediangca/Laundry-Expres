package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogSchedulePickerBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardHomeBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import com.squareup.picasso.Picasso
import java.io.File
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

    private var user: User? = null
    private var displayName: String? = null
    private var profileImageUri: Uri? = null

    private var selectedService: String? = null

    private lateinit var schedulePickerBuilder: AlertDialog.Builder
    private lateinit var schedulePickerDialogInterface: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardHomeBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }


    override fun onResume() {
        super.onResume()

        user = dashboardCustomer.getUser()
        val bundle = this.arguments
        if (bundle != null) {
            user = bundle.getParcelable<User>("user")!!
            Log.d("ON_RESUME_FETCH_USER", user.toString())
        }
        if (user != null) {
            setUserDetails(user!!)
        }
    }

    private fun initComponent() {

        user = dashboardCustomer.getUser()
        val bundle = this.arguments
        if (bundle != null) {
            user = bundle.getParcelable<User>("user")!!
            Log.d("ON_RESUME_FETCH_USER", user.toString())
        }
        if (user != null) {
            setUserDetails(user!!)
        }

        binding.btnLaundryShop.setOnClickListener {
            btnLaundryShopOnClickListener()
        }
        binding.editPickup.setOnClickListener {
            setSchedule(it, "Pick-Up")
        }
        binding.editPickup.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                setSchedule(binding.editPickup, "Pick-Up")
            }
        }
        binding.editDelivery.setOnClickListener {
            setSchedule(it, "Delivery")
        }
        binding.editDelivery.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                setSchedule(binding.editDelivery, "Delivery")
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun setUserDetails(user: User) {
        dashboardCustomer.showLoadingDialog()
        firebaseAuth.currentUser?.let {
            for (profile in it.providerData) {
                displayName = profile.displayName
                profileImageUri = profile.photoUrl
            }

            binding.apply {

                val profileView: ImageView = binding.profilePic

                if (!displayName.isNullOrEmpty()) {
                    Log.d("displayUserName", "Hi ${displayName}, Good Day!")
                    userDisplayName.text = displayName
                }

                if (profileImageUri != null) {
                    Log.d("profilePic_profileData", "$profileImageUri")
                    Picasso.with(context).load(profileImageUri)
                        .into(profileView);
                } else {
                    if (user!!.photoUri != null) {
                        val filename = "profile_${user!!.uid}"
                        profileImageUri = Uri.parse(user!!.photoUri)
                        val firebaseStorageReference =
                            FirebaseStorage.getInstance().reference.child("profile/$filename")
                        Log.d("PROFILE_FILENAME", filename)
                        Log.d("PROFILE_URI", profileImageUri!!.toString())
                        val localFile = File.createTempFile("temp_profile", ".jpg")
                        firebaseStorageReference.getFile(localFile)
                            .addOnSuccessListener {
                                profileView.setImageBitmap(BitmapFactory.decodeFile(localFile.absolutePath))
                                Log.d(
                                    "USER_PROFILE_PIC",
                                    "User Profile has been successfully load!"
                                )
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "User Profile failed to load!> ${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("USER_PROFILE_PIC", "User Profile failed to load!")
                            }
                        Log.d("profilePic_user", "$profileImageUri")
                        Picasso.with(context).load(profileImageUri)
                            .into(profileView);
                    }
                }

                if (user != null) {
                    Log.d("displayUserName", "Hi ${user.firstname} ${user.lastname}, Good Day!")
                    userDisplayName.text = "Hi ${user.firstname} ${user.lastname}, Good Day!"
                }
            }
            dashboardCustomer.dismissLoadingDialog()
        }

    }

    private fun btnLaundryShopOnClickListener() {
        val pickUp = binding.editPickup.text.toString()
        val delivery = binding.editDelivery.text.toString()
        if (pickUp.isEmpty() || delivery.isEmpty()) {

            if (pickUp.isEmpty()) {
                binding.editPickup.error = "Please select pick-up schedule."
            }
            if (delivery.isEmpty()) {
                binding.editDelivery.error = "Please select delivery schedule."
            }
            return
        }
        dashboardCustomer.showShopList(
            binding.editPickup.text.toString(),
            binding.editDelivery.text.toString()
        )
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

        var time: String? = null
        var h = schedulePickerBinding.timePicker.hour
        var m = schedulePickerBinding.timePicker.minute
        var am_pm = ""

        when {
            h == 0 -> {
                h += 12
                am_pm = "AM"
            }

            h == 12 -> am_pm = "PM"
            h > 12 -> {
                h -= 12
                am_pm = "PM"
            }

            else -> am_pm = "AM"
        }
        val hour = if (h < 10) "0$h" else h
        val min = if (m < 10) "0$m" else m
        // display format of time
        time = "$hour:$min $am_pm"

        schedulePickerBinding.timePicker.setOnTimeChangedListener { _, hour, minute ->
            var h = hour
            // AM_PM decider logic
            when {
                h == 0 -> {
                    h += 12
                    am_pm = "AM"
                }

                h == 12 -> am_pm = "PM"
                h > 12 -> {
                    h -= 12
                    am_pm = "PM"
                }

                else -> am_pm = "AM"
            }
            val hour = if (h < 10) "0$h" else h
            val min = if (minute < 10) "0$minute" else minute
            // display format of time
            time = "$hour:$min $am_pm"
        }

        when (type) {
            "Pick-Up" -> {
                binding.editDelivery.text = null
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            with(schedulePickerBinding) {
                dateTitle.text = "$type Datetime Schedule"
                // set maximum date to be selected as today
//                calendar.maxDate = cal.timeInMillis
                calendar.minDate = cal.timeInMillis
                timePicker.setIs24HourView(false)
                btnSelect.setOnClickListener {
                    schedulePickerDialogInterface.dismiss()
                    calendarOnDateChangedListener(
                        textInputEditText,
                        calendar.year,
                        calendar.month,
                        calendar.dayOfMonth, time!!
                    )
                }
                btnCancel.setOnClickListener {
                    schedulePickerDialogInterface.dismiss()
                }
            }
            schedulePickerBuilder = AlertDialog.Builder(dashboardCustomer)
            schedulePickerBuilder.setCancelable(true)
            schedulePickerBuilder.setView(schedulePickerBinding.root)
            schedulePickerDialogInterface = schedulePickerBuilder.create()
            schedulePickerDialogInterface.show()
        } else {
            val dateSetListener = object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(
                    view: DatePicker, year: Int, monthOfYear: Int,
                    dayOfMonth: Int,
                ) {
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//                    val myFormat = "MM/dd/yyyy" // mention the format you need
//                    val sdf = SimpleDateFormat(myFormat, Locale.US)
//                    textInputEditText.setText(sdf.format(cal.time))
                    val displayMonth = monthOfYear + 1 // Convert to one-based
                    val formattedDate = "$displayMonth/$dayOfMonth/$year"
                    textInputEditText.setText(formattedDate)
                }
            }
            DatePickerDialog(
                dashboardCustomer,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }


    }

    @SuppressLint("SetTextI18n")
    private fun calendarOnDateChangedListener(
        textInputEditText: TextInputEditText,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int,
        time: String,
    ) {
        Log.d("DATE_PICKER", "Year: $year, Month: $monthOfYear, Day: $dayOfMonth")
//        textInputEditText.setText("$monthOfYear/$dayOfMonth/$year $time")
        val displayMonth = monthOfYear + 1 // Convert to one-basedd
        textInputEditText.setText("$displayMonth/$dayOfMonth/$year $time")
    }


}