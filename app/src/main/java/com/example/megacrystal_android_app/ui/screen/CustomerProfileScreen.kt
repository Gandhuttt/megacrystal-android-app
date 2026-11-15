package com.example.megacrystal_android_app.ui.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.megacrystal_android_app.R
import com.example.megacrystal_android_app.network.MegaCrystalApiClient
import com.example.megacrystal_android_app.network.model.ProfileUpdateRequest
import com.example.megacrystal_android_app.util.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

private val ProfileBackground = Color(0xFFFAF8FF)
private val ProfileBlue = Color(0xFF0066FF)
private val LogoutRed = Color(0xFFC51920)

private fun profileErrorMessage(error: Exception): String {
    if (error is HttpException) {
        val body = error.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            val message = runCatching {
                val json = JSONObject(body)
                json.optString("message").ifBlank { json.optString("error") }
            }.getOrNull()
            if (!message.isNullOrBlank()) return message
        }
        return "Permintaan profil gagal (${error.code()}). Silakan coba lagi."
    }
    return "Tidak dapat terhubung ke server. Periksa koneksi lalu coba lagi."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    name: String,
    email: String,
    phone: String,
    roleLabel: String = "PELANGGAN",
    onBackClick: () -> Unit,
    onSaveClick: (String, String, String) -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val session = remember(context) { SessionManager(context) }
    val scope = rememberCoroutineScope()
    var draftName by remember { mutableStateOf(name) }
    var draftEmail by remember { mutableStateOf(email) }
    var draftPhone by remember { mutableStateOf(phone) }
    var profileRole by remember { mutableStateOf(roleLabel) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var loaded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loadAttempt by remember { mutableStateOf(0) }
    var showErrors by remember { mutableStateOf(false) }

    LaunchedEffect(loadAttempt) {
        loading = true
        errorMessage = null
        try {
            val token = session.getToken()
            if (token.isNullOrBlank()) {
                errorMessage = "Sesi berakhir. Silakan keluar dan masuk kembali."
            } else {
                val profile = MegaCrystalApiClient.instance.getProfile("Bearer $token").data
                draftName = profile.fullName
                draftEmail = profile.email
                draftPhone = profile.phoneNumber
                profileRole = profile.role
                loaded = true
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            errorMessage = profileErrorMessage(error)
        } finally {
            loading = false
        }
    }

    BackHandler(onBack = onBackClick)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ProfileBlue,
        unfocusedBorderColor = Color(0xFF79747E),
        focusedLabelColor = ProfileBlue,
        unfocusedLabelColor = Color(0xFF49454F)
    )

    Scaffold(
        containerColor = ProfileBackground,
        topBar = {
            TopAppBar(
                title = { Text("Kelola Akun") },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
                        Image(
                            painter = painterResource(R.drawable.icon_arrow_back_figma),
                            contentDescription = "Kembali",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProfileBackground)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 88.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Memuat profil…")
                }
                errorMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (!loading && !loaded) {
                    OutlinedButton(onClick = { loadAttempt += 1 }) { Text("Coba Lagi") }
                }
                if (loaded && !loading) {
                    Surface(
                        modifier = Modifier.widthIn(min = 196.dp).height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE7E7EF)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Status Role: $profileRole",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF49454F)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        enabled = !saving,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nama") },
                        singleLine = true,
                        isError = showErrors && draftName.isBlank(),
                        colors = fieldColors
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = draftEmail,
                        onValueChange = { draftEmail = it },
                        enabled = !saving,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        isError = showErrors && draftEmail.isBlank(),
                        colors = fieldColors
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = draftPhone,
                        onValueChange = { draftPhone = it },
                        enabled = !saving,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("No. HP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        isError = showErrors && draftPhone.isBlank(),
                        colors = fieldColors
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Untuk mengubah kata sandi, isi kata sandi saat ini dan kata sandi baru.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        enabled = !saving,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Kata sandi saat ini") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        isError = showErrors && newPassword.isNotEmpty() && currentPassword.isEmpty(),
                        colors = fieldColors
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        enabled = !saving,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Kata sandi baru (opsional)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        isError = showErrors && currentPassword.isNotEmpty() && newPassword.isEmpty(),
                        colors = fieldColors
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            showErrors = true
                            errorMessage = null
                            if (draftName.isBlank() || draftEmail.isBlank() || draftPhone.isBlank()) {
                                errorMessage = "Lengkapi nama, email, dan nomor HP."
                            } else if (currentPassword.isEmpty() != newPassword.isEmpty()) {
                                errorMessage = "Isi kata sandi saat ini dan kata sandi baru untuk mengubah kata sandi."
                            } else {
                                saving = true
                                scope.launch {
                                    try {
                                        val token = session.getToken()
                                        if (token.isNullOrBlank()) {
                                            errorMessage = "Sesi berakhir. Silakan keluar dan masuk kembali."
                                        } else {
                                            val profile = MegaCrystalApiClient.instance.updateProfile(
                                                "Bearer $token",
                                                ProfileUpdateRequest(
                                                    fullName = draftName.trim(),
                                                    email = draftEmail.trim(),
                                                    phoneNumber = draftPhone.trim(),
                                                    currentPassword = currentPassword.takeIf { it.isNotEmpty() },
                                                    newPassword = newPassword.takeIf { it.isNotEmpty() }
                                                )
                                            ).data
                                            session.saveSession(
                                                token,
                                                profile.fullName,
                                                profile.email,
                                                profile.phoneNumber,
                                                profile.role
                                            )
                                            draftName = profile.fullName
                                            draftEmail = profile.email
                                            draftPhone = profile.phoneNumber
                                            profileRole = profile.role
                                            currentPassword = ""
                                            newPassword = ""
                                            showErrors = false
                                            onSaveClick(profile.fullName, profile.email, profile.phoneNumber)
                                            Toast.makeText(context, "Perubahan profil tersimpan", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (error: CancellationException) {
                                        throw error
                                    } catch (error: Exception) {
                                        errorMessage = profileErrorMessage(error)
                                    } finally {
                                        saving = false
                                    }
                                }
                            }
                        },
                        enabled = !saving,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ProfileBlue)
                    ) {
                        Text(if (saving) "Menyimpan…" else "Simpan Perubahan")
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE7E7EF)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Profil Perusahaan",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("MegaCrystal", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("MegaCrystal menyediakan produk es kristal. Pemesanan dan informasi pengiriman tersedia melalui aplikasi.")
                    }
                }
            }
            OutlinedButton(
                onClick = onLogoutClick,
                enabled = !saving,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 18.dp)
                    .height(48.dp)
                    .border(1.dp, LogoutRed, RoundedCornerShape(50.dp)),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ProfileBackground,
                    contentColor = LogoutRed
                )
            ) {
                Text("Log Out")
            }
        }
    }
}
