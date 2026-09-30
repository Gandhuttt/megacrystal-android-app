package com.example.megacrystal_android_app.network.model

data class Product(
    val id: Int,
    val sku: String,
    val name: String,
    val packageWeightGrams: Int,
    val unitName: String,
    val unitPrice: Int,
    val availableStock: Int
)
