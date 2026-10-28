package com.evolve.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.evolve.app.data.ExerciseRepository
import com.evolve.app.data.SampleExerciseRepository
import com.evolve.app.navigation.Destination
import com.evolve.app.ui.screens.EntrenamientoScreen
import com.evolve.app.ui.screens.PerfilScreen
import com.evolve.app.ui.screens.PlanesScreen
import com.evolve.app.ui.screens.ProgresoScreen
import com.evolve.app.ui.screens.SplashScreen
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.ForestCharcoal
import com.evolve.app.ui.theme.ObsidianBlack
import com.evolve.app.ui.theme.SteelGray

@Composable
fun EvolveApp() {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    // Cuando exista la API, cambia esta línea por ApiExerciseRepository()
    val repo: ExerciseRepository = remember { SampleExerciseRepository() }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        containerColor = ObsidianBlack,
        floatingActionButton = {
            // Botón flotante del boceto: aparece en Entrenamiento y Planes
            when (currentRoute) {
                Destination.Entrenamiento.route -> FloatingActionButton(
                    onClick = { /* TODO: iniciar sesión rápida */ },
                    containerColor = EvolutionLime,
                    contentColor = ObsidianBlack
                ) { Icon(Icons.Rounded.PlayArrow, contentDescription = "Iniciar") }

                Destination.Planes.route -> FloatingActionButton(
                    onClick = { /* TODO: crear plan */ },
                    containerColor = EvolutionLime,
                    contentColor = ObsidianBlack
                ) { Icon(Icons.Rounded.Add, contentDescription = "Nuevo plan") }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = ForestCharcoal) {
                Destination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
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
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Progreso.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destination.Progreso.route) { ProgresoScreen(repo) }
            composable(Destination.Entrenamiento.route) { EntrenamientoScreen(repo) }
            composable(Destination.Planes.route) { PlanesScreen(repo) }
            composable(Destination.Perfil.route) { PerfilScreen(repo) }
        }
    }
}
