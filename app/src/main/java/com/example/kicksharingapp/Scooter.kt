package com.example.kicksharingapp

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Scooter(val latitude: Double?, val longitude : Double? = null, val scooterID: String?) {
    constructor() : this(null, null, null)
}