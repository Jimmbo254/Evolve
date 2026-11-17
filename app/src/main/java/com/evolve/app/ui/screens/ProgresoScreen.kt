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
import com.evolve.app.data.EvolveRepository
import com.evolve.app.data.api.ProgressSummary
import com.evolve.app.data.api.RecordItem
import com.evolve.app.data.api.VolumeDay
import com.evolve.app.ui.components.EmptyMessage
import com.evolve.app.ui.components.ErrorBox
import com.evolve.app.ui.components.EvolveCard
import com.evolve.app.ui.components.LoadingBox
import com.evolve.app.ui.components.Remote
import com.evolve.app.ui.components.SectionTitle
import com.evolve.app.ui.components.StatCard
import com.evolve.app.ui.components.kgText
import com.evolve.app.ui.components.rememberRemote
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.ForestCharcoal
import com.evolve.app.ui.theme.ObsidianBlack
import com.evolve.app.ui.theme.PerformanceCoral
import com.evolve.app.ui.theme.SteelGray
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

private data class ProgressData(
    val summary: ProgressSummary,
    val volume: List<VolumeDay>,
    val records: List<RecordItem>
)

@Composable
fun ProgresoScreen(repo: EvolveRepository) {
    val (state, retry) = rememberRemote(Unit) {
        coroutineScope {
            val summary = async { repo.progressSummary() }
            val volume = async { repo.weeklyVolume() }
            val records = async { repo.records() }
            ProgressData(summary.await(), volume.await(), records.await())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Tu progreso", style = MaterialTheme.typography.headlineLarge)

        when (state) {
            is Remote.Loading -> LoadingBox()
            is Remote.Error -> ErrorBox(state.message, retry)
            is Remote.Ready -> {
                Text(
                    "Así viene tu semana. Seguí sumando.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SteelGray
                )
                val data = state.value

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        "RACHA",
                        "${data.summary.streakDays} días",
                        Modifier.weight(1f),
                        EvolutionLime
                    )
                    StatCard(
                        "ENTRENOS",
                        "${data.summary.workoutsThisWeek}",
                        Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        "VOLUMEN SEMANAL",
                        "%,d kg".format(data.summary.weeklyVolumeKg),
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "NUEVOS PR",
                        "${data.summary.newPrs}",
                        Modifier.weight(1f),
                        PerformanceCoral
                    )
                }

                SectionTitle("Volumen por día")
                EvolveCard {
                    if (data.volume.isEmpty()) {
                        EmptyMessage("Todavía no hay series registradas.")
                    } else {
                        val max = (data.volume.maxOfOrNull { it.kg } ?: 0).coerceAtLeast(1)
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            val barWidth = size.width / (data.volume.size * 2f)
                            data.volume.forEachIndexed { i, day ->
                                val h = size.height *
                                    (day.kg / max.toFloat()).coerceAtLeast(0.03f)
                                val x = barWidth * (i * 2 + 0.5f)
                                drawRoundRect(
                                    color = if (day.kg == 0) ObsidianBlack else EvolutionLime,
                                    topLeft = Offset(x, size.height - h),
                                    size = Size(barWidth, h),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            data.volume.forEach {
                                Text(
                                    it.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SteelGray
                                )
                            }
                        }
                    }
                }

                SectionTitle("Récords personales")
                if (data.records.isEmpty()) {
                    EmptyMessage("Todavía no hay récords. Se calculan al finalizar un entrenamiento.")
                } else {
                    data.records.forEachIndexed { index, record ->
                        EvolveCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        record.exerciseName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        record.date,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SteelGray
                                    )
                                }
                                Text(
                                    "${record.weightKg.kgText()} kg × ${record.reps}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (index == 0) PerformanceCoral else EvolutionLime
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
