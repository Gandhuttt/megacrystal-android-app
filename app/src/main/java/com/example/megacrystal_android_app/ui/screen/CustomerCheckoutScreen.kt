package com.example.megacrystal_android_app.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
    weightKg: Int,
    quantity: Int,
    onBackClick: () -> Unit,
    onPayClick: () -> Unit
) {
    val unitPrice = if (weightKg == 8) 22_000 else 15_000
    val subtotal = quantity * unitPrice
    val deliveryFee = 10_000
    val total = subtotal + deliveryFee

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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Detail Pesanan",
                fontSize = 22.sp,
                lineHeight = 28.sp,
                color = Color(0xFF1D1B20)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(177.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF7F2FA)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Es Kristal MegaCrystal",
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$quantity karung × ${rupiah(unitPrice)}",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF49454F)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFCAC4D0))
                    Spacer(modifier = Modifier.height(16.dp))

                    CheckoutRow("Subtotal", rupiah(subtotal))
                    Spacer(modifier = Modifier.height(16.dp))
                    CheckoutRow("Ongkir", rupiah(deliveryFee))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.height(80.dp)) {
                Spacer(modifier = Modifier.height(8.dp))

                CheckoutRow(
                    label = "Total Harga",
                    value = rupiah(total),
                    isTotal = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pembayaran aman melalui QRIS. Kode berlaku 5 menit.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF49454F)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onPayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CheckoutBlue
                )
            ) {
                Text("Bayar QRIS")
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