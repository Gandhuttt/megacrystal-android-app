package com.example.megacrystal_android_app.network.model

data class WorkerOrder(
    val id: Int,
    val orderNumber: String,
    val customerName: String,
    val recipientName: String,
    val shippingAddress: String,
    val totalAmount: Int,
    val assignedAt: String,
    val itemSummary: String
)
