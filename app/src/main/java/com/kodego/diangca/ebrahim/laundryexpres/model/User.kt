package com.kodego.diangca.ebrahim.laundryexpres.model

import android.os.Parcel
import android.os.Parcelable
import android.util.Log


data class User(
    var uid: String? = null,
    var email: String? = null,
    var type: String? = null,
    var password: String? = null,
    var firstname: String? = null,
    var lastname: String? = null,
    var sex: String? = null,
    var birthdate: String? = null,
    var address: String? = null,
    var phone: String? = null,
    val photoUrl: String? = null,
    var isVerified: Boolean? = null,
    val datetimeCreated: String? = null,
    val datetimeUpdated: String? = null
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
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readString()
    ) {
    }

    fun printLOG() {
        Log.d("USER", toString())
    }



    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(email)
        parcel.writeString(type)
        parcel.writeString(password)
        parcel.writeString(firstname)
        parcel.writeString(lastname)
        parcel.writeString(sex)
        parcel.writeString(birthdate)
        parcel.writeString(address)
        parcel.writeString(phone)
        parcel.writeString(photoUrl)
        parcel.writeValue(isVerified)
        parcel.writeString(datetimeCreated)
        parcel.writeString(datetimeUpdated)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "User(uid=$uid, email=$email, type=$type, password=$password, firstname=$firstname, lastname=$lastname, sex=$sex, birthdate=$birthdate, address=$address, phone=$phone, photoUrl=$photoUrl, isVerified=$isVerified, datetimeCreated=$datetimeCreated, datetimeUpdated=$datetimeUpdated)"
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
