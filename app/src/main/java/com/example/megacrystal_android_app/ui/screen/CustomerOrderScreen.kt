package com.example.megacrystal_android_app.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.megacrystal_android_app.R

private val OrderBlue = Color(0xFF0066FF)
private val OrderBackground = Color(0xFFFAF8FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderScreen(
    weightKg: Int = 5,
    onBackClick: () -> Unit = {},
    onCheckoutClick: (Int) -> Unit = {}
) {
    val stock = if (weightKg == 8) 15 else 25
    val price = if (weightKg == 8) "Rp22.000" else "Rp15.000"

    var quantityText by rememberSaveable(weightKg) {
        mutableStateOf("3")
    }
    val quantity = quantityText.toIntOrNull()

    BackHandler(onBack = onBackClick)

    Scaffold(
        containerColor = OrderBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MegaCrystal",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.icon_arrow_back_figma),
                            contentDescription = "Kembali",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrderBackground
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
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Pesan es kristal lebih mudah",
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1D1B20)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Es kristal higienis, siap diantar ke alamat Anda.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF49454F)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF7F2FA)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Text(
                        text = "Stok: $stock karung",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF49454F)
                    )
                    Text(
                        text = "Es kristal • $weightKg kg per karung",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1D1B20)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = quantityText,
                onValueChange = { input ->
                    quantityText = input.filter { it.isDigit() }.take(3)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Jml Karung") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                isError = quantity != null && quantity > stock
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$price per karung • minimum 1 karung",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF49454F)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    quantity?.let(onCheckoutClick)
                },
                enabled = quantity != null && quantity in 1..stock,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrderBlue
                )
            ) {
                Text("Checkout")
            }
        }
    }
}
