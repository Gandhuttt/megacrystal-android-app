package com.example.megacrystal_android_app.ui.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.megacrystal_android_app.network.model.ConfirmPaymentRequest
import com.example.megacrystal_android_app.util.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val QrisBlue = Color(0xFF0066FF)
private val QrisBackground = Color(0xFFFAF8FF)

@Composable
fun CustomerQrisScanScreen(
    paymentToken: String?,
    onPaymentConfirmed: () -> Unit,
    onCancelClick: () -> Unit,
    onExpired: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = SessionManager(context)
    var isConfirming by rememberSaveable { mutableStateOf(false) }

    BackHandler(onBack = onCancelClick)

    val expiresAt = rememberSaveable { System.currentTimeMillis() + 299_000L }
    var secondsLeft by rememberSaveable { mutableLongStateOf(299L) }
    LaunchedEffect(expiresAt) {
        while (true) {
            secondsLeft = ((expiresAt - System.currentTimeMillis() + 999L) / 1000L)
                .coerceAtLeast(0L)
            if (secondsLeft == 0L) {
                onExpired()
                break
            }
            delay(1000)
        }
    }
    val timeText = "%02d:%02d".format(secondsLeft / 60, secondsLeft % 60)

    val doConfirmPayment = {
        val userToken = sessionManager.getToken()
        if (!paymentToken.isNullOrEmpty() && !userToken.isNullOrEmpty()) {
            isConfirming = true
            scope.launch {
                try {
                    MegaCrystalApiClient.instance.confirmPayment(
                        authorization = "Bearer $userToken",
                        request = ConfirmPaymentRequest(token = paymentToken)
                    )
                    isConfirming = false
                    onPaymentConfirmed()
                } catch (e: Exception) {
                    isConfirming = false
                    Toast.makeText(context, "Konfirmasi Pembayaran Gagal: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    onPaymentConfirmed()
                }
            }
        } else {
            onPaymentConfirmed()
        }
    }

    Scaffold(containerColor = QrisBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pindai QRIS",
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1B20)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Gunakan aplikasi pembayaran pilihan Anda.\nKetuk QR untuk simulasi bayar.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF49454F)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    modifier = Modifier
                        .width(220.dp)
                        .height(243.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFCAC4D0),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF7F2FA)
                ) {
                    Column(
                        modifier = Modifier.padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isConfirming) {
                            CircularProgressIndicator(
                                color = QrisBlue,
                                modifier = Modifier.size(60.dp).padding(top = 20.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.qris_code_figma),
                                contentDescription = "Ketuk untuk konfirmasi bayar",
                                modifier = Modifier
                                    .size(171.dp)
                                    .clickable(onClick = { doConfirmPayment() })
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "MEGACRYSTAL",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    color = Color(0xFFECF3FF),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Box(
                        modifier = Modifier.height(40.dp).padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (secondsLeft > 0) "Sisa Waktu $timeText" else "Waktu habis",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = QrisBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier.width(180.dp).height(48.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = QrisBlue
                    )
                ) {
                    Text("Batalkan")
                }
            }
        }
    }
}
