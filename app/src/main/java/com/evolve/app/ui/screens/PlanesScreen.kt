package com.evolve.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.evolve.app.data.EvolveRepository
import com.evolve.app.data.api.PlanSummary
import com.evolve.app.ui.components.EmptyMessage
import com.evolve.app.ui.components.ErrorBox
import com.evolve.app.ui.components.EvolveCard
import com.evolve.app.ui.components.LoadingBox
import com.evolve.app.ui.components.Remote
import com.evolve.app.ui.components.rememberRemote
import com.evolve.app.ui.components.userMessage
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.ForestCharcoal
import com.evolve.app.ui.theme.ObsidianBlack
import com.evolve.app.ui.theme.SteelGray
import com.evolve.app.ui.weekdayLetter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun PlanesScreen(
    repo: EvolveRepository,
    version: Int,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit
) {
    var localAttempt by remember { mutableIntStateOf(0) }
    val (state, retry) = rememberRemote(version, localAttempt) { repo.plans() }
    var pendingDelete by remember { mutableStateOf<PlanSummary?>(null) }
    var busy by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun reload() {
        actionError = null
        localAttempt++
    }

    fun activate(id: String) {
        if (busy) return
        scope.launch {
            busy = true
            actionError = null
            try {
                repo.activatePlan(id)
                reload()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                actionError = userMessage(e)
            } finally {
                busy = false
            }
        }
    }

    fun delete(id: String) {
        if (busy) return
        scope.launch {
            busy = true
            actionError = null
            try {
                repo.deletePlan(id)
                reload()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                actionError = userMessage(e)
            } finally {
                busy = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Planes", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Tus rutinas personalizadas",
                style = MaterialTheme.typography.bodyMedium,
                color = SteelGray
            )
        }

        if (actionError != null) {
            item {
                Text(
                    actionError!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        when (state) {
            is Remote.Loading -> item { LoadingBox() }
            is Remote.Error -> item { ErrorBox(state.message, retry) }
            is Remote.Ready -> {
                val plans = state.value
                if (plans.isEmpty()) {
                    item {
                        EvolveCard {
                            EmptyMessage("Todavía no tenés planes. Creá el primero con el botón +.")
                            TextButton(onClick = onCreate) {
                                Text("Crear plan", color = EvolutionLime)
                            }
                        }
                    }
                } else {
                    items(plans, key = { it.id }) { plan ->
                        EvolveCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        plan.name.uppercase(),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        plan.goal.ifBlank { "Sin objetivo" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SteelGray
                                    )
                                }
                                if (plan.isActive) {
                                    Text(
                                        "ACTIVO",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ObsidianBlack,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(EvolutionLime)
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                plan.days.sorted().forEach { weekday ->
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(EvolutionLime),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            weekdayLetter(weekday),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = ObsidianBlack
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "${plan.exerciseCount} ejercicios",
                                style = MaterialTheme.typography.labelMedium,
                                color = SteelGray
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (!plan.isActive) {
                                    TextButton(
                                        onClick = { activate(plan.id) },
                                        enabled = !busy
                                    ) {
                                        Text("Activar", color = EvolutionLime)
                                    }
                                }
                                TextButton(
                                    onClick = { onEdit(plan.id) },
                                    enabled = !busy
                                ) {
                                    Text("Editar", color = SteelGray)
                                }
                                TextButton(
                                    onClick = { pendingDelete = plan },
                                    enabled = !busy
                                ) {
                                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val toDelete = pendingDelete
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = ForestCharcoal,
            title = { Text("Borrar plan") },
            text = {
                Text("Se va a borrar \"${toDelete.name}\". Los entrenamientos ya registrados no se pierden.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        delete(toDelete.id)
                    }
                ) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancelar", color = SteelGray)
                }
            }
        )
    }
}
