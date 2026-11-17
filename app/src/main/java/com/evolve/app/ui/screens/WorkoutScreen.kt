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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.evolve.app.data.EvolveRepository
import com.evolve.app.data.api.ExerciseSummary
import com.evolve.app.data.api.TodayExercise
import com.evolve.app.data.api.TodayWorkout
import com.evolve.app.data.api.WorkoutDetail
import com.evolve.app.data.api.WorkoutSet
import com.evolve.app.ui.components.ErrorBox
import com.evolve.app.ui.components.EvolveCard
import com.evolve.app.ui.components.ExerciseImage
import com.evolve.app.ui.components.ExercisePickerDialog
import com.evolve.app.ui.components.LoadingBox
import com.evolve.app.ui.components.PrimaryButton
import com.evolve.app.ui.components.Remote
import com.evolve.app.ui.components.kgText
import com.evolve.app.ui.components.rememberRemote
import com.evolve.app.ui.components.userMessage
import com.evolve.app.ui.targetLabel
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.ForestCharcoal
import com.evolve.app.ui.theme.ObsidianBlack
import com.evolve.app.ui.theme.SteelGray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private data class WorkoutData(val workout: WorkoutDetail, val today: TodayWorkout?)

/** Registro de series de un entrenamiento abierto, con finalización. */
@Composable
fun WorkoutScreen(
    repo: EvolveRepository,
    workoutId: String,
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    var reload by remember { mutableIntStateOf(0) }
    val (state, retry) = rememberRemote(workoutId, reload) {
        coroutineScope {
            val workout = async { repo.workout(workoutId) }
            // El plan de hoy solo sirve para precargar los ejercicios planificados.
            val today = async {
                try {
                    repo.today()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Throwable) {
                    null
                }
            }
            WorkoutData(workout.await(), today.await())
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver")
            }
            Text("Entrenamiento", style = MaterialTheme.typography.titleLarge)
        }

        when (state) {
            is Remote.Loading -> LoadingBox()
            is Remote.Error -> ErrorBox(state.message, retry)
            is Remote.Ready -> WorkoutContent(
                repo = repo,
                workoutId = workoutId,
                data = state.value,
                onFinished = onFinished
            )
        }
    }
}

@Composable
private fun WorkoutContent(
    repo: EvolveRepository,
    workoutId: String,
    data: WorkoutData,
    onFinished: () -> Unit
) {
    val logged = remember(data) {
        mutableStateListOf<WorkoutSet>().apply { addAll(data.workout.sets) }
    }
    val extras = remember(data) { mutableStateListOf<ExerciseSummary>() }
    var picker by remember { mutableStateOf(false) }
    var logging by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmFinish by remember { mutableStateOf(false) }
    var finishing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val finished = data.workout.finishedAt != null

    val planned = data.today
        ?.takeIf { it.dayId == data.workout.planDayId }
        ?.exercises
        .orEmpty()

    val rows: List<Pair<ExerciseSummary, TodayExercise?>> = buildList {
        planned.forEach { add(it.exercise to it) }
        (logged.map { it.exercise } + extras).forEach { exercise ->
            if (none { it.first.id == exercise.id }) add(exercise to null)
        }
    }

    fun log(exerciseId: String, setNumber: Int, weightKg: Double, reps: Int) {
        if (logging || finished) return
        scope.launch {
            logging = true
            error = null
            try {
                val saved = repo.addSet(workoutId, exerciseId, setNumber, weightKg, reps)
                logged.removeAll {
                    it.exercise.id == saved.exercise.id && it.setNumber == saved.setNumber
                }
                logged.add(saved)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                error = userMessage(e)
            } finally {
                logging = false
            }
        }
    }

    fun finish() {
        if (finishing) return
        scope.launch {
            finishing = true
            error = null
            try {
                repo.finishWorkout(workoutId)
                onFinished()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                error = userMessage(e)
            } finally {
                finishing = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                if (finished) "Este entrenamiento ya está cerrado." else "Registrá cada serie que completes.",
                style = MaterialTheme.typography.bodyMedium,
                color = SteelGray
            )
        }

        if (error != null) {
            item {
                Text(
                    error!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (rows.isEmpty()) {
            item {
                EvolveCard {
                    Text("No hay ejercicios planificados.", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Agregá un ejercicio para registrar series.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SteelGray
                    )
                }
            }
        }

        items(rows, key = { it.first.id }) { (exercise, plannedExercise) ->
            val sets = logged
                .filter { it.exercise.id == exercise.id }
                .sortedBy { it.setNumber }
            val nextSet = (sets.maxOfOrNull { it.setNumber } ?: 0) + 1

            EvolveCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExerciseImage(imageUrl = exercise.imageUrl, size = 48.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            targetLabel(exercise.target),
                            style = MaterialTheme.typography.bodySmall,
                            color = SteelGray
                        )
                    }
                }

                if (plannedExercise != null) {
                    Text(
                        "${plannedExercise.sets} series × ${plannedExercise.reps} reps" +
                            (plannedExercise.restSeconds?.let { " · ${it} s descanso" } ?: ""),
                        style = MaterialTheme.typography.labelMedium,
                        color = EvolutionLime
                    )
                }

                if (sets.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        sets.joinToString("  ·  ") { "S${it.setNumber}  ${it.weightKg.kgText()} kg × ${it.reps}" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SteelGray
                    )
                }

                if (!finished) {
                    Spacer(Modifier.height(8.dp))
                    SetLogger(
                        defaultWeightKg = plannedExercise?.lastWeightKg
                            ?: sets.lastOrNull()?.weightKg,
                        nextSet = nextSet,
                        enabled = !logging,
                        onLog = { weightKg, reps -> log(exercise.id, nextSet, weightKg, reps) }
                    )
                }
            }
        }

        if (!finished) {
            item {
                TextButton(
                    onClick = { picker = true },
                    enabled = !logging,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = EvolutionLime)
                    Spacer(Modifier.width(4.dp))
                    Text("Agregar ejercicio", color = EvolutionLime)
                }
            }
            item {
                PrimaryButton(
                    text = if (finishing) "Finalizando…" else "Finalizar entrenamiento",
                    onClick = { confirmFinish = true },
                    enabled = !finishing && !logging
                )
            }
        } else {
            item {
                PrimaryButton(text = "Volver", onClick = onFinished)
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }

    if (picker) {
        ExercisePickerDialog(
            repo = repo,
            onDismiss = { picker = false },
            onConfirm = { picked ->
                extras.addAll(picked.filter { exercise -> rows.none { it.first.id == exercise.id } })
                picker = false
            }
        )
    }

    if (confirmFinish) {
        AlertDialog(
            onDismissRequest = { confirmFinish = false },
            containerColor = ForestCharcoal,
            title = { Text("Finalizar entrenamiento") },
            text = { Text("Después de finalizar no se pueden registrar más series.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmFinish = false
                        finish()
                    },
                    enabled = !finishing
                ) {
                    Text("Finalizar", color = EvolutionLime)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmFinish = false }) {
                    Text("Seguir entrenando", color = SteelGray)
                }
            }
        )
    }
}

/** Campos de peso/reps y botón para registrar la próxima serie. */
@Composable
private fun SetLogger(
    defaultWeightKg: Double?,
    nextSet: Int,
    enabled: Boolean,
    onLog: (weightKg: Double, reps: Int) -> Unit
) {
    var weight by remember { mutableStateOf(defaultWeightKg?.kgText() ?: "") }
    var reps by remember { mutableStateOf("") }

    val weightValue = if (weight.isBlank()) 0.0 else weight.replace(',', '.').toDoubleOrNull()
    val repsValue = reps.toIntOrNull()
    val valid = weightValue != null && weightValue in 0.0..2000.0 &&
        repsValue != null && repsValue in 1..1000

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            modifier = Modifier.weight(1f),
            singleLine = true,
            label = { Text("kg") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it.filter(Char::isDigit) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            label = { Text("reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(
            onClick = {
                onLog(weightValue ?: 0.0, repsValue ?: 0)
                reps = ""
            },
            enabled = enabled && valid,
            colors = ButtonDefaults.buttonColors(
                containerColor = EvolutionLime,
                contentColor = ObsidianBlack
            )
        ) {
            Text("Serie $nextSet")
        }
    }
}
