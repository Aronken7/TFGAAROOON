package com.example.tfg_aaron.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tfg_aaron.data.local.dao.*
import com.example.tfg_aaron.data.local.entities.*

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""CREATE TABLE IF NOT EXISTS plantilla_entrenamiento (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idEntrenador INTEGER NOT NULL,
            nombre TEXT NOT NULL,
            descripcion TEXT NOT NULL,
            activa INTEGER NOT NULL,
            fechaCreacion INTEGER NOT NULL
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_plantilla_idEntrenador ON plantilla_entrenamiento(idEntrenador)")
        database.execSQL("""CREATE TABLE IF NOT EXISTS plantilla_item (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idPlantilla INTEGER NOT NULL,
            diaSemana INTEGER NOT NULL,
            tipo TEXT NOT NULL,
            titulo TEXT NOT NULL,
            descripcion TEXT NOT NULL,
            duracionMinutos INTEGER NOT NULL
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_plantilla_item_idPlantilla ON plantilla_item(idPlantilla)")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE shot_chart_data ADD COLUMN sesionId INTEGER NOT NULL DEFAULT 0")
        database.execSQL("""CREATE TABLE IF NOT EXISTS sesion_tiro (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idEntrenador INTEGER NOT NULL,
            idJugadora INTEGER NOT NULL,
            titulo TEXT NOT NULL,
            fecha INTEGER NOT NULL,
            notas TEXT NOT NULL
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_sesion_tiro_idEntrenador ON sesion_tiro(idEntrenador)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_sesion_tiro_idJugadora ON sesion_tiro(idJugadora)")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""CREATE TABLE IF NOT EXISTS clasificacion (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idEntrenador INTEGER NOT NULL,
            temporada TEXT NOT NULL DEFAULT '2024-25',
            equipoNombre TEXT NOT NULL,
            pj INTEGER NOT NULL DEFAULT 0,
            pg INTEGER NOT NULL DEFAULT 0,
            pp INTEGER NOT NULL DEFAULT 0,
            sf INTEGER NOT NULL DEFAULT 0,
            sc INTEGER NOT NULL DEFAULT 0,
            puntos INTEGER NOT NULL DEFAULT 0
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_clasificacion_idEntrenador ON clasificacion(idEntrenador)")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""CREATE TABLE IF NOT EXISTS convocatoria (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idPartido INTEGER NOT NULL,
            idJugadora INTEGER NOT NULL,
            idEntrenador INTEGER NOT NULL,
            esTitular INTEGER NOT NULL DEFAULT 0,
            posicionIndice INTEGER NOT NULL DEFAULT -1
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_convocatoria_idPartido ON convocatoria(idPartido)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_convocatoria_idEntrenador ON convocatoria(idEntrenador)")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop and recreate with correct schema (no SQL DEFAULT values, add indices)
        database.execSQL("DROP TABLE IF EXISTS lesion")
        database.execSQL("""CREATE TABLE IF NOT EXISTS lesion (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idJugadora INTEGER NOT NULL,
            idEntrenador INTEGER NOT NULL,
            tipo TEXT NOT NULL,
            zonaCorpo TEXT NOT NULL,
            gravedad INTEGER NOT NULL,
            fechaInicio INTEGER NOT NULL,
            fechaEstimadaVuelta INTEGER NOT NULL,
            fechaVueltaReal INTEGER NOT NULL,
            protocolo TEXT NOT NULL,
            notas TEXT NOT NULL,
            estado TEXT NOT NULL
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_lesion_idJugadora ON lesion(idJugadora)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_lesion_idEntrenador ON lesion(idEntrenador)")

        database.execSQL("DROP TABLE IF EXISTS objetivo")
        database.execSQL("""CREATE TABLE IF NOT EXISTS objetivo (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idEntrenador INTEGER NOT NULL,
            idJugadora INTEGER NOT NULL,
            titulo TEXT NOT NULL,
            descripcion TEXT NOT NULL,
            tipo TEXT NOT NULL,
            metrica TEXT NOT NULL,
            valorObjetivo REAL NOT NULL,
            valorActual REAL NOT NULL,
            unidad TEXT NOT NULL,
            fechaInicio INTEGER NOT NULL,
            fechaFin INTEGER NOT NULL,
            completado INTEGER NOT NULL,
            notas TEXT NOT NULL
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_objetivo_idEntrenador ON objetivo(idEntrenador)")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""CREATE TABLE IF NOT EXISTS lesion (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idJugadora INTEGER NOT NULL,
            idEntrenador INTEGER NOT NULL,
            tipo TEXT NOT NULL,
            zonaCorpo TEXT NOT NULL,
            gravedad INTEGER NOT NULL,
            fechaInicio INTEGER NOT NULL,
            fechaEstimadaVuelta INTEGER NOT NULL,
            fechaVueltaReal INTEGER NOT NULL,
            protocolo TEXT NOT NULL,
            notas TEXT NOT NULL,
            estado TEXT NOT NULL
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_lesion_idJugadora ON lesion(idJugadora)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_lesion_idEntrenador ON lesion(idEntrenador)")
        database.execSQL("""CREATE TABLE IF NOT EXISTS objetivo (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idEntrenador INTEGER NOT NULL,
            idJugadora INTEGER NOT NULL,
            titulo TEXT NOT NULL,
            descripcion TEXT NOT NULL,
            tipo TEXT NOT NULL,
            metrica TEXT NOT NULL,
            valorObjetivo REAL NOT NULL,
            valorActual REAL NOT NULL,
            unidad TEXT NOT NULL,
            fechaInicio INTEGER NOT NULL,
            fechaFin INTEGER NOT NULL,
            completado INTEGER NOT NULL,
            notas TEXT NOT NULL
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_objetivo_idEntrenador ON objetivo(idEntrenador)")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE historial_fisico ADD COLUMN masaGrasa REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE historial_fisico ADD COLUMN masaMuscular REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE historial_fisico ADD COLUMN envergadura REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE historial_fisico ADD COLUMN alturaSentado REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE historial_fisico ADD COLUMN perimetroBrazo REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE historial_fisico ADD COLUMN perimetroCintura REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE historial_fisico ADD COLUMN perimetroMuslo REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE historial_fisico ADD COLUMN perimetroCadera REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE historial_fisico ADD COLUMN perimetroPecho REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE historial_fisico ADD COLUMN saltoVertical REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE historial_fisico ADD COLUMN saltoHorizontal REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE historial_fisico ADD COLUMN sprint10m REAL NOT NULL DEFAULT 0.0")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE shot_chart_data ADD COLUMN idEntrenador INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop all tables added in 6→7 (wrong schema: had indices + SQL DEFAULTs not matching entities)
        database.execSQL("DROP TABLE IF EXISTS test_fisico")
        database.execSQL("DROP TABLE IF EXISTS wellness_diario")
        database.execSQL("DROP TABLE IF EXISTS asistencia")
        database.execSQL("DROP TABLE IF EXISTS skill_rating")
        database.execSQL("DROP TABLE IF EXISTS game_plan")
        database.execSQL("DROP TABLE IF EXISTS rival_perfil")
        database.execSQL("DROP TABLE IF EXISTS shot_chart_data")

        database.execSQL("""CREATE TABLE IF NOT EXISTS test_fisico (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idJugadora INTEGER NOT NULL,
            idEntrenador INTEGER NOT NULL,
            tipo TEXT NOT NULL,
            nombreCustom TEXT NOT NULL,
            valor REAL NOT NULL,
            unidad TEXT NOT NULL,
            fecha INTEGER NOT NULL,
            notas TEXT NOT NULL
        )""")
        database.execSQL("""CREATE TABLE IF NOT EXISTS wellness_diario (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idJugadora INTEGER NOT NULL,
            idEntrenador INTEGER NOT NULL,
            fecha INTEGER NOT NULL,
            fatiga INTEGER NOT NULL,
            sueno INTEGER NOT NULL,
            motivacion INTEGER NOT NULL,
            dolor INTEGER NOT NULL,
            rpe INTEGER NOT NULL,
            notas TEXT NOT NULL
        )""")
        database.execSQL("""CREATE TABLE IF NOT EXISTS asistencia (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idJugadora INTEGER NOT NULL,
            idEntrenador INTEGER NOT NULL,
            tipo TEXT NOT NULL,
            referenciaId INTEGER NOT NULL,
            estado TEXT NOT NULL,
            notas TEXT NOT NULL,
            fecha INTEGER NOT NULL
        )""")
        database.execSQL("""CREATE TABLE IF NOT EXISTS skill_rating (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idJugadora INTEGER NOT NULL,
            idEntrenador INTEGER NOT NULL,
            fecha INTEGER NOT NULL,
            tiro INTEGER NOT NULL,
            defensa INTEGER NOT NULL,
            balonMano INTEGER NOT NULL,
            vision INTEGER NOT NULL,
            atletismo INTEGER NOT NULL,
            mentalidad INTEGER NOT NULL,
            liderazgo INTEGER NOT NULL,
            notas TEXT NOT NULL
        )""")
        database.execSQL("""CREATE TABLE IF NOT EXISTS game_plan (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idEntrenador INTEGER NOT NULL,
            rival TEXT NOT NULL,
            fecha INTEGER NOT NULL,
            descripcion TEXT NOT NULL,
            focoOfensivo TEXT NOT NULL,
            focoDefensivo TEXT NOT NULL,
            jugadasClave TEXT NOT NULL,
            sistemaDefensivo TEXT NOT NULL,
            ajustesIndividuales TEXT NOT NULL,
            notasFinales TEXT NOT NULL
        )""")
        database.execSQL("""CREATE TABLE IF NOT EXISTS rival_perfil (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idEntrenador INTEGER NOT NULL,
            nombre TEXT NOT NULL,
            temporada TEXT NOT NULL,
            sistemaOfensivo TEXT NOT NULL,
            sistemaDefensivo TEXT NOT NULL,
            jugadoresClave TEXT NOT NULL,
            fortalezas TEXT NOT NULL,
            debilidades TEXT NOT NULL,
            puntosAFavor INTEGER NOT NULL,
            puntosEnContra INTEGER NOT NULL,
            victorias INTEGER NOT NULL,
            derrotas INTEGER NOT NULL,
            notas TEXT NOT NULL
        )""")
        database.execSQL("""CREATE TABLE IF NOT EXISTS shot_chart_data (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idPartido INTEGER NOT NULL,
            idJugadora INTEGER NOT NULL,
            zonaX REAL NOT NULL,
            zonaY REAL NOT NULL,
            zona TEXT NOT NULL,
            hecha INTEGER NOT NULL,
            tipo TEXT NOT NULL,
            cuarto INTEGER NOT NULL
        )""")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS test_fisico (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                idJugadora INTEGER NOT NULL,
                idEntrenador INTEGER NOT NULL,
                tipo TEXT NOT NULL,
                nombreCustom TEXT NOT NULL DEFAULT '',
                valor REAL NOT NULL,
                unidad TEXT NOT NULL,
                fecha INTEGER NOT NULL,
                notas TEXT NOT NULL DEFAULT ''
            )"""
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_test_fisico_idEntrenador ON test_fisico(idEntrenador)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_test_fisico_idJugadora ON test_fisico(idJugadora)")
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS wellness_diario (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                idJugadora INTEGER NOT NULL,
                idEntrenador INTEGER NOT NULL,
                fecha INTEGER NOT NULL,
                fatiga INTEGER NOT NULL DEFAULT 5,
                sueno INTEGER NOT NULL DEFAULT 5,
                motivacion INTEGER NOT NULL DEFAULT 5,
                dolor INTEGER NOT NULL DEFAULT 1,
                rpe INTEGER NOT NULL DEFAULT 5,
                notas TEXT NOT NULL DEFAULT ''
            )"""
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_wellness_diario_idEntrenador ON wellness_diario(idEntrenador)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_wellness_diario_idJugadora ON wellness_diario(idJugadora)")

        // Asistencia
        database.execSQL("""CREATE TABLE IF NOT EXISTS asistencia (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idJugadora INTEGER NOT NULL,
            idEntrenador INTEGER NOT NULL,
            tipo TEXT NOT NULL,
            referenciaId INTEGER NOT NULL,
            estado TEXT NOT NULL DEFAULT 'PRESENTE',
            notas TEXT NOT NULL DEFAULT '',
            fecha INTEGER NOT NULL
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_asistencia_tipo_ref ON asistencia(tipo, referenciaId)")

        // Skill rating
        database.execSQL("""CREATE TABLE IF NOT EXISTS skill_rating (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idJugadora INTEGER NOT NULL,
            idEntrenador INTEGER NOT NULL,
            fecha INTEGER NOT NULL,
            tiro INTEGER NOT NULL DEFAULT 5,
            defensa INTEGER NOT NULL DEFAULT 5,
            balonMano INTEGER NOT NULL DEFAULT 5,
            vision INTEGER NOT NULL DEFAULT 5,
            atletismo INTEGER NOT NULL DEFAULT 5,
            mentalidad INTEGER NOT NULL DEFAULT 5,
            liderazgo INTEGER NOT NULL DEFAULT 5,
            notas TEXT NOT NULL DEFAULT ''
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_skill_rating_idJugadora ON skill_rating(idJugadora)")

        // Game plan
        database.execSQL("""CREATE TABLE IF NOT EXISTS game_plan (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idEntrenador INTEGER NOT NULL,
            rival TEXT NOT NULL,
            fecha INTEGER NOT NULL,
            descripcion TEXT NOT NULL DEFAULT '',
            focoOfensivo TEXT NOT NULL DEFAULT '',
            focoDefensivo TEXT NOT NULL DEFAULT '',
            jugadasClave TEXT NOT NULL DEFAULT '',
            sistemaDefensivo TEXT NOT NULL DEFAULT '',
            ajustesIndividuales TEXT NOT NULL DEFAULT '',
            notasFinales TEXT NOT NULL DEFAULT ''
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_game_plan_idEntrenador ON game_plan(idEntrenador)")

        // Rival perfil
        database.execSQL("""CREATE TABLE IF NOT EXISTS rival_perfil (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idEntrenador INTEGER NOT NULL,
            nombre TEXT NOT NULL,
            temporada TEXT NOT NULL DEFAULT '',
            sistemaOfensivo TEXT NOT NULL DEFAULT '',
            sistemaDefensivo TEXT NOT NULL DEFAULT '',
            jugadoresClave TEXT NOT NULL DEFAULT '',
            fortalezas TEXT NOT NULL DEFAULT '',
            debilidades TEXT NOT NULL DEFAULT '',
            puntosAFavor INTEGER NOT NULL DEFAULT 0,
            puntosEnContra INTEGER NOT NULL DEFAULT 0,
            victorias INTEGER NOT NULL DEFAULT 0,
            derrotas INTEGER NOT NULL DEFAULT 0,
            notas TEXT NOT NULL DEFAULT ''
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_rival_perfil_idEntrenador ON rival_perfil(idEntrenador)")

        // Shot chart data
        database.execSQL("""CREATE TABLE IF NOT EXISTS shot_chart_data (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idPartido INTEGER NOT NULL,
            idJugadora INTEGER NOT NULL,
            zonaX REAL NOT NULL,
            zonaY REAL NOT NULL,
            zona TEXT NOT NULL,
            hecha INTEGER NOT NULL,
            tipo TEXT NOT NULL DEFAULT 'CAMPO',
            cuarto INTEGER NOT NULL DEFAULT 1
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_shot_chart_idJugadora ON shot_chart_data(idJugadora)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_shot_chart_idPartido ON shot_chart_data(idPartido)")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS ejercicios (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                entrenadorId INTEGER NOT NULL,
                nombre TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                categoria TEXT NOT NULL,
                duracionMinutos INTEGER NOT NULL,
                intensidad TEXT NOT NULL,
                materialesNecesarios TEXT NOT NULL,
                jugadoresMinimos INTEGER NOT NULL,
                notas TEXT NOT NULL,
                esFavorito INTEGER NOT NULL,
                timestampCreacion INTEGER NOT NULL
            )"""
        )
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE jugadoras ADD COLUMN condicionFisica TEXT NOT NULL DEFAULT 'DISPONIBLE'"
        )
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS partidos (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                idEntrenador INTEGER NOT NULL,
                rival TEXT NOT NULL,
                fecha INTEGER NOT NULL,
                lugar TEXT NOT NULL DEFAULT '',
                puntosPropio INTEGER NOT NULL DEFAULT 0,
                puntosRival INTEGER NOT NULL DEFAULT 0,
                jornada INTEGER NOT NULL DEFAULT 0,
                esLocal INTEGER NOT NULL DEFAULT 1,
                notas TEXT NOT NULL DEFAULT ''
            )"""
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_partidos_idEntrenador ON partidos(idEntrenador)")
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS estadisticas_partido (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                idPartido INTEGER NOT NULL,
                idJugadora INTEGER NOT NULL,
                puntos INTEGER NOT NULL DEFAULT 0,
                rebotes INTEGER NOT NULL DEFAULT 0,
                asistencias INTEGER NOT NULL DEFAULT 0,
                faltas INTEGER NOT NULL DEFAULT 0,
                minutos INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (idPartido) REFERENCES partidos(id) ON DELETE CASCADE,
                FOREIGN KEY (idJugadora) REFERENCES jugadoras(id) ON DELETE CASCADE
            )"""
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_estadisticas_partido_idPartido ON estadisticas_partido(idPartido)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_estadisticas_partido_idJugadora ON estadisticas_partido(idJugadora)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS eventos_partido (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                idPartido INTEGER NOT NULL,
                idJugadora INTEGER NOT NULL DEFAULT -1,
                tipoEvento TEXT NOT NULL,
                cuarto INTEGER NOT NULL DEFAULT 1,
                valor INTEGER NOT NULL DEFAULT 0,
                timestamp INTEGER NOT NULL
            )"""
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_eventos_partido_idPartido ON eventos_partido(idPartido)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE eventos_partido ADD COLUMN texto TEXT NOT NULL DEFAULT ''"
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""CREATE TABLE IF NOT EXISTS historial_fisico (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idJugadora INTEGER NOT NULL,
            fecha INTEGER NOT NULL,
            peso REAL NOT NULL DEFAULT 0.0,
            altura REAL NOT NULL DEFAULT 0.0,
            condicionFisica TEXT NOT NULL DEFAULT 'DISPONIBLE',
            observaciones TEXT NOT NULL DEFAULT ''
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_historial_fisico_idJugadora ON historial_fisico(idJugadora)")
        database.execSQL("""CREATE TABLE IF NOT EXISTS videos (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            idEntrenador INTEGER NOT NULL,
            titulo TEXT NOT NULL,
            url TEXT NOT NULL,
            descripcion TEXT NOT NULL DEFAULT '',
            categoria TEXT NOT NULL DEFAULT 'ANÁLISIS',
            fecha INTEGER NOT NULL
        )""")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_videos_idEntrenador ON videos(idEntrenador)")
    }
}

@Database(
    entities = [
        EntrenadorEntity::class,
        JugadoraEntity::class,
        ScoutingEntity::class,
        SesionEntrenamientoEntity::class,
        AsignacionSesionEntity::class,
        EstadisticaTirosEntity::class,
        PizarraJugadaEntity::class,
        ReporteEntity::class,
        PartidoEntity::class,
        EstadisticaPartidoEntity::class,
        EventoPartidoEntity::class,
        HistorialFisicoEntity::class,
        VideoEntity::class,
        EjercicioEntity::class,
        TestFisicoEntity::class,
        WellnessDiarioEntity::class,
        AsistenciaEntity::class,
        SkillRatingEntity::class,
        GamePlanEntity::class,
        RivalPerfilEntity::class,
        ShotChartDataEntity::class,
        LesionEntity::class,
        ObjetivoEntity::class,
        ConvocatoriaEntity::class,
        ClasificacionEntity::class,
        SesionTiroEntity::class,
        PlantillaEntrenamientoEntity::class,
        PlantillaItemEntity::class
    ],
    version = 16,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entrenadorDao(): EntrenadorDao
    abstract fun jugadoraDao(): JugadoraDao
    abstract fun scoutingDao(): ScoutingDao
    abstract fun sesionDao(): SesionDao
    abstract fun asignacionSesionDao(): AsignacionSesionDao
    abstract fun estadisticaTirosDao(): EstadisticaTirosDao
    abstract fun pizarraJugadaDao(): PizarraJugadaDao
    abstract fun reporteDao(): ReporteDao
    abstract fun partidoDao(): PartidoDao
    abstract fun eventoPartidoDao(): EventoPartidoDao
    abstract fun historialFisicoDao(): HistorialFisicoDao
    abstract fun videoDao(): VideoDao
    abstract fun ejercicioDao(): EjercicioDao
    abstract fun testFisicoDao(): TestFisicoDao
    abstract fun wellnessDiarioDao(): WellnessDiarioDao
    abstract fun asistenciaDao(): AsistenciaDao
    abstract fun skillRatingDao(): SkillRatingDao
    abstract fun gamePlanDao(): GamePlanDao
    abstract fun rivalPerfilDao(): RivalPerfilDao
    abstract fun shotChartDao(): ShotChartDao
    abstract fun lesionDao(): LesionDao
    abstract fun objetivoDao(): ObjetivoDao
    abstract fun convocatoriaDao(): ConvocatoriaDao
    abstract fun estadisticaPartidoDao(): EstadisticaPartidoDao
    abstract fun clasificacionDao(): ClasificacionDao
    abstract fun sesionTiroDao(): SesionTiroDao
    abstract fun plantillaDao(): PlantillaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tfg_aaron_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
