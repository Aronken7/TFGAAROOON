package com.example.tfg_aaron.data.network

import com.example.tfg_aaron.data.local.database.AppDatabase
import com.example.tfg_aaron.data.local.entities.*
import com.example.tfg_aaron.data.network.dto.*

class SyncManager(private val db: AppDatabase) {

    suspend fun syncAll(entrenadorId: Int) {
        val eid = entrenadorId.toLong()
        listOf(
            suspend { syncJugadoras(eid, entrenadorId) },
            suspend { syncPartidos(eid, entrenadorId) },
            suspend { syncSesiones(eid, entrenadorId) },
            suspend { syncScouting(eid, entrenadorId) },
            suspend { syncPizarra(eid, entrenadorId) },
            suspend { syncEstadisticasPartido(eid) },
            suspend { syncHistorialFisico(eid, entrenadorId) },
            suspend { syncTestFisico(eid, entrenadorId) },
            suspend { syncWellness(eid, entrenadorId) },
            suspend { syncLesiones(eid, entrenadorId) },
            suspend { syncObjetivos(eid, entrenadorId) },
            suspend { syncGamePlan(eid, entrenadorId) },
            suspend { syncRivalPerfil(eid, entrenadorId) },
            suspend { syncVideos(eid, entrenadorId) },
            suspend { syncEjercicios(eid, entrenadorId) },
            suspend { syncSkillRating(eid, entrenadorId) },
            suspend { syncEventosPartido(eid, entrenadorId) },
            suspend { syncShotChart(eid, entrenadorId) },
            suspend { syncAsistencias(eid, entrenadorId) }
        ).forEach { fn -> try { fn() } catch (_: Exception) {} }
    }

    private suspend fun syncJugadoras(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getJugadoras(eid)
        val dao = db.jugadoraDao()
        for (dto in dtos) {
            val parts = dto.nombre.split(" ", limit = 2)
            val nombre = parts.getOrElse(0) { dto.nombre }
            val apellidos = if (parts.size > 1) parts[1] else ""
            dao.insert(
                JugadoraEntity(
                    id = dto.id.toInt(),
                    idEntrenador = entrenadorId,
                    nombre = nombre,
                    apellidos = apellidos,
                    numero = dto.numero ?: 0,
                    posicion = dto.posicion ?: "",
                    rol = dto.rol ?: "Suplente",
                    condicionFisica = dto.condicionFisica ?: "DISPONIBLE"
                )
            )
        }
    }

    private suspend fun syncPartidos(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getPartidos(eid)
        val dao = db.partidoDao()
        for (dto in dtos) {
            dao.insertPartido(
                PartidoEntity(
                    id = dto.id.toInt(),
                    idEntrenador = entrenadorId,
                    rival = dto.rival,
                    fecha = dto.fecha?.toLongOrNull() ?: 0L,
                    puntosPropio = dto.puntosAnotados ?: 0,
                    puntosRival = dto.puntosEncajados ?: 0,
                    esLocal = dto.esLocal ?: true,
                    jornada = dto.jornada ?: 0
                )
            )
        }
    }

    private suspend fun syncSesiones(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getSesiones(eid)
        val dao = db.sesionDao()
        for (dto in dtos) {
            val fechaMs = dto.fecha?.toLongOrNull() ?: 0L
            dao.insert(
                SesionEntrenamientoEntity(
                    id = dto.id.toInt(),
                    idEntrenador = entrenadorId,
                    titulo = dto.titulo,
                    descripcion = dto.descripcion ?: "",
                    semana = 0,
                    diaSemana = "",
                    horaInicio = "",
                    horaFin = "",
                    fechaInicio = fechaMs,
                    fechaFin = fechaMs
                )
            )
        }
    }

    private suspend fun syncScouting(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getScouting(eid)
        val dao = db.scoutingDao()
        for (dto in dtos) {
            dao.insert(
                ScoutingEntity(
                    id = dto.id.toInt(),
                    idEntrenador = entrenadorId,
                    rivalNombre = dto.rival,
                    observaciones = dto.observacion ?: "",
                    tipo = dto.tipo ?: "RIVAL",
                    valoracion = dto.valoracion ?: 3
                )
            )
        }
    }

    private suspend fun syncPizarra(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getPizarra(eid)
        val dao = db.pizarraJugadaDao()
        for (dto in dtos) {
            val fechaMs = dto.fechaCreacion?.toLongOrNull() ?: System.currentTimeMillis()
            dao.insert(
                PizarraJugadaEntity(
                    id = dto.id.toInt(),
                    idEntrenador = entrenadorId,
                    nombreJugada = dto.nombre,
                    dibujoData = dto.datosJson ?: "",
                    fechaCreacion = fechaMs,
                    ultimaModificacion = fechaMs
                )
            )
        }
    }

    private suspend fun syncEstadisticasPartido(eid: Long) {
        val dtos = RetrofitClient.api.getEstadisticasPartido(eid)
        val dao = db.estadisticaPartidoDao()
        for (dto in dtos) {
            dao.insertOrUpdate(
                EstadisticaPartidoEntity(
                    id = dto.id?.toInt() ?: 0,
                    idPartido = dto.partidoId.toInt(),
                    idJugadora = dto.jugadoraId.toInt(),
                    puntos = dto.puntos,
                    rebotes = dto.rebotes,
                    asistencias = dto.asistencias,
                    faltas = dto.faltas,
                    minutos = dto.minutos
                )
            )
        }
    }

    private suspend fun syncHistorialFisico(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getHistorialFisico(eid)
        val dao = db.historialFisicoDao()
        for (dto in dtos) {
            dao.insert(
                HistorialFisicoEntity(
                    id = dto.id?.toInt() ?: 0,
                    idJugadora = dto.jugadoraId.toInt(),
                    fecha = dto.fecha,
                    condicionFisica = dto.condicionFisica.ifEmpty { "DISPONIBLE" },
                    peso = dto.peso.toFloat(),
                    altura = dto.altura.toFloat(),
                    masaGrasa = dto.masaGrasa.toFloat(),
                    masaMuscular = dto.masaMuscular.toFloat(),
                    envergadura = dto.envergadura.toFloat(),
                    alturaSentado = dto.alturaSentado.toFloat(),
                    perimetroBrazo = dto.perimetroBrazo.toFloat(),
                    perimetroCintura = dto.perimetroCintura.toFloat(),
                    perimetroMuslo = dto.perimetroMuslo.toFloat(),
                    perimetroCadera = dto.perimetroCadera.toFloat(),
                    perimetroPecho = dto.perimetroPecho.toFloat(),
                    saltoVertical = dto.saltoVertical.toFloat(),
                    saltoHorizontal = dto.saltoHorizontal.toFloat(),
                    sprint10m = dto.sprint10m.toFloat(),
                    observaciones = dto.observaciones
                )
            )
        }
    }

    private suspend fun syncTestFisico(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getTestFisico(eid)
        val dao = db.testFisicoDao()
        for (dto in dtos) {
            dao.insert(
                TestFisicoEntity(
                    id = dto.id?.toInt() ?: 0,
                    idJugadora = dto.jugadoraId.toInt(),
                    idEntrenador = entrenadorId,
                    tipo = dto.tipo,
                    nombreCustom = dto.nombreCustom,
                    valor = dto.valor,
                    unidad = dto.unidad,
                    fecha = dto.fecha,
                    notas = dto.notas
                )
            )
        }
    }

    private suspend fun syncWellness(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getWellness(eid)
        val dao = db.wellnessDiarioDao()
        for (dto in dtos) {
            dao.insert(
                WellnessDiarioEntity(
                    id = dto.id?.toInt() ?: 0,
                    idJugadora = dto.jugadoraId.toInt(),
                    idEntrenador = entrenadorId,
                    fecha = dto.fecha,
                    fatiga = dto.fatiga,
                    sueno = dto.sueno,
                    motivacion = dto.motivacion,
                    dolor = dto.dolor,
                    rpe = dto.rpe,
                    notas = dto.notas
                )
            )
        }
    }

    private suspend fun syncLesiones(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getLesiones(eid)
        val dao = db.lesionDao()
        for (dto in dtos) {
            dao.insert(
                LesionEntity(
                    id = dto.id?.toInt() ?: 0,
                    idJugadora = dto.jugadoraId.toInt(),
                    idEntrenador = entrenadorId,
                    tipo = dto.tipo,
                    zonaCorpo = dto.zonaCorpo,
                    gravedad = dto.gravedad,
                    fechaInicio = dto.fechaInicio,
                    fechaEstimadaVuelta = dto.fechaEstimadaVuelta,
                    fechaVueltaReal = dto.fechaVueltaReal,
                    protocolo = dto.protocolo,
                    notas = dto.notas,
                    estado = dto.estado.ifEmpty { "ACTIVA" }
                )
            )
        }
    }

    private suspend fun syncObjetivos(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getObjetivos(eid)
        val dao = db.objetivoDao()
        for (dto in dtos) {
            dao.insert(
                ObjetivoEntity(
                    id = dto.id?.toInt() ?: 0,
                    idEntrenador = entrenadorId,
                    idJugadora = dto.jugadoraId.toInt(),
                    titulo = dto.titulo,
                    descripcion = dto.descripcion,
                    tipo = dto.tipo.ifEmpty { "RENDIMIENTO" },
                    metrica = dto.metrica,
                    valorObjetivo = dto.valorObjetivo.toFloat(),
                    valorActual = dto.valorActual.toFloat(),
                    unidad = dto.unidad,
                    fechaInicio = dto.fechaInicio,
                    fechaFin = dto.fechaFin,
                    completado = dto.completado,
                    notas = dto.notas
                )
            )
        }
    }

    private suspend fun syncGamePlan(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getGamePlan(eid)
        val dao = db.gamePlanDao()
        for (dto in dtos) {
            dao.insert(
                GamePlanEntity(
                    id = dto.id?.toInt() ?: 0,
                    idEntrenador = entrenadorId,
                    rival = dto.rival,
                    fecha = dto.fecha,
                    descripcion = dto.descripcion,
                    focoOfensivo = dto.focoOfensivo,
                    focoDefensivo = dto.focoDefensivo,
                    jugadasClave = dto.jugadasClave,
                    sistemaDefensivo = dto.sistemaDefensivo,
                    ajustesIndividuales = dto.ajustesIndividuales,
                    notasFinales = dto.notasFinales
                )
            )
        }
    }

    private suspend fun syncRivalPerfil(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getRivalPerfil(eid)
        val dao = db.rivalPerfilDao()
        for (dto in dtos) {
            dao.insert(
                RivalPerfilEntity(
                    id = dto.id?.toInt() ?: 0,
                    idEntrenador = entrenadorId,
                    nombre = dto.nombre,
                    temporada = dto.temporada,
                    sistemaOfensivo = dto.sistemaOfensivo,
                    sistemaDefensivo = dto.sistemaDefensivo,
                    jugadoresClave = dto.jugadoresClave,
                    fortalezas = dto.fortalezas,
                    debilidades = dto.debilidades,
                    puntosAFavor = dto.puntosAFavor,
                    puntosEnContra = dto.puntosEnContra,
                    victorias = dto.victorias,
                    derrotas = dto.derrotas,
                    notas = dto.notas
                )
            )
        }
    }

    private suspend fun syncVideos(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getVideos(eid)
        val dao = db.videoDao()
        for (dto in dtos) {
            dao.insert(
                VideoEntity(
                    id = dto.id?.toInt() ?: 0,
                    idEntrenador = entrenadorId,
                    titulo = dto.titulo,
                    url = dto.url,
                    descripcion = dto.descripcion,
                    categoria = dto.categoria.ifEmpty { "ANÁLISIS" },
                    fecha = dto.fecha
                )
            )
        }
    }

    private suspend fun syncEjercicios(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getEjercicios(eid)
        val dao = db.ejercicioDao()
        for (dto in dtos) {
            dao.insert(
                EjercicioEntity(
                    id = dto.id?.toInt() ?: 0,
                    entrenadorId = entrenadorId,
                    nombre = dto.nombre,
                    descripcion = dto.descripcion,
                    categoria = dto.categoria.ifEmpty { "OTRO" },
                    duracionMinutos = dto.duracionMinutos,
                    intensidad = dto.intensidad.ifEmpty { "MEDIA" },
                    materialesNecesarios = dto.materialesNecesarios,
                    jugadoresMinimos = dto.jugadoresMinimos,
                    notas = dto.notas,
                    esFavorito = dto.esFavorito,
                    timestampCreacion = dto.timestampCreacion
                )
            )
        }
    }

    private suspend fun syncSkillRating(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getSkillRating(eid)
        val dao = db.skillRatingDao()
        for (dto in dtos) {
            dao.insert(
                SkillRatingEntity(
                    id = dto.id?.toInt() ?: 0,
                    idJugadora = dto.jugadoraId.toInt(),
                    idEntrenador = entrenadorId,
                    fecha = dto.fecha,
                    tiro = dto.tiro,
                    defensa = dto.defensa,
                    balonMano = dto.balonMano,
                    vision = dto.vision,
                    atletismo = dto.atletismo,
                    mentalidad = dto.mentalidad,
                    liderazgo = dto.liderazgo,
                    notas = dto.notas
                )
            )
        }
    }

    private suspend fun syncEventosPartido(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getEventosPartido(eid)
        val dao = db.eventoPartidoDao()
        for (dto in dtos) {
            dao.insert(
                EventoPartidoEntity(
                    id = dto.id?.toInt() ?: 0,
                    idPartido = dto.partidoId.toInt(),
                    idJugadora = dto.jugadoraId.toInt(),
                    tipoEvento = dto.tipoEvento,
                    cuarto = dto.cuarto,
                    valor = dto.valor,
                    texto = dto.texto,
                    timestamp = dto.timestamp
                )
            )
        }
    }

    private suspend fun syncShotChart(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getShotChart(eid)
        val dao = db.shotChartDao()
        for (dto in dtos) {
            dao.insert(
                ShotChartDataEntity(
                    id = dto.id?.toInt() ?: 0,
                    idPartido = dto.partidoId.toInt(),
                    idJugadora = dto.jugadoraId.toInt(),
                    idEntrenador = entrenadorId,
                    zonaX = dto.zonaX,
                    zonaY = dto.zonaY,
                    zona = dto.zona,
                    hecha = dto.hecha,
                    tipo = dto.tipo,
                    cuarto = dto.cuarto
                )
            )
        }
    }

    private suspend fun syncAsistencias(eid: Long, entrenadorId: Int) {
        val dtos = RetrofitClient.api.getAsistencias(eid)
        val dao = db.asistenciaDao()
        for (dto in dtos) {
            dao.insert(
                AsistenciaEntity(
                    id = dto.id?.toInt() ?: 0,
                    idJugadora = dto.jugadoraId.toInt(),
                    idEntrenador = entrenadorId,
                    tipo = dto.tipo,
                    referenciaId = dto.referenciaId.toInt(),
                    estado = dto.estado,
                    notas = dto.notas,
                    fecha = dto.fecha
                )
            )
        }
    }
}
