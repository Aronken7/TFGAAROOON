package com.example.tfg_aaron.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tfg_aaron.ui.components.CoachNavigationRail
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.ui.screens.auth.LoginScreen
import com.example.tfg_aaron.ui.screens.auth.RegisterScreen
import com.example.tfg_aaron.ui.screens.calendario.CalendarioScreen
import com.example.tfg_aaron.ui.screens.dashboard.DashboardScreen
import com.example.tfg_aaron.ui.screens.estadisticas.EstadisticasScreen
import com.example.tfg_aaron.ui.screens.historialfisico.HistorialFisicoScreen
import com.example.tfg_aaron.ui.screens.jugadoras.AddEditJugadoraScreen
import com.example.tfg_aaron.ui.screens.jugadoras.JugadoraDetailScreen
import com.example.tfg_aaron.ui.screens.jugadoras.JugadorasScreen
import com.example.tfg_aaron.ui.screens.partidos.AddEditPartidoScreen
import com.example.tfg_aaron.ui.screens.partidos.PartidoEnVivoScreen
import com.example.tfg_aaron.ui.screens.partidos.PartidosScreen
import com.example.tfg_aaron.ui.screens.perfil.PerfilScreen
import com.example.tfg_aaron.ui.screens.pizarra.PizarraEditorScreen
import com.example.tfg_aaron.ui.screens.pizarra.PizarraScreen
import com.example.tfg_aaron.ui.screens.reportes.ReportesScreen
import com.example.tfg_aaron.ui.screens.scouting.AddEditScoutingScreen
import com.example.tfg_aaron.ui.screens.scouting.ScoutingScreen
import com.example.tfg_aaron.ui.screens.sesiones.AddEditSesionScreen
import com.example.tfg_aaron.ui.screens.sesiones.SesionesScreen
import com.example.tfg_aaron.ui.screens.ejercicios.AddEditEjercicioScreen
import com.example.tfg_aaron.ui.screens.ejercicios.EjerciciosScreen
import com.example.tfg_aaron.ui.screens.video.VideoScreen
import com.example.tfg_aaron.ui.screens.testfisico.TestFisicoScreen
import com.example.tfg_aaron.ui.screens.wellness.WellnessScreen
import com.example.tfg_aaron.ui.screens.asistencia.AsistenciaScreen
import com.example.tfg_aaron.ui.screens.shotchart.ShotChartScreen
import com.example.tfg_aaron.ui.screens.estadisticasavanzadas.EstadisticasAvanzadasScreen
import com.example.tfg_aaron.ui.screens.gameplan.GamePlanScreen
import com.example.tfg_aaron.ui.screens.rivalprofile.RivalPerfilScreen
import com.example.tfg_aaron.ui.screens.lesion.LesionScreen
import com.example.tfg_aaron.ui.screens.objetivo.ObjetivoScreen
import com.example.tfg_aaron.ui.screens.estadisticaspartido.EstadisticasPartidoScreen
import com.example.tfg_aaron.ui.screens.alineacion.AlineacionScreen
import com.example.tfg_aaron.ui.screens.clasificacion.ClasificacionScreen
import com.example.tfg_aaron.ui.screens.busqueda.BusquedaScreen
import com.example.tfg_aaron.ui.screens.comparador.ComparadorScreen
import com.example.tfg_aaron.ui.theme.NavyDark
import com.example.tfg_aaron.ui.theme.OrangeBase

// -2 = todavía cargando DataStore, -1 = no hay sesión, >0 = sesión activa
private const val LOADING = -2
private const val NO_SESSION = -1

@Composable
fun AppNavGraph(app: TFGApplication) {
    val navController = rememberNavController()
    val isTablet = LocalIsTablet.current

    // initial = LOADING para distinguir "cargando" de "sin sesión"
    val entrenadorId by app.userPreferences.entrenadorId.collectAsState(initial = LOADING)
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    if (entrenadorId == LOADING) {
        Box(
            modifier = Modifier.fillMaxSize().background(NavyDark),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangeBase)
        }
        return
    }

    val startDestination = if (entrenadorId > 0) Screen.Dashboard.route else Screen.Login.route
    val id = entrenadorId

    val isAuthRoute = currentRoute == Screen.Login.route || currentRoute == Screen.Register.route
    val showRail = isTablet && !isAuthRoute && currentRoute != null

    if (isTablet) {
        Row(modifier = Modifier.fillMaxSize().background(NavyDark)) {
            if (showRail) {
                CoachNavigationRail(navController)
            }
            Box(modifier = Modifier.weight(1f)) {
                PlayVisionNavHost(navController, startDestination, id)
            }
        }
    } else {
        PlayVisionNavHost(navController, startDestination, id)
    }
}

@Composable
private fun PlayVisionNavHost(
    navController: NavHostController,
    startDestination: String,
    id: Int
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize().background(NavyDark),
        enterTransition = {
            slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { it / 3 } +
            fadeIn(tween(260))
        },
        exitTransition = {
            slideOutHorizontally(tween(220, easing = FastOutSlowInEasing)) { -it / 3 } +
            fadeOut(tween(180))
        },
        popEnterTransition = {
            slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { -it / 3 } +
            fadeIn(tween(260))
        },
        popExitTransition = {
            slideOutHorizontally(tween(220, easing = FastOutSlowInEasing)) { it / 3 } +
            fadeOut(tween(180))
        }
    ) {
        // Auth
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }

        // Dashboard
        composable(Screen.Dashboard.route) { DashboardScreen(navController, id) }

        // Jugadoras
        composable(Screen.Jugadoras.route) { JugadorasScreen(navController, id) }
        composable(
            route = Screen.JugadoraDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { back ->
            JugadoraDetailScreen(navController, back.arguments?.getInt("id") ?: -1, id)
        }
        composable(
            route = Screen.AddEditJugadora.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { back ->
            AddEditJugadoraScreen(navController, id, back.arguments?.getInt("id") ?: -1)
        }

        // Scouting
        composable(Screen.Scouting.route) { ScoutingScreen(navController, id) }
        composable(
            route = Screen.AddEditScouting.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { AddEditScoutingScreen(navController, id) }

        // Sesiones
        composable(Screen.Sesiones.route) { SesionesScreen(navController, id) }
        composable(
            route = Screen.AddEditSesion.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { AddEditSesionScreen(navController, id) }

        // Estadísticas
        composable(Screen.Estadisticas.route) { EstadisticasScreen(navController, id) }

        // Pizarra
        composable(Screen.Pizarra.route) { PizarraScreen(navController, id) }
        composable(
            route = Screen.PizarraEditor.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { back ->
            PizarraEditorScreen(navController, id, back.arguments?.getInt("id") ?: -1)
        }

        // Reportes
        composable(Screen.Reportes.route) { ReportesScreen(navController, id) }

        // Perfil
        composable(Screen.Perfil.route) { PerfilScreen(navController, id) }

        // Partidos
        composable(Screen.Partidos.route) { PartidosScreen(navController, id) }
        composable(
            route = Screen.AddEditPartido.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { back ->
            AddEditPartidoScreen(navController, id, back.arguments?.getInt("id") ?: -1)
        }
        composable(
            route = Screen.PartidoEnVivo.route,
            arguments = listOf(navArgument("idPartido") { type = NavType.IntType })
        ) { back ->
            PartidoEnVivoScreen(navController, id, back.arguments?.getInt("idPartido") ?: -1)
        }

        // Historial Físico
        composable(
            route = Screen.HistorialFisico.route,
            arguments = listOf(navArgument("jugadoraId") { type = NavType.IntType })
        ) { back ->
            HistorialFisicoScreen(navController, back.arguments?.getInt("jugadoraId") ?: -1, id)
        }

        // Videos
        composable(Screen.Videos.route) { VideoScreen(navController, id) }

        // Calendario
        composable(Screen.Calendario.route) { CalendarioScreen(navController, id) }

        // Ejercicios
        composable(Screen.Ejercicios.route) { EjerciciosScreen(navController, id) }
        composable(
            route = Screen.AddEditEjercicio.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { back ->
            AddEditEjercicioScreen(navController, id, back.arguments?.getInt("id") ?: -1)
        }

        // Tests Físicos
        composable(
            route = Screen.TestFisico.route,
            arguments = listOf(navArgument("jugadoraId") { type = NavType.IntType; defaultValue = -1 })
        ) { back ->
            TestFisicoScreen(navController, id, back.arguments?.getInt("jugadoraId") ?: -1)
        }

        // Wellness & RPE
        composable(Screen.Wellness.route) { WellnessScreen(navController, id) }

        // Asistencia
        composable(
            route = Screen.Asistencia.route,
            arguments = listOf(
                navArgument("tipo") { type = NavType.StringType },
                navArgument("referenciaId") { type = NavType.IntType },
                navArgument("fecha") { type = NavType.LongType }
            )
        ) { back ->
            AsistenciaScreen(
                navController = navController,
                entrenadorId = id,
                tipo = back.arguments?.getString("tipo") ?: "SESION",
                referenciaId = back.arguments?.getInt("referenciaId") ?: -1,
                titulo = if ((back.arguments?.getString("tipo") ?: "") == "PARTIDO") "Partido" else "Sesión",
                fecha = back.arguments?.getLong("fecha") ?: System.currentTimeMillis()
            )
        }


        // Shot Chart
        composable(Screen.ShotChart.route) { ShotChartScreen(navController, id) }

        // Estadísticas Avanzadas
        composable(Screen.EstadisticasAvanzadas.route) { EstadisticasAvanzadasScreen(navController, id) }

        // Game Plan
        composable(Screen.GamePlan.route) { GamePlanScreen(navController, id) }

        // Rival Perfil
        composable(Screen.RivalPerfil.route) { RivalPerfilScreen(navController, id) }

        // Lesiones
        composable(Screen.Lesiones.route) { LesionScreen(navController, id) }

        // Objetivos
        composable(Screen.Objetivos.route) { ObjetivoScreen(navController, id) }

        // Stats por jugadora por partido
        composable(
            route = Screen.EstadisticasPartido.route,
            arguments = listOf(navArgument("idPartido") { type = NavType.IntType })
        ) { back ->
            EstadisticasPartidoScreen(navController, id, back.arguments?.getInt("idPartido") ?: -1)
        }

        // Alineación / Convocatoria
        composable(
            route = Screen.Alineacion.route,
            arguments = listOf(navArgument("idPartido") { type = NavType.IntType })
        ) { back ->
            AlineacionScreen(navController, id, back.arguments?.getInt("idPartido") ?: -1)
        }

        // Clasificación
        composable(Screen.Clasificacion.route) { ClasificacionScreen(navController, id) }

        // Búsqueda Global
        composable(Screen.Busqueda.route) { BusquedaScreen(navController, id) }

        // Comparador de jugadoras
        composable(Screen.Comparador.route) { ComparadorScreen(navController, id) }

    }
}
