package com.kodego.diangca.ebrahim.laundryexpres.model

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
data class Order(
    val uid: String? = null,
    val shopID: String? = null,
    val regular: Boolean? =null,
    val pets: Boolean? =null,
    val dry: Boolean? =null,
    val sneaker: Boolean? =null,
    val ratesUnit: String? = null,
    val regularWhiteMaxKg: Int? = null,
    val regularWhiteRate: Double? = null,
    val regularColorMaxKg: Int? = null,
    val regularColorRate: Double? = null,
    val regularComforterMaxKg: Int? = null,
    val regularComforterRate: Double? = null,
    val regularOthersMaxKg: Int? = null,
    val regularOthersRate: Double? = null,
    val petsWhiteMaxKg: Int? = null,
    val petsWhiteRate: Double? = null,
    val petsColorMaxKg: Int? = null,
    val petsColorRate: Double? = null,
    val dryWhiteMaxKg: Int? = null,
    val dryWhiteRate: Double? = null,
    val dryColorMaxKg: Int? = null,
    val dryColorRate: Double? = null,
    val sneakerOrdinaryMaxKg: Int? = null,
    val sneakerOrdinaryRate: Double? = null,
    val sneakerBootsMaxKg: Int? = null,
    val sneakerBootsRate: Double? = null,
    val totalLaundryPrice: Double? = null,
    val pickUpFee: Double? = null,
    val deliveryFee: Double? = null,
    val totalOrder: Double? = null,
    val status: String? = "WAITING",
    val datetimeCreated: String? = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date()),
    val datetimeUpdated: String? = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date()),
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    fun printLOG() {
        Log.d("RATES INFO", toString())
        println(toString())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(shopID)
        parcel.writeValue(regular)
        parcel.writeValue(pets)
        parcel.writeValue(dry)
        parcel.writeValue(sneaker)
        parcel.writeString(ratesUnit)
        parcel.writeValue(regularWhiteMaxKg)
        parcel.writeValue(regularWhiteRate)
        parcel.writeValue(regularColorMaxKg)
        parcel.writeValue(regularColorRate)
        parcel.writeValue(regularComforterMaxKg)
        parcel.writeValue(regularComforterRate)
        parcel.writeValue(regularOthersMaxKg)
        parcel.writeValue(regularOthersRate)
        parcel.writeValue(petsWhiteMaxKg)
        parcel.writeValue(petsWhiteRate)
        parcel.writeValue(petsColorMaxKg)
        parcel.writeValue(petsColorRate)
        parcel.writeValue(dryWhiteMaxKg)
        parcel.writeValue(dryWhiteRate)
        parcel.writeValue(dryColorMaxKg)
        parcel.writeValue(dryColorRate)
        parcel.writeValue(sneakerOrdinaryMaxKg)
        parcel.writeValue(sneakerOrdinaryRate)
        parcel.writeValue(sneakerBootsMaxKg)
        parcel.writeValue(sneakerBootsRate)
        parcel.writeValue(totalLaundryPrice)
        parcel.writeValue(pickUpFee)
        parcel.writeValue(deliveryFee)
        parcel.writeValue(totalOrder)
        parcel.writeString(status)
        parcel.writeString(datetimeCreated)
        parcel.writeString(datetimeUpdated)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Order(uid=$uid, shopID=$shopID, regular=$regular, pets=$pets, dry=$dry, sneaker=$sneaker, ratesUnit=$ratesUnit, regularWhiteMaxKg=$regularWhiteMaxKg, regularWhiteRate=$regularWhiteRate, regularColorMaxKg=$regularColorMaxKg, regularColorRate=$regularColorRate, regularComforterMaxKg=$regularComforterMaxKg, regularComforterRate=$regularComforterRate, regularOthersMaxKg=$regularOthersMaxKg, regularOthersRate=$regularOthersRate, petsWhiteMaxKg=$petsWhiteMaxKg, petsWhiteRate=$petsWhiteRate, petsColorMaxKg=$petsColorMaxKg, petsColorRate=$petsColorRate, dryWhiteMaxKg=$dryWhiteMaxKg, dryWhiteRate=$dryWhiteRate, dryColorMaxKg=$dryColorMaxKg, dryColorRate=$dryColorRate, sneakerOrdinaryMaxKg=$sneakerOrdinaryMaxKg, sneakerOrdinaryRate=$sneakerOrdinaryRate, sneakerBootsMaxKg=$sneakerBootsMaxKg, sneakerBootsRate=$sneakerBootsRate, totalLaundryPrice=$totalLaundryPrice, pickUpFee=$pickUpFee, deliveryFee=$deliveryFee, totalOrder=$totalOrder, status=$status, datetimeCreated=$datetimeCreated, datetimeUpdated=$datetimeUpdated)"
    }

    companion object CREATOR : Parcelable.Creator<Order> {
        override fun createFromParcel(parcel: Parcel): Order {
            return Order(parcel)
        }

        override fun newArray(size: Int): Array<Order?> {
            return arrayOfNulls(size)
        }
    }
}
