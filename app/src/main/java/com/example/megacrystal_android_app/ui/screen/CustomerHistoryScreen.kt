package com.example.megacrystal_android_app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.megacrystal_android_app.util.SessionManager
import kotlinx.coroutines.CancellationException

private val HistoryBlue = Color(0xFF0066FF)
private val HistoryBackground = Color(0xFFFAF8FF)

private data class HistoryOrder(
    val number: String,
    val status: String,
    val date: String,
    val paymentStatus: String,
    val total: String,
    val statusBackground: Color,
    val statusTextColor: Color
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHistoryScreen(onHomeClick: () -> Unit = {}) {
    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context) }
    var dateSearch by rememberSaveable { mutableStateOf("") }
    var apiOrders by remember { mutableStateOf<List<HistoryOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var loadError by remember { mutableStateOf<String?>(null) }
    var retryCount by remember { mutableStateOf(0) }

    LaunchedEffect(retryCount) {
        isLoading = true
        loadError = null
        val token = sessionManager.getToken()
        if (!token.isNullOrBlank()) {
            try {
                val response = MegaCrystalApiClient.instance.getOrders("Bearer $token")
                apiOrders = response.data.map { item ->
                    val statusText = when (item.status.lowercase()) {
                        "pending" -> "Menunggu Pembayaran"
                        "unpaid" -> "Belum Dibayar"
                        "paid" -> "Dibayar"
                        "confirmed" -> "Dikonfirmasi"
                        "assigned" -> "Kurir Ditugaskan"
                        "shipped", "on_delivery" -> "Sedang Dikirim"
                        "delivered" -> "Terkirim"
                        "completed" -> "Selesai"
                        "cancelled", "canceled" -> "Dibatalkan"
                        "expired" -> "Kedaluwarsa"
                        else -> item.status
                    }
                    val bg = when (item.status.lowercase()) {
                        "delivered", "completed" -> Color(0xFFDDF5E7)
                        "paid", "confirmed", "assigned", "shipped", "on_delivery" -> Color(0xFFEAF2FF)
                        else -> Color(0xFFFFF3CD)
                    }
                    val txtColor = when (item.status.lowercase()) {
                        "delivered", "completed" -> Color(0xFF16834A)
                        "paid", "confirmed", "assigned", "shipped", "on_delivery" -> HistoryBlue
                        else -> Color(0xFF856404)
                    }
                    HistoryOrder(
                        number = item.orderNumber,
                        status = statusText,
                        date = item.placedAt.take(10),
                        paymentStatus = when (item.paymentStatus?.lowercase()) {
                            "paid" -> "Dibayar"
                            "pending" -> "Menunggu Pembayaran"
                            "unpaid" -> "Belum Dibayar"
                            "expired" -> "Kedaluwarsa"
                            "cancelled", "canceled" -> "Dibatalkan"
                            null -> "Tidak tersedia"
                            else -> item.paymentStatus.orEmpty()
                        },
                        total = "Rp${String.format("%,d", item.totalAmount).replace(',', '.')}",
                        statusBackground = bg,
                        statusTextColor = txtColor
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                loadError = "Gagal memuat riwayat. Periksa koneksi dan coba lagi."
            } finally {
                isLoading = false
            }
        } else {
            loadError = "Sesi tidak tersedia. Silakan masuk kembali."
            isLoading = false
        }
    }

    val shownOrders = apiOrders.filter {
        it.date.contains(dateSearch.trim(), ignoreCase = true) ||
                it.number.contains(dateSearch.trim(), ignoreCase = true)
    }

    Scaffold(
        containerColor = HistoryBackground,
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Pembelian") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HistoryBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFEEEDF4)) {
                NavigationBarItem(
                    selected = false,
                    onClick = onHomeClick,
                    icon = {
                        Image(
                            painter = painterResource(R.drawable.icon_home),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Beranda") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Image(
                            painter = painterResource(R.drawable.icon_history),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Riwayat") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = HistoryBlue,
                        indicatorColor = Color(0xFFEAF2FF)
                    )
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dateSearch,
                    onValueChange = { dateSearch = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Cari tanggal / No. Pesanan")
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFECE6F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    R.drawable.icon_calendar_history
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Daftar Riwayat Pembelian",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1B20)
                )
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HistoryBlue)
                    }
                }
            } else if (loadError != null) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = loadError.orEmpty(),
                            textAlign = TextAlign.Center,
                            color = Color(0xFFB3261E)
                        )
                        TextButton(onClick = { retryCount++ }) {
                            Text("Coba Lagi")
                        }
                    }
                }
            } else if (shownOrders.isEmpty()) {
                item {
                    Text(
                        text = "Tidak ada pesanan ditemukan.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF49454F)
                    )
                }
            } else {
                items(shownOrders) { order ->
                    HistoryOrderCard(order)
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }
        }
    }
}

@Composable
private fun HistoryOrderCard(order: HistoryOrder) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF7F2FA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "No. Pesanan",
                        fontSize = 12.sp,
                        color = Color(0xFF49454F)
                    )
                    Text(text = order.number, fontSize = 12.sp)
                }

                Box(
                    modifier = Modifier
                        .background(
                            order.statusBackground,
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = order.status,
                        fontSize = 12.sp,
                        color = order.statusTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFCAC4D0))
            Spacer(modifier = Modifier.height(12.dp))

            HistoryDetailRow("Tgl", order.date)
            Spacer(modifier = Modifier.height(6.dp))
            HistoryDetailRow("Pembayaran", order.paymentStatus)
            Spacer(modifier = Modifier.height(6.dp))
            HistoryDetailRow("Total Harga", order.total, isPrice = true)
        }
    }
}

@Composable
private fun HistoryDetailRow(
    label: String,
    value: String,
    isPrice: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF49454F)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            color = if (isPrice) HistoryBlue else Color(0xFF1D1B20)
        )
    }
}
