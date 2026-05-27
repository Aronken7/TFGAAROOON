package com.example.tfg_aaron.ui.screens.alineacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.ConvocatoriaEntity
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import com.example.tfg_aaron.data.repository.ConvocatoriaRepository
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.PartidoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AlineacionUiState(
    val partido: PartidoEntity? = null,
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val convocadas: List<ConvocatoriaEntity> = emptyList(),
    val isLoading: Boolean = true,
    val savedMessage: String? = null
)

class AlineacionViewModel(
    private val entrenadorId: Int,
    private val idPartido: Int,
    private val convocatoriaRepository: ConvocatoriaRepository,
    private val jugadoraRepository: JugadoraRepository,
    private val partidoRepository: PartidoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlineacionUiState())
    val uiState: StateFlow<AlineacionUiState> = _uiState.asStateFlow()

    // Titulares sorted by posicion index (0..4)
    val titulares: List<ConvocatoriaEntity>
        get() = _uiState.value.convocadas
            .filter { it.esTitular }
            .sortedBy { it.posicionIndice }

    // Suplentes: convocadas but not titular
    val suplentes: List<ConvocatoriaEntity>
        get() = _uiState.value.convocadas.filter { !it.esTitular }

    init {
        viewModelScope.launch {
            // Load partido once
            val partido = partidoRepository.getPartidoById(idPartido)
            _uiState.update { it.copy(partido = partido) }

            // Combine jugadoras + convocadas flows
            combine(
                jugadoraRepository.getActivas(entrenadorId),
                convocatoriaRepository.getByPartido(idPartido, entrenadorId)
            ) { jugadoras, convocadas ->
                jugadoras to convocadas
            }.collect { (jugadoras, convocadas) ->
                _uiState.update {
                    it.copy(
                        jugadoras = jugadoras,
                        convocadas = convocadas,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Assigns a player to a starting position (posicionIndice 0-4).
     * Max 5 titulars enforced. If another player already holds that slot,
     * they are moved to suplente. The assigned player's previous slot (if any)
     * is also cleared first.
     */
    fun assignTitular(jugadora: JugadoraEntity, posicionIndice: Int) {
        viewModelScope.launch {
            val current = _uiState.value.convocadas

            // If another player is in this slot, demote them to suplente
            val slotHolder = current.find { it.posicionIndice == posicionIndice && it.esTitular }
            if (slotHolder != null && slotHolder.idJugadora != jugadora.id) {
                convocatoriaRepository.update(
                    slotHolder.copy(esTitular = false, posicionIndice = -1)
                )
            }

            // Remove the target player from any existing convocatoria entry
            val existing = current.find { it.idJugadora == jugadora.id }
            if (existing != null) {
                convocatoriaRepository.update(
                    existing.copy(esTitular = true, posicionIndice = posicionIndice)
                )
            } else {
                // Check max 5 titulars
                val titularCount = convocatoriaRepository.countTitulares(idPartido, entrenadorId)
                if (titularCount >= 5) return@launch
                convocatoriaRepository.insert(
                    ConvocatoriaEntity(
                        idPartido = idPartido,
                        idJugadora = jugadora.id,
                        idEntrenador = entrenadorId,
                        esTitular = true,
                        posicionIndice = posicionIndice
                    )
                )
            }
        }
    }

    /**
     * Removes the titular from the given posicion slot.
     * If the player was only a titular (not a suplente), removes them entirely.
     */
    fun removeTitular(posicionIndice: Int) {
        viewModelScope.launch {
            val entry = _uiState.value.convocadas
                .find { it.posicionIndice == posicionIndice && it.esTitular }
                ?: return@launch
            convocatoriaRepository.delete(entry)
        }
    }

    /**
     * Toggles a player on/off the bench (suplente). If she's already a
     * titular this does nothing — use assignTitular instead.
     */
    fun toggleSuplente(jugadora: JugadoraEntity) {
        viewModelScope.launch {
            val existing = _uiState.value.convocadas.find { it.idJugadora == jugadora.id }
            when {
                existing == null -> {
                    // Add as suplente
                    convocatoriaRepository.insert(
                        ConvocatoriaEntity(
                            idPartido = idPartido,
                            idJugadora = jugadora.id,
                            idEntrenador = entrenadorId,
                            esTitular = false,
                            posicionIndice = -1
                        )
                    )
                }
                !existing.esTitular -> {
                    // Remove from bench
                    convocatoriaRepository.delete(existing)
                }
                // If titular, do nothing — coach must remove from court first
            }
        }
    }

    /**
     * Persists the entire current convocatoria state (no-op since we already
     * persist on every action). Emits a confirmation message.
     */
    fun saveAlineacion() {
        viewModelScope.launch {
            _uiState.update { it.copy(savedMessage = "Alineación guardada") }
        }
    }

    fun clearSavedMessage() {
        _uiState.update { it.copy(savedMessage = null) }
    }

    fun isJugadoraConvocada(idJugadora: Int): Boolean =
        _uiState.value.convocadas.any { it.idJugadora == idJugadora }

    fun getJugadoraForPosicion(posicionIndice: Int): JugadoraEntity? {
        val conv = _uiState.value.convocadas
            .find { it.posicionIndice == posicionIndice && it.esTitular }
            ?: return null
        return _uiState.value.jugadoras.find { it.id == conv.idJugadora }
    }
}
