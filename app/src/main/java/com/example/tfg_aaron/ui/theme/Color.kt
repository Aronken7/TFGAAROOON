package com.example.tfg_aaron.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
// PLAYVISION AV — ADAPTIVE COLOR SYSTEM
// Dark (default) + Light mode via isLightTheme reactive state.
// All color getters are read during composition → Compose automatically
// recomposes any screen that uses them when the theme toggles.
// ══════════════════════════════════════════════════════════════════════════════

private val D = false  // just a shorthand alias

// ── Dark palette (private) ────────────────────────────────────────────────────
private val _D_NavyDark      = Color(0xFF0B1326)
private val _D_NavySurface   = Color(0xFF0F172A)
private val _D_NavyCard      = Color(0xFF131B2E)
private val _D_NavyElevated  = Color(0xFF1E293B)
private val _D_NavyBorder    = Color(0xFF31394D)
private val _D_TextPrimary   = Color(0xFFF5F5F0)
private val _D_TextSecondary = Color(0xFF94A3B8)
private val _D_TextTertiary  = Color(0xFF64748B)
private val _D_NeonGreen     = Color(0xFFADFF2F)
private val _D_NeonGreenDim  = Color(0xFF8FCC00)
private val _D_OrangeBase    = Color(0xFFFF5A1F)
private val _D_OrangeLight   = Color(0xFFFF7A45)
private val _D_OrangeDark    = Color(0xFFCC4518)
private val _D_GoldAccent    = Color(0xFFD4A017)
private val _D_GoldLight     = Color(0xFFE8920A)
private val _D_GoldDark      = Color(0xFF8B6914)
private val _D_TealAccent    = Color(0xFF0A84FF)
private val _D_ElectricBlue  = Color(0xFF0A84FF)
private val _D_GreenSuccess  = Color(0xFF00C853)
private val _D_GreenSurface  = Color(0xFF091A0F)
private val _D_RedError      = Color(0xFFFF453A)
private val _D_RedSurface    = Color(0xFF1A090F)
private val _D_YellowWarning = Color(0xFFFFAB00)
private val _D_PurpleAccent  = Color(0xFF8B5CF6)
private val _D_PinkAccent    = Color(0xFFFF2D78)
private val _D_CyanNeon      = Color(0xFF00D4FF)

// ── Light palette (private) ───────────────────────────────────────────────────
private val _L_NavyDark      = Color(0xFFF0F2FA)   // main bg: light blue-white
private val _L_NavySurface   = Color(0xFFE6EAF5)   // elevated surface
private val _L_NavyCard      = Color(0xFFFFFFFF)   // card: pure white
private val _L_NavyElevated  = Color(0xFFEDF1FA)   // dialogs/sheets
private val _L_NavyBorder    = Color(0xFFCAD3E8)   // borders
private val _L_TextPrimary   = Color(0xFF0B1326)   // near-black navy
private val _L_TextSecondary = Color(0xFF3D4F6E)   // medium navy
private val _L_TextTertiary  = Color(0xFF6B7A99)   // muted slate
private val _L_NeonGreen     = Color(0xFF2E7200)   // dark green — readable on white
private val _L_NeonGreenDim  = Color(0xFF3D8A00)
private val _L_OrangeBase    = Color(0xFFCC4400)   // slightly darker for contrast
private val _L_OrangeLight   = Color(0xFFE85520)
private val _L_OrangeDark    = Color(0xFF992E00)
private val _L_GoldAccent    = Color(0xFF8A6000)   // dark amber
private val _L_GoldLight     = Color(0xFFA07800)
private val _L_GoldDark      = Color(0xFF5A3E00)
private val _L_TealAccent    = Color(0xFF0055CC)   // dark blue
private val _L_ElectricBlue  = Color(0xFF0055CC)
private val _L_GreenSuccess  = Color(0xFF1A7A30)   // dark success green
private val _L_GreenSurface  = Color(0xFFE6F8EC)   // light green tint
private val _L_RedError      = Color(0xFFCC2222)   // dark red
private val _L_RedSurface    = Color(0xFFFFEEEE)   // light red tint
private val _L_YellowWarning = Color(0xFF8A6200)
private val _L_PurpleAccent  = Color(0xFF5B3DB0)
private val _L_PinkAccent    = Color(0xFFCC0055)
private val _L_CyanNeon      = Color(0xFF006880)

// ── PUBLIC ADAPTIVE GETTERS ───────────────────────────────────────────────────
// Reading isLightTheme.value inside a @Composable registers it as a state
// dependency → automatic recomposition when the theme changes.

val NavyDark:      Color get() = if (isLightTheme.value) _L_NavyDark      else _D_NavyDark
val NavySurface:   Color get() = if (isLightTheme.value) _L_NavySurface   else _D_NavySurface
val NavyCard:      Color get() = if (isLightTheme.value) _L_NavyCard      else _D_NavyCard
val NavyElevated:  Color get() = if (isLightTheme.value) _L_NavyElevated  else _D_NavyElevated
val NavyBorder:    Color get() = if (isLightTheme.value) _L_NavyBorder    else _D_NavyBorder
val TextPrimary:   Color get() = if (isLightTheme.value) _L_TextPrimary   else _D_TextPrimary
val TextSecondary: Color get() = if (isLightTheme.value) _L_TextSecondary else _D_TextSecondary
val TextTertiary:  Color get() = if (isLightTheme.value) _L_TextTertiary  else _D_TextTertiary
val NeonGreen:     Color get() = if (isLightTheme.value) _L_NeonGreen     else _D_NeonGreen
val NeonGreenDim:  Color get() = if (isLightTheme.value) _L_NeonGreenDim  else _D_NeonGreenDim
val OrangeBase:    Color get() = if (isLightTheme.value) _L_OrangeBase    else _D_OrangeBase
val OrangeLight:   Color get() = if (isLightTheme.value) _L_OrangeLight   else _D_OrangeLight
val OrangeDark:    Color get() = if (isLightTheme.value) _L_OrangeDark    else _D_OrangeDark
val OrangeGlow:    Color get() = if (isLightTheme.value) _L_OrangeBase.copy(0.18f) else _D_OrangeBase.copy(0.25f)
val GoldAccent:    Color get() = if (isLightTheme.value) _L_GoldAccent    else _D_GoldAccent
val GoldLight:     Color get() = if (isLightTheme.value) _L_GoldLight     else _D_GoldLight
val GoldDark:      Color get() = if (isLightTheme.value) _L_GoldDark      else _D_GoldDark
val GoldGlow:      Color get() = if (isLightTheme.value) _L_GoldAccent.copy(0.15f) else _D_GoldAccent.copy(0.20f)
val TealAccent:    Color get() = if (isLightTheme.value) _L_TealAccent    else _D_TealAccent
val ElectricBlue:  Color get() = if (isLightTheme.value) _L_ElectricBlue  else _D_ElectricBlue
val GreenSuccess:  Color get() = if (isLightTheme.value) _L_GreenSuccess  else _D_GreenSuccess
val GreenSurface:  Color get() = if (isLightTheme.value) _L_GreenSurface  else _D_GreenSurface
val RedError:      Color get() = if (isLightTheme.value) _L_RedError      else _D_RedError
val RedSurface:    Color get() = if (isLightTheme.value) _L_RedSurface    else _D_RedSurface
val YellowWarning: Color get() = if (isLightTheme.value) _L_YellowWarning else _D_YellowWarning
val PurpleAccent:  Color get() = if (isLightTheme.value) _L_PurpleAccent  else _D_PurpleAccent
val PinkAccent:    Color get() = if (isLightTheme.value) _L_PinkAccent    else _D_PinkAccent
val CyanNeon:      Color get() = if (isLightTheme.value) _L_CyanNeon      else _D_CyanNeon

// ── Position colors (Basketball) — keep vibrant, adapt slightly ───────────────
val ColorBase:     Color get() = if (isLightTheme.value) Color(0xFF0055CC) else Color(0xFF0A84FF)
val ColorEscolta:  Color get() = if (isLightTheme.value) Color(0xFF5B3DB0) else Color(0xFF8B5CF6)
val ColorAlero:    Color get() = if (isLightTheme.value) Color(0xFFA07800) else Color(0xFFE8920A)
val ColorAlaPivot: Color get() = if (isLightTheme.value) Color(0xFF1A7A30) else Color(0xFF00C853)
val ColorPivot:    Color get() = if (isLightTheme.value) Color(0xFFCC4400) else Color(0xFFFF5A1F)

// ── Role colors ───────────────────────────────────────────────────────────────
val ColorTitular:  Color get() = if (isLightTheme.value) Color(0xFF1A7A30) else Color(0xFF00C853)
val ColorSuplente: Color get() = if (isLightTheme.value) Color(0xFF8A6000) else Color(0xFFD4A017)
val ColorReserva:  Color get() = if (isLightTheme.value) Color(0xFF6B7A99) else Color(0xFF64748B)

// ── Jugada category ───────────────────────────────────────────────────────────
val ColorAtaque:     Color get() = if (isLightTheme.value) Color(0xFFCC2222) else Color(0xFFFF453A)
val ColorDefensa:    Color get() = if (isLightTheme.value) Color(0xFF0055CC) else Color(0xFF0A84FF)
val ColorBloqueo:    Color get() = if (isLightTheme.value) Color(0xFF5B3DB0) else Color(0xFF8B5CF6)
val ColorTransicion: Color get() = if (isLightTheme.value) Color(0xFF2E7200) else Color(0xFFADFF2F)

// ── Game result ───────────────────────────────────────────────────────────────
val ColorVictoria: Color get() = NeonGreen
val ColorDerrota:  Color get() = RedError
val ColorEmpate:   Color get() = GoldAccent

// ── Condition ─────────────────────────────────────────────────────────────────
val ColorDisponible:  Color get() = NeonGreen
val ColorLesionada:   Color get() = RedError
val ColorDescansando: Color get() = GoldAccent
