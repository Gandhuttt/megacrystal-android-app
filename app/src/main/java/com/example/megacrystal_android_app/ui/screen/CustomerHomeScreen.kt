package com.example.megacrystal_android_app.ui.screen

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.megacrystal_android_app.R
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults

private val Blue = Color(0xFF0066FF)
private val PageBackground = Color(0xFFFAF8FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    onHistoryClick: () -> Unit,
    onOrderClick: (Int) -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        containerColor = PageBackground,
        topBar = {
            TopAppBar(
                title = { Text("Beranda") },
                actions = {
                    IconButton(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Profil belum dibuat",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.icon_profile),
                            contentDescription = "Profil",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PageBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFEEEDF4)) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = {
                        Image(
                            painter = painterResource(R.drawable.icon_home),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Beranda") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFEAF2FF),
                        selectedTextColor = Blue
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onHistoryClick,
                    icon = {
                        Image(
                            painter = painterResource(R.drawable.icon_history),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Riwayat") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedTextColor = Color(0xFF49454F)
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = "MegaCrystal\n“Es higienis, antar cepat”",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D1B20)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dibuat dari air tersaring dan siap dikirim dari gudang terdekat.",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFF49454F)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Produk segar hari ini:",
                fontSize = 12.sp,
                color = Blue
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProductCard(
                    name = "Es Kristal 5 Kg",
                    stock = "Stok: 25 karung",
                    price = "Rp15.000",
                    imageRes = R.drawable.ice_crystal_5kg,
                    onOrderClick = { onOrderClick(5) },
                    modifier = Modifier.weight(1f)
                )

                ProductCard(
                    name = "Es Kristal 8 Kg",
                    stock = "Stok: 15 karung",
                    price = "Rp22.000",
                    imageRes = R.drawable.ice_crystal_8kg,
                    onOrderClick = { onOrderClick(8) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    name: String,
    stock: String,
    price: String,
    @DrawableRes imageRes: Int,
    onOrderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(294.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(126.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .padding(14.dp)
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D1B20)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = stock,
                fontSize = 12.sp,
                color = Color(0xFF49454F)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = price,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Blue
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onOrderClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                Text("Pesan")
            }
        }
    }
}
