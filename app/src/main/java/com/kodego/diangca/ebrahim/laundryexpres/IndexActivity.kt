package com.kodego.diangca.ebrahim.laundryexpres

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityIndexBinding

class IndexActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndexBinding
    lateinit var mainFrame: FragmentTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    private fun initComponent() {
        mainFrame = supportFragmentManager.beginTransaction()
        mainFrame.replace(R.id.mainFrame, MainFragment(this))
        mainFrame.commit()
    }
}