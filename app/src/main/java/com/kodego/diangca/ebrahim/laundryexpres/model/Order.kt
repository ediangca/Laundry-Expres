package com.kodego.diangca.ebrahim.laundryexpres.model

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
data class Order(
    var orderNo: String? = null,
    val uid: String? = null,
    val shopID: String? = null,
    val riderId: String? = null,
    val regular: Boolean? = null,
    val pets: Boolean? = null,
    val dry: Boolean? = null,
    val sneaker: Boolean? = null,
    val ratesUnit: String? = null,
    val regularWhiteMaxKg: Int? = null,
    val regularWhiteLoad: Int? = null,
    val regularWhiteRate: Double? = null,
    val regularColorMaxKg: Int? = null,
    val regularColorLoad: Int? = null,
    val regularColorRate: Double? = null,
    val regularComforterMaxKg: Int? = null,
    val regularComforterRate: Double? = null,
    val regularOthersMaxKg: Int? = null,
    val regularOthersLoad: Int? = null,
    val regularOthersRate: Double? = null,
    val petsWhiteMaxKg: Int? = null,
    val petsWhiteLoad: Int? = null,
    val petsWhiteRate: Double? = null,
    val petsColorMaxKg: Int? = null,
    val petsColorLoad: Int? = null,
    val petsColorRate: Double? = null,
    val dryWhiteMaxKg: Int? = null,
    val dryWhiteLoad: Int? = null,
    val dryWhiteRate: Double? = null,
    val dryColorMaxKg: Int? = null,
    val dryColorLoad: Int? = null,
    val dryColorRate: Double? = null,
    val sneakerOrdinaryMaxKg: Int? = null,
    val sneakerOrdinaryRate: Double? = null,
    val sneakerBootsMaxKg: Int? = null,
    val sneakerBootsRate: Double? = null,
    val totalLaundryPrice: Double? = null,
    val pickUpFee: Double? = null,
    val deliveryFee: Double? = null,
    val totalOrder: Double? = null,
    var status: String? = "WAITING",
    val notes: String? = null,
    val pickUpDatetime: String? = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date()),
    val deliveryDatetime: String? = SimpleDateFormat("yyyy-MM-d HH:mm:ss").format(Date()),
) : Parcelable {


    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
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
        parcel.readString(),
        parcel.readString()
    ) {
    }

    fun printLOG() {
        Log.d("RATES INFO", toString())
        println(toString())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(orderNo)
        parcel.writeString(uid)
        parcel.writeString(shopID)
        parcel.writeString(riderId)
        parcel.writeValue(regular)
        parcel.writeValue(pets)
        parcel.writeValue(dry)
        parcel.writeValue(sneaker)
        parcel.writeString(ratesUnit)
        parcel.writeValue(regularWhiteMaxKg)
        parcel.writeValue(regularWhiteLoad)
        parcel.writeValue(regularWhiteRate)
        parcel.writeValue(regularColorMaxKg)
        parcel.writeValue(regularColorLoad)
        parcel.writeValue(regularColorRate)
        parcel.writeValue(regularComforterMaxKg)
        parcel.writeValue(regularComforterRate)
        parcel.writeValue(regularOthersMaxKg)
        parcel.writeValue(regularOthersLoad)
        parcel.writeValue(regularOthersRate)
        parcel.writeValue(petsWhiteMaxKg)
        parcel.writeValue(petsWhiteLoad)
        parcel.writeValue(petsWhiteRate)
        parcel.writeValue(petsColorMaxKg)
        parcel.writeValue(petsColorLoad)
        parcel.writeValue(petsColorRate)
        parcel.writeValue(dryWhiteMaxKg)
        parcel.writeValue(dryWhiteLoad)
        parcel.writeValue(dryWhiteRate)
        parcel.writeValue(dryColorMaxKg)
        parcel.writeValue(dryColorLoad)
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
        parcel.writeString(notes)
        parcel.writeString(pickUpDatetime)
        parcel.writeString(deliveryDatetime)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Order(orderNo=$orderNo, uid=$uid, shopID=$shopID, riderId=$riderId, regular=$regular, pets=$pets, dry=$dry, sneaker=$sneaker, ratesUnit=$ratesUnit, regularWhiteMaxKg=$regularWhiteMaxKg, regularWhiteLoad=$regularWhiteLoad, regularWhiteRate=$regularWhiteRate, regularColorMaxKg=$regularColorMaxKg, regularColorLoad=$regularColorLoad, regularColorRate=$regularColorRate, regularComforterMaxKg=$regularComforterMaxKg, regularComforterRate=$regularComforterRate, regularOthersMaxKg=$regularOthersMaxKg, regularOthersLoad=$regularOthersLoad, regularOthersRate=$regularOthersRate, petsWhiteMaxKg=$petsWhiteMaxKg, petsWhiteLoad=$petsWhiteLoad, petsWhiteRate=$petsWhiteRate, petsColorMaxKg=$petsColorMaxKg, petsColorLoad=$petsColorLoad, petsColorRate=$petsColorRate, dryWhiteMaxKg=$dryWhiteMaxKg, dryWhiteLoad=$dryWhiteLoad, dryWhiteRate=$dryWhiteRate, dryColorMaxKg=$dryColorMaxKg, dryColorLoad=$dryColorLoad, dryColorRate=$dryColorRate, sneakerOrdinaryMaxKg=$sneakerOrdinaryMaxKg, sneakerOrdinaryRate=$sneakerOrdinaryRate, sneakerBootsMaxKg=$sneakerBootsMaxKg, sneakerBootsRate=$sneakerBootsRate, totalLaundryPrice=$totalLaundryPrice, pickUpFee=$pickUpFee, deliveryFee=$deliveryFee, totalOrder=$totalOrder, status=$status, notes=$notes, pickUpDatetime=$pickUpDatetime, deliveryDatetime=$deliveryDatetime)"
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
