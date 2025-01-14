package com.kodego.diangca.ebrahim.laundryexpres.model

import android.os.Parcel
import android.os.Parcelable


data class Requirements(
    val uid: String? = null,
    val ioePerson: String? = null,
    val ioeRelationship: String? = null,
    val ioePhone: String? = null,
    val ioeTIN: String? = null,
    var selfieImage: String? = null,
    var nbiImage: String? = null,
    var licenseImage: String? = null,
    var orImage: String? = null,
    var crImage: String? = null,
    var vehicleImage: String? = null,
): Parcelable{
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
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(ioePerson)
        parcel.writeString(ioeRelationship)
        parcel.writeString(ioePhone)
        parcel.writeString(ioeTIN)
        parcel.writeString(selfieImage)
        parcel.writeString(nbiImage)
        parcel.writeString(licenseImage)
        parcel.writeString(orImage)
        parcel.writeString(crImage)
        parcel.writeString(vehicleImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Requirements> {
        override fun createFromParcel(parcel: Parcel): Requirements {
            return Requirements(parcel)
        }

        override fun newArray(size: Int): Array<Requirements?> {
            return arrayOfNulls(size)
        }
    }

}