package com.example.tfg_aaron.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RegisterScreen(navController: NavController) {
    val isTablet = LocalIsTablet.current
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory {
        initializer { AuthViewModel(app.authRepository) }
    })
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var equipo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "register")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing)),
        label = "ringRotation"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.12f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowPulse"
    )
    val orbOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 6.2832f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
        label = "orbOffset"
    )

    // Step progress
    val completedFields = listOf(
        nombre.isNotBlank(), email.isNotBlank(), equipo.isNotBlank(),
        password.isNotBlank(), confirmPassword.isNotBlank()
    ).count { it }
    val progress = completedFields / 5f

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
        // Ambient orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height

            // Gold orb top-left
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(GoldAccent.copy(alpha = glowPulse), Color.Transparent),
                    center = Offset(w * 0.2f + cos(orbOffset) * 30, h * 0.08f + sin(orbOffset) * 20),
                    radius = 160f
                ),
                radius = 160f,
                center = Offset(w * 0.2f + cos(orbOffset) * 30, h * 0.08f + sin(orbOffset) * 20)
            )

            // Teal orb right
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(TealAccent.copy(alpha = glowPulse * 0.5f), Color.Transparent),
                    center = Offset(w * 0.9f, h * 0.35f + sin(orbOffset + 3f) * 25),
                    radius = 140f
                ),
                radius = 140f,
                center = Offset(w * 0.9f, h * 0.35f + sin(orbOffset + 3f) * 25)
            )

            // Orange orb bottom
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(OrangeBase.copy(alpha = glowPulse * 0.6f), Color.Transparent),
                    center = Offset(w * 0.5f + cos(orbOffset + 1.5f) * 40, h * 0.92f),
                    radius = 180f
                ),
                radius = 180f,
                center = Offset(w * 0.5f + cos(orbOffset + 1.5f) * 40, h * 0.92f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isTablet) 108.dp else 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Logo with rotating ring
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700)) + scaleIn(tween(700, easing = FastOutSlowInEasing), initialScale = 0.4f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    // Rotating gradient ring
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .drawWithContent {
                                drawContent()
                                rotate(ringRotation) {
                                    drawCircle(
                                        brush = Brush.sweepGradient(
                                            listOf(
                                                TealAccent.copy(alpha = 0.6f),
                                                GoldAccent.copy(alpha = 0.4f),
                                                Color.Transparent,
                                                Color.Transparent,
                                                TealAccent.copy(alpha = 0.5f)
                                            )
                                        ),
                                        style = Stroke(width = 2.5.dp.toPx())
                                    )
                                }
                            }
                    )

                    // Inner
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(20.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(OrangeLight, OrangeBase, OrangeDark))
                            )
                    ) {
                        Icon(Icons.Filled.PersonAdd, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200)) { it / 3 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        Text("PlayVision", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, letterSpacing = (-0.5).sp)
                        Text(" AV", color = OrangeBase, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, letterSpacing = (-0.5).sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Crear cuenta de entrenador", color = TextSecondary, fontSize = 13.sp, letterSpacing = 0.3.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Progress indicator
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 350))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Progreso del registro",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$completedFields/5",
                            color = if (completedFields == 5) GreenSuccess else GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = spring(dampingRatio = 0.7f),
                        label = "progress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (completedFields == 5) GreenSuccess else GoldAccent,
                        trackColor = NavyElevated
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Form card
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 400)) + slideInVertically(tween(600, delayMillis = 400)) { it / 5 }
            ) {
                Box {
                    // Glow behind
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(y = 6.dp)
                            .blur(24.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(GoldAccent.copy(alpha = 0.03f))
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C).copy(alpha = 0.88f)),
                        border = BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    GoldAccent.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0.05f),
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
                                    "DATOS DEL ENTRENADOR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldAccent,
                                    letterSpacing = 2.sp
                                )
                            }
                            Spacer(Modifier.height(20.dp))

                            PremiumTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = "Nombre completo",
                                icon = Icons.Filled.Person,
                                onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                            )
                            Spacer(Modifier.height(12.dp))

                            PremiumTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email",
                                icon = Icons.Filled.Email,
                                keyboardType = KeyboardType.Email,
                                onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                            )
                            Spacer(Modifier.height(12.dp))

                            PremiumTextField(
                                value = equipo,
                                onValueChange = { equipo = it },
                                label = "Nombre del equipo",
                                icon = Icons.Filled.Groups,
                                onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                            )
                            Spacer(Modifier.height(12.dp))

                            PremiumTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Contraseña",
                                icon = Icons.Filled.Lock,
                                keyboardType = KeyboardType.Password,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onTogglePassword = { passwordVisible = !passwordVisible },
                                onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                            )
                            Spacer(Modifier.height(12.dp))

                            PremiumTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = "Confirmar contraseña",
                                icon = Icons.Filled.LockReset,
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onTogglePassword = { passwordVisible = !passwordVisible },
                                onDone = { focusManager.clearFocus() }
                            )

                            // Error
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
                                        Text(uiState.error ?: "", color = RedError, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            // Register button
                            PremiumActionButton(
                                text = "Crear cuenta",
                                icon = Icons.Filled.HowToReg,
                                isLoading = uiState.isLoading,
                                onClick = {
                                    viewModel.register(nombre, email, password, confirmPassword, equipo)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Login link
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 600))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Ya tienes cuenta?", color = TextSecondary, fontSize = 13.sp)
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text(
                            "Iniciar sesión",
                            color = OrangeBase,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
