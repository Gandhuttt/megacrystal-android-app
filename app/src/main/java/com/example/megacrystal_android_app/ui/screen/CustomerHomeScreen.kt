package com.example.megacrystal_android_app.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.megacrystal_android_app.network.MegaCrystalApiClient
import com.example.megacrystal_android_app.network.model.Product

private val Blue = Color(0xFF0066FF)
private val PageBackground = Color(0xFFFAF8FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    onProfileClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onOrderClick: (Product) -> Unit
) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(0) }

    LaunchedEffect(refresh) {
        isLoading = true
        error = null
        try {
            products = MegaCrystalApiClient.instance.getProducts().data
        } catch (e: Exception) {
            error = "Gagal memuat katalog. Periksa koneksi lalu coba lagi."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            TopAppBar(
                title = { Text("Beranda") },
                actions = {
                    IconButton(
                        onClick = onProfileClick
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
                .verticalScroll(rememberScrollState())
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
                text = "Pilih produk dan jumlah dari katalog MegaCrystal.",
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

            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = Blue)
                }
            } else if (error != null) {
                Text(error!!)
                Button(onClick = { refresh++ }) { Text("Coba lagi") }
            } else if (products.isEmpty()) {
                Text("Belum ada produk tersedia.")
                Button(onClick = { refresh++ }) { Text("Muat ulang") }
            } else {
                products.forEach { prod ->
                    ProductCard(
                        name = "${prod.name} (${prod.packageWeightGrams} g)",
                        stock = "Stok: ${prod.availableStock} ${prod.unitName}",
                        price = "Rp${String.format("%,d", prod.unitPrice).replace(',', '.')}",
                        imageRes = if (prod.packageWeightGrams == 8000) R.drawable.ice_crystal_8kg else R.drawable.ice_crystal_5kg,
                        onOrderClick = { onOrderClick(prod) },
                        available = prod.availableStock > 0,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                }
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
    available: Boolean,
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
                enabled = available,
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
