package com.example.tfg_aaron

import android.app.Application
import com.example.tfg_aaron.data.local.database.AppDatabase
import com.example.tfg_aaron.data.network.RetrofitClient
import com.example.tfg_aaron.data.preferences.UserPreferences
import com.example.tfg_aaron.data.repository.*
import com.example.tfg_aaron.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TFGApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val userPreferences by lazy { UserPreferences(this) }

    val authRepository by lazy {
        AuthRepository(database.entrenadorDao(), userPreferences, database)
    }
    val jugadoraRepository by lazy {
        JugadoraRepository(database.jugadoraDao())
    }
    val scoutingRepository by lazy {
        ScoutingRepository(database.scoutingDao())
    }
    val sesionRepository by lazy {
        SesionRepository(database.sesionDao(), database.asignacionSesionDao())
    }
    val estadisticaRepository by lazy {
        EstadisticaRepository(database.estadisticaTirosDao())
    }
    val pizarraRepository by lazy {
        PizarraRepository(database.pizarraJugadaDao())
    }
    val reporteRepository by lazy {
        ReporteRepository(database.reporteDao())
    }
    val partidoRepository by lazy {
        PartidoRepository(database.partidoDao(), database.eventoPartidoDao())
    }
    val historialFisicoRepository by lazy {
        HistorialFisicoRepository(database.historialFisicoDao())
    }
    val videoRepository by lazy {
        VideoRepository(database.videoDao())
    }
    val ejercicioRepository by lazy {
        EjercicioRepository(database.ejercicioDao())
    }
    val testFisicoRepository by lazy {
        TestFisicoRepository(database.testFisicoDao())
    }
    val wellnessDiarioRepository by lazy {
        WellnessDiarioRepository(database.wellnessDiarioDao())
    }
    val asistenciaRepository by lazy {
        AsistenciaRepository(database.asistenciaDao())
    }
    val skillRatingRepository by lazy {
        SkillRatingRepository(database.skillRatingDao())
    }
    val gamePlanRepository by lazy {
        GamePlanRepository(database.gamePlanDao())
    }
    val rivalPerfilRepository by lazy {
        RivalPerfilRepository(database.rivalPerfilDao())
    }
    val shotChartRepository by lazy {
        ShotChartRepository(database.shotChartDao())
    }
    val sesionTiroRepository by lazy {
        SesionTiroRepository(database.sesionTiroDao())
    }
    val plantillaRepository by lazy {
        PlantillaRepository(database.plantillaDao())
    }
    val lesionRepository by lazy {
        LesionRepository(database.lesionDao())
    }
    val objetivoRepository by lazy {
        ObjetivoRepository(database.objetivoDao())
    }
    val convocatoriaRepository by lazy {
        ConvocatoriaRepository(database.convocatoriaDao())
    }
    val estadisticaPartidoRepository by lazy {
        EstadisticaPartidoRepository(database.estadisticaPartidoDao())
    }
    val clasificacionRepository by lazy {
        ClasificacionRepository(database.clasificacionDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Restaurar el token JWT si había sesión guardada
        CoroutineScope(Dispatchers.IO).launch {
            val token = userPreferences.jwtToken.first()
            if (token.isNotEmpty()) {
                RetrofitClient.setToken(token)
            }
        }
        // Create notification channels
        NotificationHelper.createChannel(this)
    }
}
