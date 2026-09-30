package com.example.megacrystal_android_app.network.model

data class CreateOrderData(
    val orderNumber: String,
    val status: String,
    val subtotalAmount: Int,
    val deliveryFee: Int,
    val totalAmount: Int,
    val payment: OrderPayment
)
