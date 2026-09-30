package com.example.megacrystal_android_app.network

import com.example.megacrystal_android_app.util.Constants
import com.example.megacrystal_android_app.network.model.LoginRequest
import com.example.megacrystal_android_app.network.model.LoginResponse
import com.example.megacrystal_android_app.network.model.RegisterRequest
import com.example.megacrystal_android_app.network.model.RegisterResponse
import com.example.megacrystal_android_app.network.model.ProductsResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface MegaCrystalApiInterface {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("api/products")
    suspend fun getProducts(): ProductsResponse
}

object MegaCrystalApiClient {
    val instance: MegaCrystalApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MegaCrystalApiInterface::class.java)
    }
}

