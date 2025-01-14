package com.kodego.diangca.ebrahim.laundryexpres.registration.rider

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import coil.load
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kodego.diangca.ebrahim.laundryexpres.classes.TouchEventView
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentPartnerBusinessInfoBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentRiderRequirementsBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ProgressDialogBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class RiderRequirementsFragment(var registerRiderActivity: RegisterRiderActivity) : Fragment() {

    var _binding: FragmentRiderRequirementsBinding? = null
    val binding get() = _binding!!

    private lateinit var loadingBuilder: AlertDialog.Builder
    private lateinit var loadingDialog: Dialog

    private lateinit var firebaseStorageRef: StorageReference

    var selfieImageUri: Uri? = null
    var nbiImageUri: Uri? = null
    var licenseImageUri: Uri? = null
    var orImageUri: Uri? = null
    var crImageUri: Uri? = null
    var vehicleImageUri: Uri? = null

    var selfieImageBytes: String? = null
    var nbiImageBytes: String? = null
    var licenseImageBytes: String? = null
    var orImageBytes: String? = null
    var crImageBytes: String? = null
    var vehicleImageBytes: String? = null

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private var currentRequestCode: Int = -1
    private val CAMERA_SELFIE_CODE = 1001
    private val CAMERA_NBI_CODE = 1002
    private val CAMERA_LICENSE_CODE = 1003
    private val CAMERA_OR_CODE = 1004
    private val CAMERA_CR_CODE = 1005
    private val CAMERA_VEHICLE_CODE = 1006

    private val PICK_SELFIE_CODE = 100
    private val PICK_NBI_CODE = 200
    private val PICK_LICENSE_CODE = 300
    private val PICK_OR_CODE = 400
    private val PICK_CR_CODE = 500
    private val PICK_VEHICLE_CODE = 600

    private var currentItem = 0


    @JvmName("getBinding1")
    fun getBinding(): FragmentRiderRequirementsBinding {
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRiderRequirementsBinding.inflate(layoutInflater, container, false)
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

        initializeCameraLauncher()

        binding.btnCaptureSelf.setOnClickListener {
            btnCaptureSelfOnClickListener()
        }
        binding.btnCaptureNBI.setOnClickListener {
            btnCaptureNBIOnClickListener()
        }
        binding.btnBrowseNBI.setOnClickListener {
            btnBrowseNBIOnClickListener()
        }
        binding.btnCaptureLicense.setOnClickListener {
            btnCaptureLicenseOnClickListener()
        }
        binding.btnBrowseLicense.setOnClickListener {
            btnBrowseLicenseOnClickListener()
        }
        binding.btnCaptureOR.setOnClickListener {
            btnCaptureOROnClickListener()
        }
        binding.btnBrowseOR.setOnClickListener {
            btnBrowseOROnClickListener()
        }
        binding.btnCaptureCR.setOnClickListener {
            btnCaptureCROnClickListener()
        }
        binding.btnBrowseCR.setOnClickListener {
            btnBrowseCROnClickListener()
        }
        binding.btnCaptureVehicle.setOnClickListener {
            btnCaptureVehicleOnClickListener()
        }
        binding.btnBrowseVehicle.setOnClickListener {
            btnBrowseVehicleOnClickListener()
        }
        binding.btnSubmit.setOnClickListener {
            btnSubmitOnClickListener()
        }

    }

    private fun initializeCameraLauncher() {
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val extras = result.data!!.extras
                if (extras != null && extras.containsKey("data")) {
                    val bitmap = extras.get("data") as Bitmap

                    val inputStream =
                        result.data!!.data?.let {
                            registerRiderActivity.contentResolver.openInputStream(
                                it
                            )
                        }
                    val myBitmap = BitmapFactory.decodeStream(inputStream)
                    processCapturedImage(bitmap, currentRequestCode)

                } else {
                    toast("Failed to capture the image.")
                }
            } else {
                toast("Image capture was cancelled.")
            }
        }
    }


    private fun btnCaptureSelfOnClickListener() {
        cameraCheckPermission(CAMERA_SELFIE_CODE)
    }

    private fun btnCaptureNBIOnClickListener() {
        cameraCheckPermission(CAMERA_NBI_CODE)
    }

    private fun btnBrowseNBIOnClickListener() {
        openGallery(PICK_NBI_CODE)
    }

    private fun btnCaptureLicenseOnClickListener() {
        cameraCheckPermission(CAMERA_LICENSE_CODE)
    }

    private fun btnBrowseLicenseOnClickListener() {
        openGallery(PICK_LICENSE_CODE)
    }

    private fun btnCaptureOROnClickListener() {
        cameraCheckPermission(CAMERA_OR_CODE)
    }

    private fun btnBrowseOROnClickListener() {
        openGallery(PICK_OR_CODE)
    }

    private fun btnCaptureCROnClickListener() {
        cameraCheckPermission(CAMERA_CR_CODE)
    }

    private fun btnBrowseCROnClickListener() {
        openGallery(PICK_CR_CODE)
    }

    private fun btnCaptureVehicleOnClickListener() {
        cameraCheckPermission(CAMERA_VEHICLE_CODE)
    }

    private fun btnBrowseVehicleOnClickListener() {
        openGallery(PICK_VEHICLE_CODE)
    }

    private fun openGallery(code: Int) {
        val gallery = Intent()
        gallery.type = "image/*"
        gallery.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(gallery, code)
    }


//    private fun cameraCheckPermission(code: Int) {
//        Dexter.withContext(registerRiderActivity)
//            .withPermissions(
//                android.Manifest.permission.CAMERA
//            ).withListener(object : MultiplePermissionsListener {
//                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
//                    report?.let {
//                        if (report.areAllPermissionsGranted()) {
//                            camera(code)  // Proceed to camera if permissions are granted
//                        } else {
//                            // Log which permission was denied
//                            val deniedPermissions = it.deniedPermissionResponses
//                                .joinToString(", ") { permission -> permission.permissionName }
//
//                            // Toast the denied permissions to understand what went wrong
//                            toast("Permissions Denied: $deniedPermissions")
//
//                            // Optionally, display a rationale if permissions are denied
//                            if (it.isAnyPermissionPermanentlyDenied) {
//                                // This will be invoked if the user selected "Don't ask again"
//                                toast("Some permissions are permanently denied. Please enable them in settings.")
//                            }
//                        }
//                    }
//                }
//
//                override fun onPermissionRationaleShouldBeShown(
//                    permissions: MutableList<PermissionRequest>?,
//                    token: PermissionToken?
//                ) {
//                    showRotationalDialogForPermission()
//                }
//            }).onSameThread().check()
//    }


    private fun cameraCheckPermission(code: Int) {
        Dexter.withContext(registerRiderActivity)
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


    private fun toast(message: String) {
        Toast.makeText(
            registerRiderActivity,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun camera(requestCode: Int) {
        currentRequestCode = requestCode // Store the request code
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            toast("Unable to open the camera. Please try again.")
        }
    }

    // Function to process the image and load it
    suspend fun processImage(imageUri: Uri?): Bitmap? {
        if (imageUri == null) return null

        // Open the InputStream to decode the image
        val inputStream: InputStream? =
            registerRiderActivity.contentResolver.openInputStream(imageUri)
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

    private fun showRotationalDialogForPermission() {
        AlertDialog.Builder(registerRiderActivity)
            .setMessage(
                "It looks like you have turned off permissions"
                        + "required for this feature. It can be enable under App settings!!!"
            )

            .setPositiveButton("Go TO SETTINGS") { _, _ ->

                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", registerRiderActivity.packageName, null)
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

    fun showLoadingDialog() {
        val loadingBinding = ProgressDialogBinding.inflate(this.layoutInflater)
        loadingBuilder = AlertDialog.Builder(registerRiderActivity)
        loadingBuilder.setCancelable(false)
        loadingBuilder.setView(loadingBinding.root)
        loadingBuilder.create()
        loadingDialog = loadingBuilder.create()
        if (loadingDialog.window != null) {
            loadingDialog.window!!.setLayout(150, 150)
            loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        loadingDialog.show()
    }

    fun dismissLoadingDialog() {

        loadingDialog.hide()
        loadingDialog.dismiss()
    }

    private fun processCapturedImage(bitmap: Bitmap, requestCode: Int) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        val imageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)

        when (requestCode) {
            CAMERA_SELFIE_CODE -> {
                selfieImageBytes = imageBytes
                binding.selfieImage.visibility = View.VISIBLE
                binding.selfieImage.load(bitmap) {
                    crossfade(true)
                    crossfade(1000)
                }
                loadBitmapByPicasso(registerRiderActivity, "profile", bitmap)
            }

            CAMERA_NBI_CODE -> {
                nbiImageBytes = imageBytes
                binding.nbiImage.visibility = View.VISIBLE
                binding.nbiImage.load(bitmap) {
                    crossfade(true)
                    crossfade(1000)
                }
                loadBitmapByPicasso(registerRiderActivity, "nbi", bitmap)
            }

            CAMERA_LICENSE_CODE -> {
                licenseImageBytes = imageBytes
                binding.licenseImage.visibility = View.VISIBLE
                binding.licenseImage.load(bitmap) {
                    crossfade(true)
                    crossfade(1000)
                }
                loadBitmapByPicasso(registerRiderActivity, "license", bitmap)
            }

            CAMERA_OR_CODE -> {
                orImageBytes = imageBytes
                binding.orImage.visibility = View.VISIBLE
                binding.orImage.load(bitmap) {
                    crossfade(true)
                    crossfade(1000)
                }
                loadBitmapByPicasso(registerRiderActivity, "or", bitmap)
            }

            CAMERA_CR_CODE -> {
                crImageBytes = imageBytes
                binding.crImage.visibility = View.VISIBLE
                binding.crImage.load(bitmap) {
                    crossfade(true)
                    crossfade(1000)
                }
                loadBitmapByPicasso(registerRiderActivity, "cr", bitmap)
            }

            CAMERA_VEHICLE_CODE -> {
                vehicleImageBytes = imageBytes
                binding.vehicleImage.visibility = View.VISIBLE
                binding.vehicleImage.load(bitmap) {
                    crossfade(true)
                    crossfade(1000)
                }
                loadBitmapByPicasso(registerRiderActivity, "vehicle", bitmap)
            }

            else -> {
                toast("Unknown request code.")
            }
        }
    }

    private fun loadBitmapByPicasso(
        pContext: Context,
        file: String,
        pBitmap: Bitmap
    ) {
        try {
            var imageUri: Uri =
                Uri.fromFile(File.createTempFile("temp_$file", ".jpg", pContext.cacheDir))
            when(file){
                "profile" -> {selfieImageUri = imageUri}
                "nbi" -> {nbiImageUri = imageUri}
                "license" -> {licenseImageUri = imageUri}
                "or" -> {orImageUri = imageUri}
                "cr" -> {crImageUri = imageUri}
                "vehicle" -> {vehicleImageUri = imageUri}
            }
            Log.d("LOAD_USER_${file}_URI", imageUri.toString())
            val outputStream: OutputStream? =
                pContext.contentResolver.openOutputStream(imageUri)
            if (outputStream != null) {
                pBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

                Log.d("LOAD_USER_FILE_${file}", imageUri.toString())
            }
            outputStream!!.close()
            toast("Great Profile!")
        } catch (e: java.lang.Exception) {
            Log.e("LoadBitmapByPicasso", e.message!!)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            try {
                when (requestCode) {
//
//                    CAMERA_SELFIE_CODE -> {
//                        //we are using coroutine image loader (coil)
//                        val bitmap = data.extras?.get("data") as Bitmap
//
//                        val byteArrayOutputStream = ByteArrayOutputStream()
//                        bitmap.compress(
//                            Bitmap.CompressFormat.PNG,
//                            100,
//                            byteArrayOutputStream
//                        )
//                        val bytes = byteArrayOutputStream.toByteArray()
//                        selfieImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
//                        binding.selfieImage.visibility = View.VISIBLE
//                        binding.selfieImage.load(bitmap) {
//                            crossfade(true)
//                            crossfade(1000)
//                        }
//                    }
//
//                    CAMERA_NBI_CODE -> {
//                        //we are using coroutine image loader (coil)
//                        val bitmap = data.extras?.get("data") as Bitmap
//
//                        val byteArrayOutputStream = ByteArrayOutputStream()
//                        bitmap.compress(
//                            Bitmap.CompressFormat.PNG,
//                            100,
//                            byteArrayOutputStream
//                        )
//                        val bytes = byteArrayOutputStream.toByteArray()
//                        nbiImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
//                        binding.nbiImage.visibility = View.VISIBLE
//                        binding.nbiImage.load(bitmap) {
//                            crossfade(true)
//                            crossfade(1000)
//                        }
//                    }
//
//                    CAMERA_LICENSE_CODE -> {
//                        //we are using coroutine image loader (coil)
//                        val bitmap = data.extras?.get("data") as Bitmap
//
//                        val byteArrayOutputStream = ByteArrayOutputStream()
//                        bitmap.compress(
//                            Bitmap.CompressFormat.PNG,
//                            100,
//                            byteArrayOutputStream
//                        )
//                        val bytes = byteArrayOutputStream.toByteArray()
//                        licenseImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
//                        binding.licenseImage.visibility = View.VISIBLE
//                        binding.licenseImage.load(bitmap) {
//                            crossfade(true)
//                            crossfade(1000)
//                        }
//                    }
//
//                    CAMERA_OR_CODE -> {
//                        //we are using coroutine image loader (coil)
//                        val bitmap = data.extras?.get("data") as Bitmap
//
//                        val byteArrayOutputStream = ByteArrayOutputStream()
//                        bitmap.compress(
//                            Bitmap.CompressFormat.PNG,
//                            100,
//                            byteArrayOutputStream
//                        )
//                        val bytes = byteArrayOutputStream.toByteArray()
//                        orImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
//                        binding.orImage.visibility = View.VISIBLE
//                        binding.orImage.load(bitmap) {
//                            crossfade(true)
//                            crossfade(1000)
//                        }
//                    }
//
//                    CAMERA_CR_CODE -> {
//                        //we are using coroutine image loader (coil)
//                        val bitmap = data.extras?.get("data") as Bitmap
//
//                        val byteArrayOutputStream = ByteArrayOutputStream()
//                        bitmap.compress(
//                            Bitmap.CompressFormat.PNG,
//                            100,
//                            byteArrayOutputStream
//                        )
//                        val bytes = byteArrayOutputStream.toByteArray()
//                        crImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
//                        binding.crImage.visibility = View.VISIBLE
//                        binding.crImage.load(bitmap) {
//                            crossfade(true)
//                            crossfade(1000)
//                        }
//                    }
//
//                    CAMERA_VEHICLE_CODE -> {
//                        //we are using coroutine image loader (coil)
//                        val bitmap = data.extras?.get("data") as Bitmap
//
//                        val byteArrayOutputStream = ByteArrayOutputStream()
//                        bitmap.compress(
//                            Bitmap.CompressFormat.PNG,
//                            100,
//                            byteArrayOutputStream
//                        )
//                        val bytes = byteArrayOutputStream.toByteArray()
//                        vehicleImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
//                        binding.vehicleImage.visibility = View.VISIBLE
//                        binding.vehicleImage.load(bitmap) {
//                            crossfade(true)
//                            crossfade(1000)
//                        }
//                    }

                    PICK_NBI_CODE -> {

                        showLoadingDialog()
                        // Use Coroutine to run image processing on a background thread
                        CoroutineScope(Dispatchers.Main).launch {
                            nbiImageUri = data.data
                            // Process the image asynchronously on a background thread
                            val bitmap = withContext(Dispatchers.IO) {
                                // Process the image
                                processImage(data.data)
                            }

                            // After processing, update the UI with the result
                            bitmap?.let {
                                binding.nbiImageUri.visibility = View.VISIBLE
                                binding.nbiImageUri.text =
                                    getFileName(data.data, registerRiderActivity)
                                Log.d("IMAGE_URI", "NBI: ${data.data}")

                                binding.nbiImage.visibility = View.VISIBLE
                                binding.nbiImage.setImageBitmap(it)

                                // Convert the bitmap to Base64 for later use
                                nbiImageBytes = convertBitmapToBase64(it)

                                Toast.makeText(
                                    registerRiderActivity,
                                    "Image Selected for NBI",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismissLoadingDialog()
                            } ?: run {
                                // If the bitmap is null, show a message to the user
                                Toast.makeText(
                                    registerRiderActivity,
                                    "Failed to load image",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismissLoadingDialog()
                            }
                        }
                    }

                    PICK_LICENSE_CODE -> {
                        showLoadingDialog()
                        // Use Coroutine to run image processing on a background thread
                        CoroutineScope(Dispatchers.Main).launch {
                            licenseImageUri = data.data
                            // Process the image asynchronously on a background thread
                            val bitmap = withContext(Dispatchers.IO) {
                                // Process the image
                                processImage(data.data)
                            }

                            // After processing, update the UI with the result
                            bitmap?.let {
                                binding.licenseImageUri.visibility = View.VISIBLE
                                binding.licenseImageUri.text =
                                    getFileName(data.data, registerRiderActivity)
                                Log.d("IMAGE_URI", "LICENSE: ${data.data}")

                                binding.licenseImage.visibility = View.VISIBLE
                                binding.licenseImage.setImageBitmap(it)

                                // Convert the bitmap to Base64 for later use
                                licenseImageBytes = convertBitmapToBase64(it)

                                Toast.makeText(
                                    registerRiderActivity,
                                    "Image Selected for License",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismissLoadingDialog()
                            } ?: run {
                                // If the bitmap is null, show a message to the user
                                Toast.makeText(
                                    registerRiderActivity,
                                    "Failed to load image",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismissLoadingDialog()
                            }
                        }
                    }

                    PICK_OR_CODE -> {
                        showLoadingDialog()
                        // Use Coroutine to run image processing on a background thread
                        CoroutineScope(Dispatchers.Main).launch {
                            orImageUri = data.data
                            // Process the image asynchronously on a background thread
                            val bitmap = withContext(Dispatchers.IO) {
                                // Process the image
                                processImage(data.data)
                            }

                            // After processing, update the UI with the result
                            bitmap?.let {
                                binding.orImageUri.visibility = View.VISIBLE
                                binding.orImageUri.text =
                                    getFileName(data.data, registerRiderActivity)
                                Log.d("IMAGE_URI", "OR: ${data.data}")

                                binding.orImage.visibility = View.VISIBLE
                                binding.orImage.setImageBitmap(it)

                                // Convert the bitmap to Base64 for later use
                                orImageBytes = convertBitmapToBase64(it)

                                Toast.makeText(
                                    registerRiderActivity,
                                    "Image Selected for OR",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismissLoadingDialog()
                            } ?: run {
                                // If the bitmap is null, show a message to the user
                                Toast.makeText(
                                    registerRiderActivity,
                                    "Failed to load image",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismissLoadingDialog()
                            }
                        }
                    }

                    PICK_CR_CODE -> {
                        showLoadingDialog()
                        // Use Coroutine to run image processing on a background thread
                        CoroutineScope(Dispatchers.Main).launch {
                            crImageUri = data.data
                            // Process the image asynchronously on a background thread
                            val bitmap = withContext(Dispatchers.IO) {
                                // Process the image
                                processImage(data.data)
                            }

                            // After processing, update the UI with the result
                            bitmap?.let {
                                binding.crImageUri.visibility = View.VISIBLE
                                binding.crImageUri.text =
                                    getFileName(data.data, registerRiderActivity)
                                Log.d("IMAGE_URI", "CR: ${data.data}")

                                binding.crImage.visibility = View.VISIBLE
                                binding.crImage.setImageBitmap(it)

                                // Convert the bitmap to Base64 for later use
                                crImageBytes = convertBitmapToBase64(it)

                                Toast.makeText(
                                    registerRiderActivity,
                                    "Image Selected for CR",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismissLoadingDialog()
                            } ?: run {
                                // If the bitmap is null, show a message to the user
                                Toast.makeText(
                                    registerRiderActivity,
                                    "Failed to load image",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismissLoadingDialog()
                            }
                        }
                    }

                    PICK_VEHICLE_CODE -> {
                        showLoadingDialog()
                        // Use Coroutine to run image processing on a background thread
                        CoroutineScope(Dispatchers.Main).launch {
                            vehicleImageUri = data.data
                            // Process the image asynchronously on a background thread
                            val bitmap = withContext(Dispatchers.IO) {
                                // Process the image
                                processImage(data.data)
                            }

                            // After processing, update the UI with the result
                            bitmap?.let {
                                binding.vehicleImageUri.visibility = View.VISIBLE
                                binding.vehicleImageUri.text =
                                    getFileName(data.data, registerRiderActivity)
                                Log.d("IMAGE_URI", "VEHICLE: ${data.data}")

                                binding.vehicleImage.visibility = View.VISIBLE
                                binding.vehicleImage.setImageBitmap(it)

                                // Convert the bitmap to Base64 for later use
                                vehicleImageBytes = convertBitmapToBase64(it)

                                Toast.makeText(
                                    registerRiderActivity,
                                    "Image Selected for VEHICLE",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismissLoadingDialog()
                            } ?: run {
                                // If the bitmap is null, show a message to the user
                                Toast.makeText(
                                    registerRiderActivity,
                                    "Failed to load image",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismissLoadingDialog()
                            }
                        }
                    }

                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Handle errors and notify the user
                Toast.makeText(
                    registerRiderActivity,
                    "Error processing the image",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                // Hide the progress dialog when done
                dismissLoadingDialog()
            }
        }
    }


    private fun btnSubmitOnClickListener() {
        if (registerRiderActivity.checkFields()) {
            Toast.makeText(
                registerRiderActivity,
                "Please check error field(s)!",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            registerRiderActivity.saveInfoToFirebase()
        }
    }


}