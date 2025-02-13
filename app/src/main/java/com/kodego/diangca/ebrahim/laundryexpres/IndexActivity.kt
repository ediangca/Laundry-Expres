package com.kodego.diangca.ebrahim.laundryexpres

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer.DashboardCustomerActivity
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.partner.DashboardPartnerActivity
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.rider.DashboardRiderActivity
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityIndexBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogLoadingBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import com.kodego.diangca.ebrahim.laundryexpres.registration.RegisterCustomerActivity
import com.kodego.diangca.ebrahim.laundryexpres.registration.partner.RegisterPartnerActivity
import com.kodego.diangca.ebrahim.laundryexpres.registration.rider.RegisterRiderActivity
import com.squareup.picasso.Picasso
import java.io.File
import java.util.Calendar
import java.util.Locale


class IndexActivity : AppCompatActivity() {


    private lateinit var binding: ActivityIndexBinding
    private lateinit var mainFragment: MainFragment
    lateinit var mainFrame: FragmentTransaction

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var user: User? = null
    private var userType = "UNKNOWN"
    private var displayName: String? = null
    private var profileImageUri: Uri? = null

    private lateinit var loadingBuilder: AlertDialog.Builder
    private lateinit var loadingDialog: Dialog


    fun getDatabaseReference(): DatabaseReference {
        return firebaseDatabaseReference
    }

    fun getFirebaseAuth(): FirebaseAuth {
        return firebaseAuth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponent()
    }

    private fun initComponent() {

        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }


        // Initialize fused location client

        // Check location permission and get location

    }

    private fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }

    override fun onStart() {
        super.onStart()
        if (!isNetworkAvailable(this)) {
            Log.d("NETWORK", "NO NETWORK AVAILABLE ")
            var verifyDialog: Dialog = Dialog(this)
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            builder.setTitle("INTERNET CONNECTIVITY REQUIRED")
            builder.setMessage("PLEASE TURN ON YOUR WIFI OR DATA TO USE THIS APP.")
            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS));
                verifyDialog.dismiss()
            }
            verifyDialog = builder.create()
            if (verifyDialog.window != null) {
                verifyDialog.window!!.setBackgroundDrawableResource(R.color.color_light_3)
            }
            verifyDialog.show()
//            Toast.makeText(this, "NO NETWORK AVAILABLE", Toast.LENGTH_LONG).show()
            return
        }
        if (FirebaseAuth.getInstance().currentUser != null) {
            val databaseRef = firebaseDatabase.reference.child("users")
                .child(firebaseAuth.currentUser!!.uid)

            databaseRef.get().addOnCompleteListener { dataSnapshot ->
                if (dataSnapshot.isSuccessful) {
                    user = dataSnapshot.result.getValue(User::class.java)
                    if (user != null) {
                        userType = user!!.type!!

                        val isVerified = user!!.verified

                        if ((userType != "Customer") && !isVerified!!) {
                            Log.d("SIGN_OUT_USER", "UNVERIFIED_ACCOUNT")
                            firebaseAuth.signOut()
                            showMain()
                        } else {
                            goToDashboard()
                        }
                    }
                } else {
                    Log.d("SIGN_OUT_USER", "WITH_AUTH_BUT_NOT_REGISTERED")
                    firebaseAuth.signOut()
                    showMain()
                }
            }
        } else {
            showMain()
        }
    }

    override fun onResume() {
        super.onResume()

        askNotificationPermission()
        if (user != null) {
            if (user!!.type.equals("Rider", true)) {
                setupRiderPresence(user!!.uid!!)
            }
        }
    }


    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("Monitor Message", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

//                // Get new FCM registration token
                val token = task.result
//
//                // Log and toast
//                val msg = getString(R.string.msg_token_fmt, token)
//                Log.d("Monitor Message", msg)
                Log.d("Monitor Message", "Fetching FCM registration token $token", task.exception)

                Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
            })
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }


    private fun showMain() {
        showLoadingDialog()
        Handler(Looper.getMainLooper()).postDelayed({
            dismissLoadingDialog()
            askNotificationPermission()

            mainFragment = MainFragment(this)
            mainFrame = supportFragmentManager.beginTransaction()
            mainFrame.replace(R.id.mainFrame, mainFragment)
            mainFrame.commit()
        }, 3000) // 3000 is the delayed time in milliseconds.
    }


    private fun goToDashboard() {
        showLoadingDialog()
        Handler(Looper.getMainLooper()).postDelayed({
            dismissLoadingDialog()
            when (userType) {
                "Customer" -> {
                    startActivity((Intent(this, DashboardCustomerActivity::class.java)))
                    finish()
                }

                "Partner" -> {
                    startActivity((Intent(this, DashboardPartnerActivity::class.java)))
                    finish()
                }

                "Rider" -> {
                    setupRiderPresence(user!!.uid!!)
                }

                else -> {

                }
            }
        }, 3000) // 3000 is the delayed time in milliseconds.
    }


    private fun setupRiderPresence(riderId: String) {
        val database = FirebaseDatabase.getInstance()
        val riderStatusRef = database.getReference("riders").child(riderId)
        val connectedRef = database.getReference(".info/connected")

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.getValue(Boolean::class.java) ?: false
                if (isConnected) {
                    val currentDate = System.currentTimeMillis()  // Get current timestamp
                    riderStatusRef.child("lastActive").get()
                        .addOnSuccessListener { lastActiveSnapshot ->
                            val lastActive = lastActiveSnapshot.getValue(Long::class.java) ?: 0L

                            // Update rider status and reset activeRequests if necessary
                            riderStatusRef.apply {
                                child("name").setValue("${user!!.firstname} ${user!!.lastname}")
                                child("status").setValue("online")
                                // Track disconnection
                                onDisconnect().setValue(
                                    mapOf(
                                        "name" to "${user!!.firstname} ${user!!.lastname}",
                                        "status" to "offline",
                                        "lastActive" to currentDate
                                    )
                                )
                            }
                            showRiderDashboard()

                        }.addOnFailureListener {
                        Log.e("FirebaseError", "Error fetching lastActive: ${it.message}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Could not check connection status: ${error.message}")
            }
        })
    }

    /**
    private fun setupRiderPresence(riderId: String) {
    val database = FirebaseDatabase.getInstance()
    val riderStatusRef = database.getReference("riders").child(riderId)
    val connectedRef = database.getReference(".info/connected")

    connectedRef.addValueEventListener(object : ValueEventListener {
    override fun onDataChange(snapshot: DataSnapshot) {
    val isConnected = snapshot.getValue(Boolean::class.java) ?: false
    if (isConnected) {
    val currentDate = System.currentTimeMillis()

    // Fetch lastActive to check if reset is needed
    riderStatusRef.child("lastActive").get().addOnSuccessListener { lastActiveSnapshot ->
    val lastActive = lastActiveSnapshot.getValue(Long::class.java) ?: 0L
    val shouldResetRequests = !isSameDay(lastActive, currentDate) // Reset only if not the same day

    riderStatusRef.get().addOnSuccessListener { riderSnapshot ->
    val activeRequest = riderSnapshot.child("activeRequest").getValue(Int::class.java) ?: 0
    val transactionId = riderSnapshot.child("transactionId").getValue(String::class.java) ?: ""

    // 🚀 **Do not overwrite transactionId or activeRequest if they already exist!**
    riderStatusRef.child("name").setValue("${user!!.firstname} ${user!!.lastname}")
    riderStatusRef.child("status").setValue("online")

    if (shouldResetRequests) {
    riderStatusRef.child("activeRequest").setValue(0) // Reset if new day
    }

    Log.d("Monitor Request ", "$transactionId Request $activeRequest")

    // Track disconnection but **preserve values if they exist**
    val disconnectData = mutableMapOf<String, Any>(
    "name" to "${user!!.firstname} ${user!!.lastname}",
    "status" to "offline",
    "lastActive" to currentDate
    )

    if (activeRequest > 0) {
    disconnectData["activeRequest"] = activeRequest
    }

    if (transactionId.isNotEmpty()) {
    disconnectData["transactionId"] = transactionId
    }

    riderStatusRef.onDisconnect().setValue(disconnectData) // ✅ **Keeps values on disconnect**

    showRiderDashboard()
    }.addOnFailureListener {
    Log.e("FirebaseError", "Error fetching rider data: ${it.message}")
    }
    }.addOnFailureListener {
    Log.e("FirebaseError", "Error fetching lastActive: ${it.message}")
    }
    }
    }

    override fun onCancelled(error: DatabaseError) {
    Log.e("FirebaseError", "Could not check connection status: ${error.message}")
    }
    })
    }
     */


    /**
     * ✅ Helper function to check if two timestamps are on the same day
     */
    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val calendar1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val calendar2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }

        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }


    private fun showRiderDashboard() {
        startActivity((Intent(this, DashboardRiderActivity::class.java)))
        finish()
    }

    private fun showProgressBar(visible: Boolean) {
        if (visible) {
            binding.imageView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.imageView.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
        }
    }

    fun replaceFragment(fragment: Fragment) {
        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.mainFrame, fragment);
        mainFrame.addToBackStack(null);
        mainFrame.commit();
    }

    fun showLogin() {
        Log.d("SHOW_LOGIN_ACTIVITY", "LOGIN ACTIVITY")
        startActivity(Intent(Intent(Intent(this, LoginActivity::class.java))))
        finish()
    }

    fun showCustomerRegister() {
        Log.d("SHOW_REGISTER_ACTIVITY", "REGISTER CUSTOMER ACTIVITY")
        startActivity(Intent(Intent(this, RegisterCustomerActivity::class.java)))
        finish()
    }

    fun showPartnershipRegister() {
        Log.d("SHOW_REGISTER_ACTIVITY", "REGISTER PARTNERSHIP ACTIVITY")
        startActivity(Intent(Intent(this, RegisterPartnerActivity::class.java)))
        finish()
    }

    fun showRiderRegister() {
        Log.d("SHOW_REGISTER_ACTIVITY", "REGISTER RIDER ACTIVITY")
        startActivity(Intent(Intent(this, RegisterRiderActivity::class.java)))
        finish()
    }

    fun showLoadingDialog() {
        val loadingBinding = DialogLoadingBinding.inflate(this.layoutInflater)
        val profileView: ImageView = loadingBinding.imageView
        firebaseAuth.currentUser?.let {
            for (profile in it.providerData) {
                displayName = profile.displayName
                profileImageUri = profile.photoUrl
            }
            if (profileImageUri != null) {
                Log.d("USER_PROFILE_FROM_PROVIDER", "$profileImageUri")
                Picasso.with(applicationContext).load(profileImageUri).into(profileView);
            } else {
                if (user!!.photoUri != null) {
                    profileImageUri = Uri.parse(user!!.photoUri)
                    val filename = "profile_${user!!.uid}"
                    val firebaseStorageReference =
                        FirebaseStorage.getInstance().reference.child("profile/$filename")
                    Log.d("PROFILE_FILENAME", filename)
                    Log.d("PROFILE_URI", profileImageUri!!.toString())
                    val localFile = File.createTempFile("temp_profile", ".jpg", this.cacheDir)
                    firebaseStorageReference.getFile(localFile)
                        .addOnSuccessListener {
                            profileView.setImageBitmap(BitmapFactory.decodeFile(localFile.absolutePath))
                            Log.d(
                                "USER_PROFILE_PIC",
                                "User Profile has been successfully load!"
                            )
                        }
                        .addOnFailureListener {
//                            Toast.makeText(
//                                applicationContext,
//                                "User Profile failed to load! > ${it.message}",
//                                Toast.LENGTH_SHORT
//                            ).show()
                            Log.d("USER_PROFILE_PIC", "User Profile failed to load!")
                        }
//                    Log.d("profilePic_user", "$profileImageUri")
//                    Picasso.with(applicationContext).load(profileImageUri)
//                        .into(profileView);
                }
            }
        }

        loadingBuilder = AlertDialog.Builder(this)
        loadingBuilder.setCancelable(false)
        loadingBuilder.setView(loadingBinding.root)
        loadingBuilder.create()
        loadingDialog = loadingBuilder.create()
        if (loadingDialog.window != null) {
            loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        loadingDialog.show()
    }

    fun dismissLoadingDialog() {
        loadingDialog.dismiss()
    }


}