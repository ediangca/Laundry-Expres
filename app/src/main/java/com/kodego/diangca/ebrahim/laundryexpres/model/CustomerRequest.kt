package com.kodego.diangca.ebrahim.laundryexpres.model

data class CustomerRequest(
    val transactionId: String,
    val customerId: String,
    val customerName: String,
    val pickupLocation: String,
    val distance: Double,
    val customerLat: Double,
    val customerLng: Double
)
