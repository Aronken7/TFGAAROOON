package com.example.tfg_aaron.ui.theme

import androidx.compose.runtime.mutableStateOf

// Global reactive theme state.
// When read inside a @Composable, Compose tracks it as a dependency and
// recomposes all affected composables when the value changes.
internal val isLightTheme = mutableStateOf(false)
