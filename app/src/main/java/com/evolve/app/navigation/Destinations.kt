package com.evolve.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector

/** Las 4 pestañas del boceto: Progreso, Entrenamiento, Planes y Perfil. */
enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    Progreso("progreso", "Progreso", Icons.Rounded.BarChart),
    Entrenamiento("entrenamiento", "Entrenamiento", Icons.Rounded.FitnessCenter),
    Planes("planes", "Planes", Icons.Rounded.CalendarMonth),
    Perfil("perfil", "Perfil", Icons.Rounded.Person)
}
