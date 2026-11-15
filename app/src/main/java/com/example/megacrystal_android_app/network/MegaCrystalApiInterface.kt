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
import com.example.megacrystal_android_app.network.model.ProfileResponse
import com.example.megacrystal_android_app.network.model.ProfileUpdateRequest
import retrofit2.http.PATCH
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

    @GET("api/profile")
    suspend fun getProfile(@Header("Authorization") authorization: String): ProfileResponse

    @PATCH("api/profile")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: ProfileUpdateRequest
    ): ProfileResponse

    @POST("api/worker/orders/{id}/assign")
    suspend fun assignWorkerOrder(
        @Header("Authorization") authorization: String,
        @Path("id") orderId: Int,
        @Body request: Map<String, Int> = emptyMap()
    ): ShipOrderResponse

    @POST("api/worker/orders/{id}/complete")
    suspend fun completeWorkerOrder(
        @Header("Authorization") authorization: String,
        @Path("id") orderId: Int,
        @Body request: Map<String, String> = emptyMap()
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

