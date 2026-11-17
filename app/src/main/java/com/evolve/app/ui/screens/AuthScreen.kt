package com.evolve.app.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.evolve.app.R
import com.evolve.app.data.SessionManager
import com.evolve.app.ui.components.PrimaryButton
import com.evolve.app.ui.components.userMessage
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.SteelGray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(session: SessionManager) {
    var signUp by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        val problem = when {
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Ingresá un email válido."
            signUp && name.isBlank() -> "Ingresá tu nombre."
            signUp && password.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            password.isEmpty() -> "Ingresá tu contraseña."
            else -> null
        }
        if (problem != null) {
            error = problem
            return
        }

        scope.launch {
            loading = true
            error = null
            try {
                if (signUp) session.signUp(name, email, password) else session.login(email, password)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                error = userMessage(e)
            } finally {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Image(
            painter = painterResource(R.drawable.logo_evolve_icon),
            contentDescription = null,
            modifier = Modifier.size(96.dp)
        )
        Text("EVOLVE", style = MaterialTheme.typography.headlineLarge)
        Text(
            if (signUp) "Creá tu cuenta para guardar tu progreso" else "Bienvenido de nuevo",
            style = MaterialTheme.typography.bodyMedium,
            color = SteelGray,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = {
                    signUp = false
                    error = null
                }
            ) {
                Text(
                    "Iniciar sesión",
                    color = if (!signUp) EvolutionLime else SteelGray,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            TextButton(
                onClick = {
                    signUp = true
                    error = null
                }
            ) {
                Text(
                    "Crear cuenta",
                    color = if (signUp) EvolutionLime else SteelGray,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        if (signUp) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Nombre") }
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            supportingText = if (signUp) {
                { Text("Mínimo 8 caracteres", color = SteelGray) }
            } else {
                null
            }
        )

        if (error != null) {
            Text(
                error!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(4.dp))
        PrimaryButton(
            text = when {
                loading -> "Conectando…"
                signUp -> "Crear cuenta"
                else -> "Iniciar sesión"
            },
            onClick = { submit() },
            enabled = !loading
        )

        Text(
            "El servidor puede tardar hasta un minuto en despertar la primera vez.",
            style = MaterialTheme.typography.labelSmall,
            color = SteelGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
