package com.kodego.diangca.ebrahim.laundryexpres.dashboard.partner

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.classes.TouchEventView
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogTimePickerBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardPartnerBusinessBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream


class DashboardBusinessFragment(var dashboardPartner: DashboardPartnerActivity) : Fragment() {

    private var _binding: FragmentDashboardPartnerBusinessBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseStorageRef: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var currentItem = 0

    private lateinit var timePickerBuilder: AlertDialog.Builder
    private lateinit var timePickerDialogInterface: DialogInterface

    private lateinit var drawingBoard: TouchEventView
    private var businessImageUri: Uri? = null
    private var businessBIRUri: Uri? = null
    private var bankImageUri: Uri? = null

    var businessImageBytes: String? = null
    var businessBIRBytes: String? = null
    var bankImageBytes: String? = null

    private val PICK_BUSINESS_CODE = 100
    private val CAMERA_BUSINESS_CODE = 1
    private val PICK_BIR_CODE = 200
    private val CAMERA_BIR_CODE = 2
    private val PICK_BANK_CODE = 300
    private val CAMERA_BANK_CODE = 3

    private lateinit var profileOptionDialog: Dialog

    private var shop: Shop? = null

    var businessName: String? = null
    var businessLegalName: String? = null
    var businessEmail: String? = null
    var businessPhone: String? = null
    var businessAddress: String? = null

    var businessHoursMondayFrom: String? = null
    var businessHoursMondayTo: String? = null
    var businessHoursTuesdayFrom: String? = null
    var businessHoursTuesdayTo: String? = null
    var businessHoursWednesdayFrom: String? = null
    var businessHoursWednesdayTo: String? = null
    var businessHoursThursdayFrom: String? = null
    var businessHoursThursdayTo: String? = null
    var businessHoursFridayFrom: String? = null
    var businessHoursFridayTo: String? = null
    var businessHoursSaturdayFrom: String? = null
    var businessHoursSaturdayTo: String? = null
    var businessHoursSundayFrom: String? = null
    var businessHoursSundayTo: String? = null
    var businessHoursHolidayFrom: String? = null
    var businessHoursHolidayTo: String? = null

    var businessBankName: String? = null
    var businessBankAccountName: String? = null
    var businessBankAccNo: String? = null
    var businessBankBIC: String? = null
    var businessBankProofImage: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardPartnerBusinessBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }

    private fun initComponent() {


        val bundle = arguments
        if (bundle!=null) {
            shop = bundle.getParcelable<Shop>("shop")!!
            Log.d("ON_RESUME_FETCH_SHOP", shop.toString())
        } else {
            shop = dashboardPartner.getShop()
        }
        setShopDetails(shop)

        binding.shopLogo.setOnClickListener {
            showOptionProfile()
        }
        binding.fromMonday.setOnClickListener {
            setTime(it)
        }
        binding.fromMondayBut.setOnClickListener {
            setTime(binding.fromMonday)
        }
        binding.toMonday.setOnClickListener {
            setTime(it)
        }
        binding.toMondayBut.setOnClickListener {
            setTime(binding.toMonday)
        }

        binding.fromTuesday.setOnClickListener {
            setTime(it)
        }
        binding.fromTuesdayBut.setOnClickListener {
            setTime(binding.fromTuesday)
        }
        binding.toTuesday.setOnClickListener {
            setTime(it)
        }
        binding.toTuesdayBut.setOnClickListener {
            setTime(binding.toTuesday)
        }

        binding.fromWednesday.setOnClickListener {
            setTime(it)
        }
        binding.fromWednesdayBut.setOnClickListener {
            setTime(binding.fromWednesday)
        }
        binding.toWednesday.setOnClickListener {
            setTime(it)
        }
        binding.toWednesdayBut.setOnClickListener {
            setTime(binding.toWednesday)
        }

        binding.fromThursday.setOnClickListener {
            setTime(it)
        }
        binding.fromThursdayBut.setOnClickListener {
            setTime(binding.fromThursday)
        }
        binding.toThursday.setOnClickListener {
            setTime(it)
        }
        binding.toThursdayBut.setOnClickListener {
            setTime(binding.toThursday)
        }

        binding.fromFriday.setOnClickListener {
            setTime(it)
        }
        binding.fromFridayBut.setOnClickListener {
            setTime(binding.fromFriday)
        }
        binding.toFriday.setOnClickListener {
            setTime(it)
        }
        binding.toFridayBut.setOnClickListener {
            setTime(binding.toFriday)
        }

        binding.fromSaturday.setOnClickListener {
            setTime(it)
        }
        binding.fromSaturdayBut.setOnClickListener {
            setTime(binding.fromSaturday)
        }
        binding.toSaturday.setOnClickListener {
            setTime(it)
        }
        binding.toSaturdayBut.setOnClickListener {
            setTime(binding.toSaturday)
        }

        binding.fromSunday.setOnClickListener {
            setTime(it)
        }
        binding.fromSundayBut.setOnClickListener {
            setTime(binding.fromSunday)
        }
        binding.toSunday.setOnClickListener {
            setTime(it)
        }
        binding.toSundayBut.setOnClickListener {
            setTime(binding.toSunday)
        }

        binding.fromHoliday.setOnClickListener {
            setTime(it)
        }
        binding.fromHolidayBut.setOnClickListener {
            setTime(binding.fromHoliday)
        }
        binding.toHoliday.setOnClickListener {
            setTime(it)
        }
        binding.toHolidayBut.setOnClickListener {
            setTime(binding.toHoliday)
        }

        binding.btnBrowseBusinessBIR.setOnClickListener {
            btnBrowseBusinessBIROnClickListener()
        }
        binding.btnCaptureBusinessBIR.setOnClickListener {
            btnCaptureBusinessBIROnClickListener()
        }

        binding.btnBrowseBankSlip.setOnClickListener {
            btnBrowseBankSlipOnClickListener()
        }

        binding.btnCaptureBankSlip.setOnClickListener {
            btnCaptureBankSlipOnClickListener()
        }

        binding.btnSubmit.setOnClickListener {
            btnSubmitOnClickListener()
        }

    }

    private fun setShopDetails(shop: Shop?) {
        if (shop!=null) {
            val businessImageFilename = "business_${shop.uid}"
            val businessBIRFilename = "BIR_Cert_${shop.uid}"
            val businessBankFilename = "bank_proof_${shop.uid}"

            binding.apply {
                val businessLogo: ImageView = binding.shopLogo
                if (businessImageUri!=null) {
                    businessLogo.setImageURI(businessImageUri)
                }else{
                    if(shop.businessLogo!=null){
                        businessImageUri = Uri.parse(shop.businessLogo)
                        retrieveImageFromFirebaseStorage(
                            businessImageFilename,
                            businessImageUri,
                            businessLogo
                        )
                    }
                }

                titleView.text = shop.businessName
                subTitleView.text = shop.businessEmail

                businessName.setText(shop.businessName)
                businessLegalName.setText(shop.businessLegalName)
                businessEmail.setText(shop.businessEmail)
                businessPhone.setText(shop.businessPhoneNumber)
                businessAddress.setText(shop.businessAddress)

                if (businessBIRUri!=null) {
                    binding.businessBIRImage.setImageURI(businessBIRUri)
                }else{
                    if (shop.businessBIRImage!=null) {
                        businessBIRUri = Uri.parse(shop.businessBIRImage)
                        retrieveImageFromFirebaseStorage(
                            businessBIRFilename,
                            businessBIRUri,
                            binding.businessBIRImage
                        )
                    }

                }

                if (!shop.mondayFrom.equals("Closed")) {
                    fromMonday.setText(shop.mondayFrom)
                }
                if (!shop.mondayTo.equals("Closed")) {
                    toMonday.setText(shop.mondayTo)
                }
                if (!shop.tuesdayFrom.equals("Closed")) {
                    fromTuesday.setText(shop.tuesdayFrom)
                }
                if (!shop.tuesdayTo.equals("Closed")) {
                    toTuesday.setText(shop.tuesdayTo)
                }
                if (!shop.wednesdayFrom.equals("Closed")) {
                    fromWednesday.setText(shop.wednesdayFrom)
                }
                if (!shop.wednesdayTo.equals("Closed")) {
                    toWednesday.setText(shop.wednesdayTo)
                }
                if (!shop.thursdayFrom.equals("Closed")) {
                    fromThursday.setText(shop.thursdayFrom)
                }
                if (!shop.thursdayTo.equals("Closed")) {
                    toThursday.setText(shop.thursdayTo)
                }
                if (!shop.fridayFrom.equals("Closed")) {
                    fromFriday.setText(shop.fridayFrom)
                }
                if (!shop.fridayTo.equals("Closed")) {
                    toFriday.setText(shop.fridayTo)
                }
                if (!shop.saturdayFrom.equals("Closed")) {
                    fromSaturday.setText(shop.saturdayFrom)
                }
                if (!shop.saturdayTo.equals("Closed")) {
                    toSaturday.setText(shop.saturdayTo)
                }
                if (!shop.sundayFrom.equals("Closed")) {
                    fromSunday.setText(shop.sundayFrom)
                }
                if (!shop.sundayTo.equals("Closed")) {
                    toSunday.setText(shop.sundayTo)
                }
                if (!shop.holidayFrom.equals("Closed")) {
                    fromHoliday.setText(shop.holidayFrom)
                }
                if (!shop.holidayTo.equals("Closed")) {
                    toHoliday.setText(shop.holidayTo)
                }
                bankName.setText(shop.bankName)
                bankAccNameHolder.setText(shop.bankAccountName)
                bankAccNo.setText(shop.bankAccountNumber)
                bankBIC.setText(shop.bankAccountBIC)

                if (bankImageUri!=null) {
                    binding.bankSlipImage.setImageURI(bankImageUri)
                } else {
                    if (shop.bankProofImage!=null) {
                        bankImageUri = Uri.parse(shop.bankProofImage)
                        retrieveImageFromFirebaseStorage(
                            businessBankFilename,
                            bankImageUri,
                            binding.bankSlipImage
                        )
                    }
                }
            }
        }

    }

    private fun retrieveImageFromFirebaseStorage(
        filename: String,
        imageUri: Uri?,
        imageView: ImageView,
    ) {
        val firebaseStorageReference =
            FirebaseStorage.getInstance().reference.child("shop/${shop!!.uid}/$filename")
        Log.d("PROFILE_FILENAME", filename)
        Log.d("PROFILE_URI", imageUri!!.toString())
        val localFile = File.createTempFile("temp_$filename", ".jpg")
        firebaseStorageReference.getFile(localFile)
            .addOnSuccessListener {
                imageView.setImageBitmap(BitmapFactory.decodeFile(localFile.absolutePath))
                Log.d(
                    "IMAGE_$filename",
                    "$filename has been successfully load!"
                )
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "IMAGE_$filename > ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("IMAGE_$filename", "$filename failed to load!")
            }
        Log.d("IMAGE_$filename", "$imageUri")
        Picasso.with(context).load(imageUri)
            .into(imageView);
    }

    private fun showOptionProfile() {
        profileOptionDialog = Dialog(dashboardPartner)

        val loadingBuilder = AlertDialog.Builder(context)
        loadingBuilder.setTitle("PROFILE")
        loadingBuilder.setCancelable(false)
        loadingBuilder.setMessage("Please select an option")
        loadingBuilder.setNeutralButton("BROWSE GALLERY") { _, _ ->
            btnBrowseProfileOnClickListener()
        }
        loadingBuilder.setNegativeButton("TAKE PHOTO") { _, _ ->
            btnCaptureProfileOnClickListener()
        }
        loadingBuilder.setPositiveButton("CANCEL") { _, _ ->
            profileOptionDialog.dismiss()
        }
        profileOptionDialog = loadingBuilder.create()
        if (profileOptionDialog.window!=null) {
            profileOptionDialog.window!!.setBackgroundDrawableResource(R.color.color_light_3)
        }
        profileOptionDialog.show()
    }

    private fun btnCaptureProfileOnClickListener() {
        cameraCheckPermission(CAMERA_BUSINESS_CODE)
    }

    private fun btnCaptureBusinessBIROnClickListener() {
        cameraCheckPermission(CAMERA_BIR_CODE)
    }

    private fun btnCaptureBankSlipOnClickListener() {
        cameraCheckPermission(CAMERA_BANK_CODE)
    }

    private fun camera(code: Int) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, code)
    }

    private fun btnBrowseProfileOnClickListener() {
        val gallery = Intent()
        gallery.type = "image/*"
        gallery.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(gallery, PICK_BUSINESS_CODE)
    }

    private fun btnBrowseBusinessBIROnClickListener() {
        val gallery = Intent()
        gallery.type = "image/*"
        gallery.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(gallery, PICK_BIR_CODE)
    }

    private fun btnBrowseBankSlipOnClickListener() {
        val gallery = Intent()
        gallery.type = "image/*"
        gallery.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(gallery, PICK_BANK_CODE)
    }


    private fun cameraCheckPermission(code: Int) {

        Dexter.withContext(context)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            ).withListener(

                object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {

                            if (report.areAllPermissionsGranted()) {
                                camera(code)
                            } else {
                                toast(it.toString())
                            }

                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?,
                    ) {
                        showRotationalDialogForPermission()
                    }

                }
            ).onSameThread().check()
    }

    private fun showRotationalDialogForPermission() {
        AlertDialog.Builder(context)
            .setMessage(
                "It looks like you have turned off permissions"
                        + "required for this feature. It can be enable under App settings!!!"
            )

            .setPositiveButton("Go TO SETTINGS") { _, _ ->

                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", dashboardPartner.packageName, null)
                    intent.data = uri
                    startActivity(intent)

                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }

            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }


    private fun toast(message: String) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode==Activity.RESULT_OK) {
            when (requestCode) {
                PICK_BUSINESS_CODE -> {
                    businessImageUri = data?.data
                    binding.profileFileName.text = getFileName(businessImageUri, dashboardPartner)
                    Log.d("IMAGE_URI", "BUSINESS PROFILE: $businessImageUri")

                    val inputStream =
                        dashboardPartner.contentResolver.openInputStream(businessImageUri!!)
                    val myBitmap = BitmapFactory.decodeStream(inputStream)
                    val stream = ByteArrayOutputStream()
                    myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val bytes = stream.toByteArray()
                    businessImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
                    inputStream!!.close()

                    val imageView: ImageView = binding.shopLogo
                    businessImageUri = loadBitmapByPicasso(dashboardPartner, myBitmap, imageView)

                }

                CAMERA_BUSINESS_CODE -> {
                    try {
                        if (data!=null) {
                            val myBitmap = data.extras?.get("data") as Bitmap

                            val byteArrayOutputStream = ByteArrayOutputStream()
                            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                            val bytes = byteArrayOutputStream.toByteArray()
                            businessImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
                            Log.d("IMAGE_URI", "BUSINESS PROFILE: $businessImageUri")

                            val imageView: ImageView = binding.shopLogo
                            businessImageUri =
                                loadBitmapByPicasso(dashboardPartner, myBitmap, imageView)
                        }
                    } catch (e: Exception) {
                        Log.d("CAMERA", e.toString())
                        e.printStackTrace()
                    }
                }
                PICK_BIR_CODE -> {
                    businessBIRUri = data?.data
                    binding.businessBIRImageUri.visibility = View.VISIBLE
                    binding.businessBIRImageUri.text = getFileName(businessBIRUri, dashboardPartner)
                    Log.d("IMAGE_URI", "BUSINESS BIR: $businessBIRUri")

                    val inputStream =
                        dashboardPartner.contentResolver.openInputStream(businessBIRUri!!)
                    val myBitmap = BitmapFactory.decodeStream(inputStream)
                    val stream = ByteArrayOutputStream()
                    myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val bytes = stream.toByteArray()
                    businessBIRBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
                    inputStream!!.close()

                    val imageView: ImageView = binding.businessBIRImage
                    businessBIRUri = loadBitmapByPicasso(dashboardPartner, myBitmap, imageView)
                }

                CAMERA_BIR_CODE -> {
                    try {
                        if (data!=null) {
                            val myBitmap = data.extras?.get("data") as Bitmap

                            val byteArrayOutputStream = ByteArrayOutputStream()
                            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                            val bytes = byteArrayOutputStream.toByteArray()
                            businessBIRBytes = Base64.encodeToString(bytes, Base64.DEFAULT)

                            val imageView: ImageView = binding.businessBIRImage
                            businessBIRUri =
                                loadBitmapByPicasso(dashboardPartner, myBitmap, imageView)
                        }
                    } catch (e: Exception) {
                        Log.d("CAMERA", e.toString())
                        e.printStackTrace()
                    }
                }

                PICK_BANK_CODE -> {
                    bankImageUri = data?.data
                    binding.bankSlipImageUri.visibility = View.VISIBLE
                    binding.bankSlipImageUri.text = getFileName(bankImageUri, dashboardPartner)
                    Log.d("IMAGE_URI", "BANK SLIP: $bankImageUri")

                    val inputStream =
                        dashboardPartner.contentResolver.openInputStream(bankImageUri!!)
                    val myBitmap = BitmapFactory.decodeStream(inputStream)
                    val stream = ByteArrayOutputStream()
                    myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val bytes = stream.toByteArray()
                    bankImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
                    inputStream!!.close()

                    val imageView: ImageView = binding.bankSlipImage
                    bankImageUri = loadBitmapByPicasso(dashboardPartner, myBitmap, imageView)

                }

                CAMERA_BANK_CODE -> {
                    try {
                        if (data!=null) {
                            //we are using coroutine image loader (coil)
                            val myBitmap = data.extras?.get("data") as Bitmap

                            val byteArrayOutputStream = ByteArrayOutputStream()
                            myBitmap.compress(
                                Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream
                            )
                            val bytes = byteArrayOutputStream.toByteArray()
                            bankImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)

                            val bankView: ImageView = binding.bankSlipImage
                            bankImageUri = loadBitmapByPicasso(dashboardPartner, myBitmap, bankView)
                        }
                    } catch (e: Exception) {
                        Log.d("CAMERA", e.toString())
                        e.printStackTrace()
                    }
                }

            }
        }
    }

    private fun loadBitmapByPicasso(
        pContext: Context,
        pBitmap: Bitmap,
        pImageView: ImageView,
    ): Uri? {
        var imageUri: Uri? = null
        try {
            imageUri = Uri.fromFile(File.createTempFile("temp_profile", ".jpg", pContext.cacheDir))
            val outputStream: OutputStream? = pContext.contentResolver.openOutputStream(imageUri!!)
            pBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream!!.close()
            Picasso.with(context).load(imageUri).into(pImageView)
            Toast.makeText(context, "Great Image!", Toast.LENGTH_SHORT)
                .show()
        } catch (e: java.lang.Exception) {
            Log.e("LoadBitmapByPicasso", e.message!!)
        }
        return imageUri
    }


    @SuppressLint("Range")
    private fun getFileName(imageUri: Uri?, context: Context): String? {
        var filename: String? = null
        if (imageUri!!.scheme.equals("content")) {
            val cursor = context.contentResolver.query(imageUri, null, null, null, null)
            try {
                if (cursor!=null && cursor.moveToFirst()) {
                    filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }

            if (filename==null) {
                filename = imageUri.path
                val cut: Int = filename!!.lastIndexOf('/')
                if (cut!=-1) {
                    filename = filename.substring(cut + 1)
                }

            }

        }
        return filename
    }

    private fun setTime(view: View) {
        val textInputEditText: EditText = view as TextInputEditText
        val timePickerBinding = DialogTimePickerBinding.inflate(this.layoutInflater)

        var time: String? = null
        var h = timePickerBinding.timePicker.hour
        var m = timePickerBinding.timePicker.minute
        var am_pm = ""

        when {
            h==0 -> {
                h += 12
                am_pm = "AM"
            }
            h==12 -> am_pm = "PM"
            h > 12 -> {
                h -= 12
                am_pm = "PM"
            }
            else -> am_pm = "AM"
        }

        val hour = if (h < 10) "0$h" else h
        val min = if (m < 10) "0$m" else m
        // display format of time
        time = "$hour : $min $am_pm"

        timePickerBinding.timePicker.setOnTimeChangedListener { _, hour, minute ->
            var h = hour
            // AM_PM decider logic
            when {
                h==0 -> {
                    h += 12
                    am_pm = "AM"
                }
                h==12 -> am_pm = "PM"
                h > 12 -> {
                    h -= 12
                    am_pm = "PM"
                }
                else -> am_pm = "AM"
            }
            if (textInputEditText!=null) {
                val hour = if (h < 10) "0$h" else h
                val min = if (minute < 10) "0$minute" else minute
                // display format of time
                time = "$hour : $min $am_pm"
            }
        }

        timePickerBuilder = AlertDialog.Builder(context)
        timePickerBuilder.setCancelable(false)
        timePickerBuilder.setView(timePickerBinding.root)
        timePickerBuilder.setPositiveButton("Yes") { _, _ ->
            textInputEditText.setText(time)
        }
        // performing negative action
        timePickerBuilder.setNegativeButton("Cancel") { _, _ ->
        }
        timePickerBuilder.create()
        this.timePickerDialogInterface = timePickerBuilder.show()
    }

    private fun btnSubmitOnClickListener() {
        getDataFromFields()
        if (errorFields()) {
            Toast.makeText(
                context,
                "Please check error field(s)!",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            saveBusinessInfo()
        }
    }


    private fun validEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidAddress(address: String): Boolean {
        var addresses: List<Address>? = null
        var locality: String? = null
        val trap = false
        if (address.isNotEmpty()) {
            var geocoder = Geocoder(binding.root.context)
            try {
                addresses = geocoder.getFromLocationName(address, 1)
            } catch (e: Exception) {
                Log.d("SEARCH_GEO_LOCATION", "${e.message}")
            }

            if (addresses!=null && addresses.isNotEmpty()) {
                locality = addresses[0].locality
                Log.d("SEARCH_GEO_LOCATION > $address", addresses[0].getAddressLine(0))
            } else {
                Log.d("CITY AVAILABILITY", "NO AVAILABLE FROM SELECTED > $address")
            }
        }
        return addresses!!.isEmpty()
    }

    private fun getDataFromFields() {
        val bindingBusinessInfo = binding
        businessName = bindingBusinessInfo.businessName.text.toString()
        businessLegalName = bindingBusinessInfo.businessLegalName.text.toString()
        businessEmail = bindingBusinessInfo.businessEmail.text.toString()
        businessPhone = bindingBusinessInfo.businessPhone.text.toString()
        businessAddress = bindingBusinessInfo.businessAddress.text.toString()

        businessHoursMondayFrom = closeIfEmpty(bindingBusinessInfo.fromMonday.text.toString())
        businessHoursMondayTo = closeIfEmpty(bindingBusinessInfo.toMonday.text.toString())
        businessHoursTuesdayFrom = closeIfEmpty(bindingBusinessInfo.fromTuesday.text.toString())
        businessHoursTuesdayTo = closeIfEmpty(bindingBusinessInfo.toTuesday.text.toString())
        businessHoursWednesdayFrom = closeIfEmpty(bindingBusinessInfo.fromWednesday.text.toString())
        businessHoursWednesdayTo = closeIfEmpty(bindingBusinessInfo.toWednesday.text.toString())
        businessHoursThursdayFrom = closeIfEmpty(bindingBusinessInfo.fromThursday.text.toString())
        businessHoursThursdayTo = closeIfEmpty(bindingBusinessInfo.toThursday.text.toString())
        businessHoursFridayFrom = closeIfEmpty(bindingBusinessInfo.fromFriday.text.toString())
        businessHoursFridayTo = closeIfEmpty(bindingBusinessInfo.toFriday.text.toString())
        businessHoursSaturdayFrom = closeIfEmpty(bindingBusinessInfo.fromSaturday.text.toString())
        businessHoursSaturdayTo = closeIfEmpty(bindingBusinessInfo.toSaturday.text.toString())
        businessHoursSundayFrom = closeIfEmpty(bindingBusinessInfo.fromSunday.text.toString())
        businessHoursSundayTo = closeIfEmpty(bindingBusinessInfo.toSunday.text.toString())
        businessHoursHolidayFrom = closeIfEmpty(bindingBusinessInfo.fromHoliday.text.toString())
        businessHoursHolidayTo = closeIfEmpty(bindingBusinessInfo.toHoliday.text.toString())

        businessBankName = bindingBusinessInfo.bankName.text.toString()
        businessBankAccountName = bindingBusinessInfo.bankAccNameHolder.text.toString()
        businessBankAccNo = bindingBusinessInfo.bankAccNo.text.toString()
        businessBankBIC = bindingBusinessInfo.bankBIC.text.toString()

    }

    private fun closeIfEmpty(text: String): String? {
        return if (text.isEmpty()) {
            "Closed"
        } else {
            text
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun errorFields(): Boolean {

        var invalid = false
        val bindingBusinessInfo = binding

        if (businessName!!.isEmpty() || businessLegalName!!.isEmpty() || businessEmail!!.isEmpty() || !validEmail(
                businessEmail!!
            ) || businessPhone!!.isEmpty() ||
            businessPhone!!.length!=13 || businessAddress!!.isEmpty() || businessBankName!!.isEmpty() || businessBankAccountName!!.isEmpty() ||
            businessBankAccNo!!.isEmpty() || businessBankBIC!!.isEmpty()
        ) {
            Log.d("EMPTY_VALIDATION", "PLEASE CHECK EMTPY FIELDS")
            if (businessName!!.isEmpty()) {
                bindingBusinessInfo.businessName.error = "Please enter your Business Name."
            }
            if (businessLegalName!!.isEmpty()) {
                bindingBusinessInfo.businessLegalName.error =
                    "Please enter your Business Legal Name."
            }
            if (businessEmail!!.isEmpty() || !validEmail(businessEmail!!)) {
                bindingBusinessInfo.businessEmail.error = "Please enter valid Business Email."
            }
            if (businessPhone!!.isEmpty()) {
                bindingBusinessInfo.businessPhone.error = "Please enter your Business Phone."
            }
            if (businessPhone!!.length!=13) {
                bindingBusinessInfo.businessPhone.error = "Please check length of Mobile No."
            }
            if (businessAddress!!.isEmpty()) {
                bindingBusinessInfo.businessAddress.error = "Please enter your Business Address."
                if (isValidAddress(businessAddress!!)) {
                    bindingBusinessInfo.businessAddress.error =
                        "Please enter a valid Business Address."
                }
            }

            if (businessBankName!!.isEmpty()) {
                bindingBusinessInfo.bankName.error = "Please enter your Bank Name."
            }
            if (businessBankAccountName!!.isEmpty()) {
                bindingBusinessInfo.bankAccNameHolder.error =
                    "Please enter your Bank Account Name."
            }
            if (businessBankBIC!!.isEmpty()) {
                bindingBusinessInfo.bankBIC.error = "Please enter your Bank Account BIC."
            }
            invalid = true
        }
        if (businessHoursMondayFrom!!.isNotEmpty()) {
            if (businessHoursMondayTo!!.isEmpty()) {
                bindingBusinessInfo.toMonday.error = "Please enter Monday to Time."
                invalid = true
            }
        } else if (businessHoursMondayTo!!.isNotEmpty()) {
            if (businessHoursMondayFrom!!.isEmpty()) {
                bindingBusinessInfo.fromMonday.error = "Please enter Monday from Time."
                invalid = true
            }
        }

        if (businessHoursTuesdayFrom!!.isNotEmpty()) {
            if (businessHoursTuesdayTo!!.isEmpty()) {
                bindingBusinessInfo.toTuesday.error = "Please enter Tuesday to Time."
                invalid = true
            }
        } else if (businessHoursTuesdayTo!!.isNotEmpty()) {
            if (businessHoursTuesdayFrom!!.isEmpty()) {
                bindingBusinessInfo.fromTuesday.error = "Please enter Tuesday from Time."
                invalid = true
            }
        }

        if (businessHoursWednesdayFrom!!.isNotEmpty()) {
            if (businessHoursWednesdayTo!!.isEmpty()) {
                bindingBusinessInfo.toWednesday.error = "Please enter Wednesday to Time."
                invalid = true
            }
        } else if (businessHoursWednesdayTo!!.isNotEmpty()) {
            if (businessHoursWednesdayFrom!!.isEmpty()) {
                bindingBusinessInfo.fromWednesday.error = "Please enter Wednesday from Time."
                invalid = true
            }
        }

        if (businessHoursThursdayFrom!!.isNotEmpty()) {
            if (businessHoursThursdayTo!!.isEmpty()) {
                bindingBusinessInfo.toThursday.error = "Please enter Thursday to Time."
                invalid = true
            }
        } else if (businessHoursThursdayTo!!.isNotEmpty()) {
            if (businessHoursThursdayFrom!!.isEmpty()) {
                bindingBusinessInfo.fromThursday.error = "Please enter Thursday from Time."
                invalid = true
            }
        }

        if (businessHoursFridayFrom!!.isNotEmpty()) {
            if (businessHoursFridayTo!!.isEmpty()) {
                bindingBusinessInfo.toFriday.error = "Please enter Friday to Time."
                invalid = true
            }
        } else if (businessHoursFridayTo!!.isNotEmpty()) {
            if (businessHoursFridayFrom!!.isEmpty()) {
                bindingBusinessInfo.fromFriday.error = "Please enter Friday from Time."
                invalid = true
            }
        }

        if (businessHoursSaturdayFrom!!.isNotEmpty()) {
            if (businessHoursSaturdayTo!!.isEmpty()) {
                bindingBusinessInfo.toSaturday.error = "Please enter Saturday to Time."
                invalid = true
            }
        } else if (businessHoursSaturdayTo!!.isNotEmpty()) {
            if (businessHoursSaturdayFrom!!.isEmpty()) {
                bindingBusinessInfo.fromSaturday.error = "Please enter Saturday from Time."
                invalid = true
            }
        }

        if (businessHoursSundayFrom!!.isNotEmpty()) {
            if (businessHoursSundayTo!!.isEmpty()) {
                bindingBusinessInfo.toSunday.error = "Please enter Sunday to Time."
                invalid = true
            }
        } else if (businessHoursSundayTo!!.isNotEmpty()) {
            if (businessHoursSundayFrom!!.isEmpty()) {
                bindingBusinessInfo.fromSunday.error = "Please enter Sunday from Time."
                invalid = true
            }
        }

        if (businessHoursHolidayFrom!!.isNotEmpty()) {
            if (businessHoursHolidayTo!!.isEmpty()) {
                bindingBusinessInfo.toHoliday.error = "Please enter Holiday to Time."
                invalid = true
            }
        } else if (businessHoursHolidayTo!!.isNotEmpty()) {
            if (businessHoursHolidayFrom!!.isEmpty()) {
                bindingBusinessInfo.fromHoliday.error = "Please enter Holiday from Time."
                invalid = true
            }
        }
        return invalid
    }


    private fun saveBusinessInfo() {

        dashboardPartner.showLoadingDialog()

        val databaseRef = firebaseDatabase.reference.child("shop")
            .child(firebaseAuth.currentUser!!.uid)

        shop = Shop(
            firebaseAuth.currentUser!!.uid,
            businessImageUri.toString(),
            businessName,
            businessLegalName,
            businessEmail,
            businessPhone,
            businessBIRUri.toString(),
            businessAddress,
            businessHoursMondayFrom,
            businessHoursMondayTo,
            businessHoursTuesdayFrom,
            businessHoursTuesdayTo,
            businessHoursWednesdayFrom,
            businessHoursWednesdayTo,
            businessHoursThursdayFrom,
            businessHoursThursdayTo,
            businessHoursFridayFrom,
            businessHoursFridayTo,
            businessHoursSaturdayFrom,
            businessHoursSaturdayTo,
            businessHoursSundayFrom,
            businessHoursSundayTo,
            businessHoursHolidayFrom,
            businessHoursHolidayTo,
            businessBankName,
            businessBankAccountName,
            businessBankAccNo,
            businessBankBIC,
            bankImageUri.toString(),
        )
        //businessInfo.printLOG()

        databaseRef.setValue(shop).addOnCompleteListener(dashboardPartner) { task ->
            if (task.isSuccessful) {
                Log.d("BUSINESS_UPDATE_SUCCESS", "Business has been successfully Updated!")
                saveLogo()
                saveBIRImage()
                saveBankImage()
                dashboardPartner.dismissLoadingDialog()
                Toast.makeText(
                    context,
                    "Business has been successfully Updated!",
                    Toast.LENGTH_SHORT
                ).show()
                dashboardPartner.setShop(shop)
                setShopDetails(shop)
            } else {
                dashboardPartner.dismissLoadingDialog()
                Log.d("BUSINESS -> addOnCompleteListener", task.exception!!.message!!)
            }

        }
    }

    private fun saveLogo() {
        if (businessImageUri!=null) {
            val businessImageFilename = "business_${shop!!.uid}"
            saveFileToFirebaseStorage(
                "shop/${shop!!.uid}/$businessImageFilename", businessImageFilename,
                businessImageUri!!
            )
        }
    }

    private fun saveBIRImage() {
        if (businessBIRUri!=null) {
            val businessBIRFilename = "BIR_Cert_${shop!!.uid}"
            saveFileToFirebaseStorage(
                "shop/${shop!!.uid}/$businessBIRFilename", businessBIRFilename,
                businessBIRUri!!
            )
        }
        saveBankImage()
    }

    private fun saveBankImage() {
        if (bankImageUri!=null) {
            val businessBankFilename = "bank_proof_${shop!!.uid}"
            saveFileToFirebaseStorage(
                "shop/${shop!!.uid}/$businessBankFilename", businessBankFilename,
                bankImageUri!!
            )
        }
    }

    private fun saveFileToFirebaseStorage(
        location: String,
        ImageFilename: String,
        ImageUri: Uri,
    ) {
        val firebaseStorageReference = FirebaseStorage.getInstance().getReference(location)
        firebaseStorageReference.putFile(ImageUri)
            .addOnSuccessListener {
                Log.d("SAVING_IMAGE", "SUCCESS SAVING $ImageFilename")
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "FAILED SAVING IMAGE > ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("SAVING_IMAGE", "FAILED SAVING IMAGE $ImageFilename > ${it.message}")
            }
    }
}

