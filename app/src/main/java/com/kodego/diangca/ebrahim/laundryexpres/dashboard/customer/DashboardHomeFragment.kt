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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.adater.OrderAdapter
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogSchedulePickerBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardHomeBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Order
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

    private var status =
        arrayOf(
            "ALL",
            "PENDING", //AFTER BOOKING
            "FOR PICK-UP", // AFTER ACCEPT BY LAUNDRY
            "TO PICK-UP", // AFTER ACCEPT BY RIDE
            "IN TRANSIT", // AFTER PICK-UP TRANSIT LAUNDRY TO SHOP
            "ON PROCESS", // LAUNDRY ACCEPTED BY SHOP
            "FOR DELIVERY", // TO RELEASE LAUNDRY BY SHOP
            "TO DELIVER", // AFTER PICK-UP RIDER FROM SHOP
            "COMPLETE", // RECEIVE FROM CUSTOMER
            "CANCEL" // CANCEL BY CUSTOMER
        )
    private var uid: String? = null

    private lateinit var schedulePickerBuilder: AlertDialog.Builder
    private lateinit var schedulePickerDialogInterface: Dialog

    val formatter = SimpleDateFormat("M/d/yyyy hh:mm a", Locale.ENGLISH)
    val currentDate = System.currentTimeMillis()

    private var ordersList: ArrayList<Order> = ArrayList()
    private var orderAdapter = OrderAdapter(dashboardCustomer, ordersList)

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
//        user = dashboardCustomer.getUser()
//        val bundle = this.arguments
//        if (bundle != null) {
//            user = bundle.getParcelable<User>("user")!!
//            Log.d("ON_RESUME_FETCH_USER", user.toString())
//        }
//        if (user != null) {
//            setUserDetails(user!!)
//        }

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
            uid = firebaseAuth.currentUser!!.uid

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
                                    "Please Update your Profile Picture",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("USER_PROFILE_PIC", "User Profile failed to load!")
                            }
//                        Log.d("profilePic_user", "$profileImageUri")
//                        Picasso.with(context).load(profileImageUri)
//                            .into(profileView);
                    }
                }

                if (user != null) {
                    Log.d("displayUserName", "Hi ${user.firstname} ${user.lastname}, Good Day!")
                    userDisplayName.text = "Hi ${user.firstname} ${user.lastname}, Good Day!"
                }
            }

            orderAdapter = OrderAdapter(dashboardCustomer, ordersList)
//        orderAdapter.setDashboardCustomer(dashboardCustomer)
            orderAdapter.setCallBack("Order")
            binding.orderList.layoutManager = LinearLayoutManager(dashboardCustomer)
            binding.orderList.adapter = orderAdapter


            showOrders("ALL")

            dashboardCustomer.dismissLoadingDialog()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showOrders(status: String) {
//        dashboardCustomer.showLoadingDialog()
        Log.d("SHOW ORDER STATUS", status)

        // Fetch orders for the user
        firebaseDatabaseReference.child("orders/$uid")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    ordersList.clear()
                    if (!dataSnapshot.exists()) {
                        dashboardCustomer.dismissLoadingDialog()
                        updateUI()
                        return
                    }

                    // Loop through orders
                    val ordersToAdd = mutableListOf<Order>()
                    val children = dataSnapshot.children.toList()
                    children.forEachIndexed { index, postSnapshot ->
                        val order = postSnapshot.getValue(Order::class.java)


                        if (order != null) {

                            val pickupDatetime = order!!.pickUpDatetime ?: ""

                            val localDatePickup: Date? = formatter.parse(pickupDatetime)
                            val pickupTimestamp = localDatePickup?.time

                            updateExpiredRequest(order.orderNo, order.status, pickupTimestamp)

//                            checkShopRates(order)
                            when (status) {
                                // If "All" is selected, disregard status and add all orders for this shop
                                "ALL" -> {
                                    ordersToAdd.add(order)
                                }
                                // Otherwise, filter based on status
                                else -> {
                                    if (order.status.equals(status, true)) {
                                        ordersToAdd.add(order)
                                    }
                                }
                            }
                        }

                        // Check if it's the last item
                        if (index == children.lastIndex) {
                            orderAdapter.hideSchedule(true)
                            ordersList.addAll(ordersToAdd)
                            orderAdapter.notifyDataSetChanged()
                            sortAndNotify(status)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("addValueEventListener", "loadPost:onCancelled", error.toException())
                    dashboardCustomer.dismissLoadingDialog()
                    binding.promptView.visibility = View.VISIBLE
                    Toast.makeText(dashboardCustomer, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateExpiredRequest(
        orderNo: String?,
        status: String?,
        pickupTimestamp: Long?
    ) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("orders/$uid")

        // Update the status when pickup is expired (currentDate is greater than pickup request)
        if (orderNo != null && status in setOf("PENDING", "FOR PICK-UP", "TO PICK-UP")) {
            if(isExpired(pickupTimestamp!!) ){
                databaseReference.child(orderNo).child("status").setValue("EXPIRED")
            }else{
                databaseReference.child(orderNo).child("status").setValue(status)
            }
        }


    }
    private fun isExpired(pickUpDate: Long): Boolean {
        val currentDate = Calendar.getInstance()
        val pickUpCalendar = Calendar.getInstance().apply { timeInMillis = pickUpDate }

        // Check if pickUpDate is BEFORE today's date
        return pickUpCalendar.get(Calendar.YEAR) < currentDate.get(Calendar.YEAR) ||
                pickUpCalendar.get(Calendar.DAY_OF_YEAR) < currentDate.get(Calendar.DAY_OF_YEAR)
    }

    // Update UI on empty ordersList
    private fun updateUI() {
        if (ordersList.isEmpty()) {
            binding.promptView.visibility = View.VISIBLE
        } else {
            binding.promptView.visibility = View.GONE
            orderAdapter.notifyDataSetChanged()
        }
    }


    // Sort and notify adapter
    @SuppressLint("SetTextI18n")
    private fun sortAndNotify(status: String) {
        with(binding) {
            when (status) {
                "ALL" -> promptView.text = "No Booking yet!"
                else -> promptView.text = "No $status Booking"
            }
            if (ordersList.isEmpty()) {
                promptView.visibility = View.VISIBLE
            } else {
                // Sort ordersList by descending pickUpDatetime
                val sortedOrders =
                    ordersList.sortedByDescending { parseDatetime(it.pickUpDatetime) }
                        .take(5)

                // Update the adapter's dataset instead of modifying ordersList directly
                orderAdapter.updateList(ArrayList(sortedOrders))


                promptView.visibility = View.GONE
                orderAdapter.notifyDataSetChanged()
            }
        }
    }

    // Helper to parse datetime string
    private fun parseDatetime(datetime: String?): Long? {
        return try {
            datetime?.let {
                SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault()).parse(it)?.time
            }
        } catch (e: Exception) {
            Log.e("DATETIME_PARSE", "Error parsing date: $datetime", e)
            null
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