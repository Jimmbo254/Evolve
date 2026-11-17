package com.evolve.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.evolve.app.R
import com.evolve.app.data.EvolveRepository
import com.evolve.app.data.SessionManager
import com.evolve.app.data.api.ProfileUpdateBody
import com.evolve.app.data.api.UserProfile
import com.evolve.app.ui.components.ErrorBox
import com.evolve.app.ui.components.EvolveCard
import com.evolve.app.ui.components.InfoRow
import com.evolve.app.ui.components.LoadingBox
import com.evolve.app.ui.components.PrimaryButton
import com.evolve.app.ui.components.Remote
import com.evolve.app.ui.components.SectionTitle
import com.evolve.app.ui.components.kgText
import com.evolve.app.ui.components.rememberRemote
import com.evolve.app.ui.components.userMessage
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.ObsidianBlack
import com.evolve.app.ui.theme.SteelGray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(repo: EvolveRepository, session: SessionManager) {
    val (state, retry) = rememberRemote(Unit) { repo.profile() }
    var editing by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineLarge)

        when (state) {
            is Remote.Loading -> LoadingBox()
            is Remote.Error -> ErrorBox(state.message, retry)
            is Remote.Ready -> {
                val user = state.value

                EvolveCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(ObsidianBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.logo_evolve_icon),
                                contentDescription = null,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                user.name.ifBlank { "Sin nombre" }.uppercase(),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = SteelGray
                            )
                            Text(
                                user.level ?: "Sin nivel",
                                style = MaterialTheme.typography.labelMedium,
                                color = EvolutionLime
                            )
                        }
                    }
                }

                SectionTitle("Datos físicos")
                if (editing) {
                    ProfileForm(
                        user = user,
                        saving = saving,
                        error = saveError,
                        onCancel = {
                            editing = false
                            saveError = null
                        },
                        onSave = { body ->
                            scope.launch {
                                saving = true
                                saveError = null
                                try {
                                    repo.updateProfile(body)
                                    editing = false
                                    retry()
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Throwable) {
                                    saveError = userMessage(e)
                                } finally {
                                    saving = false
                                }
                            }
                        }
                    )
                } else {
                    EvolveCard {
                        InfoRow("Peso", user.weightKg?.let { "${it.kgText()} kg" } ?: "—")
                        InfoRow("Estatura", user.heightCm?.let { "$it cm" } ?: "—")
                        InfoRow("Objetivo", user.goal ?: "—")
                        InfoRow("Nivel", user.level ?: "—")
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = { editing = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Editar datos", color = EvolutionLime)
                        }
                    }
                }

                SectionTitle("Cuenta")
                EvolveCard {
                    InfoRow("Unidades", "Kilogramos")
                    InfoRow("Versión", "1.0")
                    TextButton(onClick = { scope.launch { session.logout() } }) {
                        Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ProfileForm(
    user: UserProfile,
    saving: Boolean,
    error: String?,
    onCancel: () -> Unit,
    onSave: (ProfileUpdateBody) -> Unit
) {
    var weight by remember(user) { mutableStateOf(user.weightKg?.kgText() ?: "") }
    var height by remember(user) { mutableStateOf(user.heightCm?.toString() ?: "") }
    var goal by remember(user) { mutableStateOf(user.goal ?: "") }
    var level by remember(user) { mutableStateOf(user.level ?: "") }
    var problem by remember { mutableStateOf<String?>(null) }

    fun save() {
        val weightValue = weight.replace(',', '.').trim()
        val weightKg = if (weightValue.isEmpty()) null else weightValue.toDoubleOrNull()
        val heightCm = if (height.isBlank()) null else height.toIntOrNull()
        if (weightValue.isNotEmpty() && (weightKg == null || weightKg !in 1.0..500.0)) {
            problem = "Peso inválido (1–500 kg)."
            return
        }
        if (height.isNotBlank() && (heightCm == null || heightCm !in 50..300)) {
            problem = "Estatura inválida (50–300 cm)."
            return
        }
        problem = null
        onSave(
            ProfileUpdateBody(
                weightKg = weightKg,
                heightCm = heightCm,
                goal = goal.trim().ifBlank { null },
                level = level.trim().ifBlank { null }
            )
        )
    }

    EvolveCard {
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Peso (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = height,
            onValueChange = { height = it.filter(Char::isDigit) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Estatura (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Objetivo") }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = level,
            onValueChange = { level = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Nivel") }
        )

        val message = problem ?: error
        if (message != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(12.dp))
        PrimaryButton(
            text = if (saving) "Guardando…" else "Guardar cambios",
            onClick = { save() },
            enabled = !saving
        )
        TextButton(onClick = onCancel, enabled = !saving, modifier = Modifier.align(Alignment.End)) {
            Text("Cancelar", color = SteelGray)
        }
    }
}
