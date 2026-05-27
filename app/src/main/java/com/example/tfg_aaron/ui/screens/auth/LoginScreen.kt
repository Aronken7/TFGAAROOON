package com.example.tfg_aaron.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoginScreen(navController: NavController) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory {
        initializer { AuthViewModel(app.authRepository) }
    })
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "login")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label = "ringRotation"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.06f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowPulse"
    )
    val orbOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "orbOffset"
    )

    // Entry animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
    ) {
        // Ambient floating orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height

            // Gold orb top-right
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(GoldAccent.copy(alpha = glowPulse), Color.Transparent),
                    center = Offset(w * 0.8f + cos(orbOffset) * 40, h * 0.12f + sin(orbOffset) * 30),
                    radius = 180f
                ),
                radius = 180f,
                center = Offset(w * 0.8f + cos(orbOffset) * 40, h * 0.12f + sin(orbOffset) * 30)
            )

            // Orange orb bottom-left
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(OrangeBase.copy(alpha = glowPulse * 0.7f), Color.Transparent),
                    center = Offset(w * 0.15f + cos(orbOffset + 2f) * 30, h * 0.85f + sin(orbOffset + 2f) * 25),
                    radius = 200f
                ),
                radius = 200f,
                center = Offset(w * 0.15f + cos(orbOffset + 2f) * 30, h * 0.85f + sin(orbOffset + 2f) * 25)
            )

            // Teal orb center-left
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(TealAccent.copy(alpha = glowPulse * 0.3f), Color.Transparent),
                    center = Offset(w * 0.1f, h * 0.45f + sin(orbOffset + 4f) * 20),
                    radius = 120f
                ),
                radius = 120f,
                center = Offset(w * 0.1f, h * 0.45f + sin(orbOffset + 4f) * 20)
            )

            // Subtle court lines
            drawArc(
                color = GoldAccent.copy(alpha = 0.04f),
                startAngle = 0f, sweepAngle = 180f, useCenter = false,
                topLeft = Offset(w * 0.1f, -h * 0.1f),
                size = Size(w * 0.8f, h * 0.35f),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = GoldAccent.copy(alpha = 0.03f),
                radius = h * 0.18f,
                center = Offset(w * 0.5f, h * 0.5f),
                style = Stroke(width = 0.8.dp.toPx())
            )
        }

        val isTablet = LocalIsTablet.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isTablet) 180.dp else 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // Animated logo with rotating gradient ring
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800)) + scaleIn(tween(800, easing = FastOutSlowInEasing), initialScale = 0.5f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    // Rotating gradient ring
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .drawWithContent {
                                drawContent()
                                rotate(ringRotation) {
                                    drawCircle(
                                        brush = Brush.sweepGradient(
                                            listOf(
                                                GoldAccent.copy(alpha = 0.8f),
                                                OrangeBase.copy(alpha = 0.4f),
                                                Color.Transparent,
                                                Color.Transparent,
                                                GoldAccent.copy(alpha = 0.6f)
                                            )
                                        ),
                                        style = Stroke(width = 3.dp.toPx())
                                    )
                                }
                            }
                    )

                    // Outer glow ring (pulsing)
                    Box(
                        modifier = Modifier
                            .size((100 * pulse).dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        GoldAccent.copy(alpha = 0.12f),
                                        OrangeBase.copy(alpha = 0.04f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Inner ring
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(OrangeBase.copy(alpha = 0.18f), OrangeBase.copy(alpha = 0.05f))
                                )
                            )
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(GoldAccent.copy(alpha = 0.3f), OrangeBase.copy(alpha = 0.15f))
                                ),
                                CircleShape
                            )
                    )

                    // Core basketball icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(24.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(OrangeLight, OrangeBase, OrangeDark)
                                )
                            )
                    ) {
                        Icon(
                            Icons.Filled.SportsBasketball,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // App name with staggered entry
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 300)) + slideInVertically(tween(600, delayMillis = 300)) { it / 3 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "PlayVision",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = " AV",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OrangeBase,
                            letterSpacing = (-1).sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Inteligencia deportiva para entrenadores",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Form card with glass effect
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, delayMillis = 500)) + slideInVertically(tween(700, delayMillis = 500)) { it / 4 }
            ) {
                Box {
                    // Glow behind card
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(y = 6.dp)
                            .blur(28.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(GoldAccent.copy(alpha = 0.04f))
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C).copy(alpha = 0.88f)),
                        border = BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    GoldAccent.copy(alpha = 0.2f),
                                    Color.White.copy(alpha = 0.06f),
                                    GoldAccent.copy(alpha = 0.08f)
                                )
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            // Section label
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp, 16.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.verticalGradient(listOf(GoldAccent, OrangeBase))
                                        )
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "INICIAR SESIÓN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldAccent,
                                    letterSpacing = 2.5.sp
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Accede a tu panel de entrenador",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                            Spacer(Modifier.height(24.dp))

                            // Email field
                            PremiumTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email",
                                icon = Icons.Filled.Email,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )

                            Spacer(Modifier.height(14.dp))

                            // Password field
                            PremiumTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Contraseña",
                                icon = Icons.Filled.Lock,
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onTogglePassword = { passwordVisible = !passwordVisible },
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.login(email, password)
                                }
                            )

                            // Error message
                            AnimatedVisibility(visible = uiState.error != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 14.dp),
                                    colors = CardDefaults.cardColors(containerColor = RedSurface),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, RedError.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Error, null, tint = RedError, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            uiState.error ?: "",
                                            color = RedError,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            // Login button with animated gradient
                            PremiumActionButton(
                                text = "Entrar",
                                icon = Icons.Filled.Login,
                                isLoading = uiState.isLoading,
                                onClick = { viewModel.login(email, password) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Divider
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 700))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = NavyBorder
                    )
                    Text(
                        "  ¿Sin cuenta?  ",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = NavyBorder
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Register button
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 800)) + slideInVertically(tween(500, delayMillis = 800)) { it / 4 }
            ) {
                OutlinedButton(
                    onClick = { navController.navigate(Screen.Register.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(NavyBorder, GoldAccent.copy(alpha = 0.2f), NavyBorder)
                        )
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Icon(Icons.Filled.PersonAdd, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Crear cuenta", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

// ── Premium text field ──────────────────────────────────────────────────────

@Composable
internal fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldAccent.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = GoldAccent, modifier = Modifier.size(16.dp))
            }
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        null, tint = TextTertiary, modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onNext?.invoke() },
            onDone = { onDone?.invoke() }
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldAccent,
            unfocusedBorderColor = NavyBorder,
            focusedLabelColor = GoldAccent,
            unfocusedLabelColor = TextTertiary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = GoldAccent,
            focusedContainerColor = NavyElevated.copy(alpha = 0.6f),
            unfocusedContainerColor = NavySurface.copy(alpha = 0.5f)
        ),
        singleLine = true
    )
}

// ── Premium action button with gradient & glow ──────────────────────────────

@Composable
internal fun PremiumActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(GoldDark, GoldAccent, GoldDark)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "btn")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing)),
        label = "shimmer"
    )

    Box(modifier = modifier) {
        // Glow under button
        if (!isLoading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = 4.dp)
                    .blur(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GoldAccent.copy(alpha = 0.15f))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (!isLoading) Brush.horizontalGradient(gradientColors)
                    else Brush.horizontalGradient(listOf(NavyElevated, NavyElevated))
                )
                .then(
                    if (!isLoading) Modifier.drawWithContent {
                        drawContent()
                        // Shimmer highlight sweep
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                start = Offset(size.width * shimmerOffset, 0f),
                                end = Offset(size.width * (shimmerOffset + 0.4f), size.height)
                            )
                        )
                    } else Modifier
                )
                .clickable(enabled = !isLoading) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = TextSecondary,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
