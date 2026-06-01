package com.example.tfg_aaron.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Jugadoras : Screen("jugadoras")
    object JugadoraDetail : Screen("jugadora_detail/{id}") {
        fun createRoute(id: Int) = "jugadora_detail/$id"
    }
    object AddEditJugadora : Screen("add_edit_jugadora?id={id}") {
        fun createRoute(id: Int? = null) = if (id != null) "add_edit_jugadora?id=$id" else "add_edit_jugadora?id=-1"
    }
    object Scouting : Screen("scouting")
    object AddEditScouting : Screen("add_edit_scouting?id={id}") {
        fun createRoute(id: Int? = null) = if (id != null) "add_edit_scouting?id=$id" else "add_edit_scouting?id=-1"
    }
    object Sesiones : Screen("sesiones")
    object AddEditSesion : Screen("add_edit_sesion?id={id}") {
        fun createRoute(id: Int? = null) = if (id != null) "add_edit_sesion?id=$id" else "add_edit_sesion?id=-1"
    }
    object Estadisticas : Screen("estadisticas")
    object Pizarra : Screen("pizarra")
    object PizarraEditor : Screen("pizarra_editor?id={id}") {
        fun createRoute(id: Int? = null) = if (id != null) "pizarra_editor?id=$id" else "pizarra_editor?id=-1"
    }
    object Reportes : Screen("reportes")
    object Perfil : Screen("perfil")
    object Partidos : Screen("partidos")
    object AddEditPartido : Screen("add_edit_partido?id={id}") {
        fun createRoute(id: Int? = null) = if (id != null) "add_edit_partido?id=$id" else "add_edit_partido?id=-1"
    }
    object PartidoEnVivo : Screen("partido_en_vivo/{idPartido}") {
        fun createRoute(idPartido: Int) = "partido_en_vivo/$idPartido"
    }
    object HistorialFisico : Screen("historial_fisico/{jugadoraId}") {
        fun createRoute(jugadoraId: Int) = "historial_fisico/$jugadoraId"
    }
    object Videos : Screen("videos")
    object Calendario : Screen("calendario")
    object Ejercicios : Screen("ejercicios")
    object AddEditEjercicio : Screen("add_edit_ejercicio?id={id}") {
        fun createRoute(id: Int? = null) = if (id != null) "add_edit_ejercicio?id=$id" else "add_edit_ejercicio?id=-1"
    }
    // Tests físicos
    object TestFisico : Screen("test_fisico/{jugadoraId}") {
        fun createRoute(jugadoraId: Int = -1) = "test_fisico/$jugadoraId"
        fun createRouteEquipo() = "test_fisico/-1"
    }
    // Wellness & RPE
    object Wellness : Screen("wellness")
    // Asistencia
    object Asistencia : Screen("asistencia/{tipo}/{referenciaId}/{fecha}") {
        fun createRoute(tipo: String, referenciaId: Int, titulo: String, fecha: Long) =
            "asistencia/$tipo/$referenciaId/$fecha"
    }
    // Skill Rating
    object SkillRating : Screen("skill_rating")
    // Shot Chart
    object ShotChart : Screen("shot_chart")
    // Estadísticas Avanzadas
    object EstadisticasAvanzadas : Screen("estadisticas_avanzadas")
    // Game Plan
    object GamePlan : Screen("game_plan")
    // Rival Perfil
    object RivalPerfil : Screen("rival_perfil")
    // Clasificación
    object Clasificacion : Screen("clasificacion")
    // Lesiones
    object Lesiones : Screen("lesiones")
    // Objetivos
    object Objetivos : Screen("objetivos")
    // Stats por jugadora por partido
    object EstadisticasPartido : Screen("estadisticas_partido/{idPartido}") {
        fun createRoute(idPartido: Int) = "estadisticas_partido/$idPartido"
    }
    // Alineación / Convocatoria
    object Alineacion : Screen("alineacion/{idPartido}") {
        fun createRoute(idPartido: Int) = "alineacion/$idPartido"
    }
    // Búsqueda Global
    object Busqueda : Screen("busqueda")
    // Comparador de jugadoras
    object Comparador : Screen("comparador")
}
