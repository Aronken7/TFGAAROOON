package com.example.tfg_aaron.ui.screens.perfil

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(navController: NavController, entrenadorId: Int) {
    val isTablet = LocalIsTablet.current
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val scope = rememberCoroutineScope()

    val nombreFlow by app.userPreferences.entrenadorNombre.collectAsState(initial = "")
    val equipoFlow by app.userPreferences.equipoNombre.collectAsState(initial = "")
    val emailFlow by app.userPreferences.entrenadorEmail.collectAsState(initial = "")
    val themeMode by app.userPreferences.themeMode.collectAsState(initial = "SYSTEM")
    val notifPartido by app.userPreferences.notifPartido.collectAsState(initial = true)
    val notifEntrenamiento by app.userPreferences.notifEntrenamiento.collectAsState(initial = false)
    val notifResumen by app.userPreferences.notifResumen.collectAsState(initial = false)

    var nombre by remember { mutableStateOf("") }
    var equipo by remember { mutableStateOf("") }
    var editMode by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Section expansion states
    var expandedCuenta by remember { mutableStateOf(true) }
    var expandedApariencia by remember { mutableStateOf(false) }
    var expandedNotificaciones by remember { mutableStateOf(false) }
    var expandedDatos by remember { mutableStateOf(false) }
    var expandedEquipo by remember { mutableStateOf(false) }
    var expandedInfo by remember { mutableStateOf(false) }

    LaunchedEffect(nombreFlow, equipoFlow) {
        nombre = nombreFlow
        equipo = equipoFlow
    }

    LaunchedEffect(showSnackbar) {
        showSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            showSnackbar = null
        }
    }

    Scaffold(
        containerColor = NavyDark,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = NavyElevated,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isTablet) 80.dp else 0.dp)
        ) {
            PlayVisionTopBar()
            GradientHeader("Mi Perfil", "Configuración y preferencias", navController, true)

            // ── Profile card ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(GoldAccent)
                    ) {
                        Text(
                            nombreFlow.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(nombreFlow, style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                    Text(emailFlow, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(GoldAccent.copy(0.15f))
                            .border(1.dp, GoldAccent.copy(0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Entrenador", style = MaterialTheme.typography.labelMedium, color = GoldAccent, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── SECCIÓN: MI CUENTA ────────────────────────────────────────────
            SettingsSection(
                title = "MI CUENTA",
                icon = Icons.Filled.Person,
                expanded = expandedCuenta,
                onToggle = { expandedCuenta = !expandedCuenta }
            ) {
                if (editMode) {
                    // Edit mode
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent, unfocusedBorderColor = NavyBorder,
                            focusedLabelColor = GoldAccent, unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            cursorColor = GoldAccent, focusedContainerColor = NavyElevated, unfocusedContainerColor = NavyElevated
                        )
                        OutlinedTextField(
                            nombre, { nombre = it }, label = { Text("Nombre") },
                            leadingIcon = { Icon(Icons.Filled.Person, null, tint = GoldAccent) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = fieldColors, singleLine = true
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            equipo, { equipo = it }, label = { Text("Nombre del equipo") },
                            leadingIcon = { Icon(Icons.Filled.Groups, null, tint = GoldAccent) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = fieldColors, singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { editMode = false; nombre = nombreFlow; equipo = equipoFlow },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, NavyBorder)
                            ) { Text("Cancelar", color = TextSecondary) }
                            Button(
                                onClick = {
                                    scope.launch {
                                        app.authRepository.updatePerfil(entrenadorId, nombre, equipo)
                                        editMode = false
                                    }
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                            ) {
                                Icon(Icons.Filled.Save, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Guardar", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                } else {
                    SettingsRow(
                        icon = Icons.Filled.Edit,
                        iconTint = GoldAccent,
                        title = "Editar perfil",
                        subtitle = nombreFlow.ifEmpty { "Sin nombre" },
                        onClick = { editMode = true }
                    )
                }
                HorizontalDivider(color = NavyBorder.copy(0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Filled.Lock,
                    iconTint = TealAccent,
                    title = "Cambiar contraseña",
                    subtitle = "Actualiza tu contraseña de acceso",
                    onClick = { showSnackbar = "Próximamente disponible" }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── SECCIÓN: APARIENCIA ───────────────────────────────────────────
            SettingsSection(
                title = "APARIENCIA",
                icon = Icons.Filled.Palette,
                expanded = expandedApariencia,
                onToggle = { expandedApariencia = !expandedApariencia }
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Modo de pantalla", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("SYSTEM", "Sistema", Icons.Filled.PhoneAndroid),
                            Triple("LIGHT", "Claro", Icons.Filled.LightMode),
                            Triple("DARK", "Oscuro", Icons.Filled.DarkMode)
                        ).forEach { (mode, label, icon) ->
                            val selected = themeMode == mode
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) TealAccent.copy(0.15f) else NavyElevated)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) TealAccent else NavyBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { scope.launch { app.userPreferences.setThemeMode(mode) } }
                                    .padding(vertical = 10.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(icon, null, tint = if (selected) TealAccent else TextTertiary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        label,
                                        color = if (selected) TealAccent else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = NavyBorder.copy(0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Filled.Language,
                    iconTint = PurpleAccent,
                    title = "Idioma",
                    subtitle = "Español",
                    onClick = { showSnackbar = "Próximamente disponible" }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── SECCIÓN: NOTIFICACIONES ───────────────────────────────────────
            SettingsSection(
                title = "NOTIFICACIONES",
                icon = Icons.Filled.Notifications,
                expanded = expandedNotificaciones,
                onToggle = { expandedNotificaciones = !expandedNotificaciones }
            ) {
                SettingsRow(
                    icon = Icons.Filled.SportsSoccer,
                    iconTint = OrangeBase,
                    title = "Recordatorio de partido",
                    subtitle = "2 horas antes del partido",
                    trailing = {
                        Switch(
                            checked = notifPartido,
                            onCheckedChange = { scope.launch { app.userPreferences.setNotifPartido(it) } },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OrangeBase,
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = NavyBorder
                            )
                        )
                    }
                )
                HorizontalDivider(color = NavyBorder.copy(0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Filled.FitnessCenter,
                    iconTint = GreenSuccess,
                    title = "Recordatorio de entrenamiento",
                    subtitle = "1 hora antes de la sesión",
                    trailing = {
                        Switch(
                            checked = notifEntrenamiento,
                            onCheckedChange = { scope.launch { app.userPreferences.setNotifEntrenamiento(it) } },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GreenSuccess,
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = NavyBorder
                            )
                        )
                    }
                )
                HorizontalDivider(color = NavyBorder.copy(0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Filled.BarChart,
                    iconTint = TealAccent,
                    title = "Resumen semanal",
                    subtitle = "Estadísticas cada lunes",
                    trailing = {
                        Switch(
                            checked = notifResumen,
                            onCheckedChange = { scope.launch { app.userPreferences.setNotifResumen(it) } },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TealAccent,
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = NavyBorder
                            )
                        )
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── SECCIÓN: DATOS ────────────────────────────────────────────────
            SettingsSection(
                title = "DATOS",
                icon = Icons.Filled.Storage,
                expanded = expandedDatos,
                onToggle = { expandedDatos = !expandedDatos }
            ) {
                SettingsRow(
                    icon = Icons.Filled.FileDownload,
                    iconTint = GoldAccent,
                    title = "Exportar estadísticas",
                    subtitle = "Exportar datos a CSV",
                    onClick = { showSnackbar = "Próximamente disponible" }
                )
                HorizontalDivider(color = NavyBorder.copy(0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Filled.Backup,
                    iconTint = GreenSuccess,
                    title = "Copia de seguridad",
                    subtitle = "Guardar datos en la nube",
                    onClick = { showSnackbar = "Próximamente disponible" }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── SECCIÓN: EQUIPO ───────────────────────────────────────────────
            SettingsSection(
                title = "EQUIPO",
                icon = Icons.Filled.Groups,
                expanded = expandedEquipo,
                onToggle = { expandedEquipo = !expandedEquipo }
            ) {
                SettingsRow(
                    icon = Icons.Filled.PeopleAlt,
                    iconTint = NeonGreen,
                    title = "Gestionar jugadoras",
                    subtitle = "Añadir, editar o eliminar jugadoras",
                    onClick = { navController.navigate(Screen.Jugadoras.route) }
                )
                HorizontalDivider(color = NavyBorder.copy(0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Filled.CalendarMonth,
                    iconTint = PurpleAccent,
                    title = "Configuración de temporada",
                    subtitle = "Fechas de inicio y fin de temporada",
                    onClick = { showSnackbar = "Próximamente disponible" }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── SECCIÓN: INFORMACIÓN ──────────────────────────────────────────
            SettingsSection(
                title = "INFORMACIÓN",
                icon = Icons.Filled.Info,
                expanded = expandedInfo,
                onToggle = { expandedInfo = !expandedInfo }
            ) {
                SettingsRow(
                    icon = Icons.Filled.AppSettingsAlt,
                    iconTint = TextSecondary,
                    title = "Versión de la app",
                    subtitle = null,
                    trailing = {
                        Text("1.0.0", color = TextTertiary, fontSize = 13.sp)
                    }
                )
                HorizontalDivider(color = NavyBorder.copy(0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Filled.AutoAwesome,
                    iconTint = GoldAccent,
                    title = "Acerca de PlayVision AV",
                    subtitle = "App de gestión para entrenadores",
                    onClick = { showSnackbar = "PlayVision AV v1.0.0 — TFG DAM 2025" }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Cerrar sesión ─────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { showLogoutDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RedSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.Logout, null, tint = RedError, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Cerrar Sesión", color = RedError, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
            }

            Spacer(Modifier.height(40.dp))
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                containerColor = NavyCard,
                title = { Text("Cerrar sesión", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("¿Seguro que quieres cerrar sesión?", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch { app.authRepository.logout() }
                            navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedError)
                    ) { Text("Cerrar sesión") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showLogoutDialog = false }, border = BorderStroke(1.dp, NavyBorder)) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            )
        }
    }
}

// ── Reusable composables ──────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard)
    ) {
        Column {
            // Section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldAccent.copy(0.12f))
                    ) {
                        Icon(icon, null, tint = GoldAccent, modifier = Modifier.size(17.dp))
                    }
                    Text(
                        title,
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null,
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column { content() }
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(0.12f))
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        } else if (onClick != null) {
            Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = GoldAccent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            Text(value.ifEmpty { "-" }, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}
