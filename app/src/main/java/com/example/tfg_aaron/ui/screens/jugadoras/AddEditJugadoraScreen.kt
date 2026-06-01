package com.example.tfg_aaron.ui.screens.jugadoras

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.ui.components.GradientHeader
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditJugadoraScreen(navController: NavController, entrenadorId: Int, jugadoraId: Int = -1) {
    val isTablet = LocalIsTablet.current
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: JugadorasViewModel = viewModel(factory = viewModelFactory {
        initializer { JugadorasViewModel(entrenadorId, app.jugadoraRepository, app.estadisticaRepository) }
    })
    val uiState by viewModel.uiState.collectAsState()
    val isEdit = jugadoraId > 0

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var posicion by remember { mutableStateOf(viewModel.posiciones[0]) }
    var rol by remember { mutableStateOf(viewModel.roles[0]) }
    var edad by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var areasMejora by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var condicionFisica by remember { mutableStateOf("DISPONIBLE") }
    var posicionExpanded by remember { mutableStateOf(false) }
    var rolExpanded by remember { mutableStateOf(false) }
    var fotoUri by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Copiar a almacenamiento interno para persistencia
            val input = context.contentResolver.openInputStream(it) ?: return@let
            val file = File(context.filesDir, "player_photo_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { out -> input.copyTo(out) }
            fotoUri = file.absolutePath
        }
    }

    LaunchedEffect(jugadoraId) {
        if (isEdit) {
            viewModel.loadDetail(jugadoraId)
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) isSaving = false
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            navController.popBackStack()
        }
    }

    // Pre-fill when editing
    val detail by viewModel.detailState.collectAsState()
    LaunchedEffect(detail.jugadora) {
        detail.jugadora?.let { j ->
            if (isEdit) {
                nombre = j.nombre
                apellidos = j.apellidos
                numero = j.numero.toString()
                posicion = j.posicion
                rol = j.rol
                edad = if (j.edad > 0) j.edad.toString() else ""
                altura = if (j.altura > 0) j.altura.toString() else ""
                areasMejora = j.areasMejora
                notas = j.notas
                condicionFisica = j.condicionFisica
                if (fotoUri.isEmpty()) fotoUri = j.fotoUri
            }
        }
    }

    Scaffold(containerColor = NavyDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            GradientHeader(
                title = if (isEdit) "Editar Jugadora" else "Nueva Jugadora",
                subtitle = if (isEdit) "Modifica los datos" else "Añade al equipo",
                navController = navController,
                showBack = true
            )

            Column(modifier = Modifier.padding(16.dp).padding(horizontal = if (isTablet) 80.dp else 0.dp)) {

                @Composable
                fun FieldLabel(text: String) {
                    Text(text, color = TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 6.dp, top = 14.dp))
                }

                @Composable
                fun CoachTextField(
                    value: String,
                    onValueChange: (String) -> Unit,
                    label: String,
                    keyboardType: KeyboardType = KeyboardType.Text
                ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = NavyBorder,
                            focusedLabelColor = GoldAccent,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = GoldAccent,
                            focusedContainerColor = NavyCard,
                            unfocusedContainerColor = NavyCard
                        ),
                        singleLine = true
                    )
                }

                // ── Foto de perfil ────────────────────────────────────────
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(NavyCard)
                            .border(2.dp, if (fotoUri.isNotBlank()) GoldAccent else NavyBorder, CircleShape)
                            .clickable { photoPicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoUri.isNotBlank()) {
                            AsyncImage(
                                model = fotoUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            // Overlay edit icon
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CameraAlt, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(24.dp))
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.CameraAlt, null, tint = TextTertiary, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.height(4.dp))
                                Text("Foto", color = TextTertiary, fontSize = 10.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))

                FieldLabel("Nombre *")
                CoachTextField(nombre, { nombre = it }, "Nombre")

                FieldLabel("Apellidos")
                CoachTextField(apellidos, { apellidos = it }, "Apellidos")

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel("Dorsal")
                        CoachTextField(numero, { numero = it }, "Nº", KeyboardType.Number)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel("Edad")
                        CoachTextField(edad, { edad = it }, "Edad", KeyboardType.Number)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel("Altura (cm)")
                        CoachTextField(altura, { altura = it }, "cm", KeyboardType.Decimal)
                    }
                }

                FieldLabel("Posición")
                ExposedDropdownMenuBox(expanded = posicionExpanded, onExpandedChange = { posicionExpanded = it }) {
                    OutlinedTextField(
                        value = posicion,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Posición") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = posicionExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent, unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedContainerColor = NavyCard, unfocusedContainerColor = NavyCard
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = posicionExpanded,
                        onDismissRequest = { posicionExpanded = false },
                        modifier = Modifier.background(NavyCard)
                    ) {
                        viewModel.posiciones.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p, color = if (p == posicion) GoldAccent else TextPrimary) },
                                onClick = { posicion = p; posicionExpanded = false }
                            )
                        }
                    }
                }

                FieldLabel("Rol")
                ExposedDropdownMenuBox(expanded = rolExpanded, onExpandedChange = { rolExpanded = it }) {
                    OutlinedTextField(
                        value = rol,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rolExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent, unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedContainerColor = NavyCard, unfocusedContainerColor = NavyCard
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = rolExpanded,
                        onDismissRequest = { rolExpanded = false },
                        modifier = Modifier.background(NavyCard)
                    ) {
                        viewModel.roles.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r, color = if (r == rol) GoldAccent else TextPrimary) },
                                onClick = { rol = r; rolExpanded = false }
                            )
                        }
                    }
                }

                FieldLabel("Condición física")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("DISPONIBLE", ColorDisponible, Icons.Filled.CheckCircle),
                        Triple("LESIONADA", ColorLesionada, Icons.Filled.LocalHospital),
                        Triple("DESCANSANDO", ColorDescansando, Icons.Filled.Bedtime)
                    ).forEach { (cond, color, icon) ->
                        val selected = condicionFisica == cond
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) color.copy(0.2f) else NavyCard)
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) color else NavyBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { condicionFisica = cond }
                                .padding(vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, null, tint = if (selected) color else TextTertiary, modifier = Modifier.size(18.dp))
                                Text(
                                    cond.replaceFirstChar { it.uppercase() }.lowercase().replaceFirstChar { it.uppercase() },
                                    color = if (selected) color else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                FieldLabel("Áreas de mejora (separadas por comas)")
                OutlinedTextField(
                    value = areasMejora,
                    onValueChange = { areasMejora = it },
                    label = { Text("Ej: Defensa, Tiro, Físico") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent, unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = GoldAccent, unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = GoldAccent, focusedContainerColor = NavyCard, unfocusedContainerColor = NavyCard
                    )
                )

                FieldLabel("Notas adicionales")
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Observaciones del entrenador") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent, unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = GoldAccent, unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = GoldAccent, focusedContainerColor = NavyCard, unfocusedContainerColor = NavyCard
                    )
                )

                uiState.error?.let { err ->
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RedSurface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Error, null, tint = RedError, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(err, color = RedError, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (!isSaving) {
                            isSaving = true
                            if (isEdit) {
                                detail.jugadora?.let { j ->
                                    viewModel.updateJugadora(
                                        j.copy(
                                            nombre = nombre.trim(),
                                            apellidos = apellidos.trim(),
                                            numero = numero.toIntOrNull() ?: j.numero,
                                            posicion = posicion,
                                            rol = rol,
                                            edad = edad.toIntOrNull() ?: 0,
                                            altura = altura.toFloatOrNull() ?: 0f,
                                            areasMejora = areasMejora.trim(),
                                            notas = notas.trim(),
                                            condicionFisica = condicionFisica,
                                            fotoUri = fotoUri
                                        )
                                    )
                                }
                            } else {
                                viewModel.addJugadora(
                                    nombre = nombre,
                                    apellidos = apellidos,
                                    numero = numero.toIntOrNull() ?: 0,
                                    posicion = posicion,
                                    rol = rol,
                                    edad = edad.toIntOrNull() ?: 0,
                                    altura = altura.toFloatOrNull() ?: 0f,
                                    notas = notas,
                                    fotoUri = fotoUri
                                )
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Icon(if (isEdit) Icons.Filled.Save else Icons.Filled.PersonAdd, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEdit) "Guardar cambios" else "Añadir jugadora", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
