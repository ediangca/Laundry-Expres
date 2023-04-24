package com.kodego.diangca.ebrahim.laundryexpres.model

import android.os.Parcel
import android.os.Parcelable
import android.util.Log

data class BusinessInfo(
    var uid: String? = null,
    var businessName: String? = null,
    var businessLegalName: String? = null,
    var businessEmail: String? = null,
    var businessPhoneNumber: String? = null,
    var businessBIRImage: String? = null,
    var mondayFrom: String? = null,
    var mondayTo: String? = null,
    var TuesdayFrom: String? = null,
    var TuesdayTo: String? = null,
    var wednesdayFrom: String? = null,
    var wednesdayTo: String? = null,
    var thursdayFrom: String? = null,
    var thursdayTo: String? = null,
    var fridayFrom: String? = null,
    var fridayTo: String? = null,
    var SaturdayFrom: String? = null,
    var SaturdayTo: String? = null,
    var SundayFrom: String? = null,
    var SundayTo: String? = null,
    var HolidayFrom: String? = null,
    var HolidayTo: String? = null,
    var bankName: String? = null,
    var bankAccountName: String? = null,
    var bankAccountNumber: String? = null,
    var bankAccountBIC: String? = null,
    var bankProofImage: String? = null,
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
        parcel.readString()
    ) {
    }

    fun printLOG() {
        Log.d("BUSINESS INFO", toString())
        println(toString())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(businessName)
        parcel.writeString(businessLegalName)
        parcel.writeString(businessEmail)
        parcel.writeString(businessPhoneNumber)
        parcel.writeString(businessBIRImage)
        parcel.writeString(mondayFrom)
        parcel.writeString(mondayTo)
        parcel.writeString(TuesdayFrom)
        parcel.writeString(TuesdayTo)
        parcel.writeString(wednesdayFrom)
        parcel.writeString(wednesdayTo)
        parcel.writeString(thursdayFrom)
        parcel.writeString(thursdayTo)
        parcel.writeString(fridayFrom)
        parcel.writeString(fridayTo)
        parcel.writeString(SaturdayFrom)
        parcel.writeString(SaturdayTo)
        parcel.writeString(SundayFrom)
        parcel.writeString(SundayTo)
        parcel.writeString(HolidayFrom)
        parcel.writeString(HolidayTo)
        parcel.writeString(bankName)
        parcel.writeString(bankAccountName)
        parcel.writeString(bankAccountNumber)
        parcel.writeString(bankAccountBIC)
        parcel.writeString(bankProofImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "BusinessInfo(uid=$uid, businessName=$businessName, businessLegalName=$businessLegalName, businessEmail=$businessEmail, businessPhoneNumber=$businessPhoneNumber, businessBIRImage=$businessBIRImage, mondayFrom=$mondayFrom, mondayTo=$mondayTo, TuesdayFrom=$TuesdayFrom, TuesdayTo=$TuesdayTo, wednesdayFrom=$wednesdayFrom, wednesdayTo=$wednesdayTo, thursdayFrom=$thursdayFrom, thursdayTo=$thursdayTo, fridayFrom=$fridayFrom, fridayTo=$fridayTo, SaturdayFrom=$SaturdayFrom, SaturdayTo=$SaturdayTo, SundayFrom=$SundayFrom, SundayTo=$SundayTo, HolidayFrom=$HolidayFrom, HolidayTo=$HolidayTo, bankName=$bankName, bankAccountName=$bankAccountName, bankAccountNumber=$bankAccountNumber, bankAccountBIC=$bankAccountBIC, bankProofImage=$bankProofImage)"
    }

    companion object CREATOR : Parcelable.Creator<BusinessInfo> {
        override fun createFromParcel(parcel: Parcel): BusinessInfo {
            return BusinessInfo(parcel)
        }

        override fun newArray(size: Int): Array<BusinessInfo?> {
            return arrayOfNulls(size)
        }
    }


}
