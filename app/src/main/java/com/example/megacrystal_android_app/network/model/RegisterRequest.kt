package com.example.megacrystal_android_app.network.model

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val password: String
)
