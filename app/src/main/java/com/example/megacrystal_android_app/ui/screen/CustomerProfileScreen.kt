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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.megacrystal_android_app.R

private val ProfileBackground = Color(0xFFFAF8FF)
private val ProfileBlue = Color(0xFF0066FF)
private val LogoutRed = Color(0xFFC51920)

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
    var draftName by rememberSaveable(name) { mutableStateOf(name) }
    var draftEmail by rememberSaveable(email) { mutableStateOf(email) }
    var draftPhone by rememberSaveable(phone) { mutableStateOf(phone) }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var showMaskedPassword by rememberSaveable { mutableStateOf(true) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

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
                    containerColor = ProfileBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 88.dp)
            ) {
                Surface(
                    modifier = Modifier.widthIn(min = 196.dp).height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE7E7EF)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Status Role: $roleLabel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA0A0A8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
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
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    isError = showErrors && draftEmail.isBlank(),
                    colors = fieldColors
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = if (showMaskedPassword) "************" else newPassword,
                    onValueChange = { newPassword = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { state ->
                            if (state.isFocused && showMaskedPassword) {
                                showMaskedPassword = false
                                newPassword = ""
                            }
                        },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = fieldColors
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = draftPhone,
                    onValueChange = { input ->
                        draftPhone = input.filter { it.isDigit() }.take(15)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("No. HP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    isError = showErrors && draftPhone.isBlank(),
                    colors = fieldColors
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        showErrors = true
                        if (draftName.isNotBlank() && draftEmail.isNotBlank() &&
                            draftPhone.isNotBlank()
                        ) {
                            onSaveClick(
                                draftName.trim(),
                                draftEmail.trim(),
                                draftPhone.trim()
                            )
                            newPassword = ""
                            showMaskedPassword = true
                            Toast.makeText(
                                context,
                                "Perubahan profil tersimpan untuk demo",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Lengkapi nama, email, dan nomor HP",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ProfileBlue)
                ) {
                    Text("Simpan Perubahan")
                }
            }

            OutlinedButton(
                onClick = onLogoutClick,
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
