package com.kodego.diangca.ebrahim.laundryexpres

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentStartGoBinding
import com.kodego.diangca.ebrahim.loginregistrationmodule.LoginFragment
import com.kodego.diangca.ebrahim.loginregistrationmodule.databinding.ActivityMainBinding


class StartGoFragment(private var mainActivity: MainFragment) : Fragment() {

    private var _binding: FragmentStartGoBinding? = null
    private val binding get() = _binding!!
    private lateinit var main_login_binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStartGoBinding.inflate(layoutInflater, container, false)
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
        _binding!!.btnBack.setOnClickListener {
            btnBackOnClickListener()
        }
        _binding!!.btnLogin.setOnClickListener {
            btnLoginOnClickListener()
        }

    }

    private fun btnLoginOnClickListener() {
//        var intent: Intent = Intent(
//            mainActivity,
//            com.kodego.diangca.ebrahim.loginregistrationmodule.MainActivity::class.java
//        );
//        if (intent!=null) {
//            startActivity(intent);
//        }
//        _binding!!.root.removeSelf()
//        main_login_binding = ActivityMainBinding.inflate(layoutInflater)
//        mainActivity.setContentView(binding.root)

//        mainActivity.fragmentAdapter.addFragment(LoginFragment())
//        mainActivity.binding.viewPager2.currentItem = 3

        mainActivity.indexActivity.mainFrame = mainActivity.indexActivity.supportFragmentManager.beginTransaction()
        mainActivity.indexActivity.mainFrame.replace(R.id.mainFrame, LoginFragment());
        mainActivity.indexActivity.mainFrame.addToBackStack(null);
        mainActivity.indexActivity.mainFrame.commit();
    }

    fun View?.removeSelf() {
        this ?: return
        val parentView = parent as? ViewGroup ?: return
        parentView.removeView(this)
    }

    private fun btnBackOnClickListener() {
        mainActivity.binding.viewPager2.currentItem = 1
    }

}