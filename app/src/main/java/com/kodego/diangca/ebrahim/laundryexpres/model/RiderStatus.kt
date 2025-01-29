package com.kodego.diangca.ebrahim.laundryexpres.model



data class RiderStatus(
    val id: String,
    val lat: Double,
    val lng: Double,
    var activeRequests: Int, // Track the number of ongoing requests
    val messagingToken: String? = null // Add this property
)