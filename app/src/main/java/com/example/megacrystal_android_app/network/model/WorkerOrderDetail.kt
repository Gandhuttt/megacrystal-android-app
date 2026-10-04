package com.example.megacrystal_android_app.network.model

data class WorkerOrderDetail(
    val id: Int,
    val orderNumber: String,
    val status: String,
    val recipientName: String,
    val recipientPhone: String,
    val shippingAddress: String,
    val customerNote: String,
    val totalAmount: Int,
    val items: List<WorkerOrderItem>
)
