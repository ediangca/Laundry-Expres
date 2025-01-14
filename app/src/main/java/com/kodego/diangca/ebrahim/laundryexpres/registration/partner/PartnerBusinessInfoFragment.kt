package com.kodego.diangca.ebrahim.laundryexpres.registration.partner

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import ProgressDialog
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import coil.load
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kodego.diangca.ebrahim.laundryexpres.classes.TouchEventView
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogTimePickerBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentPartnerBusinessInfoBinding
import java.io.ByteArrayOutputStream
import com.karumi.dexter.Dexter
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ProgressDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream


class PartnerBusinessInfoFragment(var registerPartnerActivity: RegisterPartnerActivity) :
    Fragment() {

    var bindingBusinessInfo: FragmentPartnerBusinessInfoBinding? = null
    val binding get() = bindingBusinessInfo!!

    private lateinit var loadingBuilder: AlertDialog.Builder
    private lateinit var loadingDialog: Dialog

    private lateinit var firebaseStorageRef: StorageReference

    private lateinit var timePickerBuilder: AlertDialog.Builder
    private lateinit var timePickerDialogInterface: DialogInterface

    private var businessImageUri: Uri? = null
    private var bankImageUri: Uri? = null

    var businessImageBytes: String? = null
    var bankImageBytes: String? = null

    private val PICK_BUSINESS_CODE = 100
    private val PICK_BANK_CODE = 200
    private val CAMERA_BUSINESS_CODE = 1001
    private val CAMERA_BANK_CODE = 1002


    @JvmName("getBinding1")
    fun getBinding(): FragmentPartnerBusinessInfoBinding {
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        bindingBusinessInfo =
            FragmentPartnerBusinessInfoBinding.inflate(layoutInflater, container, false)
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

        firebaseStorageRef =
            FirebaseStorage.getInstance().reference.child(System.currentTimeMillis().toString())

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

    private fun btnCaptureBusinessBIROnClickListener() {
        cameraCheckPermission(CAMERA_BUSINESS_CODE)
    }


    private fun btnCaptureBankSlipOnClickListener() {
        cameraCheckPermission(CAMERA_BANK_CODE)
    }

    private fun cameraCheckPermission(code: Int) {
        Dexter.withContext(registerPartnerActivity)
            .withPermissions(
                android.Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            camera(code)  // Proceed to camera if permissions are granted
                        } else {
                            // Log which permission was denied
                            val deniedPermissions = it.deniedPermissionResponses
                                .joinToString(", ") { permission -> permission.permissionName }

                            // Toast the denied permissions to understand what went wrong
                            toast("Permissions Denied: $deniedPermissions")

                            // Optionally, display a rationale if permissions are denied
                            if (it.isAnyPermissionPermanentlyDenied) {
                                // This will be invoked if the user selected "Don't ask again"
                                toast("Some permissions are permanently denied. Please enable them in settings.")
                            }
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRotationalDialogForPermission()
                }
            }).onSameThread().check()
    }

    private fun camera(code: Int) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, code)
    }

    private fun btnBrowseBusinessBIROnClickListener() {
        val gallery = Intent()
        gallery.type = "image/*"
        gallery.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(gallery, PICK_BUSINESS_CODE)
    }

    private fun btnBrowseBankSlipOnClickListener() {
//        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
//        startActivityForResult(gallery, PICK_BANK_CODE)
        val gallery = Intent()
        gallery.type = "image/*"
        gallery.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(gallery, PICK_BANK_CODE)
    }

    private fun showRotationalDialogForPermission() {
        AlertDialog.Builder(registerPartnerActivity)
            .setMessage(
                "It looks like you have turned off permissions"
                        + "required for this feature. It can be enable under App settings!!!"
            )

            .setPositiveButton("Go TO SETTINGS") { _, _ ->

                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", registerPartnerActivity.packageName, null)
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
            registerPartnerActivity,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_BUSINESS_CODE -> {
//                    businessImageUri = data?.data
//                    binding.businessBIRImageUri.visibility = View.VISIBLE
//                    binding.businessBIRImageUri.text = getFileName(businessImageUri, registerPartnerActivity)
//                    Log.d("IMAGE_URI", "BUSINESS BIR: $businessImageUri")
//
//                    val inputStream = registerPartnerActivity.contentResolver.openInputStream(businessImageUri!!)
//                    val myBitmap = BitmapFactory.decodeStream(inputStream)
//                    val stream = ByteArrayOutputStream()
//                    myBitmap.compress(Bitmap.CompressFormat.PNG, 100,stream)
//                    val bytes = stream.toByteArray()
//                    businessImageBytes = Base64.encodeToString(bytes,Base64.DEFAULT)
//                    binding.businessBIRImage.visibility = View.VISIBLE
//                    binding.businessBIRImage.setImageBitmap(myBitmap)
//                    inputStream!!.close()
//                    registerPartnerActivity.dismissLoadingDialog()
//                    Toast.makeText(registerPartnerActivity,"Image Selected for BIR", Toast.LENGTH_SHORT).show()


                    showLoadingDialog()

                    // Use Coroutine to run image processing on a background thread
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            if (data != null) {
                                businessImageUri = data.data
                                // Process the image asynchronously on a background thread
                                val bitmap = withContext(Dispatchers.IO) {
                                    // Process the image
                                    processImage(data.data)
                                }

                                // After processing, update the UI with the result
                                bitmap?.let {
                                    binding.businessBIRImageUri.visibility = View.VISIBLE
                                    binding.businessBIRImageUri.text =
                                        getFileName(data.data, registerPartnerActivity)
                                    Log.d("IMAGE_URI", "BUSINESS BIR: ${data.data}")

                                    binding.businessBIRImage.visibility = View.VISIBLE
                                    binding.businessBIRImage.setImageBitmap(it)

                                    // Convert the bitmap to Base64 for later use
                                    businessImageBytes = convertBitmapToBase64(it)

                                    Toast.makeText(
                                        registerPartnerActivity,
                                        "Image Selected for BIR",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dismissLoadingDialog()
                                } ?: run {
                                    // If the bitmap is null, show a message to the user
                                    Toast.makeText(
                                        registerPartnerActivity,
                                        "Failed to load image",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dismissLoadingDialog()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Handle errors and notify the user
                            Toast.makeText(
                                registerPartnerActivity,
                                "Error processing the image",
                                Toast.LENGTH_SHORT
                            ).show()
                        } finally {
                            // Hide the progress dialog when done
                            dismissLoadingDialog()
                        }
                    }
                }

                CAMERA_BUSINESS_CODE -> {
                    try {
                        if (data != null) {
                            //we are using coroutine image loader (coil)
                            val bitmap = data.extras?.get("data") as Bitmap

                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                byteArrayOutputStream
                            )
                            val bytes = byteArrayOutputStream.toByteArray()
                            businessImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
                            binding.businessBIRImage.visibility = View.VISIBLE
                            binding.businessBIRImage.load(bitmap) {
                                crossfade(true)
                                crossfade(1000)
//                        transformations(CircleCropTransformation())
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("CAMERA", e.toString())
                        e.printStackTrace()
                    }
                }

                PICK_BANK_CODE -> {
//                    bankImageUri = data?.data
//                    binding.bankSlipImageUri.visibility = View.VISIBLE
//                    binding.bankSlipImageUri.text = getFileName(bankImageUri, registerPartnerActivity)
//                    Log.d("IMAGE_URI", "BANK SLIP: $bankImageUri")
////                    binding.bankSlipImage.setImageURI(imageUri)
//
//                    val inputStream = registerPartnerActivity.contentResolver.openInputStream(bankImageUri!!)
//                    val myBitmap = BitmapFactory.decodeStream(inputStream)
//                    val stream = ByteArrayOutputStream()
//                    myBitmap.compress(Bitmap.CompressFormat.PNG, 100,stream)
//                    val bytes = stream.toByteArray()
//                    bankImageBytes = Base64.encodeToString(bytes,Base64.DEFAULT)
//                    binding.bankSlipImage.visibility = View.VISIBLE
//                    binding.bankSlipImage.setImageBitmap(myBitmap)
//                    inputStream!!.close()
//                    Toast.makeText(registerPartnerActivity,"Image Selected for Proof Bank", Toast.LENGTH_SHORT).show()

                    showLoadingDialog()
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            if (data != null) {
                                bankImageUri = data.data
                                // Process the image asynchronously on a background thread
                                val bitmap = withContext(Dispatchers.IO) {
                                    // Process the image
                                    processImage(data.data)
                                }

                                // After processing, update the UI with the result
                                bitmap?.let {
                                    binding.bankSlipImageUri.visibility = View.VISIBLE
                                    binding.bankSlipImageUri.text =
                                        getFileName(data.data, registerPartnerActivity)
                                    Log.d("IMAGE_URI", "BANK SLIP: ${data.data}")

                                    binding.bankSlipImage.visibility = View.VISIBLE
                                    binding.bankSlipImage.setImageBitmap(it)

                                    // Convert the bitmap to Base64 for later use
                                    bankImageBytes = convertBitmapToBase64(it)

                                    Toast.makeText(
                                        registerPartnerActivity,
                                        "Image Selected for Bank Slip",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dismissLoadingDialog()
                                } ?: run {
                                    // If the bitmap is null, show a message to the user
                                    Toast.makeText(
                                        registerPartnerActivity,
                                        "Failed to load image",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dismissLoadingDialog()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Handle errors and notify the user
                            Toast.makeText(
                                registerPartnerActivity,
                                "Error processing the image",
                                Toast.LENGTH_SHORT
                            ).show()
                        } finally {
                            // Hide the progress dialog when done
                            dismissLoadingDialog()
                        }
                    }
                }

                CAMERA_BANK_CODE -> {
                    try {
                        if (data != null) {
                            //we are using coroutine image loader (coil)
                            val bitmap = data.extras?.get("data") as Bitmap

                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                byteArrayOutputStream
                            )
                            val bytes = byteArrayOutputStream.toByteArray()
                            bankImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
                            binding.bankSlipImage.visibility = View.VISIBLE
                            binding.bankSlipImage.load(bitmap) {
                                crossfade(true)
                                crossfade(1000)
//                        transformations(CircleCropTransformation())
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("CAMERA", e.toString())
                        e.printStackTrace()
                    }
                }

            }

        }
    }

    // Function to process the image and load it
    suspend fun processImage(imageUri: Uri?): Bitmap? {
        if (imageUri == null) return null

        // Open the InputStream to decode the image
        val inputStream: InputStream? =
            registerPartnerActivity.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        inputStream?.close() // Ensure InputStream is closed properly

        return bitmap
    }

    // Function to convert Bitmap to Base64
    fun convertBitmapToBase64(bitmap: Bitmap?): String {
        if (bitmap == null) return ""

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)  // Adjust compression if needed
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    @SuppressLint("Range")
    private fun getFileName(imageUri: Uri?, context: Context): String? {
        var filename: String? = null
        if (imageUri!!.scheme.equals("content")) {
            val cursor = context.contentResolver.query(imageUri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }

            if (filename == null) {
                filename = imageUri.path
                val cut: Int = filename!!.lastIndexOf('/')
                if (cut != -1) {
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
        time = "$hour : $min $am_pm"

        timePickerBinding.timePicker.setOnTimeChangedListener { _, hour, minute ->
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
            if (textInputEditText != null) {
                val hour = if (h < 10) "0$h" else h
                val min = if (minute < 10) "0$minute" else minute
                // display format of time
                time = "$hour : $min $am_pm"
            }
        }

        timePickerBuilder = AlertDialog.Builder(registerPartnerActivity)
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
        if (registerPartnerActivity.checkFields()) {
            Toast.makeText(
                registerPartnerActivity,
                "Please check error field(s)!",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            registerPartnerActivity.saveInfoToFirebase()
        }
    }

    fun showLoadingDialog() {
        val loadingBinding = ProgressDialogBinding.inflate(this.layoutInflater)
        loadingBuilder = AlertDialog.Builder(registerPartnerActivity)
        loadingBuilder.setCancelable(false)
        loadingBuilder.setView(loadingBinding.root)
        loadingBuilder.create()
        loadingDialog = loadingBuilder.create()
        loadingDialog.window!!.setLayout(150,150)
        loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        loadingDialog.show()
    }

    fun dismissLoadingDialog() {

        loadingDialog.hide()
        loadingDialog.dismiss()
    }
}