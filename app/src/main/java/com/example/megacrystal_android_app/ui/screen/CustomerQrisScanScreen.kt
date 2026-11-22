package com.example.megacrystal_android_app.ui.screen

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.megacrystal_android_app.network.MegaCrystalApiClient
import com.example.megacrystal_android_app.network.model.ConfirmPaymentRequest
import com.example.megacrystal_android_app.util.QrCodeEncoder
import com.example.megacrystal_android_app.util.SessionManager
import com.google.zxing.common.BitMatrix
import java.text.NumberFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val QrisBlue = Color(0xFF0066FF)
private val QrisBackground = Color(0xFFFAF8FF)
private val QrisMuted = Color(0xFF49454F)
private val QrisDanger = Color(0xFFB3261E)
private val QrisBorder = Color(0xFFCAC4D0)

@Composable
fun CustomerQrisScanScreen(
    orderNumber: String,
    totalAmount: Int,
    qrPayload: String?,
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
        expiresAt?.takeIf { it.isNotBlank() }?.let { parsePaymentExpiry(it) }
    }
    var isConfirming by remember(paymentToken, expiresAt) { mutableStateOf(false) }
    var completed by remember(paymentToken, expiresAt) { mutableStateOf(false) }
    var error by remember(paymentToken, expiresAt) { mutableStateOf<String?>(null) }
    var secondsLeft by remember(paymentToken, expiresAt) { mutableLongStateOf(0L) }
    val currentOnExpired by rememberUpdatedState(onExpired)
    val currentOnConfirmed by rememberUpdatedState(onPaymentConfirmed)

    val qrImage: ImageBitmap? = remember(qrPayload) {
        qrPayload
            ?.takeIf { it.isNotBlank() }
            ?.let { payload ->
                runCatching { QrCodeEncoder.encode(payload).toImageBitmap() }.getOrNull()
            }
    }

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

    // Payment status comes from the server, so a confirmation made elsewhere is
    // picked up without the customer pressing the simulation button.
    LaunchedEffect(paymentToken, orderNumber, expiryMillis) {
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
                // Keep the countdown running; the next poll retries.
            }
        }
    }

    val inputError = when {
        qrPayload.isNullOrBlank() -> "Kode QR pembayaran tidak tersedia. Kembali dan buat pembayaran baru."
        qrImage == null -> "Kode QR pembayaran gagal dibuat. Kembali dan buat pembayaran baru."
        expiryMillis == null -> "Waktu kedaluwarsa pembayaran tidak tersedia atau tidak valid. Kembali dan buat pembayaran baru."
        else -> null
    }
    // Pembayaran QRIS sungguhan tidak punya token simulasi; statusnya hanya
    // berubah lewat notifikasi penyedia, bukan tombol konfirmasi.
    val isSimulated = !paymentToken.isNullOrBlank()

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
                text = "Pembayaran QRIS",
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
                    text = if (isSimulated) "SIMULASI" else "QRIS",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = QrisBlue
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Pesanan $orderNumber",
                fontSize = 13.sp,
                color = QrisMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, QrisBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MEGACRYSTAL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1D1B20)
                    )
                    Spacer(Modifier.height(10.dp))
                    if (qrImage != null) {
                        Image(
                            bitmap = qrImage,
                            contentDescription = "Kode QR pembayaran simulasi",
                            modifier = Modifier.size(220.dp),
                            filterQuality = FilterQuality.None
                        )
                    }
                    Spacer(Modifier.height(10.dp))
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
                    color = QrisBlue
                )
                Spacer(Modifier.height(12.dp))
            }

            Text(
                text = if (isSimulated) {
                    "Kode QR ini berisi tautan konfirmasi simulasi, bukan QRIS EMVCo yang dapat dibayar. Tidak ada dana yang ditransfer. Status pembayaran diperiksa otomatis dari server."
                } else {
                    "Pindai kode QR ini dengan aplikasi pembayaran atau mobile banking yang mendukung QRIS. Status lunas diperiksa otomatis dari server setelah pembayaran diterima."
                },
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                color = QrisMuted
            )
            Spacer(Modifier.height(16.dp))

            val visibleError = inputError ?: error
            if (visibleError != null) {
                Text(
                    text = visibleError,
                    color = QrisDanger,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
            }
            if (isConfirming) {
                CircularProgressIndicator(color = QrisBlue)
                Spacer(Modifier.height(16.dp))
            }
            if (!isSimulated && inputError == null && !completed) {
                CircularProgressIndicator(color = QrisBlue, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Menunggu pembayaran diterima…",
                    fontSize = 13.sp,
                    color = QrisMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
            }
            if (isSimulated) {
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
                    Text(if (isConfirming) "Mengonfirmasi…" else "Simulasikan Pembayaran Berhasil")
                }
                Spacer(Modifier.height(12.dp))
            }
            OutlinedButton(onClick = onCancelClick, modifier = Modifier.fillMaxWidth()) {
                Text("Batalkan", color = QrisBlue)
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

private fun BitMatrix.toImageBitmap(): ImageBitmap {
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
        val row = y * width
        for (x in 0 until width) {
            pixels[row + x] = if (this[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
    }
    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888).asImageBitmap()
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
