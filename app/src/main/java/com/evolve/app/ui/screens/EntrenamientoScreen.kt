package com.evolve.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.evolve.app.data.ExerciseRepository
import com.evolve.app.ui.components.EvolveCard
import com.evolve.app.ui.components.ExerciseImage
import com.evolve.app.ui.components.PrimaryButton
import com.evolve.app.ui.components.SectionTitle
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.SteelGray

@Composable
fun EntrenamientoScreen(repo: ExerciseRepository) {
    val exercises = repo.todayExercises()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Entrenamiento", style = MaterialTheme.typography.headlineLarge)
            Text("Rutina de hoy", style = MaterialTheme.typography.bodyMedium, color = SteelGray)
        }
        item {
            EvolveCard {
                Text("PUSH · PECHO, HOMBRO Y TRÍCEPS", style = MaterialTheme.typography.labelMedium, color = EvolutionLime)
                Text("EMPUJE A", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "${exercises.size} ejercicios · ~65 min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SteelGray
                )
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Iniciar entrenamiento", onClick = { /* TODO: sesión de entrenamiento */ })
            }
        }
        item { SectionTitle("Ejercicios") }
        items(exercises, key = { it.id }) { ex ->
            EvolveCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExerciseImage(imageUrl = ex.imageUrl)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(ex.name, style = MaterialTheme.typography.titleMedium)
                        Text(ex.muscle, style = MaterialTheme.typography.bodySmall, color = SteelGray)
                        Text(
                            "${ex.sets} series × ${ex.reps} reps",
                            style = MaterialTheme.typography.labelMedium,
                            color = EvolutionLime
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ÚLTIMO", style = MaterialTheme.typography.labelSmall, color = SteelGray)
                        Text(
                            ex.lastWeightKg?.let { "${it.toInt()} kg" } ?: "—",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
