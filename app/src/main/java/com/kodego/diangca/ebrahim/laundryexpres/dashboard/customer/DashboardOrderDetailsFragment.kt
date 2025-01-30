package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.LoginActivity
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.partner.DashboardPartnerActivity
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.rider.DashboardRiderActivity
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogAgreementBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardOrderDetailsFormBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Order
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("SimpleDateFormat")
class DashboardOrderDetailsFragment(var activityDashboard: Activity) : Fragment() {

    private var _binding: FragmentDashboardOrderDetailsFormBinding? = null
    private val binding get() = _binding!!

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()


    private var order: Order? = null
    private var customer: User? = null
    private var user: String? = null

    private var orderNo: String? = null
    private var uid: String? = null
    private var shopID: String? = null
    private var regular: Boolean? = null
    private var pets: Boolean? = null
    private var dry: Boolean? = null
    private var sneaker: Boolean? = null
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
    private var status: String? = "FOR PICK-UP"
    private var notes: String? = null
    private var pickUpDatetime: String? = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date())
    private var deliveryDatetime: String? = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date())


    private var callBack: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun setCallBack(callBack: String?) {
        this.callBack = callBack
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            FragmentDashboardOrderDetailsFormBinding.inflate(layoutInflater, container, false)
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
            user = bundle.getString("user")
            order = bundle.getParcelable<Order>("order")!!
            Log.d("FETCH_ORDER", order.toString())
            retrieveOrder()
            updateOrderActions();
        }
        binding.apply {

            btnAgreement.setOnClickListener {
                btnAgreementOnClickListener()
            }
            btnAcknowledgement.setOnClickListener {
                btnAcknowledgementOnClickListener()
            }

            btnCancel.setOnClickListener {
                btnCancelOnClickListener()
            }

            btnAccept.setOnClickListener {
                btnbtnAcceptOnClickListener()
            }


            when (callBack) {
                "OrderForm" -> {
                    btnHome.text = "Home"
                }

                "Order" -> {
                    btnHome.text = "Back"
                }

            }

            btnHome.setOnClickListener {
                btnHomeOnClickListener()
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

    private fun updateOrderActions() {
//        if (user.equals("partner", true) && !order!!.status.equals("PENDING", true)) {
//            binding.btnAccept.visibility = View.GONE
//        }
//        if (user.equals("rider", true) && !order!!.status.equals("FOR PICK-UP", true)) {
//            binding.btnAccept.visibility = View.GONE
//        }
//        if (user.equals("customer", true)) {
//            binding.btnAccept.visibility = View.GONE
//        }
//
//        if(user.equals("customer", true) && !order!!.status.equals("PENDING", true)){
//            binding.btnCancel.visibility = View.GONE
//        }else{
//            binding.btnCancel.visibility = View.GONE
//        }
        binding.btnAccept.visibility = when {
            user.equals("partner", true) && !order!!.status.equals("PENDING", true) -> View.GONE
            user.equals("rider", true) && !order!!.status.equals("FOR PICK-UP", true) -> View.GONE
            user.equals("customer", true) -> View.GONE
            else -> View.VISIBLE
        }

        binding.btnCancel.visibility =
            if (user.equals("customer", true) && order!!.status.equals("PENDING", true)) {
                View.VISIBLE
            } else {
                View.GONE
            }

    }

    private fun btnbtnAcceptOnClickListener() {
        if (user.equals("partner", true)) {

            changeOrderStatus("FOR PICK-UP")
        }
        if (user.equals("rider", true)) {
            changeOrderStatus("TO PICK-UP")

        }
    }

    private fun changeOrderStatus(status: String) {

        val databaseRef = firebaseDatabase.reference.child("order")
            .child(order!!.uid.toString())

        order!!.status = status

        val loadingBuilder = AlertDialog.Builder(activityDashboard)
        loadingBuilder.setTitle("CONFIRM")
        loadingBuilder.setMessage("Do you really accept booking?")
        loadingBuilder.setPositiveButton("Yes") { _, _ ->

            databaseRef.setValue(order).addOnCompleteListener(activityDashboard) { task ->
                if (task.isSuccessful) {
                    Log.d("UPDATE ORDER STATUS SUCCESS", "")
                    Toast.makeText(
                        context,
                        "Order Accepted",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.d("UPDATE ORDER STATUS FAILURE", "${task.exception}")

                }
            }
        }
    }

    private fun btnHomeOnClickListener() {

        when (activity) {
            is DashboardCustomerActivity -> {
                Log.d("CALL_BACK", "DASHBOARD CUSTOMER ACTIVITY")
                when (callBack) {
                    "OrderForm" -> {
                        (activity as? DashboardCustomerActivity)?.showHome()
                    }

                    "Order" -> {
                        (activity as? DashboardCustomerActivity)?.showOrder()
                    }
                }
            }

            is DashboardPartnerActivity -> {
                Log.d("CALL_BACK", "DASHBOARD PARTNER ACTIVITY")
                (activity as? DashboardPartnerActivity)?.showOrder()
            }

            is DashboardRiderActivity -> {
                Log.d("CALL_BACK", "DASHBOARD RIDER ACTIVITY")
                (activity as? DashboardRiderActivity)?.showrRides()
            }
        }

    }

    private fun btnCancelOnClickListener() {
        var promptDialog = Dialog(activityDashboard)
        val promptBuilder = AlertDialog.Builder(context)
        promptBuilder.setTitle("CANCEL BOOKING")
        promptBuilder.setMessage("Do you really want to cancel the Booking?")
        promptBuilder.setPositiveButton("YES") { _, _ ->
            val databaseRef = firebaseDatabase.reference.child("orders/$uid")
                .child(orderNo!!).child("status")
            databaseRef.setValue("CANCEL")
                .addOnCompleteListener(activityDashboard) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Order $orderNo has been successfully canceled!",
                            Toast.LENGTH_LONG
                        ).show()
                        promptDialog.dismiss()
                        btnHomeOnClickListener()
                    } else {
                        Log.d("CANCEL_FAILED", "${task.exception!!.message}")
                        Toast.makeText(
                            activityDashboard,
                            "${task.exception!!.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
        promptBuilder.setNegativeButton("CANCEL") { _, _ ->
            promptDialog.dismiss()
        }
        promptDialog = promptBuilder.create()
        if (promptDialog.window != null) {
            promptDialog.window!!.setBackgroundDrawableResource(R.color.color_light_3)
        }
        promptDialog.show()
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
        var promptDialog = Dialog(activityDashboard)
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

    @SuppressLint("SetTextI18n")
    private fun propertyChange() {
        totalLaundryPrice = 0.0
        totalOrder = 0.0

        with(binding) {
            if (linear2.visibility == View.VISIBLE) {
                Log.d(regularLabel1.text.toString(), "LOAD -> ${load1.text.toString()}")
                Log.d(regularLabel2.text.toString(), "LOAD -> ${load2.text.toString()}")
                Log.d(regularLabel3.text.toString(), "LOAD -> ${load3.text.toString()}")
                Log.d(regularLabel4.text.toString(), "LOAD -> ${load4.text.toString()}")

                regularWhiteLoad = load1.text.toString().toInt()
                regularColorLoad = load2.text.toString().toInt()
                regularComforterMaxKg = load3.text.toString().toInt()
                regularOthersLoad = load4.text.toString().toInt()

                val totalRegular = (regularWhiteLoad!! * regularWhiteRate!!) +
                        (regularColorLoad!! * regularColorRate!!) +
                        (regularComforterMaxKg!! * regularComforterRate!!) +
                        (regularOthersLoad!! * regularOthersRate!!)

                totalLaundryPrice = totalLaundryPrice!! + totalRegular
            }
            if (linear3.visibility == View.VISIBLE) {
                Log.d(petsLabel1.text.toString(), "LOAD -> ${load1p.text.toString()}")
                Log.d(petsLabel2.text.toString(), "LOAD -> ${load2p.text.toString()}")

                petsWhiteLoad = load1p.text.toString().toInt()
                petsColorLoad = load2p.text.toString().toInt()

                val totalPets = (petsWhiteLoad!! * petsWhiteRate!!) +
                        (petsColorLoad!! * petsColorRate!!)

                totalLaundryPrice = totalLaundryPrice!! + totalPets
            }
            if (linear4.visibility == View.VISIBLE) {
                Log.d(dryLabel1.text.toString(), "LOAD -> ${load1d.text.toString()}")
                Log.d(dryLabel2.text.toString(), "LOAD -> ${load2d.text.toString()}")

                dryWhiteLoad = load1d.text.toString().toInt()
                dryColorLoad = load2d.text.toString().toInt()

                val totalDry = (dryWhiteLoad!! * dryWhiteRate!!) +
                        (dryColorLoad!! * dryColorRate!!)

                totalLaundryPrice = totalLaundryPrice!! + totalDry
            }
            if (linear5.visibility == View.VISIBLE) {
                Log.d(sneakersLabel1.text.toString(), "LOAD -> ${load1s.text.toString()}")
                Log.d(sneakersLabel2.text.toString(), "LOAD -> ${load2s.text.toString()}")

                sneakerOrdinaryMaxKg = load1s.text.toString().toInt()
                sneakerBootsMaxKg = load2s.text.toString().toInt()

                val totalSneakers = (sneakerOrdinaryMaxKg!! * sneakerOrdinaryRate!!) +
                        (sneakerBootsMaxKg!! * sneakerBootsRate!!)

                totalLaundryPrice = totalLaundryPrice!! + totalSneakers
            }

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
    private fun retrieveOrder() {

        orderNo = order!!.orderNo
        shopID = order!!.shopID
        status = order!!.status
        pickUpFee = order!!.pickUpFee
        deliveryFee = order!!.deliveryFee
        pickUpDatetime = order!!.pickUpDatetime
        deliveryDatetime = order!!.deliveryDatetime

        regular = if (order!!.regular == null) {
            false
        } else {
            order!!.regular
        }
        pets = if (order!!.pets == null) {
            false
        } else {
            order!!.pets
        }
        dry = if (order!!.dry == null) {
            false
        } else {
            order!!.dry
        }
        sneaker = if (order!!.sneaker == null) {
            false
        } else {
            order!!.sneaker
        }

        Log.d("RATES_DATA", "GET DATA RATES SUCCESSFUL")

        with(binding) {

            orderNoLabel.text = orderNo
            orderStatus.text = "$status STATUS"
            pickUpDatetimeLabel.text = "PICK UP ON - $pickUpDatetime"
            deliveryDatetimeLabel.text = "DELIVERY ON - $deliveryDatetime"


            //Regular Rates
            this@DashboardOrderDetailsFragment.regularWhiteMaxKg = order!!.regularWhiteMaxKg
            regularLabel1.text = "White Clothes (${order!!.regularWhiteMaxKg} Kg Max)"
            regularLoadRate1.text = "${order!!.regularWhiteRate} / LOAD"
            this@DashboardOrderDetailsFragment.regularColorMaxKg = order!!.regularColorMaxKg
            regularLabel2.text = "Color Clothes (${order!!.regularColorMaxKg} Kg Max)"
            regularLoadRate2.text = "${order!!.regularColorRate} / LOAD"
            regularLabel3.text = "Comforter (Per Piece)"
            regularLoadRate3.text = "${order!!.regularComforterRate} / LOAD"
            this@DashboardOrderDetailsFragment.regularOthersMaxKg = order!!.regularOthersMaxKg
            regularLabel4.text =
                "Blankets/ Bedsheets/ Curtains/ Towels ${order!!.regularOthersMaxKg} Kg Max)"
            regularLoadRate4.text = "${order!!.regularOthersRate} / LOAD"
            //Pets Rates
            this@DashboardOrderDetailsFragment.petsWhiteMaxKg = order!!.petsWhiteMaxKg
            petsLabel1.text = "White Clothes (${order!!.petsWhiteMaxKg} Kg Max)"
            petsLoadRate1.text = "${order!!.petsWhiteRate} / LOAD"
            this@DashboardOrderDetailsFragment.petsColorMaxKg = order!!.petsColorMaxKg
            petsLabel2.text = "Color Clothes (${order!!.petsColorMaxKg} Kg Max)"
            petsLoadRate2.text = "${order!!.petsColorRate} / LOAD"
            //Dry Rates
            this@DashboardOrderDetailsFragment.dryWhiteMaxKg = order!!.dryWhiteMaxKg
            dryLabel1.text = "White Clothes (${order!!.dryWhiteMaxKg} Kg Max)"
            dryLoadRate1.text = "${order!!.dryWhiteRate} / LOAD"
            this@DashboardOrderDetailsFragment.dryColorMaxKg = order!!.dryColorMaxKg
            dryLabel2.text = "Color Clothes (${order!!.dryColorMaxKg} Kg Max)"
            dryLoadRate2.text = "${order!!.dryColorRate} / LOAD"
            //Sneakers
            sneakersLabel1.text = "Ordinary High Cut and Boots (Per Pair)"
            sneakersLoadRate1.text = "${order!!.sneakerOrdinaryRate} / PAIR"
            sneakersLabel2.text = "Above Ankle to Knee High Cut and Boots (Per Pair)"
            sneakersLoadRate2.text = "${order!!.sneakerBootsRate} / PAIR"

            Log.d("RATES_DATA", "GET DATA RATES SUCCESSFUL >>>>>>>>>>")

            if (regular!!) {
                btnServiceRegular.visibility = View.VISIBLE
                linear2.visibility = View.VISIBLE
                ContextCompat.getDrawable(activityDashboard, R.drawable.button_secondary)
                load1.text = "${order!!.regularWhiteLoad}"
                load2.text = "${order!!.regularColorLoad}"
                load3.text = "${order!!.regularComforterMaxKg}"
                load4.text = "${order!!.regularOthersLoad}"
                //Regular Rates
                this@DashboardOrderDetailsFragment.regularWhiteRate = order!!.regularWhiteRate
                this@DashboardOrderDetailsFragment.regularColorRate = order!!.regularColorRate
                this@DashboardOrderDetailsFragment.regularComforterRate =
                    order!!.regularComforterRate
                this@DashboardOrderDetailsFragment.regularOthersRate = order!!.regularOthersRate
            } else {
                Log.d("RATES_DATA", "NO LOAD REGULAR")
                btnServiceRegular.visibility = View.GONE
                linear2.visibility = View.GONE
                load1.text = "0"
                load2.text = "0"
                load3.text = "0"
                load4.text = "0"
            }
            if (pets!!) {
                Log.d("RATES_DATA", "LOAD PETS")
                btnServicePets.visibility = View.VISIBLE
                linear3.visibility = View.VISIBLE
                ContextCompat.getDrawable(activityDashboard, R.drawable.button_secondary)
                load1p.text = "${order!!.petsWhiteLoad}"
                load2p.text = "${order!!.petsColorLoad}"
                //Pates Rates
                this@DashboardOrderDetailsFragment.petsWhiteRate = order!!.petsWhiteRate
                this@DashboardOrderDetailsFragment.petsColorRate = order!!.petsColorRate
            } else {
                Log.d("RATES_DATA", "NO LOAD PETS")
                btnServicePets.visibility = View.GONE
                linear3.visibility = View.GONE
                load1p.text = "0"
                load2p.text = "0"
            }
            if (dry!!) {
                Log.d("RATES_DATA", "LOAD DRY")
                btnServiceDryClean.visibility = View.VISIBLE
                linear4.visibility = View.VISIBLE
                ContextCompat.getDrawable(activityDashboard, R.drawable.button_secondary)
                load1d.text = "${order!!.dryWhiteLoad}"
                load2d.text = "${order!!.dryColorLoad}"
                this@DashboardOrderDetailsFragment.dryWhiteRate = order!!.dryWhiteRate
                this@DashboardOrderDetailsFragment.dryColorRate = order!!.dryColorRate
            } else {
                Log.d("RATES_DATA", "NO LOAD DRY")
                btnServiceDryClean.visibility = View.GONE
                linear4.visibility = View.GONE
                load1d.text = "0"
                load2d.text = "0"
            }
            if (sneaker!!) {
                Log.d("RATES_DATA", "LOAD SNEAKER")
                btnServiceSneakers.visibility = View.VISIBLE
                linear5.visibility = View.VISIBLE
                ContextCompat.getDrawable(activityDashboard, R.drawable.button_secondary)
                load1s.text = "${order!!.sneakerOrdinaryMaxKg}"
                load2s.text = "${order!!.sneakerBootsMaxKg}"
                this@DashboardOrderDetailsFragment.sneakerOrdinaryRate = order!!.sneakerOrdinaryRate
                this@DashboardOrderDetailsFragment.sneakerBootsRate = order!!.sneakerBootsRate
            } else {
                Log.d("RATES_DATA", "NO LOAD SNEAKER")
                btnServiceSneakers.visibility = View.GONE
                linear5.visibility = View.GONE
                load1s.text = "0"
                load2s.text = "0"
            }

            if (!status.equals("FOR PICK-UP")) {
                btnCancel.visibility = View.GONE
            }

            Log.d("ORDER_DATA", "GET ORDER LOAD SUCCESSFUL >>>>>>>>>>")


            firebaseDatabase.reference.child("users")
                .child(order!!.uid.toString()).get().addOnCompleteListener { dataSnapshot ->
                    if (dataSnapshot.isSuccessful) {
                        customer = dataSnapshot.result.getValue(User::class.java)
                        if (customer != null) {
                            customerName.text = "${customer!!.firstname}  ${customer!!.lastname}"
                        }
                    }
                }
            propertyChange()
        }
    }


}

