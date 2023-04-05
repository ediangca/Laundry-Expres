package com.kodego.diangca.ebrahim.laundryexpres.registration

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kodego.diangca.ebrahim.laundryexpres.databinding.ActivityRegisterPartnerBinding

class RegisterPartnerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterPartnerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPartnerBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}