package com.kodego.diangca.ebrahim.laundryexpres.registration

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityRegisterRiderBinding

class RegisterRiderActivity : AppCompatActivity() {


    private lateinit var binding: ActivityRegisterRiderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterRiderBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}