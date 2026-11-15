package com.example.megacrystal_android_app.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.megacrystal_android_app.R
import com.example.megacrystal_android_app.network.MegaCrystalApiClient
import com.example.megacrystal_android_app.network.model.ShipOrderRequest
import com.example.megacrystal_android_app.network.model.WorkerOrder
import com.example.megacrystal_android_app.network.model.WorkerOrderDetail
import com.example.megacrystal_android_app.util.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val WorkerBackground = Color(0xFFFAF8FF)
private val WorkerBlue = Color(0xFF0066FF)
private val WorkerText = Color(0xFF1D1B20)
private val WorkerSecondary = Color(0xFF49454F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDashboardScreen(
    onProfileClick: () -> Unit,
    onOrderClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context) }
    var orders by remember { mutableStateOf<List<WorkerOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadVersion by remember { mutableStateOf(0) }

    LaunchedEffect(reloadVersion) {
        isLoading = true
        error = null
        try {
            val token = sessionManager.getToken()
            if (token.isNullOrBlank()) {
                error = "Sesi tidak tersedia. Silakan masuk kembali."
            } else {
                orders = MegaCrystalApiClient.instance.getWorkerOrders("Bearer $token").data
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = "Gagal memuat pesanan: ${e.localizedMessage ?: "Kesalahan jaringan"}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = WorkerBackground,
        topBar = {
            TopAppBar(
                title = { Text("Tugas Gudang", color = WorkerText) },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Image(
                            painter = painterResource(R.drawable.icon_profile),
                            contentDescription = "Profil",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WorkerBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState()).padding(20.dp)
        ) {
            when {
                isLoading -> WorkerLoading()
                error != null -> WorkerError(error.orEmpty()) { reloadVersion++ }
                orders.isEmpty() -> Text("Belum ada pesanan untuk ditangani.", color = WorkerSecondary)
                else -> {
                    Text("Antrian pesanan", color = WorkerBlue, fontSize = 12.sp)
                    Text(
                        "${orders.size} pesanan", color = WorkerText, fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(16.dp))
                    orders.forEach { order ->
                        WorkerOrderCard(order = order, onClick = { onOrderClick(order.id) })
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkerOrderCard(order: WorkerOrder, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xFFF3F2F8), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFC5C5CF), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(16.dp)
    ) {
        Text(order.orderNumber, color = WorkerBlue, fontSize = 12.sp)
        Text(order.customerName, color = WorkerText, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(order.itemSummary, color = WorkerSecondary)
        Text(order.shippingAddress, color = WorkerSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text("Status: ${order.status}", color = WorkerBlue, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(orderId: Int, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context) }
    val scope = rememberCoroutineScope()
    var order by remember(orderId) { mutableStateOf<WorkerOrderDetail?>(null) }
    var isLoading by remember(orderId) { mutableStateOf(true) }
    var isActing by remember(orderId) { mutableStateOf(false) }
    var loadError by remember(orderId) { mutableStateOf<String?>(null) }
    var actionError by remember(orderId) { mutableStateOf<String?>(null) }
    var reloadVersion by remember(orderId) { mutableStateOf(0) }
    var sealChecked by rememberSaveable(orderId) { mutableStateOf(false) }
    var addressConfirmed by rememberSaveable(orderId) { mutableStateOf(false) }

    suspend fun refreshOrder() {
        isLoading = true
        loadError = null
        try {
            val token = sessionManager.getToken()
            if (token.isNullOrBlank()) {
                loadError = "Sesi tidak tersedia. Silakan masuk kembali."
            } else {
                order = MegaCrystalApiClient.instance.getWorkerOrderDetail("Bearer $token", orderId).data
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            loadError = "Gagal memuat detail pesanan: ${e.localizedMessage ?: "Kesalahan jaringan"}"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(orderId, reloadVersion) { refreshOrder() }
    BackHandler(onBack = onBackClick)

    Scaffold(
        containerColor = WorkerBackground,
        topBar = {
            TopAppBar(
                title = { Text("Detail Pengiriman", color = WorkerText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Image(
                            painter = painterResource(R.drawable.icon_arrow_back_figma),
                            contentDescription = "Kembali", modifier = Modifier.size(48.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WorkerBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState()).padding(20.dp)
        ) {
            if (isLoading) WorkerLoading()
            loadError?.let { message ->
                WorkerError(message) { if (!isActing && !isLoading) reloadVersion++ }
            }
            order?.let { detail ->
                Text("Pesanan ${detail.orderNumber}", color = WorkerBlue, fontSize = 12.sp)
                Text(
                    detail.recipientName, color = WorkerText, fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Status: ${detail.status}", color = WorkerBlue)
                Spacer(Modifier.height(24.dp))
                WorkerDetailSection("Info Alamat Tujuan") {
                    Text(detail.shippingAddress, color = WorkerText)
                    Spacer(Modifier.height(8.dp))
                    Text("Penerima: ${detail.recipientName}", color = WorkerSecondary)
                    Text("Telepon: ${detail.recipientPhone}", color = WorkerSecondary)
                }
                WorkerDetailSection("Catatan Pembeli") {
                    Text(
                        detail.customerNote?.takeIf { it.isNotBlank() } ?: "Tidak ada catatan pembeli.",
                        color = WorkerSecondary
                    )
                }
                WorkerDetailSection("Barang Pesanan") {
                    detail.items.forEach { item ->
                        Text(
                            "${item.productName} · ${item.packageWeightGrams} g × ${item.quantity}",
                            color = WorkerSecondary
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                val canAct = !isActing && !isLoading && loadError == null
                if (detail.status == "assigned") {
                    Text("Sebelum berangkat", color = WorkerText, fontWeight = FontWeight.SemiBold)
                    ChecklistRow("Segel karung sudah diperiksa", sealChecked, canAct) { sealChecked = it }
                    ChecklistRow("Alamat sudah dikonfirmasi", addressConfirmed, canAct) { addressConfirmed = it }
                    Spacer(Modifier.height(12.dp))
                }
                actionError?.let {
                    Text(it, color = Color(0xFFB3261E))
                    Spacer(Modifier.height(12.dp))
                }
                val actionLabel = when (detail.status) {
                    "paid" -> "Ambil Pesanan"
                    "assigned" -> "Kirim Pesanan"
                    "shipped" -> "Selesaikan Pesanan"
                    else -> null
                }
                if (actionLabel != null) {
                    Button(
                        onClick = {
                            if (!isActing && !isLoading && loadError == null &&
                                (detail.status != "assigned" || (sealChecked && addressConfirmed))) {
                                isActing = true
                                actionError = null
                                scope.launch {
                                    try {
                                        val token = sessionManager.getToken()
                                        if (token.isNullOrBlank()) {
                                            actionError = "Sesi tidak tersedia. Silakan masuk kembali."
                                        } else {
                                            val api = MegaCrystalApiClient.instance
                                            val auth = "Bearer $token"
                                            when (detail.status) {
                                                "paid" -> api.assignWorkerOrder(auth, orderId)
                                                "assigned" -> api.shipWorkerOrder(
                                                    auth, orderId,
                                                    ShipOrderRequest(
                                                        sealChecked = sealChecked,
                                                        addressConfirmed = addressConfirmed
                                                    )
                                                )
                                                "shipped" -> api.completeWorkerOrder(auth, orderId)
                                            }
                                            refreshOrder()
                                        }
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        actionError = "Gagal memperbarui pesanan: ${e.localizedMessage ?: "Kesalahan jaringan"}"
                                    } finally {
                                        isActing = false
                                    }
                                }
                            }
                        },
                        enabled = canAct && (detail.status != "assigned" || (sealChecked && addressConfirmed)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = WorkerBlue)
                    ) {
                        if (isActing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(actionLabel)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun WorkerDetailSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xFFF3F2F8), RoundedCornerShape(16.dp)).padding(16.dp)
    ) {
        Text(title, color = WorkerText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        content()
    }
    Spacer(Modifier.height(18.dp))
}

@Composable
private fun ChecklistRow(label: String, checked: Boolean, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        Spacer(Modifier.width(4.dp))
        Text(label, color = WorkerSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun WorkerLoading() {
    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = WorkerBlue)
    }
}

@Composable
private fun WorkerError(message: String, onRetry: () -> Unit) {
    Text(message, color = Color(0xFFB3261E))
    Spacer(Modifier.height(8.dp))
    Button(onClick = onRetry) { Text("Coba lagi") }
    Spacer(Modifier.height(16.dp))
}
