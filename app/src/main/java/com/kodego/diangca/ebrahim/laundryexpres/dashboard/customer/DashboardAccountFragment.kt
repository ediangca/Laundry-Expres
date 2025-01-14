package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardAccountBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class DashboardAccountFragment(var dashboardCustomer: DashboardCustomerActivity) : Fragment() {

    private var _binding: FragmentDashboardAccountBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")

    private var user: User? = null
    private var userType: String? = null
    private var displayName: String? = null
    private var isVerified: Boolean? = null

    private val PICK_PROFILE_CODE = 100
    private val CAMERA_PROFILE_CODE = 2

    private var profileFileName: String? = null
    private var profileImageUri: Uri? = null
    var profileImageBytes: String? = null
    private var profileOptionDialog: Dialog = Dialog(dashboardCustomer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardAccountBinding.inflate(layoutInflater, container, false)
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

        val sexAdapter = ArrayAdapter(dashboardCustomer,  android.R.layout.simple_spinner_item, this.resources.getStringArray(R.array.sex))
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sex.adapter = sexAdapter

        with(binding.sex)
        {
            adapter = sexAdapter
            setSelection(0, false)
            prompt = "Select Sex"
            gravity = Gravity.CENTER
        }

        val bundle = arguments
        if (bundle!=null) {
            user = bundle.getParcelable<User>("user")!!
            Log.d("ON_RESUME_FETCH_USER", user.toString())
        }else{
            user = dashboardCustomer.getUser()
        }
        setUserDetails(user)

        binding.btnLogout.setOnClickListener {
            btnLogoutOnClickListener()
        }
        binding.profilePicture.setOnClickListener {
            showOptionProfile()
        }
        binding.btnAccSubmit.setOnClickListener {
            btnAccSubmitOnClickListener()
        }
    }

    private fun showOptionProfile() {
        profileOptionDialog = Dialog(dashboardCustomer)

        val loadingBuilder = AlertDialog.Builder(dashboardCustomer)
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

    private fun btnBrowseProfileOnClickListener() {
        val gallery = Intent()
        gallery.type = "image/*"
        gallery.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(gallery, PICK_PROFILE_CODE)
    }

    private fun btnCaptureProfileOnClickListener() {
        cameraCheckPermission(CAMERA_PROFILE_CODE)
    }

    private fun cameraCheckPermission(code: Int) {

        Dexter.withContext(dashboardCustomer)
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

    private fun showRotationalDialogForPermission() {
        AlertDialog.Builder(dashboardCustomer)
            .setMessage(
                "It looks like you have turned off permissions"
                        + "required for this feature. It can be enable under App settings!!!"
            )

            .setPositiveButton("Go TO SETTINGS") { _, _ ->

                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", dashboardCustomer.packageName, null)
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
            dashboardCustomer,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun camera(code: Int) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, code)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode==AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                PICK_PROFILE_CODE -> {
                    profileImageUri = data?.data
                    profileFileName = getFileName(profileImageUri, dashboardCustomer)
                    Log.d("IMAGE_URI", "BUSINESS BIR: $profileImageUri")

                    val inputStream =
                        dashboardCustomer.contentResolver.openInputStream(profileImageUri!!)
                    val myBitmap = BitmapFactory.decodeStream(inputStream)
                    val stream = ByteArrayOutputStream()
                    myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val bytes = stream.toByteArray()
                    profileImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)

                    val profileView: ImageView = binding.profilePicture
                    loadBitmapByPicasso(dashboardCustomer, myBitmap, profileView)
                }

                CAMERA_PROFILE_CODE -> {
                    try {
                        if (data!=null) {
                            //we are using coroutine image loader (coil)
                            val myBitmap = data.extras?.get("data") as Bitmap

                            val stream = ByteArrayOutputStream()
                            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                            val bytes = stream.toByteArray()
                            profileImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)

                            val profileView: ImageView = binding.profilePicture
                            loadBitmapByPicasso(dashboardCustomer, myBitmap, profileView)
                        }
                    } catch (e: Exception) {
                        Log.d("CAMERA", e.toString())
                        e.printStackTrace()
                    }
                }

            }
        }
    }

    private fun loadBitmapByPicasso(pContext: Context, pBitmap: Bitmap, pImageView: ImageView) {
        try {
            profileImageUri =
                Uri.fromFile(File.createTempFile("temp_profile", ".jpg", pContext.cacheDir))
            Log.d("LOAD_USER_PROFILE_URI", profileImageUri.toString())
            val outputStream: OutputStream? =
                pContext.contentResolver.openOutputStream(profileImageUri!!)
            if (outputStream != null) {
                pBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            outputStream!!.close()
            Picasso.with(dashboardCustomer).load(profileImageUri).into(pImageView)
            Toast.makeText(dashboardCustomer, "Great Profile!", Toast.LENGTH_SHORT)
                .show()
        } catch (e: java.lang.Exception) {
            Log.e("LoadBitmapByPicasso", e.message!!)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun btnAccSubmitOnClickListener() {
        val mobileNo = binding.phoneNo.text.toString()
        val firstName = binding.firstName.text.toString()
        val lastName = binding.lastName.text.toString()
        val street = binding.address.text.toString()
        val city = binding.city.text.toString()
        val state = binding.state.text.toString()
        val zipCode = binding.zipCode.text.toString()
        val country = binding.country.text.toString()
        val sex = binding.sex.getItemAtPosition(binding.sex.selectedItemPosition).toString()
        val email = binding.email.text.toString()


        if (email.isEmpty() || !validEmail(email) || mobileNo.isEmpty() || mobileNo.length!=13 || firstName.isEmpty() || lastName.isEmpty() || binding.sex.selectedItemPosition < 0 || street.isEmpty() || city.isEmpty() || state.isEmpty() || zipCode.isEmpty() || country.isEmpty()
            || isValidAddress(street)
            || isValidAddress(city)
            || isValidAddress(state)
            || isValidAddress(country)
        ) {
            if (email.isEmpty() || !validEmail(email)) {
                binding.email.error = "Please enter an email or a valid email."
            }
            if (mobileNo.isEmpty()) {
                binding.phoneNo.error = "Please enter your Mobile No."
            }
            if (mobileNo.length!=13) {
                binding.phoneNo.error = "Please check length of Mobile No."
            }
            if (firstName.isEmpty()) {
                binding.firstName.error = "Please enter your Firstname."
            }
            if (lastName.isEmpty()) {
                binding.lastName.error = "Please enter your Lastname."
            }
            if (binding.sex.selectedItemPosition < 0) {
                (binding.sex.selectedView as TextView).error = "Please select your sex."
            }
            if (street.isEmpty() || isValidAddress(street)) {
                binding.address.error = "Please enter your Street."
            }
            if (city.isEmpty() || isValidAddress(city)) {
                binding.city.error = "Please enter your City."
            }
            if (state.isEmpty() || isValidAddress(state)) {
                binding.state.error = "Please enter your State."
            }
            if (zipCode.isEmpty()) {
                binding.zipCode.error = "Please enter your Zip Code."
            }
            if (country.isEmpty() || isValidAddress(country)) {
                binding.country.error = "Please enter your Country."
            }

            Toast.makeText(dashboardCustomer, "Please check empty fields!", Toast.LENGTH_SHORT)
                .show()
            return
        } else {

        dashboardCustomer.showLoadingDialog()
        val databaseRef = firebaseDatabase.reference.child("users")
            .child(firebaseAuth.currentUser!!.uid)

        user = User(
            firebaseAuth.currentUser!!.uid,
            email,
            userType,
            firstName,
            lastName,
            sex,
            street,
            city,
            state,
            zipCode,
            country,
            mobileNo,
            profileImageUri.toString(),
            isVerified,
            this.user!!.datetimeCreated,
            SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date())
            )
        databaseRef.setValue(user).addOnCompleteListener(dashboardCustomer) { task ->
            if (task.isSuccessful) {
                val filename = "profile_${user!!.uid}"
                profileImageUri = Uri.parse(user!!.photoUri)

                Log.d("SAVING_USER", "SUCCESS SAVING USER DATA")
                setUserDetails(user)
                dashboardCustomer.setUser(user)

                val firebaseStorageReference = FirebaseStorage.getInstance().getReference("profile/$filename")
                Log.d("PROFILE_FILENAME", filename)
                Log.d("PROFILE_URI", profileImageUri!!.toString())
                firebaseStorageReference.putFile(profileImageUri!!)
                    .addOnSuccessListener {
                        Log.d("SAVING_USER_PROFILE", "SUCCESS SAVING PROFILE $filename")
                    }
                    .addOnFailureListener {
                        Log.d("FAILED_SAVING_USER_PROFILE", "FAILED SAVING PROFILE $filename > ${it.message}")
                    }
                dashboardCustomer.dismissLoadingDialog()
                Toast.makeText(
                    context,
                    "User has been successfully Updated!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                dashboardCustomer.dismissLoadingDialog()
                Log.d("USER_UPDATE_FAILED", "${task.exception}")
                Toast.makeText(
                    context,
                    "Sorry! ${task.exception}",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }
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

private fun validEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun btnLogoutOnClickListener() {
    var loadingDialog: Dialog = Dialog(dashboardCustomer)

    val loadingBuilder = AlertDialog.Builder(context)
    loadingBuilder.setTitle("LOGOUT")
    loadingBuilder.setMessage("Do you really want to Logout?")
    loadingBuilder.setPositiveButton("Yes") { _, _ ->
        dashboardCustomer.showLoadingDialog()
        Handler(Looper.getMainLooper()).postDelayed({
            firebaseAuth.signOut()
            dashboardCustomer.signOut()
            dashboardCustomer.dismissLoadingDialog()
        }, 3000) // 3000 is the delayed time in milliseconds.
    }
    loadingBuilder.setNegativeButton("Cancel") { _, _ ->
        loadingDialog.dismiss()
    }
    loadingDialog = loadingBuilder.create()
    if (loadingDialog.window!=null) {
        loadingDialog.window!!.setBackgroundDrawableResource(R.color.color_light_3)
    }
    loadingDialog.show()

}

private fun setUserDetails(user: User?) {
    firebaseAuth.currentUser?.let {
        for (profile in it.providerData) {
            displayName = profile.displayName
            profileImageUri = profile.photoUrl
        }

        if (!displayName.isNullOrEmpty()) {
            Log.d("displayUserName", "Hi ${displayName}, Good Day!")
            binding.userDisplayName.text = displayName
        } else {
            if (user!=null) {
                displayName = "${user!!.firstname} ${user!!.lastname}"
                binding.userDisplayName.text = displayName
            }
        }

        val profileView: ImageView = binding.profilePicture
//        profileView.setImageResource(R.drawable.icon_logo)
        if (profileImageUri!=null) {
            Log.d("profilePic_profileData", "$profileImageUri")
            Picasso.with(context).load(profileImageUri).into(profileView);
        } else {
            if (user!!.photoUri!=null) {
                val filename = "profile_${user!!.uid}"
                profileImageUri = Uri.parse(user!!.photoUri)
                val firebaseStorageReference =
                    FirebaseStorage.getInstance().reference.child("profile/$filename")
                Log.d("PROFILE_FILENAME", filename)
                Log.d("PROFILE_URI", profileImageUri!!.toString())
                val localFile = File.createTempFile("temp_profile", ".jpg", dashboardCustomer.cacheDir)
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

        if (user!=null) {
            userType = user.type
            this.isVerified = user.verified
            binding.apply {
                userAddress.text = user.address
                email.setText(user.email)
                phoneNo.setText(user.phone)
                firstName.setText(user.firstname)
                lastName.setText(user.lastname)
                val sexList = resources.getStringArray(R.array.sex)
                for ((index, value) in sexList.withIndex()) {
                    if (value.equals(user.sex, true)) {
                        sex.setSelection(index)
                    }
                }
                if(user.photoUri!=null) {
                    profileImageUri = Uri.parse(user.photoUri)
                }
                address.setText(user.address)
                city.setText(user.city)
                state.setText(user.state)
                zipCode.setText(user.zipCode)
                country.setText(user.country)
            }
        }
    }
}

}