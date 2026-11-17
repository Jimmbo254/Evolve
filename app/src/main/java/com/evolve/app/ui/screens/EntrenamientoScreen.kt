package com.evolve.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.evolve.app.data.EvolveRepository
import com.evolve.app.ui.components.ErrorBox
import com.evolve.app.ui.components.EvolveCard
import com.evolve.app.ui.components.ExerciseImage
import com.evolve.app.ui.components.LoadingBox
import com.evolve.app.ui.components.PrimaryButton
import com.evolve.app.ui.components.Remote
import com.evolve.app.ui.components.SectionTitle
import com.evolve.app.ui.components.kgText
import com.evolve.app.ui.components.rememberRemote
import com.evolve.app.ui.components.userMessage
import com.evolve.app.ui.targetLabel
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.SteelGray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun EntrenamientoScreen(repo: EvolveRepository, version: Int, onOpenWorkout: (String) -> Unit) {
    val (state, retry) = rememberRemote(version) { repo.today() }
    var starting by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun startWorkout(planDayId: String?) {
        if (starting) return
        scope.launch {
            starting = true
            actionError = null
            try {
                onOpenWorkout(repo.startOrResumeWorkout(planDayId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                actionError = userMessage(e)
            } finally {
                starting = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Entrenamiento", style = MaterialTheme.typography.headlineLarge)
            Text("Rutina de hoy", style = MaterialTheme.typography.bodyMedium, color = SteelGray)
        }

        when (state) {
            is Remote.Loading -> item { LoadingBox() }
            is Remote.Error -> item { ErrorBox(state.message, retry) }
            is Remote.Ready -> {
                val today = state.value
                if (actionError != null) {
                    item {
                        Text(
                            actionError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (today == null) {
                    item {
                        EvolveCard {
                            Text(
                                "HOY NO TOCA PLAN",
                                style = MaterialTheme.typography.labelMedium,
                                color = EvolutionLime
                            )
                            Text("Entrenamiento libre", style = MaterialTheme.typography.headlineMedium)
                            Text(
                                "Tu plan activo no tiene un día para hoy. Podés entrenar igual y registrar tus series.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SteelGray
                            )
                            Spacer(Modifier.height(12.dp))
                            PrimaryButton(
                                text = if (starting) "Iniciando…" else "Empezar entrenamiento",
                                onClick = { startWorkout(null) },
                                enabled = !starting
                            )
                        }
                    }
                } else {
                    item {
                        EvolveCard {
                            Text(
                                today.planName.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = EvolutionLime
                            )
                            Text(today.dayName.uppercase(), style = MaterialTheme.typography.headlineMedium)
                            Text(
                                "${today.exercises.size} ejercicios",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SteelGray
                            )
                            Spacer(Modifier.height(12.dp))
                            PrimaryButton(
                                text = when {
                                    today.workoutId != null -> "Reanudar entrenamiento"
                                    starting -> "Iniciando…"
                                    else -> "Iniciar entrenamiento"
                                },
                                onClick = {
                                    val openId = today.workoutId
                                    if (openId != null) onOpenWorkout(openId)
                                    else startWorkout(today.dayId)
                                },
                                enabled = !starting
                            )
                        }
                    }

                    item { SectionTitle("Ejercicios") }
                    items(today.exercises, key = { it.id }) { todayExercise ->
                        EvolveCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ExerciseImage(imageUrl = todayExercise.exercise.imageUrl)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        todayExercise.exercise.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        targetLabel(todayExercise.exercise.target),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SteelGray
                                    )
                                    Text(
                                        "${todayExercise.sets} series × ${todayExercise.reps} reps",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EvolutionLime
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "ÚLTIMO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SteelGray
                                    )
                                    Text(
                                        todayExercise.lastWeightKg
                                            ?.let { "${it.kgText()} kg" } ?: "—",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    }

                    val attribution = today.exercises.firstOrNull()?.exercise?.attribution
                    if (!attribution.isNullOrBlank()) {
                        item {
                            Text(
                                attribution,
                                style = MaterialTheme.typography.labelSmall,
                                color = SteelGray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
