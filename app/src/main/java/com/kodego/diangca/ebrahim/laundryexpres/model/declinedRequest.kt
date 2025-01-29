package com.kodego.diangca.ebrahim.laundryexpres.model

data class declinedRequest(
    val riderId : String,
    val transactionId : String,
val customerId : String,
val customerName : String,
val pickupLocation : String,
val distance : Double,
val customerLat : Double,
val customerLng : Double,
val riderLat : Double,
val riderLng : Double,
val declineReason : String,  // 🟢 Include the decline reason
val timestamp : Long
)
