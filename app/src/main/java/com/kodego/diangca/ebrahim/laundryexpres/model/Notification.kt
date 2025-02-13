package com.kodego.diangca.ebrahim.laundryexpres.model

import android.os.Parcel
import android.os.Parcelable
import java.text.SimpleDateFormat
import java.util.Date

data class Notification(
    val orderNo: String? = null,
    var customerID: String? = null,
    val shopID: String? = null,
    val riderID: String? = null,
    val status: String? = null, //status of Booking
    val cunread: Boolean = false,
    val sunread: Boolean = false,
    val runread: Boolean = false,
    val note: String? = null,
    val notificationTimestamp: String? = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date())
        ): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(orderNo)
        parcel.writeString(customerID)
        parcel.writeString(shopID)
        parcel.writeString(riderID)
        parcel.writeString(status)
        parcel.writeByte(if (cunread) 1 else 0)
        parcel.writeByte(if (sunread) 1 else 0)
        parcel.writeByte(if (runread) 1 else 0)
        parcel.writeString(note)
        parcel.writeString(notificationTimestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Notification> {
        override fun createFromParcel(parcel: Parcel): Notification {
            return Notification(parcel)
        }

        override fun newArray(size: Int): Array<Notification?> {
            return arrayOfNulls(size)
        }
    }


}