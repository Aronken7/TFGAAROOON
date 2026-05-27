package com.example.tfg_aaron

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import com.example.tfg_aaron.ui.navigation.AppNavGraph
import com.example.tfg_aaron.ui.theme.TFGAaronTheme
import com.example.tfg_aaron.ui.utils.LocalIsTablet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as TFGApplication
        setContent {
            val themeMode by app.userPreferences.themeMode.collectAsState(initial = "SYSTEM")
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemDark  // "SYSTEM"
            }
            val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600
            TFGAaronTheme(darkTheme = isDark) {
                CompositionLocalProvider(LocalIsTablet provides isTablet) {
                    AppNavGraph(app)
                }
            }
        }
    }
}
