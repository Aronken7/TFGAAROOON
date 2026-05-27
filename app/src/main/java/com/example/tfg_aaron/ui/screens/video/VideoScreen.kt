package com.example.tfg_aaron.ui.screens.video

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.VideoEntity
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(navController: NavController, entrenadorId: Int) {
    val context = LocalContext.current
    val app = context.applicationContext as TFGApplication
    val viewModel: VideoViewModel = viewModel(factory = viewModelFactory {
        initializer { VideoViewModel(entrenadorId, app.videoRepository) }
    })
    val isTablet = LocalIsTablet.current
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteDialog by remember { mutableStateOf<VideoEntity?>(null) }

    Scaffold(
        bottomBar = { CoachBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = PurpleAccent,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Nuevo video")
            }
        },
        containerColor = NavyDark
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isTablet) 2 else 1),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp),
            horizontalArrangement = if (isTablet) Arrangement.spacedBy(10.dp) else Arrangement.Start
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PlayVisionTopBar { navController.navigate(Screen.Perfil.route) }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ANÁLISIS", fontSize = 10.sp, color = PurpleAccent,
                                fontWeight = FontWeight.ExtraBold, letterSpacing = 2.5.sp)
                            Text("Videos", fontSize = 22.sp, color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp)
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PurpleAccent.copy(alpha = 0.15f))
                                .border(1.dp, PurpleAccent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        ) {
                            Icon(Icons.Filled.PlayCircle, null, tint = PurpleAccent, modifier = Modifier.size(22.dp))
                        }
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = NavyBorder)
            }

            // Stats summary
            if (uiState.videos.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val categorias = listOf("ANÁLISIS", "TÁCTICA", "ENTRENAMIENTO")
                        categorias.forEach { cat ->
                            val count = uiState.videos.count { it.categoria == cat }
                            val color = when (cat) {
                                "ANÁLISIS" -> PurpleAccent
                                "TÁCTICA" -> TealAccent
                                "ENTRENAMIENTO" -> NeonGreen
                                else -> TextSecondary
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color.copy(0.08f))
                                    .border(1.dp, color.copy(0.2f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$count", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                                Text(cat.take(4), color = TextTertiary, fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "LISTA DE VIDEOS",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    color = TextTertiary, fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                )
            }

            if (uiState.videos.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            icon = Icons.Filled.PlayCircle,
                            title = "Sin videos añadidos",
                            subtitle = "Añade URLs de YouTube para análisis táctico"
                        )
                    }
                }
            } else {
                gridItems(uiState.videos, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onPlay = {
                            val url = buildYoutubeUrl(video.url)
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (_: Exception) {}
                        },
                        onDelete = { showDeleteDialog = video }
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = NavyElevated,
            tonalElevation = 0.dp,
            scrimColor = Color.Black.copy(alpha = 0.8f)
        ) {
            AddVideoContent(
                onSave = { titulo, url, descripcion, categoria ->
                    viewModel.addVideo(titulo, url, descripcion, categoria)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false }
            )
        }
    }

    showDeleteDialog?.let { video ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = NavyElevated,
            shape = RoundedCornerShape(16.dp),
            title = { Text("Eliminar video", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("¿Eliminar \"${video.titulo}\"?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteVideo(video); showDeleteDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Eliminar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = null },
                    border = BorderStroke(1.dp, NavyBorder),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Cancelar", color = TextSecondary) }
            }
        )
    }
}

private fun buildYoutubeUrl(url: String): String {
    // Accept full URLs or just video IDs
    return if (url.startsWith("http://") || url.startsWith("https://")) url
    else "https://www.youtube.com/watch?v=$url"
}

@Composable
private fun VideoCard(
    video: VideoEntity,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es"))
    val catColor = when (video.categoria) {
        "ANÁLISIS" -> PurpleAccent
        "TÁCTICA" -> TealAccent
        "ENTRENAMIENTO" -> NeonGreen
        else -> TextSecondary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, catColor.copy(0.15f), RoundedCornerShape(14.dp))
            .clickable(onClick = onPlay)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(catColor.copy(0.15f))
                    .border(1.dp, catColor.copy(0.3f), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Filled.PlayArrow, null, tint = catColor, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(video.titulo, color = TextPrimary, fontWeight = FontWeight.Bold,
                    fontSize = 14.sp, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                BadgeChip(video.categoria, catColor)
                Spacer(Modifier.height(4.dp))
                if (video.descripcion.isNotEmpty()) {
                    Text(video.descripcion, color = TextSecondary, fontSize = 11.sp, maxLines = 2)
                    Spacer(Modifier.height(4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.CalendarToday, null, tint = TextTertiary, modifier = Modifier.size(10.dp))
                    Text(sdf.format(Date(video.fecha)), color = TextTertiary, fontSize = 10.sp)
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = NavyBorder)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onPlay) {
                Icon(Icons.Filled.OpenInNew, null, tint = catColor, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Ver video", color = catColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.7f), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Eliminar", color = RedError.copy(0.7f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun AddVideoContent(
    onSave: (String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("ANÁLISIS") }
    val categorias = listOf("ANÁLISIS", "TÁCTICA", "ENTRENAMIENTO")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "AÑADIR VIDEO",
            fontSize = 10.sp, color = PurpleAccent,
            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
        )
        Text(
            "Enlace de YouTube",
            fontSize = 20.sp, color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título del video", color = TextTertiary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleAccent, unfocusedBorderColor = NavyBorder,
                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                cursorColor = PurpleAccent
            ),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("URL de YouTube (o ID del vídeo)", color = TextTertiary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleAccent, unfocusedBorderColor = NavyBorder,
                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                cursorColor = PurpleAccent
            ),
            leadingIcon = { Icon(Icons.Filled.Link, null, tint = TextTertiary) },
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción (opcional)", color = TextTertiary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleAccent, unfocusedBorderColor = NavyBorder,
                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                cursorColor = PurpleAccent
            ),
            shape = RoundedCornerShape(10.dp),
            minLines = 2
        )

        // Categoría selector
        Text("CATEGORÍA", color = TextTertiary, fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categorias.forEach { cat ->
                val color = when (cat) {
                    "ANÁLISIS" -> PurpleAccent
                    "TÁCTICA" -> TealAccent
                    "ENTRENAMIENTO" -> NeonGreen
                    else -> TextSecondary
                }
                val isSelected = categoria == cat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) color.copy(0.15f) else NavyCard)
                        .border(1.dp, if (isSelected) color else NavyBorder, RoundedCornerShape(8.dp))
                        .clickable { categoria = cat }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(cat.take(5), color = if (isSelected) color else TextSecondary,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, NavyBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar", color = TextSecondary)
            }
            Button(
                onClick = { onSave(titulo, url, descripcion, categoria) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
