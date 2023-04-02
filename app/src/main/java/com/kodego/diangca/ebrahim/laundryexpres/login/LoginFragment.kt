package com.kodego.diangca.ebrahim.laundryexpres.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kodego.diangca.ebrahim.laundryexpres.MainFragment
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentLoginBinding

class LoginFragment(var mainActivity: MainFragment) : Fragment() {

    private var _binding: FragmentLoginBinding? = null
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
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
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
        binding.btnHome.setOnClickListener {
            mainActivity.indexActivity.mainFrame = mainActivity.indexActivity.supportFragmentManager.beginTransaction()
            mainActivity.indexActivity.mainFrame.replace(R.id.mainFrame, MainFragment(mainActivity.indexActivity))
            mainActivity.indexActivity.mainFrame.commit()
        }
    }

}