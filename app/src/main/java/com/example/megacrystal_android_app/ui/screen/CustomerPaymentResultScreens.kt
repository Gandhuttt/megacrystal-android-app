package com.example.megacrystal_android_app.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.megacrystal_android_app.R
import java.text.NumberFormat
import java.util.Locale

private val PaymentBackground = Color(0xFFFAF8FF)
private val PaymentBlue = Color(0xFF0066FF)

@Composable
fun CustomerPaymentSuccessScreen(
    totalAmount: Int,
    onHomeClick: () -> Unit
) {
    BackHandler(onBack = onHomeClick)

    val formattedAmount = NumberFormat
        .getNumberInstance(Locale.forLanguageTag("id-ID"))
        .format(totalAmount)

    Scaffold(containerColor = PaymentBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 12.dp)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.payment_success_figma),
                    contentDescription = "Pembayaran berhasil",
                    modifier = Modifier.size(96.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Berhasil!",
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1B20)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pesanan segera dikirim.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF49454F)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pembayaran Rp$formattedAmount terkonfirmasi.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF00945E)
                )

                Spacer(modifier = Modifier.height(44.dp))

                Button(
                    onClick = onHomeClick,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PaymentBlue)
                ) {
                    Text("Beranda")
                }
            }
        }
    }
}

@Composable
fun CustomerPaymentFailureScreen(
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)

    Scaffold(containerColor = PaymentBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-18).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.payment_failure_figma),
                    contentDescription = "Pembayaran gagal",
                    modifier = Modifier.size(96.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Gagal",
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1B20)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Waktu habis/Dibatalkan.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF49454F)
                )

                Spacer(modifier = Modifier.height(64.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onRetryClick,
                        modifier = Modifier.width(154.dp).height(40.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PaymentBlue)
                    ) {
                        Text("Coba Lagi")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = onBackClick,
                        modifier = Modifier.width(100.dp).height(40.dp)
                    ) {
                        Text("Kembali", color = Color(0xFF415B9B))
                    }
                }
            }
        }
    }
}
