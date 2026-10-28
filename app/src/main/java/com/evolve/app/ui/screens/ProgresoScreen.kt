package com.evolve.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.evolve.app.data.ExerciseRepository
import com.evolve.app.ui.components.EvolveCard
import com.evolve.app.ui.components.SectionTitle
import com.evolve.app.ui.components.StatCard
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.ObsidianBlack
import com.evolve.app.ui.theme.PerformanceCoral
import com.evolve.app.ui.theme.SteelGray
import com.evolve.app.ui.theme.ForestCharcoal

@Composable
fun ProgresoScreen(repo: ExerciseRepository) {
    val volume = repo.weeklyVolume()
    val records = repo.records()
    val total = volume.sumOf { it.kg }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Tu progreso", style = MaterialTheme.typography.headlineLarge)
        Text("Esta semana vas muy bien. Sigue sumando.", style = MaterialTheme.typography.bodyMedium, color = SteelGray)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("RACHA", "5 días", Modifier.weight(1f), EvolutionLime)
            StatCard("ENTRENOS", "4 / 4", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("VOLUMEN SEMANAL", "%,d kg".format(total), Modifier.weight(1f))
            StatCard("NUEVOS PR", "${records.size}", Modifier.weight(1f), PerformanceCoral)
        }

        SectionTitle("Volumen por día")
        EvolveCard {
            val max = (volume.maxOf { it.kg }).coerceAtLeast(1)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val barWidth = size.width / (volume.size * 2f)
                volume.forEachIndexed { i, v ->
                    val h = size.height * (v.kg / max.toFloat()).coerceAtLeast(0.03f)
                    val x = barWidth * (i * 2 + 0.5f)
                    drawRoundRect(
                        color = if (v.kg == 0) ObsidianBlack else EvolutionLime,
                        topLeft = Offset(x, size.height - h),
                        size = Size(barWidth, h),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                volume.forEach {
                    Text(it.day, style = MaterialTheme.typography.labelMedium, color = SteelGray)
                }
            }
        }

        SectionTitle("Récords personales")
        records.forEachIndexed { index, pr ->
            EvolveCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(pr.exercise, style = MaterialTheme.typography.titleMedium)
                        Text(pr.date, style = MaterialTheme.typography.bodySmall, color = SteelGray)
                    }
                    Text(
                        "${pr.weightKg.toInt()} kg × ${pr.reps}",
                        style = MaterialTheme.typography.titleLarge,
                        // El coral solo se usa para el récord más reciente
                        color = if (index == 0) PerformanceCoral else EvolutionLime
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
