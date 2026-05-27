package com.example.tfg_aaron.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet

// ══════════════════════════════════════════════════════════════════════════════
// PLAYVISION AV — STITCH DESIGN SYSTEM Components
// ══════════════════════════════════════════════════════════════════════════════

// ── GLASS CARD ──────────────────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color = NeonGreen,
    cornerRadius: Dp = 12.dp,
    borderAlpha: Float = 0.3f,
    glowAlpha: Float = 0.04f,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = BorderStroke(1.dp, NavyBorder)
    ) {
        Column(content = content)
    }
}

// ── GLOW BOX ────────────────────────────────────────────────────────────────

@Composable
fun GlowBox(
    color: Color,
    glowRadius: Dp = 24.dp,
    glowAlpha: Float = 0.15f,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier, content = content)
}

// ── ANIMATED GRADIENT BORDER ────────────────────────────────────────────────

@Composable
fun AnimatedGradientBorderBox(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(NeonGreen, OrangeBase, TealAccent, PurpleAccent, NeonGreen),
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 12.dp,
    animDurationMs: Int = 3000,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier, content = content)
}

// ══════════════════════════════════════════════════════════════════════════════
// BOTTOM NAVIGATION — Stitch style
// ══════════════════════════════════════════════════════════════════════════════

private data class NavItem(val label: String, val icon: ImageVector, val route: String, val color: Color)
private data class MoreMenuItem(val label: String, val icon: ImageVector, val route: String, val color: Color, val subtitle: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachBottomNavBar(navController: NavController) {
    if (LocalIsTablet.current) return
    val primaryItems = listOf(
        NavItem("Inicio",    Icons.Filled.Home,             Screen.Dashboard.route, NeonGreen),
        NavItem("Equipo",    Icons.Filled.People,           Screen.Jugadoras.route, NeonGreen),
        NavItem("Partidos",  Icons.Filled.SportsBasketball, Screen.Partidos.route,  NeonGreen),
        NavItem("Táctica",   Icons.Filled.Draw,             Screen.Pizarra.route,   NeonGreen),
    )
    val moreItems = listOf(
        MoreMenuItem("Estadísticas", Icons.Filled.BarChart,          Screen.Estadisticas.route, NeonGreen,   "Tiros y rendimiento"),
        MoreMenuItem("Sesiones",     Icons.Filled.CalendarMonth,     Screen.Sesiones.route,     TealAccent,  "Planificación semanal"),
        MoreMenuItem("Scouting",     Icons.Filled.Visibility,        Screen.Scouting.route,     OrangeBase,  "Análisis de rivales"),
        MoreMenuItem("Reportes",     Icons.Filled.Description,       Screen.Reportes.route,     PurpleAccent,"Exportar PDFs"),
        MoreMenuItem("Calendario",   Icons.Filled.CalendarViewMonth, Screen.Calendario.route,   TealAccent,  "Temporada anual"),
        MoreMenuItem("Videos",       Icons.Filled.PlayCircle,        Screen.Videos.route,       PurpleAccent,"Análisis de video"),
        MoreMenuItem("Mi Perfil",    Icons.Filled.Person,            Screen.Perfil.route,       GoldAccent,  "Cuenta y ajustes"),
    )

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    var showMoreSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val moreRoutes = moreItems.map { it.route }.toSet()
    val moreSelected = currentRoute in moreRoutes

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 0.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(NavyElevated)
                .border(BorderStroke(1.dp, NavyBorder), shape = RoundedCornerShape(0.dp))
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            primaryItems.forEach { item ->
                val selected = currentRoute == item.route
                StitchNavItem(
                    label = item.label,
                    icon = item.icon,
                    selected = selected,
                    modifier = Modifier.weight(1f)
                ) {
                    navController.navigate(item.route) {
                        popUpTo(Screen.Dashboard.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            StitchNavItem(
                label = "Más",
                icon = if (showMoreSheet) Icons.Filled.Close else Icons.Filled.Menu,
                selected = moreSelected || showMoreSheet,
                modifier = Modifier.weight(1f)
            ) {
                showMoreSheet = true
            }
        }
    }

    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = sheetState,
            containerColor = NavyElevated,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 14.dp, bottom = 10.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NavyBorder)
                )
            },
            tonalElevation = 0.dp,
            scrimColor = Color.Black.copy(alpha = 0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    "MÓDULO PRINCIPAL",
                    fontSize = 10.sp, color = NeonGreen,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    "Todas las herramientas",
                    fontSize = 20.sp, color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                moreItems.forEach { item ->
                    val isActive = currentRoute == item.route
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) NeonGreen.copy(0.06f) else NavyCard)
                            .border(1.dp, if (isActive) NeonGreen.copy(0.2f) else NavyBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                showMoreSheet = false
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(item.color.copy(0.1f))
                                .border(1.dp, item.color.copy(0.2f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(item.icon, null, tint = item.color, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.label,
                                color = if (isActive) NeonGreen else TextPrimary,
                                fontWeight = FontWeight.Bold, fontSize = 15.sp
                            )
                            Text(item.subtitle, color = TextTertiary, fontSize = 11.sp)
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StitchNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) NeonGreen else TextTertiary,
        animationSpec = tween(200), label = "navColor"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(22.dp), tint = iconColor)
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = iconColor
            )
            Box(
                modifier = Modifier
                    .size(width = 16.dp, height = 2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (selected) NeonGreen else Color.Transparent)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PLAYVISION TOP BAR — Shared across all main screens
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun PlayVisionTopBar(onSettingsClick: (() -> Unit)? = null, onSearchClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(alpha = 0.15f))
                    .border(1.5.dp, NeonGreen.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.SportsBasketball,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                "PlayVision AV",
                color = NeonGreen,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )
        }
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            if (onSearchClick != null) {
                IconButton(onClick = onSearchClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
            if (onSettingsClick != null) {
                IconButton(onClick = onSettingsClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SHIMMER LOADING
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun ShimmerCard(modifier: Modifier = Modifier, height: Int = 80) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
    )
}

@Composable
fun ShimmerListPlaceholder(itemCount: Int = 4, itemHeight: Int = 80) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(itemCount) { ShimmerCard(height = itemHeight) }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ANIMATED COUNTER
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    fontSize: Int = 22,
    fontWeight: FontWeight = FontWeight.ExtraBold
) {
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
        },
        modifier = modifier,
        label = "counter"
    ) { n ->
        Text(text = "$n", color = color, fontSize = fontSize.sp, fontWeight = fontWeight)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// GRADIENT HEADER — Stitch style
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun GradientHeader(
    title: String, subtitle: String,
    navController: NavController? = null, showBack: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(NavyDark)
            .padding(
                start = if (showBack) 4.dp else 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 14.dp
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showBack && navController != null) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                }
            }
            Column {
                Text(
                    title.uppercase(), fontSize = 10.sp, color = NeonGreen,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle, fontSize = 20.sp, color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = NavyBorder, thickness = 1.dp)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// COLOR HELPERS
// ══════════════════════════════════════════════════════════════════════════════

fun posicionColor(posicion: String): Color = when (posicion) {
    "Base" -> TealAccent
    "Escolta" -> PurpleAccent
    "Alero" -> GoldLight
    "Ala-Pívot" -> GreenSuccess
    "Pívot" -> OrangeBase
    else -> TextSecondary
}

fun rolColor(rol: String): Color = when (rol) {
    "Titular" -> NeonGreen
    "Suplente" -> TextSecondary
    "Capitana" -> GoldAccent
    else -> TextTertiary
}

// ══════════════════════════════════════════════════════════════════════════════
// STAT CARD — Stitch grid style
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun StatCard(
    value: String, label: String, icon: ImageVector, color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(icon, null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                Text(
                    label.uppercase(), color = TextTertiary, fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = TextPrimary)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// BADGE CHIP — Stitch style
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun BadgeChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text, color = color,
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// EMPTY STATE — Stitch style
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun EmptyState(
    icon: ImageVector, title: String, subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(NeonGreen.copy(alpha = 0.08f))
                .border(1.dp, NeonGreen.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            Icon(icon, null, tint = NeonGreen, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            subtitle, color = TextTertiary, fontSize = 13.sp,
            textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 48.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// LOADING OVERLAY
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun LoadingOverlay(isLoading: Boolean) {
    AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyDark.copy(alpha = 0.9f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = NeonGreen, modifier = Modifier.size(44.dp), strokeWidth = 3.dp
                )
                Spacer(Modifier.height(14.dp))
                Text("Cargando...", color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION LABEL
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun SectionLabel(text: String, color: Color = NeonGreen) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text, color = color, fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = NavyBorder, thickness = 1.dp)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// NAVIGATION RAIL — Tablet (>= 600dp) replacement for BottomNavBar
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun CoachNavigationRail(navController: NavController) {
    val railItems = listOf(
        NavItem("Inicio",        Icons.Filled.Home,             Screen.Dashboard.route,   NeonGreen),
        NavItem("Equipo",        Icons.Filled.People,           Screen.Jugadoras.route,   NeonGreen),
        NavItem("Partidos",      Icons.Filled.SportsBasketball, Screen.Partidos.route,    NeonGreen),
        NavItem("Pizarra",       Icons.Filled.Draw,             Screen.Pizarra.route,     NeonGreen),
        NavItem("Estadísticas",  Icons.Filled.BarChart,         Screen.Estadisticas.route,TealAccent),
        NavItem("Sesiones",      Icons.Filled.CalendarMonth,    Screen.Sesiones.route,    TealAccent),
        NavItem("Scouting",      Icons.Filled.Visibility,       Screen.Scouting.route,    OrangeBase),
        NavItem("Reportes",      Icons.Filled.Description,      Screen.Reportes.route,    PurpleAccent),
        NavItem("Perfil",        Icons.Filled.Person,           Screen.Perfil.route,      GoldAccent),
    )

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationRail(
        containerColor = NavyElevated,
        modifier = Modifier
            .fillMaxHeight()
            .border(BorderStroke(1.dp, NavyBorder))
    ) {
        Spacer(Modifier.height(12.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NeonGreen.copy(alpha = 0.15f))
                .border(1.5.dp, NeonGreen.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Filled.SportsBasketball, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), color = NavyBorder)
        Spacer(Modifier.height(4.dp))

        railItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationRailItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Screen.Dashboard.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, null, modifier = Modifier.size(20.dp)) },
                label = { Text(item.label, fontSize = 9.sp, fontWeight = FontWeight.Medium) },
                alwaysShowLabel = false,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = NeonGreen,
                    selectedTextColor = NeonGreen,
                    unselectedIconColor = TextTertiary,
                    unselectedTextColor = TextTertiary,
                    indicatorColor = NeonGreen.copy(alpha = 0.12f)
                )
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}
