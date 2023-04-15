package com.example.kicksharingapp

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(val userID: String? = null, val userName: String? = null, val userRole: String? = null) {
}