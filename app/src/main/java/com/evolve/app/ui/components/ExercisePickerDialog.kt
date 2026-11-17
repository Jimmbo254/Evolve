package com.evolve.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.evolve.app.data.EvolveRepository
import com.evolve.app.data.api.ExerciseSummary
import com.evolve.app.ui.theme.ObsidianBlack

/** Selector de ejercicios a pantalla completa, con miniaturas y detalle. */
@Composable
fun ExercisePickerDialog(
    repo: EvolveRepository,
    title: String = "Agregar ejercicios",
    onDismiss: () -> Unit,
    onConfirm: (List<ExerciseSummary>) -> Unit
) {
    val selected = remember { mutableStateMapOf<String, ExerciseSummary>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = ObsidianBlack) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                    Text(title, style = MaterialTheme.typography.titleLarge)
                }
                ExerciseBrowser(
                    repo = repo,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    selectedIds = selected.keys.toSet(),
                    onToggle = { exercise ->
                        if (selected.containsKey(exercise.id)) selected.remove(exercise.id)
                        else selected[exercise.id] = exercise
                    },
                    contentPadding = PaddingValues(bottom = 16.dp)
                )
                PrimaryButton(
                    text = if (selected.isEmpty()) "Agregar" else "Agregar (${selected.size})",
                    onClick = { onConfirm(selected.values.toList()) },
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
