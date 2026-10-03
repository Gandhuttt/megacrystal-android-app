package com.example.megacrystal_android_app

import android.widget.Toast
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.megacrystal_android_app.ui.screen.CustomerHistoryScreen
import com.example.megacrystal_android_app.ui.screen.CustomerHomeScreen
import com.example.megacrystal_android_app.ui.screen.CustomerOrderScreen
import com.example.megacrystal_android_app.ui.theme.MegacrystalandroidappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MegacrystalandroidappTheme {
                var showHistory by rememberSaveable { mutableStateOf(false) }
                var selectedProductKg by rememberSaveable { mutableStateOf<Int?>(null) }

                val productKg = selectedProductKg
                if (productKg != null) {
                    CustomerOrderScreen(
                        weightKg = productKg,
                        onBackClick = { selectedProductKg = null },
                        onCheckoutClick = { quantity ->
                            Toast.makeText(
                                this,
                                "Pesanan $quantity karung siap dilanjutkan ke checkout",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                } else if (showHistory) {
                    CustomerHistoryScreen(
                        onHomeClick = { showHistory = false }
                    )
                } else {
                    CustomerHomeScreen(
                        onHistoryClick = { showHistory = true },
                        onOrderClick = { selectedProductKg = it }
                    )
                }
            }
        }
    }
}
