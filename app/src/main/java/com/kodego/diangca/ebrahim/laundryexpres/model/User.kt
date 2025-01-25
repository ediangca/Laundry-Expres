package com.kodego.diangca.ebrahim.laundryexpres.model

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
data class User(
    val uid: String? = null,
    val email: String? = null,
    var type: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val sex: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String? = null,
    val phone: String? = null,
    val photoUri: String? = null,
    val verified: Boolean? = null,
    val datetimeCreated: String? = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date()),
    val datetimeUpdated: String? = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date()),
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readString()
    ) {
    }

    fun printLOG() {
        Log.d("USER", toString())
        println(toString())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(email)
        parcel.writeString(type)
        parcel.writeString(firstname)
        parcel.writeString(lastname)
        parcel.writeString(sex)
        parcel.writeString(address)
        parcel.writeString(city)
        parcel.writeString(state)
        parcel.writeString(zipCode)
        parcel.writeString(country)
        parcel.writeString(phone)
        parcel.writeString(photoUri)
        parcel.writeValue(verified)
        parcel.writeString(datetimeCreated)
        parcel.writeString(datetimeUpdated)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "User(uid=$uid, email=$email, type=$type, firstname=$firstname, lastname=$lastname, sex=$sex, address=$address, city=$city, state=$state, zipCode=$zipCode, country=$country, phone=$phone, photoUrl=$photoUri, isVerified=$verified, datetimeCreated=$datetimeCreated, datetimeUpdated=$datetimeUpdated)"
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }


}


/*

firebaseDatabaseReference.child("users").child(uid).child("firstname").setValue(firstName)
firebaseDatabaseReference.child("users").child(uid).child("lastname").setValue(lastName)
firebaseDatabaseReference.child("users").child(uid).child("email").setValue(email)
firebaseDatabaseReference.child("users").child(uid).child("type").setValue("customer")
firebaseDatabaseReference.child("users").child(uid).child("password").setValue(password)
firebaseDatabaseReference.child("users").child(uid).child("phone").setValue(mobileNo)
firebaseDatabaseReference.child("users").child(uid).child("isVerified").setValue(false)

*/
