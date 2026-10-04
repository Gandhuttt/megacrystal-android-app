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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.megacrystal_android_app.R

private val WorkerBackground = Color(0xFFFAF8FF)
private val WorkerBlue = Color(0xFF0066FF)
private val WorkerText = Color(0xFF1D1B20)
private val WorkerSecondary = Color(0xFF49454F)

data class WorkerOrder(
    val id: String,
    val customer: String,
    val quantity: String,
    val destination: String,
    val address: String,
    val recipient: String,
    val phone: String,
    val note: String
)

val demoWorkerOrders = listOf(
    WorkerOrder(
        id = "MC-1048",
        customer = "Nadia Pratama",
        quantity = "3 karung 5 kg, 2 karung 8 kg",
        destination = "Jl. Melati 18, Sukajadi",
        address = "Jl. Melati No. 18, Sukajadi\nBandung 40162",
        recipient = "Nadia",
        phone = "0812 3456 7890",
        note = "Mohon telepon saat tiba. Titipkan ke petugas keamanan bila saya belum sampai."
    ),
    WorkerOrder("MC-1049", "Rizky Maulana", "2 karung 8 kg", "Kafe Tepi Kota, Pasteur", "Kafe Tepi Kota, Pasteur", "Rizky", "0812 3456 7891", ""),
    WorkerOrder("MC-1050", "Sari Wulandari", "5 karung 5 kg, 2 karung 8 kg", "Komplek Setra Duta B-12", "Komplek Setra Duta B-12", "Sari", "0812 3456 7892", ""),
    WorkerOrder("MC-1051", "Bima Catering", "2 karung 5 kg", "Jl. Cibaduyut Lama 42", "Jl. Cibaduyut Lama 42", "Bima", "0812 3456 7893", "")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDashboardScreen(
    onProfileClick: () -> Unit,
    onOrderClick: (String) -> Unit
) {
    Scaffold(
        containerColor = WorkerBackground,
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 8.dp),
                title = { Text("Tugas Gudang", color = WorkerText) },
                navigationIcon = { Spacer(Modifier.size(48.dp)) },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(18.dp))
            Text("Antrian aktif", color = WorkerBlue, fontSize = 12.sp, lineHeight = 16.sp)
            Text(
                "${demoWorkerOrders.size} pesanan",
                color = WorkerText,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            demoWorkerOrders.forEachIndexed { index, order ->
                WorkerOrderCard(
                    order = order,
                    highlighted = index == 0,
                    onClick = { onOrderClick(order.id) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun WorkerOrderCard(order: WorkerOrder, highlighted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .background(
                if (highlighted) Color(0xFFE7E6EB) else WorkerBackground,
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, Color(0xFFC5C5CF), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(order.customer, color = WorkerText, fontSize = 14.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "Pesanan: ${order.quantity}",
                color = WorkerSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                order.destination,
                color = WorkerSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Surface(shape = CircleShape, color = Color(0xFFD8F6E3)) {
            Text(
                "LUNAS",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                color = Color(0xFF118650),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Image(
            painter = painterResource(R.drawable.icon_arrow_right_worker),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(
    order: WorkerOrder,
    isShipped: Boolean,
    onBackClick: () -> Unit,
    onShipClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)

    Scaffold(
        containerColor = WorkerBackground,
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 8.dp),
                title = { Text("Detail Pengiriman", color = WorkerText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
                        Image(
                            painter = painterResource(R.drawable.icon_arrow_back_figma),
                            contentDescription = "Kembali",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WorkerBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            Text("Pesanan #${order.id}", color = WorkerBlue, fontSize = 12.sp, lineHeight = 16.sp)
            Text(order.customer, color = WorkerText, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(WorkerBlue, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    "${if (isShipped) "Dikirim" else "Siap diberangkatkan"} · ${order.quantity}",
                    color = WorkerSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(Modifier.height(28.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F2F8), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.worker_address_icon),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Info Alamat Tujuan", color = WorkerText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                Text(order.address, color = WorkerText, fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Penerima: ${order.recipient} · ${order.phone}",
                    color = WorkerSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F8FF), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFD5E4FF), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text("Catatan Pembeli", color = WorkerText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Text(
                    if (order.note.isBlank()) "Tidak ada catatan pembeli." else "“${order.note}”",
                    color = WorkerSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(12.dp))
            Text("Sebelum berangkat", color = WorkerText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            ChecklistRow("Segel karung sudah diperiksa")
            Spacer(Modifier.height(8.dp))
            ChecklistRow("Alamat sudah dikonfirmasi")
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onShipClick,
                enabled = !isShipped,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = WorkerBlue)
            ) {
                Text(if (isShipped) "Status: DIKIRIM" else "Ubah Status: DIKIRIM", fontSize = 14.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChecklistRow(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.worker_check_icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(label, color = WorkerSecondary, fontSize = 14.sp, lineHeight = 20.sp)
    }
}
