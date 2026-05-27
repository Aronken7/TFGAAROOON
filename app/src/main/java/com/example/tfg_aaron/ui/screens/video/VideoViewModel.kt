package com.example.tfg_aaron.ui.screens.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.VideoEntity
import com.example.tfg_aaron.data.repository.VideoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class VideoUiState(
    val videos: List<VideoEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class VideoViewModel(
    private val entrenadorId: Int,
    private val repository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getByEntrenador(entrenadorId).collect { list ->
                _uiState.value = _uiState.value.copy(videos = list)
            }
        }
    }

    fun addVideo(
        titulo: String,
        url: String,
        descripcion: String,
        categoria: String
    ) {
        if (titulo.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "El título es obligatorio")
            return
        }
        if (url.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "La URL es obligatoria")
            return
        }
        viewModelScope.launch {
            val video = VideoEntity(
                idEntrenador = entrenadorId,
                titulo = titulo,
                url = url,
                descripcion = descripcion,
                categoria = categoria
            )
            repository.insert(video)
            _uiState.value = _uiState.value.copy(successMessage = "Video añadido")
        }
    }

    fun deleteVideo(video: VideoEntity) {
        viewModelScope.launch { repository.delete(video) }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
