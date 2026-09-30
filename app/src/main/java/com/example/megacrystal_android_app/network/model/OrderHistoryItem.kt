package com.example.megacrystal_android_app.network.model

data class OrderHistoryItem(
    val id: Int,
    val orderNumber: String,
    val status: String,
    val subtotalAmount: Int,
    val deliveryFee: Int,
    val totalAmount: Int,
    val placedAt: String,
    val paymentStatus: String?,
    val paymentExpiresAt: String?
)
