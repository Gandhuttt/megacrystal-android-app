package com.example.megacrystal_android_app.network.model

data class CreateOrderRequest(
    val items: List<OrderItemRequest>,
    val recipientName: String,
    val recipientPhone: String,
    val shippingAddress: String,
    val customerNote: String? = null
)
