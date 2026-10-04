package com.example.megacrystal_android_app.network

import com.example.megacrystal_android_app.util.Constants
import com.example.megacrystal_android_app.network.model.LoginRequest
import com.example.megacrystal_android_app.network.model.LoginResponse
import com.example.megacrystal_android_app.network.model.RegisterRequest
import com.example.megacrystal_android_app.network.model.RegisterResponse
import com.example.megacrystal_android_app.network.model.ProductsResponse
import com.example.megacrystal_android_app.network.model.CreateOrderRequest
import com.example.megacrystal_android_app.network.model.CreateOrderResponse
import com.example.megacrystal_android_app.network.model.OrdersResponse
import com.example.megacrystal_android_app.network.model.ConfirmPaymentRequest
import com.example.megacrystal_android_app.network.model.ConfirmPaymentResponse
import com.example.megacrystal_android_app.network.model.WorkerOrdersResponse
import com.example.megacrystal_android_app.network.model.WorkerOrderDetailResponse
import com.example.megacrystal_android_app.network.model.ShipOrderRequest
import com.example.megacrystal_android_app.network.model.ShipOrderResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path


interface MegaCrystalApiInterface {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("api/products")
    suspend fun getProducts(): ProductsResponse

    @POST("api/orders")
    suspend fun createOrder(
        @Header("Authorization") authorization: String,
        @Body request: CreateOrderRequest
    ): CreateOrderResponse

    @GET("api/orders")
    suspend fun getOrders(
        @Header("Authorization") authorization: String
    ): OrdersResponse

    @POST("api/payments/confirm")
    suspend fun confirmPayment(
        @Header("Authorization") authorization: String,
        @Body request: ConfirmPaymentRequest
    ): ConfirmPaymentResponse

    @GET("api/worker/orders")
    suspend fun getWorkerOrders(
        @Header("Authorization") authorization: String
    ): WorkerOrdersResponse

    @GET("api/worker/orders/{id}")
    suspend fun getWorkerOrderDetail(
        @Header("Authorization") authorization: String,
        @Path("id") orderId: Int
    ): WorkerOrderDetailResponse

    @POST("api/worker/orders/{id}/ship")
    suspend fun shipWorkerOrder(
        @Header("Authorization") authorization: String,
        @Path("id") orderId: Int,
        @Body request: ShipOrderRequest
    ): ShipOrderResponse
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

