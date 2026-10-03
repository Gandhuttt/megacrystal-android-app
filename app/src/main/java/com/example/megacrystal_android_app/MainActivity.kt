package com.example.megacrystal_android_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.megacrystal_android_app.ui.screen.CustomerHomeScreen
import com.example.megacrystal_android_app.ui.theme.MegacrystalandroidappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MegacrystalandroidappTheme {
                CustomerHomeScreen()
            }
        }
    }
}