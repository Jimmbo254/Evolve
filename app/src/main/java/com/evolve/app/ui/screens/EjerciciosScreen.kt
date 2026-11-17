package com.evolve.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.evolve.app.data.EvolveRepository
import com.evolve.app.ui.components.ExerciseBrowser
import com.evolve.app.ui.theme.SteelGray

/** Catálogo visual agrupado por grupo muscular (chips de `target`). */
@Composable
fun EjerciciosScreen(repo: EvolveRepository) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
    ) {
        Text("Ejercicios", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Elegí un grupo muscular y mirá cómo se ejecuta",
            style = MaterialTheme.typography.bodyMedium,
            color = SteelGray
        )
        Spacer(Modifier.height(10.dp))
        ExerciseBrowser(repo = repo, modifier = Modifier.weight(1f))
    }
}
