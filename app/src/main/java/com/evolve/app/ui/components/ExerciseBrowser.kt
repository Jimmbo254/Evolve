package com.evolve.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.evolve.app.data.EvolveRepository
import com.evolve.app.data.api.ExerciseSummary
import com.evolve.app.data.api.Meta
import com.evolve.app.ui.sortedTargets
import com.evolve.app.ui.targetLabel
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.ForestCharcoal
import com.evolve.app.ui.theme.ObsidianBlack
import com.evolve.app.ui.theme.SteelGray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Catálogo reusable: búsqueda + grupos musculares + paginación.
 * Si `onToggle` no es null muestra el botón de agregar (selector de rutina).
 */
@Composable
fun ExerciseBrowser(
    repo: EvolveRepository,
    modifier: Modifier = Modifier,
    selectedIds: Set<String> = emptySet(),
    onToggle: ((ExerciseSummary) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(bottom = 16.dp)
) {
    var meta by remember { mutableStateOf<Meta?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<ExerciseSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableIntStateOf(0) }
    var detailFor by remember { mutableStateOf<ExerciseSummary?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            meta = repo.meta()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            // Sin metadatos no hay chips; la búsqueda sigue funcionando.
        }
    }

    LaunchedEffect(group, query, attempt) {
        loading = true
        error = null
        if (query.isNotBlank()) delay(350)
        try {
            val page = repo.exercises(search = query, target = group, offset = 0)
            items = page.data
            hasMore = page.pagination.hasMore
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            items = emptyList()
            error = userMessage(e)
        }
        loading = false
    }

    fun loadMore() {
        if (loadingMore || !hasMore) return
        loadingMore = true
        scope.launch {
            try {
                val page = repo.exercises(search = query, target = group, offset = items.size)
                val known = items.map { it.id }.toSet()
                items = items + page.data.filterNot { it.id in known }
                hasMore = page.pagination.hasMore
            } catch (e: Throwable) {
                error = userMessage(e)
            } finally {
                loadingMore = false
            }
        }
    }

    Column(modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Buscar ejercicio") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) }
        )

        val targets = meta?.targets.orEmpty()
        if (targets.isNotEmpty()) {
            Spacer(Modifier.padding(top = 8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    GroupChip("Todos", group == null) { group = null }
                }
                items(sortedTargets(targets)) { target ->
                    GroupChip(targetLabel(target), group == target) { group = target }
                }
            }
        }

        Spacer(Modifier.padding(top = 8.dp))

        when {
            loading && items.isEmpty() -> LoadingBox(Modifier.weight(1f))
            error != null && items.isEmpty() -> ErrorBox(
                message = error!!,
                onRetry = { attempt++ },
                modifier = Modifier.weight(1f)
            )
            items.isEmpty() -> Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                EmptyMessage("No hay ejercicios para ese filtro.")
            }
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { exercise ->
                    ExerciseRow(
                        exercise = exercise,
                        selected = exercise.id in selectedIds,
                        onToggle = onToggle?.let { toggle -> { toggle(exercise) } },
                        onClick = { detailFor = exercise }
                    )
                }
                item {
                    if (error != null) {
                        Text(
                            error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (hasMore) {
                        TextButton(
                            onClick = { loadMore() },
                            enabled = !loadingMore,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (loadingMore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.width(20.dp),
                                    color = EvolutionLime,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Cargar más", color = EvolutionLime)
                            }
                        }
                    }
                    Text(
                        meta?.attribution ?: "© Gym visual — https://gymvisual.com/",
                        style = MaterialTheme.typography.labelSmall,
                        color = SteelGray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }

    val detail = detailFor
    if (detail != null) {
        ExerciseDetailSheet(
            repo = repo,
            exercise = detail,
            selected = detail.id in selectedIds,
            onToggle = onToggle?.let { toggle -> { toggle(detail) } },
            onDismiss = { detailFor = null }
        )
    }
}

@Composable
private fun GroupChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = ForestCharcoal,
            labelColor = SteelGray,
            selectedContainerColor = EvolutionLime,
            selectedLabelColor = ObsidianBlack
        )
    )
}

@Composable
private fun ExerciseRow(
    exercise: ExerciseSummary,
    selected: Boolean,
    onToggle: (() -> Unit)?,
    onClick: () -> Unit
) {
    EvolveCard {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExerciseImage(imageUrl = exercise.imageUrl, size = 64.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${targetLabel(exercise.target)} · ${exercise.equipment}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SteelGray
                )
            }
            if (onToggle != null) {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.AddCircle,
                        contentDescription = if (selected) "Quitar" else "Agregar",
                        tint = if (selected) EvolutionLime else SteelGray
                    )
                }
            }
        }
    }
}
