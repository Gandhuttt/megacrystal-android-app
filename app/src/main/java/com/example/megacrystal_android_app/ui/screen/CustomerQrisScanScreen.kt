package com.example.megacrystal_android_app.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.megacrystal_android_app.network.MegaCrystalApiClient
import com.example.megacrystal_android_app.network.model.ConfirmPaymentRequest
import com.example.megacrystal_android_app.util.SessionManager
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val QrisBlue = Color(0xFF0066FF)
private val QrisBackground = Color(0xFFFAF8FF)

@Composable
fun CustomerQrisScanScreen(
    paymentToken: String?,
    expiresAt: String?,
    onPaymentConfirmed: () -> Unit,
    onCancelClick: () -> Unit,
    onExpired: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember(context) { SessionManager(context) }
    val expiryMillis = remember(expiresAt) {
        expiresAt?.takeIf { it.isNotBlank() }?.let {
            parsePaymentExpiry(it)
        }
    }
    var isConfirming by remember(paymentToken, expiresAt) { mutableStateOf(false) }
    var completed by remember(paymentToken, expiresAt) { mutableStateOf(false) }
    var error by remember(paymentToken, expiresAt) { mutableStateOf<String?>(null) }
    var secondsLeft by remember(paymentToken, expiresAt) { mutableLongStateOf(0L) }
    val currentOnExpired by rememberUpdatedState(onExpired)
    val currentOnConfirmed by rememberUpdatedState(onPaymentConfirmed)

    BackHandler(onBack = onCancelClick)

    LaunchedEffect(paymentToken, expiryMillis) {
        val deadline = expiryMillis ?: return@LaunchedEffect
        while (!completed) {
            val remaining = deadline - System.currentTimeMillis()
            secondsLeft = if (remaining > 0) (remaining + 999L) / 1000L else 0L
            if (secondsLeft == 0L && !isConfirming) {
                completed = true
                currentOnExpired()
                break
            }
            delay(250L)
        }
    }

    val inputError = when {
        paymentToken.isNullOrBlank() -> "Token pembayaran tidak tersedia. Kembali dan buat pembayaran baru."
        expiryMillis == null -> "Waktu kedaluwarsa pembayaran tidak tersedia atau tidak valid. Kembali dan buat pembayaran baru."
        else -> null
    }

    Scaffold(containerColor = QrisBackground) { innerPadding ->
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
                text = "SIMULASI PEMBAYARAN",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = QrisBlue
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Ini bukan QRIS asli. Tidak ada kode QR untuk dipindai dan tidak ada uang yang ditransfer. Tombol di bawah mengonfirmasi pembayaran simulasi ke server.",
                textAlign = TextAlign.Center,
                color = Color(0xFF49454F)
            )
            Spacer(Modifier.height(20.dp))
            if (expiryMillis != null) {
                Text(
                    text = if (secondsLeft > 0) {
                        "Sisa waktu: %02d:%02d".format(secondsLeft / 60, secondsLeft % 60)
                    } else {
                        "Waktu pembayaran habis"
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = QrisBlue
                )
                Spacer(Modifier.height(16.dp))
            }
            val visibleError = inputError ?: error
            if (visibleError != null) {
                Text(
                    text = visibleError,
                    color = Color(0xFFB3261E),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
            }
            if (isConfirming) {
                CircularProgressIndicator(color = QrisBlue)
                Spacer(Modifier.height(16.dp))
            }
            Button(
                enabled = inputError == null && !isConfirming && !completed && secondsLeft > 0,
                colors = ButtonDefaults.buttonColors(containerColor = QrisBlue),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val token = paymentToken
                    val deadline = expiryMillis
                    val userToken = sessionManager.getToken()
                    when {
                        token.isNullOrBlank() -> error = "Token pembayaran tidak tersedia."
                        deadline == null -> error = "Waktu kedaluwarsa pembayaran tidak valid."
                        System.currentTimeMillis() >= deadline -> {
                            if (!completed) {
                                completed = true
                                currentOnExpired()
                            }
                        }
                        userToken.isNullOrBlank() -> error = "Sesi tidak tersedia. Silakan masuk kembali."
                        !isConfirming && !completed -> {
                            isConfirming = true
                            error = null
                            scope.launch {
                                try {
                                    val response = MegaCrystalApiClient.instance.confirmPayment(
                                        authorization = "Bearer $userToken",
                                        request = ConfirmPaymentRequest(token = token)
                                    )
                                    if (response.data.paymentStatus.equals("paid", ignoreCase = true)) {
                                        completed = true
                                        currentOnConfirmed()
                                    } else {
                                        error = "Server belum mengonfirmasi pembayaran sebagai paid. Silakan coba lagi sebelum kedaluwarsa."
                                    }
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    error = "Konfirmasi simulasi gagal. Periksa koneksi dan coba lagi sebelum kedaluwarsa."
                                } finally {
                                    isConfirming = false
                                }
                            }
                        }
                    }
                }
            ) {
                Text(if (isConfirming) "Mengonfirmasi…" else "Konfirmasi Pembayaran Simulasi")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onCancelClick, modifier = Modifier.fillMaxWidth()) {
                Text("Batalkan", color = QrisBlue)
            }
        }
    }
}

private fun parsePaymentExpiry(value: String): Long? {
    for (pattern in arrayOf("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ssXXX")) {
        val formatter = SimpleDateFormat(pattern, Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val position = ParsePosition(0)
        val parsed = formatter.parse(value, position)
        if (parsed != null && position.index == value.length) return parsed.time
    }
    return null
}
