package com.kodego.diangca.ebrahim.laundryexpres.registration

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kodego.diangca.ebrahim.laundryexpres.LoginActivity
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer.DashboardCustomerActivity
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityRegisterCustomerBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogLoadingBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.util.*

class RegisterCustomerActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityRegisterCustomerBinding

    private lateinit var dialogLoadingBinding: DialogLoadingBinding

    private var userType: String = "Customer"

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val permissionId = 2
    private var longtitude: Double = 0.0
    private var latitude: Double = 0.0
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private val PICK_PROFILE_CODE = 100
    private val CAMERA_PROFILE_CODE = 2
    private var profileImageUri: Uri? = null
    var profileImageBytes:String? = null

    private lateinit var profileOptionDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    private fun initComponent() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (firebaseAuth.currentUser!=null) {
            binding.passwordLayout.visibility = View.GONE
            binding.confirmPasswordLayout.visibility = View.GONE

            binding.email.setText(firebaseAuth.currentUser!!.email)
        }

        val sexAdapter = ArrayAdapter.createFromResource(this, R.array.sex, R.layout.spinner_item)
        sexAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.sex.adapter = sexAdapter

        with(binding.sex)
        {
            adapter = sexAdapter
            setSelection(0, false)
            prompt = "Choose Sex"
            gravity = android.view.Gravity.CENTER
        }

        binding.btnBack.setOnClickListener {
            btnBackOnClickListener()
        }
        binding.btnSubmit.setOnClickListener {
            btnSubmitOnClickListener()
        }

        binding.btnLocation.setOnClickListener {
            btnLocationOnClickListener()
        }
        binding.profilePic.setOnClickListener {
            showOptionProfile()
        }
    }

    private fun showOptionProfile() {
        profileOptionDialog = Dialog(this)

        val loadingBuilder = AlertDialog.Builder(this)
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
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_PROFILE_CODE)
    }
    private fun btnCaptureProfileOnClickListener() {
        cameraCheckPermission(CAMERA_PROFILE_CODE)
    }

    private fun cameraCheckPermission(code: Int) {

        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA).withListener(

                object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {

                            if (report.areAllPermissionsGranted()) {
                                camera(code)
                            }else{
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

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
    private fun camera(code : Int) {
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
                    binding.profilePic.visibility = View.VISIBLE
                    binding.profileFileName.text = getFileName(profileImageUri, this)
                    Log.d("IMAGE_URI", "BUSINESS BIR: $profileImageUri")

                    val inputStream = contentResolver.openInputStream(profileImageUri!!)
                    val myBitmap = BitmapFactory.decodeStream(inputStream)
                    val stream = ByteArrayOutputStream()
                    myBitmap.compress(Bitmap.CompressFormat.PNG, 100,stream)
                    val bytes = stream.toByteArray()
                    profileImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
                    inputStream!!.close()

                    val profileView: ImageView = binding.profilePic
                    loadBitmapByPicasso(this, myBitmap, profileView)

                }

                CAMERA_PROFILE_CODE -> {
                    try {
                        if (data!=null) {
                            //we are using coroutine image loader (coil)
                            val myBitmap = data.extras?.get("data") as Bitmap

                            val stream = ByteArrayOutputStream()
                            myBitmap.compress(Bitmap.CompressFormat.PNG,100,stream)
                            val bytes = stream.toByteArray()
                            profileImageBytes = Base64.encodeToString(bytes, Base64.DEFAULT)

                            val profileView: ImageView = binding.profilePic
                            loadBitmapByPicasso(this, myBitmap, profileView)
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
            profileImageUri = Uri.fromFile(File.createTempFile("temp_profile", ".jpg", pContext.cacheDir))
            val outputStream: OutputStream? = pContext.contentResolver.openOutputStream(profileImageUri!!)
            pBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream!!.close()
            Picasso.with(this).load(profileImageUri).into(pImageView)
            Toast.makeText(this, "Great Profile!", Toast.LENGTH_SHORT)
                .show()
        } catch (e: java.lang.Exception) {
            Log.e("LoadBitmapByPicasso", e.message!!)
        }
    }

    @SuppressLint("Range")
    private fun getFileName(imageUri: Uri?, context: Context): String? {
        var filename: String? = null
        if (imageUri!!.scheme.equals("content")) {
            val cursor = context.contentResolver.query(imageUri, null, null,null, null)
            try {
                if(cursor != null && cursor.moveToFirst()){
                    filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }finally {
                cursor!!.close()
            }

            if(filename == null){
                filename = imageUri.path
                val cut: Int = filename!!.lastIndexOf('/')
                if(cut != -1){
                    filename = filename.substring(cut +1)
                }

            }

        }
        return  filename
    }

    private fun showRotationalDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions"
                    + "required for this feature. It can be enable under App settings!!!")

            .setPositiveButton("Go TO SETTINGS") { _, _ ->

                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
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

    private fun btnLocationOnClickListener() {
        getLocation()
    }

    private fun btnBackOnClickListener() {
        startActivity(Intent(Intent(this, LoginActivity::class.java)))
        finish()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )==PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )==PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        if (requestCode==permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location!=null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
                        binding.apply {
                            currentLocation.text = list[0].toString()
                            latitude = list[0].latitude
                            longtitude = list[0].longitude
                            address.setText(list[0].getAddressLine(0) ?: "n/a")
                            city.setText(list[0].locality ?: "n/a")
                            state.setText(list[0].adminArea ?: "n/a")
                            zipCode.setText(list[0].postalCode ?: "n/a")
                            country.setText(list[0].countryName ?: "n/a")

                            Log.d("GEOLOCATION", "Latitude : $latitude")
                            Log.d("GEOLOCATION", "Longitude : $longtitude")
                        }
                    } else {
                        Toast.makeText(this, "Not Found Location", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermissions()
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

    private fun btnSubmitOnClickListener() {
        val mobileNo = binding.mobileNo.text.toString()
        val firstName = binding.firstName.text.toString()
        val lastName = binding.lastName.text.toString()
        val street = binding.address.text.toString()
        val city = binding.city.text.toString()
        val state = binding.state.text.toString()
        val zipCode = binding.zipCode.text.toString()
        val country = binding.country.text.toString()
        val sex = binding.sex.getItemAtPosition(binding.sex.selectedItemPosition).toString()
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()

        binding.passwordLayout.isPasswordVisibilityToggleEnabled = true
        binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = true

        var trap: Boolean = false

        if (email.isEmpty() || !validEmail(email) || mobileNo.isEmpty() || mobileNo.length!=13 || firstName.isEmpty() || lastName.isEmpty() || binding.sex.selectedItemPosition ==0 || street.isEmpty() || city.isEmpty() || state.isEmpty() || zipCode.isEmpty() || country.isEmpty()
            || isValidAddress(street)
            || isValidAddress(city)
            || isValidAddress(state)
            || isValidAddress(country)) {

            if (email.isEmpty() || !validEmail(email)) {
                binding.email.error = "Please enter an email or a valid email."
            }
            if (mobileNo.isEmpty()) {
                binding.mobileNo.error = "Please enter your Mobile No."
                trap =  true
            }
            if (mobileNo.length!=13) {
                binding.mobileNo.error = "Please check length of Mobile No."
            }
            if (firstName.isEmpty()) {
                binding.firstName.error = "Please enter your Firstname."
            }
            if (lastName.isEmpty()) {
                binding.lastName.error = "Please enter your Lastname."
            }
            if (binding.sex.selectedItemPosition ==0) {
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
            if (country.isEmpty()) {
                binding.country.error = "Please enter your Country."
            }

            Toast.makeText(this, "Please check empty fields!", Toast.LENGTH_SHORT).show()
            return
        }
        if (firebaseAuth.currentUser==null) {
            if (password.isEmpty()) {
                binding.password.error = "Please enter your password."
                binding.passwordLayout.isPasswordVisibilityToggleEnabled = false
                trap =  true
            }
            if (confirmPassword.isEmpty()) {
                binding.confirmPassword.error = "Please enter your password."
                binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
                trap =  true
            }
            if (password.length < 6) {
                binding.password.error = "Password must be more than 6 characters."
                binding.passwordLayout.isPasswordVisibilityToggleEnabled = false
                trap =  true
            }
            if (password!=confirmPassword) {
                binding.confirmPassword.error = "Password not match."
                binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
                trap =  true
            }
            if(trap){
                Toast.makeText(this, "Please check error field(s)!", Toast.LENGTH_SHORT).show()
                return
            }
            showProgressBar(true)
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    firebaseDatabaseReference.child("users")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                                    Toast.makeText(
                                        this@RegisterCustomerActivity,
                                        "User is already Registered!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val databaseRef = firebaseDatabase.reference.child("users")
                                        .child(firebaseAuth.currentUser!!.uid)

                                    val user = User(
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
                                        false,
                                    )
                                    user.printLOG()


                                    databaseRef.setValue(user).addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(
                                                this@RegisterCustomerActivity,
                                                "User has been successfully Registered!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            goToDashboard()
                                        }
                                    }

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("ListenerForSingleValueEvent", error.message)
                            }

                        })
                } else {
                    showProgressBar(false)
                    Snackbar.make(binding.root, "User email already existing", Snackbar.LENGTH_SHORT).show()
                    Log.d("ListenerForSingleValueEvent", "${it.exception!!.message}")

                }
            }

        } else {
            firebaseDatabaseReference.child("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                            Toast.makeText(
                                this@RegisterCustomerActivity,
                                "User is already Registered!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val databaseRef = firebaseDatabase.reference.child("users")
                                .child(firebaseAuth.currentUser!!.uid)

                            val user = User(
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
                                null,
                                false,
                            )
                            user.printLOG()


                            databaseRef.setValue(user).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@RegisterCustomerActivity,
                                        "User has been successfully Registered!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    goToDashboard()
                                }else{
                                    showProgressBar(false)
                                    Toast.makeText(this@RegisterCustomerActivity, "${task.exception!!.message}", Toast.LENGTH_SHORT).show()
                                }
                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showProgressBar(false)
                        Toast.makeText(this@RegisterCustomerActivity, "${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun goToDashboard() {
        showProgressBar(false)
        startActivity(Intent(Intent(this, DashboardCustomerActivity::class.java)))
        finish()
    }

    private fun showProgressBar(visible: Boolean) {
        if (visible) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun validEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
   /* fun String.isEmailValid() =
        Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        ).matcher(this).matches()
*/
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        TODO("Not yet implemented")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}