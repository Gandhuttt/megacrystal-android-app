package com.example.megacrystal_android_app.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.megacrystal_android_app.network.MegaCrystalApiClient
import com.example.megacrystal_android_app.network.model.AuthUser
import com.example.megacrystal_android_app.network.model.LoginRequest
import com.example.megacrystal_android_app.network.model.RegisterRequest
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

private val AuthBackground = Color(0xFFFAF8FF)
private val AuthBlue = Color(0xFF0066FF)

private fun parseHttpError(e: Throwable): String {
    if (e is HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        if (!errorBody.isNullOrBlank()) {
            try {
                val json = JSONObject(errorBody)
                if (json.has("message")) return json.getString("message")
                if (json.has("error")) return json.getString("error")
                return errorBody
            } catch (_: Exception) {
                return errorBody
            }
        }
    }
    return e.localizedMessage ?: "Terjadi kesalahan koneksi"
}

@Composable
fun CustomerAuthScreen(onAuthSuccess: (token: String, user: AuthUser, phone: String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AuthBlue,
        unfocusedBorderColor = Color(0xFF79747E),
        focusedLabelColor = AuthBlue,
        unfocusedLabelColor = Color(0xFF49454F)
    )

    Scaffold(containerColor = AuthBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selamat Datang di\nMegaCrystal",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D1B20)
            )

            Spacer(modifier = Modifier.height(24.dp))

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = AuthBackground,
                contentColor = AuthBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = AuthBlue
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = {
                        selectedTabIndex = 0
                        showErrors = false
                    },
                    text = { Text("Masuk", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = {
                        selectedTabIndex = 1
                        showErrors = false
                    },
                    text = { Text("Daftar", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTabIndex == 1) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nama Lengkap") },
                    singleLine = true,
                    isError = showErrors && fullName.isBlank(),
                    colors = fieldColors
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = showErrors && email.isBlank(),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = showErrors && password.isBlank(),
                colors = fieldColors
            )

            if (selectedTabIndex == 1) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { input ->
                        phone = input.filter { it.isDigit() }.take(15)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nomor HP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    isError = showErrors && phone.isBlank(),
                    colors = fieldColors
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    showErrors = true
                    if (selectedTabIndex == 0) {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            isLoading = true
                            scope.launch {
                                try {
                                    val loginResponse = MegaCrystalApiClient.instance.login(
                                        LoginRequest(email = email.trim(), password = password)
                                    )
                                    isLoading = false
                                    Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                                    onAuthSuccess(
                                        loginResponse.data.accessToken,
                                        loginResponse.data.user,
                                        phone.ifBlank { "08123456789" }
                                    )
                                } catch (e: Exception) {
                                    isLoading = false
                                    val errMsg = parseHttpError(e)
                                    Toast.makeText(context, "Gagal Login: $errMsg", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Isi email dan password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        if (fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && phone.isNotBlank()) {
                            isLoading = true
                            scope.launch {
                                try {
                                    val regResponse = MegaCrystalApiClient.instance.register(
                                        RegisterRequest(
                                            fullName = fullName.trim(),
                                            email = email.trim(),
                                            phoneNumber = phone.trim(),
                                            password = password
                                        )
                                    )
                                    isLoading = false
                                    Toast.makeText(context, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                                    onAuthSuccess(
                                        regResponse.data.accessToken,
                                        regResponse.data.user,
                                        phone.trim()
                                    )
                                } catch (e: Exception) {
                                    isLoading = false
                                    val errMsg = parseHttpError(e)
                                    Toast.makeText(context, "Gagal Registrasi: $errMsg", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Lengkapi semua bidang untuk mendaftar", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AuthBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Text(if (selectedTabIndex == 0) "Masuk" else "Daftar Akun")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    text = if (selectedTabIndex == 0) "Belum punya akun? " else "Sudah punya akun? ",
                    fontSize = 12.sp,
                    color = Color(0xFF49454F)
                )
                Text(
                    text = if (selectedTabIndex == 0) "Daftar di sini" else "Masuk di sini",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuthBlue,
                    modifier = Modifier.clickable {
                        selectedTabIndex = if (selectedTabIndex == 0) 1 else 0
                        showErrors = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Demo Worker: masuk tab Masuk dengan email worker@megacrystal.demo",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFF49454F)
            )
        }
    }
}
