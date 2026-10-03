package com.example.megacrystal_android_app

import android.os.Bundle
import android.content.pm.ApplicationInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.megacrystal_android_app.ui.screen.CustomerCheckoutScreen
import com.example.megacrystal_android_app.ui.screen.CustomerAuthScreen
import com.example.megacrystal_android_app.ui.screen.CustomerHistoryScreen
import com.example.megacrystal_android_app.ui.screen.CustomerHomeScreen
import com.example.megacrystal_android_app.ui.screen.CustomerOrderScreen
import com.example.megacrystal_android_app.ui.screen.CustomerPaymentFailureScreen
import com.example.megacrystal_android_app.ui.screen.CustomerPaymentSuccessScreen
import com.example.megacrystal_android_app.ui.screen.CustomerProfileScreen
import com.example.megacrystal_android_app.ui.screen.CustomerQrisLoadingScreen
import com.example.megacrystal_android_app.ui.screen.CustomerQrisScanScreen
import com.example.megacrystal_android_app.ui.theme.MegacrystalandroidappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val demoEnabled = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        setContent {
            MegacrystalandroidappTheme {
                var isAuthenticated by rememberSaveable { mutableStateOf(false) }
                var customerName by rememberSaveable { mutableStateOf("") }
                var customerEmail by rememberSaveable { mutableStateOf("") }
                var customerPhone by rememberSaveable { mutableStateOf("") }
                var showHistory by rememberSaveable { mutableStateOf(false) }
                var showProfile by rememberSaveable { mutableStateOf(false) }
                var selectedProductKg by rememberSaveable { mutableStateOf<Int?>(null) }
                var checkoutQuantity by rememberSaveable { mutableStateOf<Int?>(null) }
                var isCreatingQris by rememberSaveable { mutableStateOf(false) }
                var isShowingQris by rememberSaveable { mutableStateOf(false) }
                var paymentResult by rememberSaveable { mutableStateOf<String?>(null) }

                val productKg = selectedProductKg
                val quantity = checkoutQuantity

                if (!isAuthenticated) {
                    CustomerAuthScreen { name, email, phone ->
                        customerName = name
                        customerEmail = email
                        customerPhone = phone
                        isAuthenticated = true
                    }
                } else if (showProfile) {
                    CustomerProfileScreen(
                        name = customerName,
                        email = customerEmail,
                        phone = customerPhone,
                        onBackClick = { showProfile = false },
                        onSaveClick = { name, email, phone ->
                            customerName = name
                            customerEmail = email
                            customerPhone = phone
                        },
                        onLogoutClick = {
                            showProfile = false
                            showHistory = false
                            selectedProductKg = null
                            checkoutQuantity = null
                            isCreatingQris = false
                            isShowingQris = false
                            paymentResult = null
                            customerName = ""
                            customerEmail = ""
                            customerPhone = ""
                            isAuthenticated = false
                        }
                    )
                } else if (paymentResult == "success") {
                    CustomerPaymentSuccessScreen(
                        totalAmount = ((if (productKg == 8) 22_000 else 15_000) *
                            (quantity ?: 1)) + 10_000,
                        onHomeClick = {
                            paymentResult = null
                            isShowingQris = false
                            isCreatingQris = false
                            checkoutQuantity = null
                            selectedProductKg = null
                            showHistory = false
                        }
                    )
                } else if (paymentResult == "failure") {
                    CustomerPaymentFailureScreen(
                        onRetryClick = {
                            paymentResult = null
                            isShowingQris = false
                            isCreatingQris = true
                        },
                        onBackClick = {
                            paymentResult = null
                            isShowingQris = false
                            isCreatingQris = false
                        }
                    )
                } else if (isShowingQris) {
                    CustomerQrisScanScreen(
                        onDemoSuccessClick = if (demoEnabled) {
                            { paymentResult = "success" }
                        } else {
                            null
                        },
                        onCancelClick = {
                            paymentResult = "failure"
                        },
                        onExpired = { paymentResult = "failure" }
                    )
                } else if (isCreatingQris) {
                    CustomerQrisLoadingScreen(
                        onBackClick = { isCreatingQris = false },
                        onFinished = { isShowingQris = true }
                    )
                } else if (productKg != null && quantity != null) {
                    CustomerCheckoutScreen(
                        weightKg = productKg,
                        quantity = quantity,
                        onBackClick = { checkoutQuantity = null },
                        onPayClick = { isCreatingQris = true }
                    )
                } else if (productKg != null) {
                    CustomerOrderScreen(
                        weightKg = productKg,
                        onBackClick = { selectedProductKg = null },
                        onCheckoutClick = { checkoutQuantity = it }
                    )
                } else if (showHistory) {
                    CustomerHistoryScreen(
                        onHomeClick = { showHistory = false }
                    )
                } else {
                    CustomerHomeScreen(
                        onProfileClick = { showProfile = true },
                        onHistoryClick = { showHistory = true },
                        onOrderClick = { selectedProductKg = it }
                    )
                }
            }
        }
    }
}
