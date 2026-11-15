package com.example.megacrystal_android_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.megacrystal_android_app.network.model.Product
import com.example.megacrystal_android_app.network.model.CreateOrderData
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.megacrystal_android_app.ui.screen.CustomerAuthScreen
import com.example.megacrystal_android_app.ui.screen.CustomerCheckoutScreen
import com.example.megacrystal_android_app.ui.screen.CustomerHistoryScreen
import com.example.megacrystal_android_app.ui.screen.CustomerHomeScreen
import com.example.megacrystal_android_app.ui.screen.CustomerOrderScreen
import com.example.megacrystal_android_app.ui.screen.CustomerPaymentSuccessScreen
import com.example.megacrystal_android_app.ui.screen.CustomerProfileScreen
import com.example.megacrystal_android_app.ui.screen.CustomerQrisScanScreen
import com.example.megacrystal_android_app.ui.screen.WorkerDashboardScreen
import com.example.megacrystal_android_app.ui.screen.WorkerDetailScreen
import com.example.megacrystal_android_app.ui.theme.MegacrystalandroidappTheme
import com.example.megacrystal_android_app.util.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MegacrystalandroidappTheme {
                val context = LocalContext.current
                val sessionManager = remember { SessionManager(context) }

                var isAuthenticated by rememberSaveable { mutableStateOf(sessionManager.isLoggedIn()) }
                var userRole by rememberSaveable { mutableStateOf(sessionManager.getUserRole() ?: "CUSTOMER") }
                var selectedWorkerOrderId by rememberSaveable { mutableStateOf<Int?>(null) }
                var customerName by rememberSaveable { mutableStateOf(sessionManager.getUserName() ?: "") }
                var customerEmail by rememberSaveable { mutableStateOf(sessionManager.getUserEmail() ?: "") }
                var customerPhone by rememberSaveable { mutableStateOf(sessionManager.getUserPhone() ?: "") }
                var showHistory by rememberSaveable { mutableStateOf(false) }
                var showProfile by rememberSaveable { mutableStateOf(false) }
                var selectedProduct by rememberSaveable { mutableStateOf<Product?>(null) }
                var checkoutQuantity by rememberSaveable { mutableStateOf<Int?>(null) }
                var activeOrder by rememberSaveable { mutableStateOf<CreateOrderData?>(null) }
                var paymentResult by rememberSaveable { mutableStateOf<String?>(null) }

                val product = selectedProduct
                val quantity = checkoutQuantity

                if (!isAuthenticated) {
                    CustomerAuthScreen { token, user, phone ->
                        sessionManager.saveSession(
                            token = token,
                            name = user.fullName,
                            email = user.email,
                            phone = phone,
                            role = user.role
                        )
                        customerName = user.fullName
                        customerEmail = user.email
                        customerPhone = phone
                        userRole = user.role
                        isAuthenticated = true
                    }
                } else if (showProfile) {
                    CustomerProfileScreen(
                        name = customerName,
                        email = customerEmail,
                        phone = customerPhone,
                        roleLabel = userRole,
                        onBackClick = { showProfile = false },
                        onSaveClick = { name, email, phone ->
                            customerName = name
                            customerEmail = email
                            customerPhone = phone
                            userRole = sessionManager.getUserRole() ?: userRole
                        },
                        onLogoutClick = {
                            sessionManager.clearSession()
                            showProfile = false
                            showHistory = false
                            selectedProduct = null
                            checkoutQuantity = null
                            activeOrder = null
                            paymentResult = null
                            customerName = ""
                            customerEmail = ""
                            customerPhone = ""
                            selectedWorkerOrderId = null
                            userRole = "CUSTOMER"
                            isAuthenticated = false
                        }
                    )
                } else if (userRole.equals("WORKER", ignoreCase = true) && selectedWorkerOrderId != null) {
                    WorkerDetailScreen(
                        orderId = selectedWorkerOrderId!!,
                        onBackClick = { selectedWorkerOrderId = null }
                    )
                } else if (userRole.equals("WORKER", ignoreCase = true)) {
                    WorkerDashboardScreen(
                        onProfileClick = { showProfile = true },
                        onOrderClick = { selectedWorkerOrderId = it }
                    )
                } else if (!userRole.equals("CUSTOMER", ignoreCase = true)) {
                    Column(Modifier.padding(horizontal = 24.dp, vertical = 64.dp)) {
                        Text("Akun $userRole")
                        Text("Pengelolaan admin tersedia di dashboard web MegaCrystal. Akun admin tidak digunakan untuk pemesanan pelanggan.")
                        Button(onClick = { showProfile = true }) { Text("Profil / Keluar") }
                    }
                } else if (paymentResult == "success") {
                    CustomerPaymentSuccessScreen(
                        totalAmount = activeOrder?.totalAmount ?: 0,
                        onHomeClick = {
                            paymentResult = null
                            activeOrder = null
                            checkoutQuantity = null
                            selectedProduct = null
                            showHistory = false
                        }
                    )
                } else if (activeOrder != null) {
                    val order = activeOrder!!
                    Column {
                        Text("Pesanan ${order.orderNumber}", modifier = Modifier.padding(top = 32.dp, start = 20.dp))
                        Text("Subtotal Rp${order.subtotalAmount} • Ongkir Rp${order.deliveryFee} • Total Rp${order.totalAmount}", modifier = Modifier.padding(horizontal = 20.dp))
                        CustomerQrisScanScreen(
                            paymentToken = order.payment.token,
                            expiresAt = order.payment.expiresAt,
                            onPaymentConfirmed = { paymentResult = "success" },
                            onCancelClick = {
                                activeOrder = null
                                checkoutQuantity = null
                                selectedProduct = null
                                showHistory = true
                            },
                            onExpired = {
                                activeOrder = null
                                checkoutQuantity = null
                                selectedProduct = null
                                showHistory = true
                            }
                        )
                    }
                } else if (product != null && quantity != null) {
                    CustomerCheckoutScreen(
                        product = product,
                        quantity = quantity,
                        onBackClick = { checkoutQuantity = null },
                        onPaySuccess = { createOrderData ->
                            activeOrder = createOrderData
                        }
                    )
                } else if (product != null) {
                    CustomerOrderScreen(
                        product = product,
                        onBackClick = { selectedProduct = null },
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
                        onOrderClick = { selectedProduct = it }
                    )
                }
            }
        }
    }
}
