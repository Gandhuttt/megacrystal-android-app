package com.example.megacrystal_android_app.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.megacrystal_android_app.network.MegaCrystalApiClient
import com.example.megacrystal_android_app.util.SessionManager
import android.widget.Toast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val VaBlue = Color(0xFF0066FF)
private val VaBackground = Color(0xFFFAF8FF)
private val VaMuted = Color(0xFF49454F)
private val VaDanger = Color(0xFFB3261E)
private val VaBorder = Color(0xFFCAC4D0)

@Composable
fun CustomerVaScreen(
    orderNumber: String,
    totalAmount: Int,
    vaNumber: String?,
    bank: String?,
    expiresAt: String?,
    onPaymentConfirmed: () -> Unit,
    onCancelClick: () -> Unit,
    onExpired: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context) }
    val expiryMillis = remember(expiresAt) {
        expiresAt?.takeIf { it.isNotBlank() }?.let { parsePaymentExpiry(it) }
    }
    var completed by remember(orderNumber, vaNumber) { mutableStateOf(false) }
    var isCancelling by remember(orderNumber, vaNumber) { mutableStateOf(false) }
    var secondsLeft by remember(orderNumber, vaNumber) { mutableLongStateOf(0L) }
    val currentOnExpired by rememberUpdatedState(onExpired)
    val currentOnConfirmed by rememberUpdatedState(onPaymentConfirmed)
    val currentOnCancel by rememberUpdatedState(onCancelClick)
    val scope = rememberCoroutineScope()
    LaunchedEffect(orderNumber, expiryMillis) {
        val deadline = expiryMillis ?: return@LaunchedEffect
        while (!completed) {
            val remaining = deadline - System.currentTimeMillis()
            secondsLeft = if (remaining > 0) (remaining + 999L) / 1000L else 0L
            if (secondsLeft == 0L) {
                completed = true
                currentOnExpired()
                break
            }
            delay(250L)
        }
    }

    // Status lunas hanya berasal dari server (webhook Midtrans); polling setiap 3 detik.
    LaunchedEffect(orderNumber, expiryMillis) {
        val userToken = sessionManager.getToken() ?: return@LaunchedEffect
        val deadline = expiryMillis ?: return@LaunchedEffect
        while (!completed && System.currentTimeMillis() < deadline) {
            delay(3_000L)
            if (completed) break
            try {
                val order = MegaCrystalApiClient.instance
                    .getOrders(authorization = "Bearer $userToken")
                    .data
                    .firstOrNull { it.orderNumber == orderNumber }
                if (order != null && order.paymentStatus.equals("paid", ignoreCase = true)) {
                    completed = true
                    currentOnConfirmed()
                    break
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Abaikan satu kegagalan jaringan; polling berikutnya mencoba lagi.
            }
        }
    }

    val inputError = when {
        vaNumber.isNullOrBlank() -> "Nomor Virtual Account tidak tersedia. Kembali dan buat pembayaran baru."
        expiryMillis == null -> "Waktu kedaluwarsa pembayaran tidak tersedia. Kembali dan buat pembayaran baru."
        else -> null
    }

    Scaffold(containerColor = VaBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Pembayaran Virtual Account",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = Color(0xFF1D1B20)
            )
            Spacer(Modifier.height(6.dp))
            Surface(
                color = Color(0xFFECF3FF),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "VIRTUAL ACCOUNT ${bank?.uppercase() ?: "BCA"}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VaBlue
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Pesanan $orderNumber",
                fontSize = 13.sp,
                color = VaMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, VaBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nomor Virtual Account",
                        fontSize = 13.sp,
                        color = VaMuted
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = vaNumber ?: "-",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color(0xFF1D1B20),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        enabled = inputError == null && !completed,
                        colors = ButtonDefaults.buttonColors(containerColor = VaBlue),
                        onClick = {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText("VA", vaNumber ?: "")
                            )
                            Toast.makeText(context, "Nomor VA disalin", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Salin Nomor VA")
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = rupiah(totalAmount),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            if (expiryMillis != null) {
                Text(
                    text = if (secondsLeft > 0) {
                        "Sisa waktu: %02d:%02d".format(secondsLeft / 60, secondsLeft % 60)
                    } else {
                        "Waktu pembayaran habis"
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = VaBlue
                )
                Spacer(Modifier.height(12.dp))
            }

            Text(
                text = "Bayar melalui ATM, mobile banking, atau m-Banking BCA dengan memilih menu transfer ke Virtual Account. Status lunas diperiksa otomatis dari server setelah pembayaran diterima.",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                color = VaMuted
            )
            Spacer(Modifier.height(16.dp))

            if (inputError != null) {
                Text(
                    text = inputError,
                    color = VaDanger,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
            }
            if (inputError == null && !completed) {
                CircularProgressIndicator(color = VaBlue, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Menunggu pembayaran diterima…",
                    fontSize = 13.sp,
                    color = VaMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
            }
            OutlinedButton(
                onClick = {
                    val userToken = sessionManager.getToken()
                    if (userToken.isNullOrBlank()) {
                        currentOnCancel()
                        return@OutlinedButton
                    }
                    isCancelling = true
                    scope.launch {
                        try {
                            MegaCrystalApiClient.instance.cancelOrder(
                                authorization = "Bearer $userToken",
                                orderNumber = orderNumber
                            )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            // Pembatalan server gagal; pesanan tetap auto-expire dan stok terlepas.
                        }
                        isCancelling = false
                        currentOnCancel()
                    }
                },
                enabled = !isCancelling,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isCancelling) "Membatalkan…" else "Batalkan", color = VaBlue)
            }
        }
    }
}

private fun rupiah(amount: Int): String {
    val number = NumberFormat
        .getNumberInstance(Locale.forLanguageTag("id-ID"))
        .format(amount)
    return "Rp$number"
}
