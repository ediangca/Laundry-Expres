package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardAccountBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import com.squareup.picasso.Picasso


class DashboardAccountFragment(var dashboardCustomer: DashboardCustomerActivity) : Fragment() {

    private var _binding: FragmentDashboardAccountBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")

    private var user: User? = null
    private var displayName: String? = null
    private var profileUri: Uri? = null

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

        user = dashboardCustomer.getUser()
        val bundle = this.arguments
        if (bundle!=null) {
            user = bundle.getParcelable<User>("user")!!
            Log.d("FETCH_USER", user.toString())
        }
        setUserDetails()
        binding.btnLogout.setOnClickListener {
            btnLogoutOnClickListener()
        }
    }

    private fun btnLogoutOnClickListener() {
        var loadingDialog: Dialog = Dialog(dashboardCustomer)

        val loadingBuilder = AlertDialog.Builder(context)
        loadingBuilder.setTitle("LOGOUT")
        loadingBuilder.setMessage("Do you really want to Logout?")
        loadingBuilder.setPositiveButton("Yes"){_,_ ->
            dashboardCustomer.showLoadingDialog()
            Handler(Looper.getMainLooper()).postDelayed({
                firebaseAuth.signOut()
                dashboardCustomer.signOut()
                dashboardCustomer.dismissLoadingDialog()
            }, 3000) // 3000 is the delayed time in milliseconds.
        }
        loadingBuilder.setNegativeButton("Cancel"){ _,_ ->
            loadingDialog.dismiss()
        }
        loadingDialog = loadingBuilder.create()
        if(loadingDialog.window != null){
            loadingDialog.window!!.setBackgroundDrawableResource(R.color.color_light_3)
        }
        loadingDialog.show()

    }

    private fun setUserDetails() {
        firebaseAuth.currentUser?.let {
            for (profile in it.providerData) {
                displayName = profile.displayName
                profileUri = profile.photoUrl
            }
        }

        if (profileUri!=null) {
            Log.d("profilePic", "$profileUri")
            val profileView: ImageView = binding.profilePic
            Picasso.with(context).load(profileUri).into(profileView);
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

        val profileView: ImageView = binding.profilePic
//        profileView.setImageResource(R.drawable.icon_logo)
        if (profileUri!=null) {
            Log.d("profilePic_profileData", "$profileUri")
            Picasso.with(context).load(profileUri).into(profileView);
        } else {
            if (user!!.photoUri!=null) {
                profileUri = Uri.parse(user!!.photoUri)
                Log.d("profilePic_user", "$profileUri")
                Picasso.with(context).load(profileUri).into(profileView);
            }
        }

        if (user!=null) {
            binding.apply {
                userAddress.text = user!!.address
                email.setText(user!!.email)
                phoneNo.setText(user!!.phone)
                firstName.setText(user!!.firstname)
                lastName.setText(user!!.lastname)
                val sexList = resources.getStringArray(R.array.sex)
                for ((index, value) in sexList.withIndex()){
                    if(value.equals(user!!.sex, true)){
                        sex.setSelection(index)
                    }
                }
                address.setText(user!!.address)
                city.setText(user!!.city)
                state.setText(user!!.state)
                zipCode.setText(user!!.zipCode)
                country.setText(user!!.country)
            }

        }


    }

}