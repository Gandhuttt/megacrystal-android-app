package com.example.megacrystal_android_app.network.model

data class OrderPayment(
    val method: String,
    val token: String,
    val qrPayload: String,
    val expiresAt: String
)
