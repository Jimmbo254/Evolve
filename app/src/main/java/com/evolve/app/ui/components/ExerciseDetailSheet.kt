package com.evolve.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.evolve.app.data.EvolveRepository
import com.evolve.app.data.api.ExerciseDetail
import com.evolve.app.data.api.ExerciseSummary
import com.evolve.app.ui.targetLabel
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.ForestCharcoal
import com.evolve.app.ui.theme.ObsidianBlack
import com.evolve.app.ui.theme.SteelGray
import kotlinx.coroutines.CancellationException

/** Detalle con GIF, instrucciones y pasos (en español). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailSheet(
    repo: EvolveRepository,
    exercise: ExerciseSummary,
    selected: Boolean,
    onToggle: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var detail by remember { mutableStateOf<ExerciseDetail?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(exercise.id) {
        try {
            detail = repo.exercise(exercise.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            error = userMessage(e)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ForestCharcoal
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianBlack),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = detail?.gifUrl?.takeIf { it.isNotBlank() } ?: exercise.imageUrl,
                        contentDescription = exercise.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
            }

            item {
                Column(Modifier.padding(top = 10.dp)) {
                    Text(exercise.name, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "${targetLabel(exercise.target)} · ${exercise.equipment}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SteelGray
                    )
                    if (exercise.secondaryMuscles.isNotEmpty()) {
                        Text(
                            "Secundarios: ${exercise.secondaryMuscles.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SteelGray
                        )
                    }
                }
            }

            when {
                detail == null && error == null -> item {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = EvolutionLime)
                    }
                }
                error != null -> item {
                    Text(
                        error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                else -> {
                    val loaded = detail!!
                    if (loaded.instructions.isNotBlank()) {
                        item {
                            Text(
                                "Instrucciones",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                            Text(loaded.instructions, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (loaded.steps.isNotEmpty()) {
                        item {
                            Text(
                                "Paso a paso",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        itemsIndexed(loaded.steps) { index, step ->
                            Text(
                                "${index + 1}. $step",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SteelGray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    detail?.attribution
                        ?: exercise.attribution.ifBlank { "© Gym visual — https://gymvisual.com/" },
                    style = MaterialTheme.typography.labelSmall,
                    color = SteelGray,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            if (onToggle != null) {
                item {
                    PrimaryButton(
                        text = if (selected) "Quitar de la rutina" else "Agregar a la rutina",
                        onClick = {
                            onToggle()
                            onDismiss()
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
