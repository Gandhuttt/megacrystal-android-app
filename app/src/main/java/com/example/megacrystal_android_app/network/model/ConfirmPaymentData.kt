package com.example.megacrystal_android_app.network.model

data class ConfirmPaymentData(
    val orderNumber: String,
    val paymentStatus: String,
    val orderStatus: String,
    val paidAt: String
)
