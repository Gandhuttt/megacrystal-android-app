package com.example.megacrystal_android_app.network.model

data class ProfileData(
    val id: Int,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: String
)

data class ProfileResponse(val data: ProfileData)

data class ProfileUpdateRequest(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val currentPassword: String? = null,
    val newPassword: String? = null
)
