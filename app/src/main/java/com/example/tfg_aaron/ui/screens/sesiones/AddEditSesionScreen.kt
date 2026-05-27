package com.example.tfg_aaron.ui.screens.sesiones

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.ui.components.GradientHeader
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import com.example.tfg_aaron.util.NotificationScheduler
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSesionScreen(navController: NavController, entrenadorId: Int) {
    val isTablet = LocalIsTablet.current
    val context = LocalContext.current
    val app = context.applicationContext as TFGApplication
    val viewModel: SesionesViewModel = viewModel(factory = viewModelFactory {
        initializer { SesionesViewModel(entrenadorId, app.sesionRepository, app.jugadoraRepository) }
    })
    val uiState by viewModel.uiState.collectAsState()

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var horaInicio by remember { mutableStateOf("09:00") }
    var horaFin by remember { mutableStateOf("10:30") }
    var lugar by remember { mutableStateOf("") }
    var objetivos by remember { mutableStateOf("") }
    var ejercicios by remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

    val sdfDisplay = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es"))
    val displayDate = sdfDisplay.format(Date(selectedDateMillis))
        .replaceFirstChar { it.uppercase() }

    fun getWeekOfYear(millis: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        return cal.get(Calendar.WEEK_OF_YEAR)
    }

    fun getDayName(millis: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale("es"))
        return sdf.format(Date(millis)).replaceFirstChar { it.uppercase() }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            val savedId = uiState.lastSavedSesionId
            if (savedId != null && savedId > 0 && titulo.isNotBlank()) {
                NotificationScheduler.scheduleSesionReminder(
                    context = context,
                    sesionId = savedId,
                    titulo = titulo.trim(),
                    fechaMillis = selectedDateMillis
                )
            }
            viewModel.clearMessages()
            navController.popBackStack()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDateMillis = millis
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = NavyElevated,
                titleContentColor = TextPrimary,
                headlineContentColor = NeonGreen,
                weekdayContentColor = TextSecondary,
                subheadContentColor = TextSecondary,
                navigationContentColor = TextPrimary,
                yearContentColor = TextPrimary,
                disabledYearContentColor = TextTertiary,
                currentYearContentColor = NeonGreen,
                selectedYearContentColor = NavyDark,
                selectedYearContainerColor = NeonGreen,
                dayContentColor = TextPrimary,
                disabledDayContentColor = TextTertiary,
                selectedDayContentColor = NavyDark,
                disabledSelectedDayContentColor = TextTertiary,
                selectedDayContainerColor = GoldAccent,
                todayContentColor = GoldAccent,
                todayDateBorderColor = GoldAccent,
                dayInSelectionRangeContentColor = TextPrimary,
                dayInSelectionRangeContainerColor = NeonGreen.copy(0.15f)
            )
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }

    Scaffold(containerColor = NavyDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            GradientHeader("Nueva Sesión", "Planifica el entrenamiento", navController, true)

            Column(modifier = Modifier.padding(16.dp).padding(horizontal = if (isTablet) 80.dp else 0.dp)) {

                @Composable
                fun FieldLabel(text: String) {
                    Text(text, color = TextSecondary, style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 6.dp, top = 14.dp))
                }

                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent, unfocusedBorderColor = NavyBorder,
                    focusedLabelColor = GoldAccent, unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    cursorColor = GoldAccent, focusedContainerColor = NavyCard, unfocusedContainerColor = NavyCard
                )

                FieldLabel("Título de la sesión *")
                OutlinedTextField(titulo, { titulo = it }, label = { Text("Ej: Entrenamiento físico") },
                    leadingIcon = { Icon(Icons.Filled.FitnessCenter, null, tint = GoldAccent) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors, singleLine = true)

                FieldLabel("Fecha")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyCard)
                        .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.CalendarMonth, null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                        Column {
                            Text(displayDate, color = TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Semana ${getWeekOfYear(selectedDateMillis)} del año",
                                color = TextTertiary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Filled.Edit, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel("Hora inicio")
                        OutlinedTextField(horaInicio, { horaInicio = it }, label = { Text("HH:mm") },
                            leadingIcon = { Icon(Icons.Filled.Schedule, null, tint = GoldAccent) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = fieldColors, singleLine = true)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel("Hora fin")
                        OutlinedTextField(horaFin, { horaFin = it }, label = { Text("HH:mm") },
                            leadingIcon = { Icon(Icons.Filled.Schedule, null, tint = GoldAccent) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = fieldColors, singleLine = true)
                    }
                }

                FieldLabel("Lugar")
                OutlinedTextField(lugar, { lugar = it }, label = { Text("Pabellón, cancha...") },
                    leadingIcon = { Icon(Icons.Filled.LocationOn, null, tint = GoldAccent) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors, singleLine = true)

                FieldLabel("Descripción")
                OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripción general") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors, maxLines = 3)

                FieldLabel("Objetivos")
                OutlinedTextField(objetivos, { objetivos = it }, label = { Text("Objetivos del entrenamiento") },
                    leadingIcon = { Icon(Icons.Filled.Flag, null, tint = GoldAccent) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors, maxLines = 3)

                FieldLabel("Ejercicios")
                OutlinedTextField(ejercicios, { ejercicios = it }, label = { Text("Ejercicios planificados") },
                    leadingIcon = { Icon(Icons.Filled.SportsMartialArts, null, tint = GoldAccent) },
                    modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp), colors = fieldColors, maxLines = 5)

                uiState.error?.let { err ->
                    Spacer(Modifier.height(8.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = RedSurface), shape = RoundedCornerShape(10.dp)) {
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
                        viewModel.addSesion(
                            titulo = titulo, descripcion = descripcion,
                            semana = getWeekOfYear(selectedDateMillis),
                            diaSemana = getDayName(selectedDateMillis),
                            horaInicio = horaInicio, horaFin = horaFin,
                            lugar = lugar, objetivos = objetivos, ejercicios = ejercicios,
                            fecha = selectedDateMillis
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Icon(Icons.Filled.Save, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar Sesión", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
