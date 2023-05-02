package com.kodego.diangca.ebrahim.laundryexpres.dashboard.partner

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.LoginActivity
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityDashboardPartnerBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogLoadingBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.NavHeaderPartnerBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.Shop
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import com.squareup.picasso.Picasso
import java.io.File

class DashboardPartnerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardPartnerBinding

    private lateinit var mainFrame: FragmentTransaction

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var dashboardHomeFragment: DashboardHomeFragment
    private lateinit var dashboardOrdersFragment: DashboardOrdersFragment
    private lateinit var dashboardNotificationFragment: DashboardNotificationFragment
    private lateinit var dashboardInboxFragment: DashboardInboxFragment
    private lateinit var dashboardAccountFragment: DashboardAccountFragment
    private lateinit var dashboardBusinessFragment: DashboardBusinessFragment

    private lateinit var loadingBuilder: AlertDialog.Builder
    private lateinit var loadingDialog: Dialog

    private var user: User? = null
    private var shop: Shop? = null
    private var bundle = Bundle()
    private var displayName: String? = null
    private var profileImageUri: Uri? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_LaundryExpress_Drawer) // change the theme/app background to pink
        binding = ActivityDashboardPartnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //for nav drawer
        drawerLayout = binding.drawerLayout
        navView = binding.navView
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(getDrawable(R.drawable.side_nav_bar))

        navView.setNavigationItemSelectedListener {
            navViewOnItemSelectedListener(it)
        }

        initComponents()
    }

    private fun initComponents() {
        Log.d("ON_ATTACH_DASHBOARD_PARTNER", "ATTACHED PARTNER DASHBOARD")
        retrieveUserDetails()
        dashboardHomeFragment = DashboardHomeFragment(this)
        dashboardOrdersFragment = DashboardOrdersFragment(this)
        dashboardNotificationFragment = DashboardNotificationFragment(this)
        dashboardInboxFragment = DashboardInboxFragment(this)
        dashboardAccountFragment = DashboardAccountFragment(this)
        dashboardBusinessFragment = DashboardBusinessFragment(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
        // Handle your other action bar items...
    }

    private fun showToast(message: String): Boolean {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        return true
    }


    private fun navViewOnItemSelectedListener(it: MenuItem): Boolean {

        onOptionsItemSelected(it)

        Log.d("MENU ITEM", "ID: ${it.itemId}")
        mainFrame = supportFragmentManager.beginTransaction()
        when (it.itemId) {
            R.id.navPartnerHome -> {
                mainFrame.replace(R.id.fragmentPartnerDashboard, dashboardHomeFragment);

            }
            R.id.navPartnerOrder -> {
                mainFrame.replace(R.id.fragmentPartnerDashboard, dashboardOrdersFragment);
            }
            R.id.navPartnerUpdates -> {
                mainFrame.replace(R.id.fragmentPartnerDashboard, dashboardNotificationFragment);
            }
            R.id.navPartnerInbox -> {
                mainFrame.replace(R.id.fragmentPartnerDashboard, dashboardInboxFragment);
            }
            R.id.navPartnerAccountPersonal -> {
                mainFrame.replace(R.id.fragmentPartnerDashboard, dashboardAccountFragment);
            }
            R.id.navLogout -> {
                signOut()
            }
            else -> {
                mainFrame.replace(R.id.fragmentPartnerDashboard, dashboardBusinessFragment);
            }

        }
        mainFrame.addToBackStack(null)
        mainFrame.commit();


        // Set action bar title
        title = it.title
        // Close the navigation drawer
        drawerLayout.closeDrawers()

        return true
    }

    private fun retrieveUserDetails() {
        var databaseRef = firebaseDatabase.reference.child("users")
            .child(firebaseAuth.currentUser!!.uid)

        databaseRef.get().addOnCompleteListener { dataSnapshot ->
            if (dataSnapshot.isSuccessful) {
                user = dataSnapshot.result.getValue(User::class.java)
                if (user!=null) {
                    setUserDetails(user)
                    mainFrame = supportFragmentManager.beginTransaction()
                    mainFrame.replace(R.id.fragmentPartnerDashboard, dashboardHomeFragment);
                    mainFrame.addToBackStack(null);
                    mainFrame.commit();
                }
            } else {
                Log.d("USER_DETAILS_NOT_FOUND", "USER NOT FOUND")
            }
        }
    }

    private fun retrieveShop(){
        val databaseRef = firebaseDatabase.reference.child("shop")
            .child(firebaseAuth.currentUser!!.uid)

        databaseRef.get().addOnCompleteListener { dataSnapshot ->
            if (dataSnapshot.isSuccessful) {
                shop = dataSnapshot.result.getValue(Shop::class.java)
            } else {
                Log.d("USER_DETAILS_NOT_FOUND", "USER NOT FOUND")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun setUserDetails(user: User?) {
        val navHeaderPartnerBinding: NavHeaderPartnerBinding =
            NavHeaderPartnerBinding.bind(binding.navView.getHeaderView(0))

/*
        val navHeader = navView.getHeaderView(0);
        val profileView: ImageView = navHeader.findViewById(R.id.profilePicture)
        val userDisplayName: TextView = navHeader.findViewById(R.id.userDisplayName)
        val textEmail: TextView = navHeader.findViewById(R.id.textEmail)
        val textMobileNo: TextView = navHeader.findViewById(R.id.textMobileNo)
*/

        navHeaderPartnerBinding.apply {

            val profileView: ImageView = profilePicture

            firebaseAuth.currentUser?.let {
                for (profile in it.providerData) {
                    displayName = profile.displayName
                    profileImageUri = profile.photoUrl
                }

                if (!displayName.isNullOrEmpty()) {
                    Log.d("displayUserName", "Hi ${displayName}, Good Day!")
                    userDisplayName.text = displayName
                }

                if (profileImageUri!=null) {
                    Log.d("profilePic_profileData", "$profileImageUri")
                    Picasso.with(applicationContext).load(profileImageUri)
                        .into(profileView);
                } else {
                    if (user!!.photoUri!=null) {
                        val filename = "profile_${user.uid}"
                        profileImageUri = Uri.parse(user!!.photoUri)
                        val firebaseStorageReference =
                            FirebaseStorage.getInstance().reference.child("profile/$filename")
                        val localFile = File.createTempFile("temp_profile", ".jpg")
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

                if (user!=null) {
                    Log.d("displayUserName", "Hi ${user.firstname} ${user.lastname}, Good Day!")
                    userDisplayName.text = "Hi ${user.firstname} ${user.lastname}, Good Day!"
                    textEmail.text = user.email
                    textMobileNo.text = user.phone

                    retrieveShop()
                }
            }
        }
    }

    fun setShop(shop: Shop?) {
        this.shop = shop
    }

    @JvmName("getShop1")
    fun getShop(): Shop? {
        return shop!!
    }

    fun setUser(user: User?) {
        this.user = user
    }

    fun getUser(): User? {
        return user!!
    }


    fun signOut() {
        val loadingBuilder = AlertDialog.Builder(this)
        loadingBuilder.setTitle("LOGOUT")
        loadingBuilder.setMessage("Do you really want to Logout?")
        loadingBuilder.setPositiveButton("Yes") { _, _ ->
            showLoadingDialog()
            Handler(Looper.getMainLooper()).postDelayed({
                firebaseAuth.signOut()
                dismissLoadingDialog()
                var loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(Intent(loginIntent))
                finish()
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


    fun showLoadingDialog() {
        val loadingBinding = DialogLoadingBinding.inflate(this.layoutInflater)
        loadingBuilder = AlertDialog.Builder(this)
        loadingBuilder.setCancelable(false)
        loadingBuilder.setView(loadingBinding.root)
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

