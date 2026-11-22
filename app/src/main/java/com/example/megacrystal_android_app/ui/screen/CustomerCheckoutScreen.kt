package com.example.megacrystal_android_app.ui.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.megacrystal_android_app.R
import com.example.megacrystal_android_app.network.MegaCrystalApiClient
import com.example.megacrystal_android_app.network.model.CreateOrderData
import com.example.megacrystal_android_app.network.model.CreateOrderRequest
import com.example.megacrystal_android_app.network.model.OrderItemRequest
import com.example.megacrystal_android_app.util.SessionManager
import com.example.megacrystal_android_app.network.model.Product
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private val CheckoutBlue = Color(0xFF0066FF)
private val CheckoutBackground = Color(0xFFFAF8FF)

private fun rupiah(amount: Int): String {
    val number = NumberFormat
        .getNumberInstance(Locale.forLanguageTag("id-ID"))
        .format(amount)
    return "Rp$number"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCheckoutScreen(
    product: Product,
    quantity: Int,
    onBackClick: () -> Unit,
    onPaySuccess: (CreateOrderData) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = SessionManager(context)

    var recipientName by rememberSaveable { mutableStateOf(sessionManager.getUserName() ?: "") }
    var recipientPhone by rememberSaveable { mutableStateOf(sessionManager.getUserPhone() ?: "") }
    var shippingAddress by rememberSaveable { mutableStateOf("") }
    var customerNote by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var paymentMethod by rememberSaveable { mutableStateOf("qris") }

    val unitPrice = product.unitPrice
    val subtotal = quantity.toLong() * unitPrice

    BackHandler(onBack = onBackClick)

    Scaffold(
        containerColor = CheckoutBackground,
        topBar = {
            TopAppBar(
                title = { Text("Ringkasan") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Image(
                            painter = painterResource(
                                R.drawable.icon_arrow_back_figma
                            ),
                            contentDescription = "Kembali",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CheckoutBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Detail Pesanan",
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D1B20)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = recipientName,
                onValueChange = { recipientName = it },
                label = { Text("Nama Penerima") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = recipientPhone,
                onValueChange = { recipientPhone = it },
                label = { Text("No. HP Penerima") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = shippingAddress,
                onValueChange = { shippingAddress = it },
                label = { Text("Alamat Pengiriman") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customerNote,
                onValueChange = { customerNote = it.take(2000) },
                label = { Text("Keterangan") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF7F2FA)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${product.name} (${product.packageWeightGrams} g)",
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$quantity ${product.unitName} × ${rupiah(unitPrice)}",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF49454F)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFCAC4D0))
                    Spacer(modifier = Modifier.height(12.dp))

                    CheckoutRow("Estimasi subtotal", "Rp${NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(subtotal)}")
                    Text("Ongkir dan total final dihitung server setelah pesanan dibuat.")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Metode Pembayaran",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D1B20)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PaymentMethodOption(
                title = "QRIS",
                subtitle = "Pindai kode QR dengan aplikasi pembayaran (Midtrans)",
                selected = paymentMethod == "qris",
                onClick = { paymentMethod = "qris" }
            )
            PaymentMethodOption(
                title = "Virtual Account BCA",
                subtitle = "Transfer ke nomor Virtual Account (Midtrans)",
                selected = paymentMethod == "bank_transfer",
                onClick = { paymentMethod = "bank_transfer" }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SIMULASI pembayaran via Midtrans Sandbox. Total final dan waktu kedaluwarsa berasal dari server.",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Color(0xFF49454F)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val token = sessionManager.getToken()
                    if (token.isNullOrEmpty()) {
                        Toast.makeText(context, "Sesi habis, silakan login ulang", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (recipientName.isBlank() || recipientPhone.isBlank() || shippingAddress.isBlank()) {
                        Toast.makeText(context, "Lengkapi data pengiriman", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (quantity !in 1..product.availableStock) {
                        Toast.makeText(context, "Jumlah melebihi stok tersedia", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        try {
                            val response = MegaCrystalApiClient.instance.createOrder(
                                authorization = "Bearer $token",
                                request = CreateOrderRequest(
                                    items = listOf(OrderItemRequest(productId = product.id, quantity = quantity)),
                                    recipientName = recipientName.trim(),
                                    recipientPhone = recipientPhone.trim(),
                                    shippingAddress = shippingAddress.trim(),
                                    customerNote = customerNote.trim().takeIf { it.isNotEmpty() },
                                    paymentMethod = paymentMethod
                                )
                            )
                            isLoading = false
                            onPaySuccess(response.data)
                        } catch (e: Exception) {
                            isLoading = false
                            Toast.makeText(context, "Gagal membuat pesanan: ${e.localizedMessage ?: "Cek koneksi"}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CheckoutBlue
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (paymentMethod == "bank_transfer") "Buat pesanan • Virtual Account" else "Buat pesanan • QRIS")
                }
            }
        }
    }
}

@Composable
private fun CheckoutRow(
    label: String,
    value: String,
    isTotal: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = if (isTotal) 16.sp else 14.sp,
            fontWeight = if (isTotal) FontWeight.SemiBold else FontWeight.Normal,
            color = Color(0xFF49454F)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            fontSize = if (isTotal) 16.sp else 14.sp,
            fontWeight = if (isTotal) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isTotal) CheckoutBlue else Color(0xFF1D1B20)
        )
    }
}

@Composable
private fun PaymentMethodOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFECF3FF) else Color.White,
            contentColor = Color(0xFF1D1B20)
        ),
        border = if (selected) BorderStroke(2.dp, CheckoutBlue) else BorderStroke(1.dp, Color(0xFFCAC4D0)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, fontSize = 12.sp, lineHeight = 17.sp, color = Color(0xFF49454F))
        }
    }
}
