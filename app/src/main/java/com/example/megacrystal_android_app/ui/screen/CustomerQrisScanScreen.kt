package com.example.megacrystal_android_app.ui.screen

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.megacrystal_android_app.R
import kotlinx.coroutines.delay

private val QrisBlue = Color(0xFF0066FF)
private val QrisBackground = Color(0xFFFAF8FF)

@Composable
fun CustomerQrisScanScreen(
    onDemoSuccessClick: (() -> Unit)?,
    onCancelClick: () -> Unit,
    onExpired: () -> Unit
) {
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
                    text = "Gunakan aplikasi pembayaran pilihan Anda.",
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
                        Image(
                            painter = painterResource(R.drawable.qris_code_figma),
                            contentDescription = if (onDemoSuccessClick != null) {
                                "Contoh kode QRIS, ketuk untuk demo berhasil"
                            } else {
                                "Contoh kode QRIS dari Figma"
                            },
                            modifier = Modifier
                                .size(171.dp)
                                .then(
                                    if (onDemoSuccessClick != null) {
                                        Modifier.clickable(onClick = onDemoSuccessClick)
                                    } else {
                                        Modifier
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "CRYSTALFLOW",
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
