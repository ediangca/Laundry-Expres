package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogAgreementBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardOrderFormBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Notification
import com.kodego.diangca.ebrahim.laundryexpres.model.Order
import com.kodego.diangca.ebrahim.laundryexpres.model.Rates
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("SimpleDateFormat")
class DashboardOrderFormFragment(var dashboardCustomer: DashboardCustomerActivity) : Fragment() {

    private var _binding: FragmentDashboardOrderFormBinding? = null
    private val binding get() = _binding!!

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()


    private var dataOrder: List<DataSnapshot>? = null
    private lateinit var shop: Shop
    private var rates: Rates? = null
    private var order: Order? = null

    private var orderNo: String? = null
    private var uid: String? = null
    private var shopID: String? = null
    private var regular: Boolean? = false
    private var pets: Boolean? = false
    private var dry: Boolean? = false
    private var sneaker: Boolean? = false
    private var ratesUnit: String? = "PHP"
    private var regularWhiteMaxKg: Int? = null
    private var regularWhiteLoad: Int? = null
    private var regularWhiteRate: Double? = null
    private var regularColorMaxKg: Int? = null
    private var regularColorLoad: Int? = null
    private var regularColorRate: Double? = null
    private var regularComforterMaxKg: Int? = null
    private var regularComforterRate: Double? = null
    private var regularOthersMaxKg: Int? = null
    private var regularOthersLoad: Int? = null
    private var regularOthersRate: Double? = null
    private var petsWhiteMaxKg: Int? = null
    private var petsWhiteLoad: Int? = null
    private var petsWhiteRate: Double? = null
    private var petsColorMaxKg: Int? = null
    private var petsColorLoad: Int? = null
    private var petsColorRate: Double? = null
    private var dryWhiteMaxKg: Int? = null
    private var dryWhiteLoad: Int? = null
    private var dryWhiteRate: Double? = null
    private var dryColorMaxKg: Int? = null
    private var dryColorLoad: Int? = null
    private var dryColorRate: Double? = null
    private var sneakerOrdinaryMaxKg: Int? = null
    private var sneakerOrdinaryRate: Double? = null
    private var sneakerBootsMaxKg: Int? = null
    private var sneakerBootsRate: Double? = null
    private var totalLaundryPrice: Double? = 0.00
    private var pickUpFee: Double? = null
    private var deliveryFee: Double? = null
    private var totalOrder: Double? = null
    private var status: String? = "PENDING"
    private var notes: String? = null
    private var pickUpDatetime: String? = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date())
    private var deliveryDatetime: String? = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardOrderFormBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }

    private fun initComponent() {

        uid = firebaseAuth.currentUser!!.uid
        val bundle = this.arguments
        if (bundle != null) {
            shop = bundle.getParcelable<Shop>("shop")!!
            pickUpDatetime = bundle.getString("pickUpDatetime")
            deliveryDatetime = bundle.getString("deliveryDatetime")
            Log.d("FETCH_SHOP", shop.toString())
            retrieveShopServices()
            setOrderNo()
        }

        binding.apply {
            btnBack.setOnClickListener {
                dashboardCustomer.resumeShopList()
            }
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

//            REGULAR LOAD
            minus1.setOnClickListener {
                minusLoad(load1, regularLabel1)
            }
            plus1.setOnClickListener {
                plusLoad(load1, regularLabel1)
            }
            minus2.setOnClickListener {
                minusLoad(load2, regularLabel2)
            }
            plus2.setOnClickListener {
                plusLoad(load2, regularLabel2)
            }
            minus3.setOnClickListener {
                minusLoad(load3, regularLabel3)
            }
            plus3.setOnClickListener {
                plusLoad(load3, regularLabel3)
            }
            minus4.setOnClickListener {
                minusLoad(load4, regularLabel4)
            }
            plus4.setOnClickListener {
                plusLoad(load4, regularLabel4)
            }
//            PETS LOAD
            minus1p.setOnClickListener {
                minusLoad(load1p, petsLabel1)
            }
            plus1p.setOnClickListener {
                plusLoad(load1p, petsLabel1)
            }
            minus2p.setOnClickListener {
                minusLoad(load2p, petsLabel2)
            }
            plus2p.setOnClickListener {
                plusLoad(load2p, petsLabel2)
            }
//            dry LOAD
            minus1d.setOnClickListener {
                minusLoad(load1d, dryLabel1)
            }
            plus1d.setOnClickListener {
                plusLoad(load1d, dryLabel1)
            }
            minus2d.setOnClickListener {
                minusLoad(load2d, dryLabel2)
            }
            plus2d.setOnClickListener {
                plusLoad(load2d, dryLabel2)
            }
//            sneaker LOAD
            minus1s.setOnClickListener {
                minusLoad(load1s, sneakersLabel1)
            }
            plus1s.setOnClickListener {
                plusLoad(load1s, sneakersLabel1)
            }
            minus2s.setOnClickListener {
                minusLoad(load2s, sneakersLabel2)
            }
            plus2s.setOnClickListener {
                plusLoad(load2s, sneakersLabel2)
            }

            btnAgreement.setOnClickListener {
                btnAgreementOnClickListener()
            }
            btnAcknowledgement.setOnClickListener {
                btnAcknowledgementOnClickListener()
            }

            btnSubmit.setOnClickListener {
                btnSubmitOnClickListener()
            }

            checkAgreement.setOnClickListener {
                if (checkAgreement.isChecked) {
                    btnAgreement.error = null
                }
            }
            checkAcknowledgement.setOnClickListener {
                if (checkAcknowledgement.isChecked) {
                    btnAcknowledgement.error = null
                }
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun btnAgreementOnClickListener() {
        val agreementBinding = DialogAgreementBinding.inflate(this.layoutInflater)
        agreementBinding.titleAgreement.text = "Terms and Condition"
        agreementBinding.agreementDescription.text = this.getString(R.string.user_agreement)
        showDialog(agreementBinding)
    }

    @SuppressLint("SetTextI18n")
    private fun btnAcknowledgementOnClickListener() {
        val agreementBinding = DialogAgreementBinding.inflate(this.layoutInflater)
        agreementBinding.titleAgreement.text = "Acknowledgement"
        agreementBinding.agreementDescription.text = this.getString(R.string.user_acknowledgement)
        showDialog(agreementBinding)
    }

    private fun showDialog(agreementBinding: DialogAgreementBinding) {
        var promptDialog = Dialog(dashboardCustomer)
        val promptBuilder = AlertDialog.Builder(context)
        promptBuilder.setView(agreementBinding.root)
        promptBuilder.setNegativeButton("Okay") { _, _ ->
            promptDialog.dismiss()
        }
        promptDialog = promptBuilder.create()
        if (promptDialog.window != null) {
            promptDialog.window!!.setBackgroundDrawableResource(R.color.color_light_3)
        }
        promptDialog.show()
    }

    private fun btnSubmitOnClickListener() {

        with(binding) {
            if (!checkAgreement.isChecked) {
                btnAgreement.error =
                    "Please check the Terms and condition section for Security Policy."
                Toast.makeText(
                    context,
                    "Please check the Terms and condition section for Security Policy.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (!checkAcknowledgement.isChecked) {
                btnAcknowledgement.error =
                    "Please check the Acknowledgement section for Security Policy."
                Toast.makeText(
                    context,
                    "Please check the Acknowledgement section for Security Policy.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                btnAgreement.error = null
                totalLaundryPrice = totalLaundryPrice ?: 0.0
                dashboardCustomer.showLoadingDialog()
                if (totalLaundryPrice!! <= 0) {
                    dashboardCustomer.dismissLoadingDialog()
                    var promptDialog = Dialog(dashboardCustomer)
                    val promptBuilder = AlertDialog.Builder(context)
                    promptBuilder.setTitle("INVALID BOOKING")
                    promptBuilder.setMessage("Please do transaction for the selected services to continue.")
                    promptBuilder.setNegativeButton("Okay") { _, _ ->
                        promptDialog.dismiss()
                    }
                    promptDialog = promptBuilder.create()
                    if (promptDialog.window != null) {
                        promptDialog.window!!.setBackgroundDrawableResource(R.color.color_light_3)
                    }
                    promptDialog.show()
                } else {
                    notes = notesInput.text.toString()

                    order = Order(
                        orderNo,
                        uid,
                        shopID,
                        null,
                        regular,
                        pets,
                        dry,
                        sneaker,
                        ratesUnit,
                        regularWhiteMaxKg,
                        regularWhiteLoad,
                        regularWhiteRate,
                        regularColorMaxKg,
                        regularColorLoad,
                        regularColorRate,
                        regularComforterMaxKg,
                        regularComforterRate,
                        regularOthersMaxKg,
                        regularOthersLoad,
                        regularOthersRate,
                        petsWhiteMaxKg,
                        petsWhiteLoad,
                        petsWhiteRate,
                        petsColorMaxKg,
                        petsColorLoad,
                        petsColorRate,
                        dryWhiteMaxKg,
                        dryWhiteLoad,
                        dryWhiteRate,
                        dryColorMaxKg,
                        dryColorLoad,
                        dryColorRate,
                        sneakerOrdinaryMaxKg,
                        sneakerOrdinaryRate,
                        sneakerBootsMaxKg,
                        sneakerBootsRate,
                        totalLaundryPrice,
                        pickUpFee,
                        deliveryFee,
                        totalOrder,
                        status,
                        notes,
                        pickUpDatetime,
                        deliveryDatetime
                    )

                    Log.d("ORDER_NO", "$orderNo")
                    val databaseRef = firebaseDatabase.reference.child("orders/$uid")
                        .child(orderNo!!)
                    databaseRef.setValue(order)
                        .addOnCompleteListener(dashboardCustomer) { task ->
                            if (task.isSuccessful) {
                                dashboardCustomer.showOrderDetails(order!!, "OrderForm")
                                dashboardCustomer.dismissLoadingDialog()
                                setNotification(order!!)
                                Log.d(
                                    "ORDER_SAVING",
                                    "Order $orderNo has been successfully saved!"
                                )
                                Toast.makeText(
                                    context,
                                    "Order $orderNo has been successfully saved!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                dashboardCustomer.dismissLoadingDialog()
                                Log.d(
                                    "ORDER_SAVING",
                                    "Order $orderNo failed to save -> ${task.exception}"
                                )
                            }
                        }


                }
            }
        }
    }

    private fun setNotification(order: Order) {
        // If no notification exists, create a new one

        firebaseDatabaseReference.child("notification")
            .orderByChild("orderNo").equalTo(order.orderNo) // Search by orderNo
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // If notification exists, update only the required fields
                        for (childSnapshot in snapshot.children) {
                            val updateMap = mapOf(
                                "riderID" to order.riderId,
                                "cunread" to true,
                                "sunread" to true,
                                "runread" to true, // Rider has seen it
                                "status" to order.status,
                                "note" to "${order.orderNo} is waiting $status by ${order.riderId}",
                                "notificationTimestamp" to SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(
                                    Date()
                                )
                            )

                            firebaseDatabaseReference.child("notification")
                                .child(childSnapshot.key!!)
                                .updateChildren(updateMap)
                                .addOnSuccessListener {
                                    Log.d(
                                        "Monitor Notification",
                                        "Notification updated successfully."
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.e(
                                        "Monitor Notification",
                                        "Failed to update notification",
                                        e
                                    )
                                }
                        }
                    } else {
                        // If no notification exists, create a new one
                        val newNotification = Notification(
                            orderNo = order.orderNo,
                            shopID = order.shopID,
                            customerID = order.uid,
                            status = order.status,
                            cunread = true,
                            sunread = true,
                            runread = true,
                            note = "${order.orderNo} is $status to accept by Laundry",
                            notificationTimestamp = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(
                                Date()
                            )
                        )

                        firebaseDatabaseReference.child("notification").child(order.orderNo!!)
                            .setValue(newNotification)
                            .addOnSuccessListener {
                                Log.d(
                                    "Monitor Notification",
                                    "New notification created successfully."
                                )
                            }
                            .addOnFailureListener { e ->
                                Log.e("Monitor Notification", "Failed to create notification", e)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(dashboardCustomer, error.message, Toast.LENGTH_SHORT).show()
                }
            })


        /**
        val newNotification = Notification(
        orderNo = order.orderNo,
        shopID = order.shopID,
        customerID = order.uid,
        status = order.status,
        cunread = true,
        sunread = true,
        runread = true,
        note = "${order.orderNo} is $status to accept by Laundry",
        notificationTimestamp = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date())
        )

        firebaseDatabaseReference.child("notification").child("${order.orderNo!!}-${SimpleDateFormat("yyyyMMdHHmmss").format(Date())}")
        .setValue(newNotification)
        .addOnSuccessListener {
        Log.d("Monitor Notification", "New notification created successfully.")
        }
        .addOnFailureListener { e ->
        Log.e("Monitor Notification", "Failed to create notification", e)
        }
         */
    }

    private fun setOrderNo() {

        val shopPattern: String =
            shopID!!.substring(
                shopID!!.length - 5,
                shopID!!.length
            ) //get Last 5 character
        val userPattern: String =
            uid!!.substring(
                uid!!.length - 5,
                uid!!.length
            ) //get Last 5 character

        firebaseDatabaseReference.child("orders/$uid")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("CHECK_ORDER_COUNT", "ON_DATA_CHANGE")
                    if (dataSnapshot.exists()) {
                        dataOrder = dataSnapshot.children.toList()
                    }

                    orderNo = if (dataOrder != null) {
                        Log.d("DATA_COUNT", "${dataOrder!!.size + 1}")
                        "TRX-$shopPattern-$userPattern-000${dataOrder!!.size + 1}"
                    } else {
                        "TRX-$shopPattern-$userPattern-0001"
                    }
                    Log.d("ORDER_NO", "$orderNo")

                }

                override fun onCancelled(error: DatabaseError) {
                    dashboardCustomer.dismissLoadingDialog()
                    Log.d("CHECK_ORDER_COUNT", error.message)
                }
            })
    }


    private fun minusLoad(load: TextView, service: TextView) {
        Log.d("${service.text}", "MINUS")
        val loadValue: Int = load.text.toString().toInt()
        if (loadValue >= 1) {
            load.text = "${loadValue - 1}"
        }
        propertyChange()
    }

    @SuppressLint("SetTextI18n")
    private fun plusLoad(load: TextView, service: TextView) {
        Log.d("${service.text}", "MINUS")
        val loadValue: Int = load.text.toString().toInt()
        load.text = "${loadValue + 1}"
        propertyChange()
    }

    @SuppressLint("SetTextI18n")
    private fun propertyChange() {
        totalLaundryPrice = 0.0
        totalOrder = 0.0

        binding.apply {
            if (linear2.visibility == View.VISIBLE) {
                Log.d(regularLabel1.text.toString(), "LOAD -> ${load1.text.toString()}")
                Log.d(regularLabel2.text.toString(), "LOAD -> ${load2.text.toString()}")
                Log.d(regularLabel3.text.toString(), "LOAD -> ${load3.text.toString()}")
                Log.d(regularLabel4.text.toString(), "LOAD -> ${load4.text.toString()}")

                regularWhiteLoad = load1.text.toString().toInt()
                regularWhiteRate = rates!!.regularWhiteRate
                regularColorLoad = load2.text.toString().toInt()
                regularColorRate = rates!!.regularColorRate
                regularComforterMaxKg = load3.text.toString().toInt()
                regularComforterRate = rates!!.regularComforterRate
                regularOthersLoad = load4.text.toString().toInt()
                regularOthersRate = rates!!.regularOthersRate

                val totalRegular = (regularWhiteLoad!! * regularWhiteRate!!) +
                        (regularColorLoad!! * regularColorRate!!) +
                        (regularComforterMaxKg!! * regularComforterRate!!) +
                        (regularOthersLoad!! * regularOthersRate!!)

                totalLaundryPrice = totalLaundryPrice!! + totalRegular
            }
            if (linear3.visibility == View.VISIBLE) {
                Log.d(petsLabel1.text.toString(), "LOAD -> ${load1p.text.toString()}")
                Log.d(petsLabel2.text.toString(), "LOAD -> ${load2p.text.toString()}")

                petsWhiteLoad = load1.text.toString().toInt()
                petsWhiteRate = rates!!.petsWhiteRate
                petsColorLoad = load2p.text.toString().toInt()
                petsColorRate = rates!!.petsColorRate

                val totalPets = (petsWhiteLoad!! * petsWhiteRate!!) +
                        (petsColorLoad!! * petsColorRate!!)

                totalLaundryPrice = totalLaundryPrice!! + totalPets
            }
            if (linear4.visibility == View.VISIBLE) {
                Log.d(dryLabel1.text.toString(), "LOAD -> ${load1d.text.toString()}")
                Log.d(dryLabel2.text.toString(), "LOAD -> ${load2d.text.toString()}")

                dryWhiteLoad = load1d.text.toString().toInt()
                dryWhiteRate = rates!!.dryWhiteRate
                dryColorLoad = load2d.text.toString().toInt()
                dryColorRate = rates!!.dryColorRate

                val totalDry = (dryWhiteLoad!! * dryWhiteRate!!) +
                        (dryColorLoad!! * dryColorRate!!)

                totalLaundryPrice = totalLaundryPrice!! + totalDry
            }
            if (linear5.visibility == View.VISIBLE) {
                Log.d(sneakersLabel1.text.toString(), "LOAD -> ${load1s.text.toString()}")
                Log.d(sneakersLabel2.text.toString(), "LOAD -> ${load2s.text.toString()}")

                sneakerOrdinaryMaxKg = load1s.text.toString().toInt()
                sneakerOrdinaryRate = rates!!.sneakerOrdinaryRate
                sneakerBootsMaxKg = load2s.text.toString().toInt()
                sneakerBootsRate = rates!!.sneakerBootsRate

                val totalSneakers = (sneakerOrdinaryMaxKg!! * sneakerOrdinaryRate!!) +
                        (sneakerBootsMaxKg!! * sneakerBootsRate!!)

                totalLaundryPrice = totalLaundryPrice!! + totalSneakers
            }

            pickUpFee = rates!!.pickUpFee
            deliveryFee = rates!!.deliveryFee
            totalOrder = totalLaundryPrice!! + pickUpFee!! + deliveryFee!!

            if (totalLaundryPrice!! <= 0) {
                totalLaundryAmount.text = "$ratesUnit 0.00"
            } else {
                totalLaundryAmount.text =
                    "$ratesUnit ${DecimalFormat("#,###.00").format(totalLaundryPrice)}"
            }
            pickUpFeeAmount.text = "$ratesUnit ${DecimalFormat("#,###.00").format(pickUpFee)}"
            deliveryFeeAmount.text = "$ratesUnit ${DecimalFormat("#,###.00").format(deliveryFee)}"
            totalOrderAmount.text = "$ratesUnit ${DecimalFormat("#,###.00").format(totalOrder)}"

            notes = notesInput.text.toString()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun retrieveShopServices() {
        shop.apply {
            shopID = shop.uid
            binding.shopName.text = businessName
            binding.shopAddress.text = businessAddress

            val databaseRef = firebaseDatabase.reference.child("rates")
                .child(shopID!!)

            databaseRef.get().addOnCompleteListener { dataSnapshot ->
                if (dataSnapshot.isSuccessful) {
                    rates = dataSnapshot.result.getValue(Rates::class.java)
                    Log.d("RATES_DATA", "GET DATA RATES SUCCESSFUL")
                    binding.apply {
                        if (!rates!!.regular!!) {
                            btnServiceRegular.visibility = View.GONE
                        }
                        if (!rates!!.pets!!) {
                            btnServicePets.visibility = View.GONE
                        }
                        if (!rates!!.dry!!) {
                            btnServiceDryClean.visibility = View.GONE
                        }
                        if (!rates!!.sneakers!!) {
                            btnServiceSneakers.visibility = View.GONE
                        }
                        //Regular
                        this@DashboardOrderFormFragment.regularWhiteMaxKg =
                            rates!!.regularWhiteMaxKg
                        regularLabel1.text = "White Clothes (${rates!!.regularWhiteMaxKg} Kg Max)"
                        regularLoadRate1.text = "${rates!!.regularWhiteRate} / LOAD"
                        this@DashboardOrderFormFragment.regularColorMaxKg =
                            rates!!.regularColorMaxKg
                        regularLabel2.text = "Color Clothes (${rates!!.regularColorMaxKg} Kg Max)"
                        regularLoadRate2.text = "${rates!!.regularColorRate} / LOAD"
                        regularLabel3.text = "Comforter (Per Piece)"
                        regularLoadRate3.text = "${rates!!.regularComforterRate} / LOAD"
                        this@DashboardOrderFormFragment.regularOthersMaxKg =
                            rates!!.regularOthersMaxKg
                        regularLabel4.text =
                            "Blankets/ Bedsheets/ Curtains/ Towels ${rates!!.regularOthersMaxKg} Kg Max)"
                        regularLoadRate4.text = "${rates!!.regularOthersRate} / LOAD"
                        //Pets Rates
                        this@DashboardOrderFormFragment.petsWhiteMaxKg = rates!!.petsWhiteMaxKg
                        petsLabel1.text = "White Clothes (${rates!!.petsWhiteMaxKg} Kg Max)"
                        petsLoadRate1.text = "${rates!!.petsWhiteRate} / LOAD"
                        this@DashboardOrderFormFragment.petsColorMaxKg = rates!!.petsColorMaxKg
                        petsLabel2.text = "Color Clothes (${rates!!.petsColorMaxKg} Kg Max)"
                        petsLoadRate2.text = "${rates!!.petsColorRate} / LOAD"
                        //Dry Rates
                        this@DashboardOrderFormFragment.dryWhiteMaxKg = rates!!.dryWhiteMaxKg
                        dryLabel1.text = "White Clothes (${rates!!.dryWhiteMaxKg} Kg Max)"
                        dryLoadRate1.text = "${rates!!.dryWhiteRate} / LOAD"
                        this@DashboardOrderFormFragment.dryColorMaxKg = rates!!.dryColorMaxKg
                        dryLabel2.text = "Color Clothes (${rates!!.dryColorMaxKg} Kg Max)"
                        dryLoadRate2.text = "${rates!!.dryColorRate} / LOAD"
                        //Sneakers
                        sneakersLabel1.text = "Ordinary High Cut and Boots (Per Pair)"
                        sneakersLoadRate1.text = "${rates!!.sneakerOrdinaryRate} / PAIR"
                        sneakersLabel2.text = "Above Ankle to Knee High Cut and Boots (Per Pair)"
                        sneakersLoadRate2.text = "${rates!!.sneakerBootsRate} / PAIR"
                    }
                } else {
                    Log.d("RATES_DATA", "GET DATA RATES FAILED")
                }
            }
        }
    }

    private fun btnServiceRegularOnClickListener() {
        regular = setVisibility(binding.linear2, binding.btnServiceRegular)
        if (!regular!!) {
            with(binding) {
                regularLoadRate1.text = "0"
                regularLoadRate2.text = "0"
                regularLoadRate3.text = "0"
                regularLoadRate4.text = "0"
            }
        }
    }

    private fun btnServicePetsOnClickListener() {
        pets = setVisibility(binding.linear3, binding.btnServicePets)
        if (!pets!!) {
            with(binding) {
                petsLoadRate1.text = "0"
                petsLoadRate2.text = "0"
            }
        }
    }

    private fun btnServiceDryCleanOnClickListener() {
        dry = setVisibility(binding.linear4, binding.btnServiceDryClean)
        if (!dry!!) {
            with(binding) {
                dryLoadRate1.text = "0"
                dryLoadRate2.text = "0"
            }
        }
    }

    private fun btnServiceSneakersOnClickListener() {
        sneaker = setVisibility(binding.linear5, binding.btnServiceSneakers)
        if (!sneaker!!) {
            with(binding) {
                sneakersLoadRate1.text = "0"
                sneakersLoadRate2.text = "0"
            }
        }
    }

    private fun setVisibility(linear: LinearLayoutCompat, view: View): Boolean {

        val button = view as AppCompatButton

        if (linear.visibility == View.GONE) {
            linear.visibility = View.VISIBLE
            button.background =
                ContextCompat.getDrawable(dashboardCustomer, R.drawable.button_secondary)
        } else {
            linear.visibility = View.GONE
            button.background =
                ContextCompat.getDrawable(dashboardCustomer, R.drawable.button_white)
        }

        return linear.isVisible
    }


}

