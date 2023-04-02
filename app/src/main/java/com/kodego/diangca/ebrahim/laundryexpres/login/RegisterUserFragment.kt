package com.kodego.diangca.ebrahim.laundryexpres.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.kodego.diangca.ebrahim.laundryexpres.MainFragment
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentRegisterUserBinding
import kotlin.math.roundToInt

class RegisterUserFragment(var mainActivity: MainFragment) : Fragment() {

    private var _binding: FragmentRegisterUserBinding? = null
    private val binding get() = _binding!!

    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterUserBinding.inflate(layoutInflater, container, false)
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
        binding.btnSubmit.setOnClickListener {
            btnSubmitOnClickListener()
        }
    }

    private fun btnSubmitOnClickListener() {
        val mobileNo = binding.mobileNo.text.toString()
        val firstName = binding.firstName.text.toString()
        val lastName = binding.lastName.text.toString()
        val email = binding.email.text.toString()
        val password = binding.email.text.toString()

        if(mobileNo.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() ){
            Toast.makeText(context, "Please check empty fields!", Toast.LENGTH_SHORT).show()
            return
        }else{

            databaseReference.child("users").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.hasChild(mobileNo)){
                        Toast.makeText(context, "User is already Registered!", Toast.LENGTH_SHORT).show()
                    }else{
                        databaseReference.child("users").child(mobileNo).child("type").setValue("customer")
                        databaseReference.child("users").child(mobileNo).child("firstname").setValue(firstName)
                        databaseReference.child("users").child(mobileNo).child("lastname").setValue(lastName)
                        databaseReference.child("users").child(mobileNo).child("email").setValue(email)
                        databaseReference.child("users").child(mobileNo).child("password").setValue(password)
                        databaseReference.child("users").child(mobileNo).child("code").setValue(Math.random().roundToInt())
                        databaseReference.child("users").child(mobileNo).child("isVerified").setValue(false)
                        databaseReference.child("users").child(mobileNo).child("profile").setValue(null)

                        Toast.makeText(context, "User has been successfully Registered!", Toast.LENGTH_SHORT).show()
                        clearField()

                        mainActivity.indexActivity.mainFrame = mainActivity.indexActivity.supportFragmentManager.beginTransaction()
                        mainActivity.indexActivity.mainFrame.replace(R.id.mainFrame, LoginFragment(mainActivity));
                        mainActivity.indexActivity.mainFrame.addToBackStack(null);
                        mainActivity.indexActivity.mainFrame.commit();
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


        }

    }

    private fun clearField() {
        binding.mobileNo.text = null
        binding.firstName.text = null
        binding.lastName.text = null
        binding.email.text = null
        binding.password.text = null
    }

}