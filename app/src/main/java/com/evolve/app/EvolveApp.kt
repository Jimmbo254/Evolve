package com.evolve.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.evolve.app.data.AppContainer
import com.evolve.app.data.SessionState
import com.evolve.app.navigation.Destination
import com.evolve.app.ui.screens.AuthScreen
import com.evolve.app.ui.screens.EjerciciosScreen
import com.evolve.app.ui.screens.EntrenamientoScreen
import com.evolve.app.ui.screens.PerfilScreen
import com.evolve.app.ui.screens.PlanEditorScreen
import com.evolve.app.ui.screens.PlanesScreen
import com.evolve.app.ui.screens.ProgresoScreen
import com.evolve.app.ui.screens.SplashScreen
import com.evolve.app.ui.screens.WorkoutScreen
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.ForestCharcoal
import com.evolve.app.ui.theme.ObsidianBlack
import com.evolve.app.ui.theme.SteelGray

private const val PLAN_EDITOR = "plan-editor?planId={planId}"
private const val WORKOUT = "workout/{workoutId}"

@Composable
fun EvolveApp(container: AppContainer) {
    val session = container.session
    val state by session.state.collectAsState()

    LaunchedEffect(Unit) { session.bootstrap() }

    when (state) {
        SessionState.Loading -> SplashScreen(onFinished = {})
        SessionState.LoggedOut -> AuthScreen(session)
        SessionState.LoggedIn -> MainNavigation(container)
    }
}

@Composable
private fun MainNavigation(container: AppContainer) {
    val repo = container.repository
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val isTab = Destination.entries.any { it.route == route }

    // Fuerza recarga de las pestañas cuando se vuelve de editar un plan o entrenar.
    var dataVersion by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = ObsidianBlack,
        floatingActionButton = {
            if (route == Destination.Planes.route) {
                FloatingActionButton(
                    onClick = { nav.navigate("plan-editor") },
                    containerColor = EvolutionLime,
                    contentColor = ObsidianBlack
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Nuevo plan")
                }
            }
        },
        bottomBar = {
            if (isTab) {
                NavigationBar(containerColor = ForestCharcoal) {
                    Destination.entries.forEach { dest ->
                        NavigationBarItem(
                            selected = route == dest.route,
                            onClick = {
                                if (route != dest.route) {
                                    nav.navigate(dest.route) {
                                        popUpTo(nav.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EvolutionLime,
                                selectedTextColor = EvolutionLime,
                                unselectedIconColor = SteelGray,
                                unselectedTextColor = SteelGray,
                                indicatorColor = ObsidianBlack
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Destination.Progreso.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destination.Progreso.route) { ProgresoScreen(repo) }
            composable(Destination.Entrenamiento.route) {
                EntrenamientoScreen(
                    repo = repo,
                    version = dataVersion,
                    onOpenWorkout = { workoutId -> nav.navigate("workout/$workoutId") }
                )
            }
            composable(Destination.Planes.route) {
                PlanesScreen(
                    repo = repo,
                    version = dataVersion,
                    onCreate = { nav.navigate("plan-editor") },
                    onEdit = { planId -> nav.navigate("plan-editor?planId=$planId") }
                )
            }
            composable(Destination.Ejercicios.route) { EjerciciosScreen(repo) }
            composable(Destination.Perfil.route) { PerfilScreen(repo, container.session) }

            composable(
                route = PLAN_EDITOR,
                arguments = listOf(
                    navArgument("planId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                PlanEditorScreen(
                    repo = repo,
                    planId = entry.arguments?.getString("planId"),
                    onBack = { nav.popBackStack() },
                    onSaved = {
                        dataVersion++
                        nav.popBackStack()
                    }
                )
            }

            composable(
                route = WORKOUT,
                arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
            ) { entry ->
                WorkoutScreen(
                    repo = repo,
                    workoutId = entry.arguments?.getString("workoutId").orEmpty(),
                    onBack = { nav.popBackStack() },
                    onFinished = {
                        dataVersion++
                        nav.popBackStack()
                    }
                )
            }
        }
    }
}
