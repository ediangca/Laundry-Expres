package com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardAccountBinding


class DashboardAccountFragment(var dashboardCustomer: DashboardCustomerActivity) : Fragment() {

    private var _binding: FragmentDashboardAccountBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")

    private var displayName: String? = null

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

        displayUserName()

        binding.btnLogout.setOnClickListener {
            btnLogoutOnClickListener()
        }
    }

    private fun btnLogoutOnClickListener() {
        firebaseAuth.signOut()
        dashboardCustomer.signOut()
    }

    private fun displayUserName() {
        firebaseAuth.currentUser?.let {
            for (profile in it.providerData){
                displayName = profile.displayName
            }
        }

        if(!displayName.isNullOrEmpty()){
            Log.d("displayUserName", "Hi ${displayName}, Good Day!")
            binding.userDisplayName.text = displayName
        }else {
            firebaseDatabaseReference.child("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                            val firstname = snapshot.child(firebaseAuth.currentUser!!.uid)
                                .child("firstname").value.toString()
                            val lastname = snapshot.child(firebaseAuth.currentUser!!.uid)
                                .child("lastname").value.toString()
                            binding.userDisplayName.text = "$firstname $lastname"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            dashboardCustomer,
                            "${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

}