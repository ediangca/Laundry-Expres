package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
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
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogAgreementBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardOrderDetailsFormBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Order
import com.kodego.diangca.ebrahim.laundryexpres.model.Rates
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("SimpleDateFormat")
class DashboardOrderDetailsFragment(var dashboardCustomer: DashboardCustomerActivity) : Fragment() {

    private var _binding: FragmentDashboardOrderDetailsFormBinding? = null
    private val binding get() = _binding!!

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()


    private var order: Order? = null
    private var rate: Rates? = null

    private var orderNo: String? = null
    private var uid: String? = null
    private var shopID: String? = null
    private var regular: Boolean? = null
    private var pets: Boolean? = null
    private var dry: Boolean? = null
    private var sneaker: Boolean? = null
    private var ratesUnit: String? = "PHP"
    private var regularWhiteMaxKg: Int? = null
    private var regularWhiteRate: Double? = null
    private var regularColorMaxKg: Int? = null
    private var regularColorRate: Double? = null
    private var regularComforterMaxKg: Int? = null
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        if (bundle!=null) {
            order = bundle.getParcelable<Order>("order")!!
            rate = bundle.getParcelable<Rates>("rate")!!
            Log.d("FETCH_ORDER", order.toString())
            retrieveOrder()
        }

        binding.apply {
            btnBack.setOnClickListener {
                dashboardCustomer.resumeShopList()
            }

            btnAgreement.setOnClickListener {
                btnAgreementOnClickListener()
            }
            btnAcknowledgement.setOnClickListener {
                btnAcknowledgementOnClickListener()
            }

            btnCancel.setOnClickListener {
                btnCancelOnClickListener()
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

    private fun btnHomeOnClickListener() {
        dashboardCustomer.showHome()
    }

    private fun btnCancelOnClickListener() {
        var promptDialog = Dialog(dashboardCustomer)
        val promptBuilder = AlertDialog.Builder(context)
        promptBuilder.setTitle("CANCEL BOOKING")
        promptBuilder.setMessage("Do you really want to cancel the Booking?")
        promptBuilder.setPositiveButton("YES") { _, _ ->
            val databaseRef = firebaseDatabase.reference.child("orders/$uid")
                .child(orderNo!!).child("status")
            databaseRef.setValue("CANCEL")
                .addOnCompleteListener(dashboardCustomer) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Order $orderNo has been successfully canceled!",
                            Toast.LENGTH_LONG
                        ).show()
                        promptDialog.dismiss()
                        dashboardCustomer.showOrder()
                    } else {
                        Log.d("CANCEL_FAILED", "${task.exception!!.message}")
                        Toast.makeText(
                            dashboardCustomer,
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
        if (promptDialog.window!=null) {
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
        var promptDialog = Dialog(dashboardCustomer)
        val promptBuilder = AlertDialog.Builder(context)
        promptBuilder.setView(agreementBinding.root)
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
    private fun propertyChange() {
        totalLaundryPrice = 0.0
        totalOrder = 0.0

        with(binding) {
            if (linear2.visibility==View.VISIBLE) {
                Log.d(regularLabel1.text.toString(), "LOAD -> ${load1.text.toString()}")
                Log.d(regularLabel2.text.toString(), "LOAD -> ${load2.text.toString()}")
                Log.d(regularLabel3.text.toString(), "LOAD -> ${load3.text.toString()}")
                Log.d(regularLabel4.text.toString(), "LOAD -> ${load4.text.toString()}")

                regularWhiteMaxKg = load1.text.toString().toInt()
                regularColorMaxKg = load2.text.toString().toInt()
                regularComforterMaxKg = load3.text.toString().toInt()
                regularOthersMaxKg = load4.text.toString().toInt()

                val totalRegular = (regularWhiteMaxKg!! * regularWhiteRate!!) +
                        (regularColorMaxKg!! * regularColorRate!!) +
                        (regularComforterMaxKg!! * regularComforterRate!!) +
                        (regularOthersMaxKg!! * regularOthersRate!!)

                totalLaundryPrice = totalLaundryPrice!! + totalRegular
            }
            if (linear3.visibility==View.VISIBLE) {
                Log.d(petsLabel1.text.toString(), "LOAD -> ${load1p.text.toString()}")
                Log.d(petsLabel2.text.toString(), "LOAD -> ${load2p.text.toString()}")

                petsWhiteMaxKg = load1p.text.toString().toInt()
                petsColorMaxKg = load2p.text.toString().toInt()

                val totalPets = (petsWhiteMaxKg!! * petsWhiteRate!!) +
                        (petsColorMaxKg!! * petsColorRate!!)

                totalLaundryPrice = totalLaundryPrice!! + totalPets
            }
            if (linear4.visibility==View.VISIBLE) {
                Log.d(dryLabel1.text.toString(), "LOAD -> ${load1d.text.toString()}")
                Log.d(dryLabel2.text.toString(), "LOAD -> ${load2d.text.toString()}")

                dryWhiteMaxKg = load1d.text.toString().toInt()
                dryColorMaxKg = load2d.text.toString().toInt()

                val totalDry = (dryWhiteMaxKg!! * dryWhiteRate!!) +
                        (dryColorMaxKg!! * dryColorRate!!)

                totalLaundryPrice = totalLaundryPrice!! + totalDry
            }
            if (linear5.visibility==View.VISIBLE) {
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

        regular = if(order!!.regular == null){false}else{order!!.regular}
        pets = if(order!!.pets == null){false}else{order!!.pets}
        dry = if(order!!.dry == null){false}else{order!!.dry}
        sneaker = if(order!!.sneaker == null){false}else{order!!.sneaker}

        Log.d("RATES_DATA", "GET DATA RATES SUCCESSFUL")

        with(binding) {

            orderNoLabel.text = orderNo
            orderStatus.text = "$status STATUS"
            pickUpDatetimeLabel.text = "PICK UP ON - $pickUpDatetime"
            deliveryDatetimeLabel.text = "DELIVERY ON - $deliveryDatetime"


            //Regular Rates
            regularLabel1.text = "White Clothes (${order!!.regularWhiteMaxKg} Kg Max)"
            regularLoadRate1.text = "${order!!.regularWhiteRate} / LOAD"
            regularLabel2.text = "Color Clothes (${order!!.regularWhiteMaxKg} Kg Max)"
            regularLoadRate2.text = "${order!!.regularColorRate} / LOAD"
            regularLabel3.text = "Comforter (Per Piece)"
            regularLoadRate3.text = "${order!!.regularComforterRate} / LOAD"
            regularLabel4.text =
                "Blankets/ Bedsheets/ Curtains/ Towels ${order!!.regularOthersMaxKg} Kg Max)"
            regularLoadRate4.text = "${order!!.regularOthersRate} / LOAD"
            //Pets Rates
            petsLabel1.text = "White Clothes (${order!!.petsWhiteMaxKg} Kg Max)"
            petsLoadRate1.text = "${order!!.petsWhiteRate} / LOAD"
            petsLabel2.text = "Color Clothes (${order!!.petsColorMaxKg} Kg Max)"
            petsLoadRate2.text = "${order!!.petsColorRate} / LOAD"
            //Dry Rates
            dryLabel1.text = "White Clothes (${order!!.dryWhiteMaxKg} Kg Max)"
            dryLoadRate1.text = "${order!!.dryWhiteRate} / LOAD"
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
                ContextCompat.getDrawable(dashboardCustomer, R.drawable.button_secondary)
                load1.text = "${order!!.regularWhiteMaxKg}"
                load2.text = "${order!!.regularColorMaxKg}"
                load3.text = "${order!!.regularComforterMaxKg}"
                load4.text = "${order!!.regularOthersMaxKg}"
                //Regular Rates
                this@DashboardOrderDetailsFragment.regularWhiteRate = rate!!.regularWhiteRate
                this@DashboardOrderDetailsFragment.regularColorRate = rate!!.regularColorRate
                this@DashboardOrderDetailsFragment.regularComforterRate = rate!!.regularComforterRate
                this@DashboardOrderDetailsFragment.regularOthersRate = rate!!.regularOthersRate
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
                ContextCompat.getDrawable(dashboardCustomer, R.drawable.button_secondary)
                load1p.text = "${order!!.petsWhiteMaxKg}"
                load2p.text = "${order!!.petsColorMaxKg}"
                //Pates Rates
                this@DashboardOrderDetailsFragment.petsWhiteRate = rate!!.petsWhiteRate
                this@DashboardOrderDetailsFragment.petsColorRate = rate!!.petsColorRate
            }else{
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
                ContextCompat.getDrawable(dashboardCustomer, R.drawable.button_secondary)
                load1d.text = "${order!!.dryWhiteMaxKg}"
                load2d.text = "${order!!.dryColorMaxKg}"
                this@DashboardOrderDetailsFragment.dryWhiteRate = rate!!.dryWhiteRate
                this@DashboardOrderDetailsFragment.dryColorRate = rate!!.dryColorRate
            }else{
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
                ContextCompat.getDrawable(dashboardCustomer, R.drawable.button_secondary)
                load1s.text = "${order!!.sneakerOrdinaryMaxKg}"
                load2s.text = "${order!!.sneakerBootsMaxKg}"
                this@DashboardOrderDetailsFragment.sneakerOrdinaryRate = rate!!.sneakerOrdinaryRate
                this@DashboardOrderDetailsFragment.sneakerBootsRate = rate!!.sneakerBootsRate
            }else{
                Log.d("RATES_DATA", "NO LOAD SNEAKER")
                btnServiceSneakers.visibility = View.GONE
                linear5.visibility = View.GONE
                load1s.text = "0"
                load2s.text = "0"
            }

            if(!status.equals("FOR PICK-UP")){
                btnCancel.visibility = View.GONE
            }

            Log.d("ORDER_DATA", "GET ORDER LOAD SUCCESSFUL >>>>>>>>>>")

            propertyChange()
        }
    }


}

