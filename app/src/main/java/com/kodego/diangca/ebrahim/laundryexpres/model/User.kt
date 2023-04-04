package com.kodego.diangca.ebrahim.laundryexpres.model

import android.util.Log


data class User(
    var uid: String? = null,
    var firstname: String? = null,
    var lastname: String? = null,
    var email: String? = null,
    var type: String? = null,
    var password: String? = null,
    var phone: String? = null,
    var isVerified: Boolean? = null,
    val photoUrl: String? = null,
) {
    fun printLOG() {
        Log.d("USER", toString())
    }

    override fun toString(): String {
        return "User(uid=$uid, firstname=$firstname, lastname=$lastname, email=$email, type=$type, password=$password, phone=$phone, isVerified=$isVerified, photoUrl=$photoUrl)"
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
