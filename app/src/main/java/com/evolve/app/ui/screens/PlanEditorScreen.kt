package com.evolve.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.evolve.app.data.EvolveRepository
import com.evolve.app.data.api.PlanBody
import com.evolve.app.data.api.PlanDetail
import com.evolve.app.data.api.PlanDayBody
import com.evolve.app.data.api.PlanExerciseBody
import com.evolve.app.ui.components.ErrorBox
import com.evolve.app.ui.components.EvolveCard
import com.evolve.app.ui.components.ExerciseImage
import com.evolve.app.ui.components.ExercisePickerDialog
import com.evolve.app.ui.components.LoadingBox
import com.evolve.app.ui.components.PrimaryButton
import com.evolve.app.ui.components.SectionTitle
import com.evolve.app.ui.components.userMessage
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.ForestCharcoal
import com.evolve.app.ui.theme.ObsidianBlack
import com.evolve.app.ui.theme.SteelGray
import com.evolve.app.ui.weekdayLetter
import com.evolve.app.ui.weekdayName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class PlanDraft(
    val name: String = "",
    val goal: String = "Hipertrofia",
    val days: List<DayDraft> = emptyList()
)

@Serializable
private data class DayDraft(
    val weekday: Int,
    val name: String,
    val exercises: List<ExerciseDraft> = emptyList()
)

@Serializable
private data class ExerciseDraft(
    val exerciseId: String,
    val name: String,
    val imageUrl: String = "",
    val sets: String = "3",
    val reps: String = "10",
    val restSeconds: String = "90"
)

private val draftJson = Json { ignoreUnknownKeys = true }

@Composable
fun PlanEditorScreen(
    repo: EvolveRepository,
    planId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    // El borrador se guarda como JSON para sobrevivir a la rotación.
    var draftJsonString by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var loadAttempt by remember { mutableIntStateOf(0) }
    var saving by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    var pickerWeekday by rememberSaveable { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(planId, loadAttempt) {
        if (draftJsonString != null) {
            loading = false
            return@LaunchedEffect
        }
        if (planId == null) {
            draftJsonString = draftJson.encodeToString(PlanDraft())
            loading = false
        } else {
            loading = true
            loadError = null
            try {
                draftJsonString = draftJson.encodeToString(repo.plan(planId).toDraft())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                loadError = userMessage(e)
            }
            loading = false
        }
    }

    val draft = draftJsonString?.let { draftJson.decodeFromString<PlanDraft>(it) }

    fun update(transform: (PlanDraft) -> PlanDraft) {
        val current = draft ?: return
        draftJsonString = draftJson.encodeToString(transform(current))
    }

    fun toggleDay(weekday: Int) {
        update { current ->
            if (current.days.any { it.weekday == weekday }) {
                current.copy(days = current.days.filterNot { it.weekday == weekday })
            } else {
                current.copy(
                    days = (current.days + DayDraft(weekday, weekdayName(weekday)))
                        .sortedBy { it.weekday }
                )
            }
        }
    }

    fun updateDay(weekday: Int, transform: (DayDraft) -> DayDraft) {
        update { current ->
            current.copy(
                days = current.days.map { if (it.weekday == weekday) transform(it) else it }
            )
        }
    }

    fun updateExercise(weekday: Int, exerciseId: String, transform: (ExerciseDraft) -> ExerciseDraft) {
        updateDay(weekday) { day ->
            day.copy(
                exercises = day.exercises.map {
                    if (it.exerciseId == exerciseId) transform(it) else it
                }
            )
        }
    }

    fun buildBody(): PlanBody? {
        val current = draft ?: return null
        if (current.name.isBlank()) {
            formError = "Poné un nombre al plan."
            return null
        }
        if (current.days.isEmpty()) {
            formError = "Elegí al menos un día."
            return null
        }

        val days = mutableListOf<PlanDayBody>()
        for (day in current.days.sortedBy { it.weekday }) {
            if (day.name.isBlank()) {
                formError = "Poné un nombre al día ${weekdayName(day.weekday)}."
                return null
            }
            if (day.exercises.isEmpty()) {
                formError = "Agregá al menos un ejercicio al día ${day.name}."
                return null
            }
            val exercises = mutableListOf<PlanExerciseBody>()
            for (exercise in day.exercises) {
                val sets = exercise.sets.toIntOrNull()
                if (sets == null || sets !in 1..50) {
                    formError = "Series inválidas en ${exercise.name} (1–50)."
                    return null
                }
                val reps = exercise.reps.trim()
                if (reps.isEmpty() || reps.length > 30) {
                    formError = "Reps inválidas en ${exercise.name}."
                    return null
                }
                val restText = exercise.restSeconds.trim()
                val rest = restText.toIntOrNull()
                if (restText.isNotEmpty() && (rest == null || rest !in 0..3600)) {
                    formError = "Descanso inválido en ${exercise.name} (0–3600 s)."
                    return null
                }
                exercises += PlanExerciseBody(exercise.exerciseId, sets, reps, rest)
            }
            days += PlanDayBody(day.weekday, day.name.trim(), exercises)
        }

        return PlanBody(
            name = current.name.trim(),
            goal = current.goal.trim().ifBlank { "Hipertrofia" },
            days = days
        )
    }

    fun save() {
        formError = null
        val body = buildBody() ?: return
        scope.launch {
            saving = true
            try {
                if (planId == null) repo.createPlan(body) else repo.updatePlan(planId, body)
                onSaved()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                formError = userMessage(e)
            } finally {
                saving = false
            }
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
            Text(
                if (planId == null) "Nuevo plan" else "Editar plan",
                style = MaterialTheme.typography.titleLarge
            )
        }

        when {
            loading -> LoadingBox()
            loadError != null -> ErrorBox(
                message = loadError!!,
                onRetry = {
                    loadError = null
                    draftJsonString = null
                    loadAttempt++
                }
            )
            draft == null -> LoadingBox()
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = draft.name,
                            onValueChange = { value -> update { it.copy(name = value) } },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Nombre del plan") }
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = draft.goal,
                            onValueChange = { value -> update { it.copy(goal = value) } },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Objetivo") }
                        )
                    }

                    item {
                        SectionTitle("Días")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..7).forEach { weekday ->
                                val included = draft.days.any { it.weekday == weekday }
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (included) EvolutionLime else ForestCharcoal)
                                        .clickable { toggleDay(weekday) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        weekdayLetter(weekday),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (included) ObsidianBlack else SteelGray
                                    )
                                }
                            }
                        }
                    }

                    items(draft.days, key = { it.weekday }) { day ->
                        EvolveCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        weekdayName(day.weekday),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "${day.exercises.size} ejercicios",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SteelGray
                                    )
                                }
                                IconButton(onClick = { toggleDay(day.weekday) }) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Quitar día",
                                        tint = SteelGray
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = day.name,
                                onValueChange = { value ->
                                    updateDay(day.weekday) { it.copy(name = value) }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Nombre del día") }
                            )

                            day.exercises.forEach { exercise ->
                                Spacer(Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ExerciseImage(imageUrl = exercise.imageUrl, size = 44.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        exercise.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            updateDay(day.weekday) { currentDay ->
                                                currentDay.copy(
                                                    exercises = currentDay.exercises.filterNot {
                                                        it.exerciseId == exercise.exerciseId
                                                    }
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Rounded.Close,
                                            contentDescription = "Quitar ejercicio",
                                            tint = SteelGray
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = exercise.sets,
                                        onValueChange = { value ->
                                            updateExercise(day.weekday, exercise.exerciseId) {
                                                it.copy(sets = value.filter(Char::isDigit))
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        label = { Text("Series") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = exercise.reps,
                                        onValueChange = { value ->
                                            updateExercise(day.weekday, exercise.exerciseId) {
                                                it.copy(reps = value)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        label = { Text("Reps") }
                                    )
                                    OutlinedTextField(
                                        value = exercise.restSeconds,
                                        onValueChange = { value ->
                                            updateExercise(day.weekday, exercise.exerciseId) {
                                                it.copy(restSeconds = value.filter(Char::isDigit))
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        label = { Text("Desc. s") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }

                            TextButton(onClick = { pickerWeekday = day.weekday }) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = EvolutionLime)
                                Spacer(Modifier.width(4.dp))
                                Text("Agregar ejercicio", color = EvolutionLime)
                            }
                        }
                    }

                    item {
                        if (formError != null) {
                            Text(
                                formError!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        PrimaryButton(
                            text = when {
                                saving -> "Guardando…"
                                planId == null -> "Crear plan"
                                else -> "Guardar cambios"
                            },
                            onClick = { save() },
                            enabled = !saving
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    val picker = pickerWeekday
    if (picker != null) {
        ExercisePickerDialog(
            repo = repo,
            title = "Ejercicios para ${weekdayName(picker)}",
            onDismiss = { pickerWeekday = null },
            onConfirm = { picked ->
                updateDay(picker) { day ->
                    val known = day.exercises.map { it.exerciseId }.toSet()
                    val added = picked
                        .filterNot { it.id in known }
                        .map { ExerciseDraft(it.id, it.name, it.imageUrl) }
                    day.copy(exercises = day.exercises + added)
                }
                pickerWeekday = null
            }
        )
    }
}

private fun PlanDetail.toDraft(): PlanDraft = PlanDraft(
    name = name,
    goal = goal.ifBlank { "Hipertrofia" },
    days = days.map { day ->
        DayDraft(
            weekday = day.weekday,
            name = day.name,
            exercises = day.exercises.map { planExercise ->
                ExerciseDraft(
                    exerciseId = planExercise.exercise.id,
                    name = planExercise.exercise.name,
                    imageUrl = planExercise.exercise.imageUrl,
                    sets = planExercise.sets.toString(),
                    reps = planExercise.reps,
                    restSeconds = planExercise.restSeconds?.toString() ?: ""
                )
            }
        )
    }
)
