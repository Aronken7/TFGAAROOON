package com.example.tfg_aaron.data.network

import com.example.tfg_aaron.data.network.dto.*
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    // Jugadoras
    @GET("api/entrenadores/{entrenadorId}/jugadoras")
    suspend fun getJugadoras(@Path("entrenadorId") entrenadorId: Long): List<JugadoraDto>

    @POST("api/entrenadores/{entrenadorId}/jugadoras")
    suspend fun createJugadora(@Path("entrenadorId") entrenadorId: Long, @Body jugadora: JugadoraDto): JugadoraDto

    @PUT("api/entrenadores/{entrenadorId}/jugadoras/{id}")
    suspend fun updateJugadora(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body jugadora: JugadoraDto): JugadoraDto

    @DELETE("api/entrenadores/{entrenadorId}/jugadoras/{id}")
    suspend fun deleteJugadora(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // Partidos
    @GET("api/entrenadores/{entrenadorId}/partidos")
    suspend fun getPartidos(@Path("entrenadorId") entrenadorId: Long): List<PartidoDto>

    @POST("api/entrenadores/{entrenadorId}/partidos")
    suspend fun createPartido(@Path("entrenadorId") entrenadorId: Long, @Body partido: PartidoDto): PartidoDto

    @PUT("api/entrenadores/{entrenadorId}/partidos/{id}")
    suspend fun updatePartido(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body partido: PartidoDto): PartidoDto

    @DELETE("api/entrenadores/{entrenadorId}/partidos/{id}")
    suspend fun deletePartido(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // Scouting
    @GET("api/entrenadores/{entrenadorId}/scouting")
    suspend fun getScouting(@Path("entrenadorId") entrenadorId: Long): List<ScoutingDto>

    @POST("api/entrenadores/{entrenadorId}/scouting")
    suspend fun createScouting(@Path("entrenadorId") entrenadorId: Long, @Body scouting: ScoutingDto): ScoutingDto

    @PUT("api/entrenadores/{entrenadorId}/scouting/{id}")
    suspend fun updateScouting(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body scouting: ScoutingDto): ScoutingDto

    @DELETE("api/entrenadores/{entrenadorId}/scouting/{id}")
    suspend fun deleteScouting(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // Sesiones
    @GET("api/entrenadores/{entrenadorId}/sesiones")
    suspend fun getSesiones(@Path("entrenadorId") entrenadorId: Long): List<SesionDto>

    @POST("api/entrenadores/{entrenadorId}/sesiones")
    suspend fun createSesion(@Path("entrenadorId") entrenadorId: Long, @Body sesion: SesionDto): SesionDto

    @PUT("api/entrenadores/{entrenadorId}/sesiones/{id}")
    suspend fun updateSesion(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body sesion: SesionDto): SesionDto

    @DELETE("api/entrenadores/{entrenadorId}/sesiones/{id}")
    suspend fun deleteSesion(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // Pizarra
    @GET("api/entrenadores/{entrenadorId}/pizarra")
    suspend fun getPizarra(@Path("entrenadorId") entrenadorId: Long): List<PizarraDto>

    @POST("api/entrenadores/{entrenadorId}/pizarra")
    suspend fun createPizarra(@Path("entrenadorId") entrenadorId: Long, @Body jugada: PizarraDto): PizarraDto

    @PUT("api/entrenadores/{entrenadorId}/pizarra/{id}")
    suspend fun updatePizarra(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body jugada: PizarraDto): PizarraDto

    @DELETE("api/entrenadores/{entrenadorId}/pizarra/{id}")
    suspend fun deletePizarra(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // Estadísticas tiros
    @GET("api/entrenadores/{entrenadorId}/estadisticas-tiros")
    suspend fun getEstadisticasTiros(@Path("entrenadorId") entrenadorId: Long): List<EstadisticaTirosDto>

    @POST("api/entrenadores/{entrenadorId}/estadisticas-tiros/jugadoras/{jugadoraId}")
    suspend fun saveEstadisticaTiros(
        @Path("entrenadorId") entrenadorId: Long,
        @Path("jugadoraId") jugadoraId: Long,
        @Body datos: EstadisticaTirosDto
    ): EstadisticaTirosDto

    // EstadisticaPartido
    @GET("api/entrenadores/{entrenadorId}/estadisticas-partido")
    suspend fun getEstadisticasPartido(@Path("entrenadorId") entrenadorId: Long): List<EstadisticaPartidoDto>

    @GET("api/entrenadores/{entrenadorId}/estadisticas-partido/partidos/{partidoId}")
    suspend fun getEstadisticasByPartido(@Path("entrenadorId") entrenadorId: Long, @Path("partidoId") partidoId: Long): List<EstadisticaPartidoDto>

    @POST("api/entrenadores/{entrenadorId}/estadisticas-partido/jugadoras/{jugadoraId}/partidos/{partidoId}")
    suspend fun createEstadisticaPartido(@Path("entrenadorId") entrenadorId: Long, @Path("jugadoraId") jugadoraId: Long, @Path("partidoId") partidoId: Long, @Body dto: EstadisticaPartidoDto): EstadisticaPartidoDto

    @PUT("api/entrenadores/{entrenadorId}/estadisticas-partido/{id}")
    suspend fun updateEstadisticaPartido(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body dto: EstadisticaPartidoDto): EstadisticaPartidoDto

    @DELETE("api/entrenadores/{entrenadorId}/estadisticas-partido/{id}")
    suspend fun deleteEstadisticaPartido(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // HistorialFisico
    @GET("api/entrenadores/{entrenadorId}/historial-fisico")
    suspend fun getHistorialFisico(@Path("entrenadorId") entrenadorId: Long): List<HistorialFisicoDto>

    @POST("api/entrenadores/{entrenadorId}/historial-fisico/jugadoras/{jugadoraId}")
    suspend fun createHistorialFisico(@Path("entrenadorId") entrenadorId: Long, @Path("jugadoraId") jugadoraId: Long, @Body dto: HistorialFisicoDto): HistorialFisicoDto

    @PUT("api/entrenadores/{entrenadorId}/historial-fisico/{id}")
    suspend fun updateHistorialFisico(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body dto: HistorialFisicoDto): HistorialFisicoDto

    @DELETE("api/entrenadores/{entrenadorId}/historial-fisico/{id}")
    suspend fun deleteHistorialFisico(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // TestFisico
    @GET("api/entrenadores/{entrenadorId}/test-fisico")
    suspend fun getTestFisico(@Path("entrenadorId") entrenadorId: Long): List<TestFisicoDto>

    @POST("api/entrenadores/{entrenadorId}/test-fisico/jugadoras/{jugadoraId}")
    suspend fun createTestFisico(@Path("entrenadorId") entrenadorId: Long, @Path("jugadoraId") jugadoraId: Long, @Body dto: TestFisicoDto): TestFisicoDto

    @PUT("api/entrenadores/{entrenadorId}/test-fisico/{id}")
    suspend fun updateTestFisico(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body dto: TestFisicoDto): TestFisicoDto

    @DELETE("api/entrenadores/{entrenadorId}/test-fisico/{id}")
    suspend fun deleteTestFisico(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // WellnessDiario
    @GET("api/entrenadores/{entrenadorId}/wellness")
    suspend fun getWellness(@Path("entrenadorId") entrenadorId: Long): List<WellnessDiarioDto>

    @POST("api/entrenadores/{entrenadorId}/wellness/jugadoras/{jugadoraId}")
    suspend fun createWellness(@Path("entrenadorId") entrenadorId: Long, @Path("jugadoraId") jugadoraId: Long, @Body dto: WellnessDiarioDto): WellnessDiarioDto

    @PUT("api/entrenadores/{entrenadorId}/wellness/{id}")
    suspend fun updateWellness(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body dto: WellnessDiarioDto): WellnessDiarioDto

    @DELETE("api/entrenadores/{entrenadorId}/wellness/{id}")
    suspend fun deleteWellness(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // Lesiones
    @GET("api/entrenadores/{entrenadorId}/lesiones")
    suspend fun getLesiones(@Path("entrenadorId") entrenadorId: Long): List<LesionDto>

    @POST("api/entrenadores/{entrenadorId}/lesiones")
    suspend fun createLesion(@Path("entrenadorId") entrenadorId: Long, @Body dto: LesionDto): LesionDto

    @PUT("api/entrenadores/{entrenadorId}/lesiones/{id}")
    suspend fun updateLesion(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body dto: LesionDto): LesionDto

    @DELETE("api/entrenadores/{entrenadorId}/lesiones/{id}")
    suspend fun deleteLesion(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // Objetivos
    @GET("api/entrenadores/{entrenadorId}/objetivos")
    suspend fun getObjetivos(@Path("entrenadorId") entrenadorId: Long): List<ObjetivoDto>

    @POST("api/entrenadores/{entrenadorId}/objetivos")
    suspend fun createObjetivo(@Path("entrenadorId") entrenadorId: Long, @Body dto: ObjetivoDto): ObjetivoDto

    @PUT("api/entrenadores/{entrenadorId}/objetivos/{id}")
    suspend fun updateObjetivo(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body dto: ObjetivoDto): ObjetivoDto

    @DELETE("api/entrenadores/{entrenadorId}/objetivos/{id}")
    suspend fun deleteObjetivo(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // GamePlan
    @GET("api/entrenadores/{entrenadorId}/game-plan")
    suspend fun getGamePlan(@Path("entrenadorId") entrenadorId: Long): List<GamePlanDto>

    @POST("api/entrenadores/{entrenadorId}/game-plan")
    suspend fun createGamePlan(@Path("entrenadorId") entrenadorId: Long, @Body dto: GamePlanDto): GamePlanDto

    @PUT("api/entrenadores/{entrenadorId}/game-plan/{id}")
    suspend fun updateGamePlan(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body dto: GamePlanDto): GamePlanDto

    @DELETE("api/entrenadores/{entrenadorId}/game-plan/{id}")
    suspend fun deleteGamePlan(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // RivalPerfil
    @GET("api/entrenadores/{entrenadorId}/rival-perfil")
    suspend fun getRivalPerfil(@Path("entrenadorId") entrenadorId: Long): List<RivalPerfilDto>

    @POST("api/entrenadores/{entrenadorId}/rival-perfil")
    suspend fun createRivalPerfil(@Path("entrenadorId") entrenadorId: Long, @Body dto: RivalPerfilDto): RivalPerfilDto

    @PUT("api/entrenadores/{entrenadorId}/rival-perfil/{id}")
    suspend fun updateRivalPerfil(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body dto: RivalPerfilDto): RivalPerfilDto

    @DELETE("api/entrenadores/{entrenadorId}/rival-perfil/{id}")
    suspend fun deleteRivalPerfil(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // EventoPartido
    @GET("api/entrenadores/{entrenadorId}/eventos-partido")
    suspend fun getEventosPartido(@Path("entrenadorId") entrenadorId: Long): List<EventoPartidoDto>

    @GET("api/entrenadores/{entrenadorId}/eventos-partido/partidos/{partidoId}")
    suspend fun getEventosByPartido(@Path("entrenadorId") entrenadorId: Long, @Path("partidoId") partidoId: Long): List<EventoPartidoDto>

    @POST("api/entrenadores/{entrenadorId}/eventos-partido")
    suspend fun createEventoPartido(@Path("entrenadorId") entrenadorId: Long, @Body dto: EventoPartidoDto): EventoPartidoDto

    @DELETE("api/entrenadores/{entrenadorId}/eventos-partido/{id}")
    suspend fun deleteEventoPartido(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // Convocatoria
    @GET("api/entrenadores/{entrenadorId}/convocatorias/partidos/{partidoId}")
    suspend fun getConvocatoriaByPartido(@Path("entrenadorId") entrenadorId: Long, @Path("partidoId") partidoId: Long): List<ConvocatoriaDto>

    @POST("api/entrenadores/{entrenadorId}/convocatorias/partidos/{partidoId}")
    suspend fun saveConvocatoria(@Path("entrenadorId") entrenadorId: Long, @Path("partidoId") partidoId: Long, @Body dtos: List<ConvocatoriaDto>): List<ConvocatoriaDto>

    // Videos
    @GET("api/entrenadores/{entrenadorId}/videos")
    suspend fun getVideos(@Path("entrenadorId") entrenadorId: Long): List<VideoDto>

    @POST("api/entrenadores/{entrenadorId}/videos")
    suspend fun createVideo(@Path("entrenadorId") entrenadorId: Long, @Body dto: VideoDto): VideoDto

    @PUT("api/entrenadores/{entrenadorId}/videos/{id}")
    suspend fun updateVideo(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body dto: VideoDto): VideoDto

    @DELETE("api/entrenadores/{entrenadorId}/videos/{id}")
    suspend fun deleteVideo(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // Ejercicios
    @GET("api/entrenadores/{entrenadorId}/ejercicios")
    suspend fun getEjercicios(@Path("entrenadorId") entrenadorId: Long): List<EjercicioDto>

    @POST("api/entrenadores/{entrenadorId}/ejercicios")
    suspend fun createEjercicio(@Path("entrenadorId") entrenadorId: Long, @Body dto: EjercicioDto): EjercicioDto

    @PUT("api/entrenadores/{entrenadorId}/ejercicios/{id}")
    suspend fun updateEjercicio(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body dto: EjercicioDto): EjercicioDto

    @DELETE("api/entrenadores/{entrenadorId}/ejercicios/{id}")
    suspend fun deleteEjercicio(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // SkillRating
    @GET("api/entrenadores/{entrenadorId}/skill-rating")
    suspend fun getSkillRating(@Path("entrenadorId") entrenadorId: Long): List<SkillRatingDto>

    @POST("api/entrenadores/{entrenadorId}/skill-rating/jugadoras/{jugadoraId}")
    suspend fun createSkillRating(@Path("entrenadorId") entrenadorId: Long, @Path("jugadoraId") jugadoraId: Long, @Body dto: SkillRatingDto): SkillRatingDto

    @PUT("api/entrenadores/{entrenadorId}/skill-rating/{id}")
    suspend fun updateSkillRating(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body dto: SkillRatingDto): SkillRatingDto

    @DELETE("api/entrenadores/{entrenadorId}/skill-rating/{id}")
    suspend fun deleteSkillRating(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // ShotChart
    @GET("api/entrenadores/{entrenadorId}/shot-chart")
    suspend fun getShotChart(@Path("entrenadorId") entrenadorId: Long): List<ShotChartDataDto>

    @POST("api/entrenadores/{entrenadorId}/shot-chart/partidos/{partidoId}/jugadoras/{jugadoraId}")
    suspend fun createShotChart(@Path("entrenadorId") entrenadorId: Long, @Path("partidoId") partidoId: Long, @Path("jugadoraId") jugadoraId: Long, @Body dto: ShotChartDataDto): ShotChartDataDto

    @DELETE("api/entrenadores/{entrenadorId}/shot-chart/{id}")
    suspend fun deleteShotChart(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)

    // Asistencia
    @GET("api/entrenadores/{entrenadorId}/asistencias")
    suspend fun getAsistencias(@Path("entrenadorId") entrenadorId: Long): List<AsistenciaDto>

    @POST("api/entrenadores/{entrenadorId}/asistencias")
    suspend fun createAsistencia(@Path("entrenadorId") entrenadorId: Long, @Body dto: AsistenciaDto): AsistenciaDto

    @PUT("api/entrenadores/{entrenadorId}/asistencias/{id}")
    suspend fun updateAsistencia(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long, @Body dto: AsistenciaDto): AsistenciaDto

    @DELETE("api/entrenadores/{entrenadorId}/asistencias/{id}")
    suspend fun deleteAsistencia(@Path("entrenadorId") entrenadorId: Long, @Path("id") id: Long)
}
