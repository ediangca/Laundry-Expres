package com.kodego.diangca.ebrahim.laundryexpres

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }

    }
    private fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
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
        if(!isNetworkAvailable(this)){
            Log.d("NETWORK", "NO NETWORK AVAILABLE ")
            var verifyDialog: Dialog = Dialog(this)
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            builder.setTitle("INTERNET CONNECTIVITY REQUIRED")
            builder.setMessage("PLEASE TURN ON YOU WIFI OR DATA TO USE THIS APP.")
            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS));
                verifyDialog.dismiss()
            }
            verifyDialog = builder.create()
            if(verifyDialog.window != null){
                verifyDialog.window!!.setBackgroundDrawableResource(R.color.color_light_3)
            }
            verifyDialog.show()
//            Toast.makeText(this, "NO NETWORK AVAILABLE", Toast.LENGTH_LONG).show()
            return
        }
            if (FirebaseAuth.getInstance().currentUser!=null) {
                val databaseRef = firebaseDatabase.reference.child("users")
                    .child(firebaseAuth.currentUser!!.uid)

                databaseRef.get().addOnCompleteListener { dataSnapshot ->
                    if (dataSnapshot.isSuccessful) {
                        user = dataSnapshot.result.getValue(User::class.java)
                        if (user!=null) {
                            userType = user!!.type!!

                            val isVerified = user!!.verified

                            if ((userType!="Customer") && !isVerified!!) {
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

    private fun showMain() {
        showLoadingDialog()
        Handler(Looper.getMainLooper()).postDelayed({
            dismissLoadingDialog()
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
                    startActivity((Intent(this, DashboardRiderActivity::class.java)))
                    finish()
                }
                else -> {

                }
            }
        }, 3000) // 3000 is the delayed time in milliseconds.
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
            if (profileImageUri!=null) {
                Log.d("USER_PROFILE_FROM_PROVIDER", "$profileImageUri")
                Picasso.with(applicationContext).load(profileImageUri).into(profileView);
            } else {
                if (user!!.photoUri!=null) {
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
                            Toast.makeText(
                                applicationContext,
                                "User Profile failed to load! > ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("USER_PROFILE_PIC", "User Profile failed to load!")
                        }
                    Log.d("profilePic_user", "$profileImageUri")
                    Picasso.with(applicationContext).load(profileImageUri)
                        .into(profileView);
                }
            }
        }

        loadingBuilder = AlertDialog.Builder(this)
        loadingBuilder.setCancelable(false)
        loadingBuilder.setView(loadingBinding.root)
        loadingBuilder.create()
        loadingDialog = loadingBuilder.create()
        if (loadingDialog.window!=null) {
            loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        loadingDialog.show()
    }

    fun dismissLoadingDialog() {
        loadingDialog.dismiss()
    }


}