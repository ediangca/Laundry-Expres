package com.kodego.diangca.ebrahim.laundryexpres.registration.partner

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogTimePickerBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentPartnerBusinessInfoBinding

class PartnerBusinessInfoFragment(var registerPartnerActivity: RegisterPartnerActivity) :
    Fragment() {

    var _binding: FragmentPartnerBusinessInfoBinding? = null
    val binding get() = _binding!!

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var currentItem = 0


    private lateinit var timePickerBuilder: AlertDialog.Builder
    private lateinit var timePickerDialogInterface: DialogInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPartnerBusinessInfoBinding.inflate(layoutInflater, container, false)
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

        binding.fromMonday.setOnClickListener {
            setTime(it)
        }
        binding.fromMondayBut.setOnClickListener {
            setTime(binding.fromMonday)
        }
        binding.toMonday.setOnClickListener {
            setTime(it)
        }
        binding.toMondayBut.setOnClickListener {
            setTime(binding.toMonday)
        }

        binding.fromTuesday.setOnClickListener {
            setTime(it)
        }
        binding.fromTuesdayBut.setOnClickListener {
            setTime(binding.fromTuesday)
        }
        binding.toTuesday.setOnClickListener {
            setTime(it)
        }
        binding.toTuesdayBut.setOnClickListener {
            setTime(binding.toTuesday)
        }

        binding.fromWednesday.setOnClickListener {
            setTime(it)
        }
        binding.fromWednesdayBut.setOnClickListener {
            setTime(binding.fromWednesday)
        }
        binding.toWednesday.setOnClickListener {
            setTime(it)
        }
        binding.toWednesdayBut.setOnClickListener {
            setTime(binding.toWednesday)
        }

        binding.fromThursday.setOnClickListener {
            setTime(it)
        }
        binding.fromThursdayBut.setOnClickListener {
            setTime(binding.fromThursday)
        }
        binding.toThursday.setOnClickListener {
            setTime(it)
        }
        binding.toThursdayBut.setOnClickListener {
            setTime(binding.toThursday)
        }

        binding.fromFriday.setOnClickListener {
            setTime(it)
        }
        binding.fromFridayBut.setOnClickListener {
            setTime(binding.fromFriday)
        }
        binding.toFriday.setOnClickListener {
            setTime(it)
        }
        binding.toFridayBut.setOnClickListener {
            setTime(binding.toFriday)
        }

        binding.fromSaturday.setOnClickListener {
            setTime(it)
        }
        binding.fromSaturdayBut.setOnClickListener {
            setTime(binding.fromSaturday)
        }
        binding.toSaturday.setOnClickListener {
            setTime(it)
        }
        binding.toSaturdayBut.setOnClickListener {
            setTime(binding.toSaturday)
        }

        binding.fromSunday.setOnClickListener {
            setTime(it)
        }
        binding.fromSundayBut.setOnClickListener {
            setTime(binding.fromSunday)
        }
        binding.toSunday.setOnClickListener {
            setTime(it)
        }
        binding.toSundayBut.setOnClickListener {
            setTime(binding.toSunday)
        }

        binding.fromHoliday.setOnClickListener {
            setTime(it)
        }
        binding.fromHolidayBut.setOnClickListener {
            setTime(binding.fromHoliday)
        }
        binding.toHoliday.setOnClickListener {
            setTime(it)
        }
        binding.toHolidayBut.setOnClickListener {
            setTime(binding.toHoliday)
        }

    }

    private fun setTime(view: View) {
        val textInputEditText: EditText = view as TextInputEditText
        val timePickerBinding = DialogTimePickerBinding.inflate(this.layoutInflater)

        var time: String? = null
        var h = timePickerBinding.timePicker.hour
        var m = timePickerBinding.timePicker.minute
        var am_pm = ""

        when {
            h==0 -> {
                h += 12
                am_pm = "AM"
            }
            h==12 -> am_pm = "PM"
            h > 12 -> {
                h -= 12
                am_pm = "PM"
            }
            else -> am_pm = "AM"
        }

        val hour = if (h < 10) "0$h" else h
        val min = if (m < 10) "0$m" else m
        // display format of time
        time = "$hour : $min $am_pm"

        timePickerBinding.timePicker.setOnTimeChangedListener { _, hour, minute ->
            var h = hour
            // AM_PM decider logic
            when {
                h==0 -> {
                    h += 12
                    am_pm = "AM"
                }
                h==12 -> am_pm = "PM"
                h > 12 -> {
                    h -= 12
                    am_pm = "PM"
                }
                else -> am_pm = "AM"
            }
            if (textInputEditText!=null) {
                val hour = if (h < 10) "0$h" else h
                val min = if (minute < 10) "0$minute" else minute
                // display format of time
                time = "$hour : $min $am_pm"
            }
        }

        timePickerBuilder = AlertDialog.Builder(registerPartnerActivity)
        timePickerBuilder.setCancelable(false)
        timePickerBuilder.setView(timePickerBinding.root)
        timePickerBuilder.setPositiveButton("Yes") { _, _ ->
            textInputEditText.setText(time)
        }
        // performing negative action
        timePickerBuilder.setNegativeButton("Cancel") { _, _ ->
        }
        timePickerBuilder.create()
        this.timePickerDialogInterface = timePickerBuilder.show()
    }

    private fun btnSubmitOnClickListener() {
        if (registerPartnerActivity.checkFields()) {
            Toast.makeText(
                registerPartnerActivity,
                "Please check error field(s)!",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            registerPartnerActivity.saveInfoToFirebase()
        }
    }


}