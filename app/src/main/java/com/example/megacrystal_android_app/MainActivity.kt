package com.example.megacrystal_android_app

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
import com.example.megacrystal_android_app.ui.theme.MegacrystalandroidappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MegacrystalandroidappTheme {
                var showHistory by rememberSaveable { mutableStateOf(false) }

                if (showHistory) {
                    CustomerHistoryScreen(
                        onHomeClick = { showHistory = false }
                    )
                } else {
                    CustomerHomeScreen(
                        onHistoryClick = { showHistory = true }
                    )
                }
            }
        }
    }
}