package com.evolve.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.evolve.app.data.api.ApiException
import com.evolve.app.data.api.firstValidationMessage
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.SteelGray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.SocketTimeoutException

sealed interface Remote<out T> {
    data object Loading : Remote<Nothing>
    data class Error(val message: String) : Remote<Nothing>
    data class Ready<T>(val value: T) : Remote<T>
}

/** Carga asíncrona con reintento. Se reinicia cuando cambian las claves. */
@Composable
fun <T> rememberRemote(vararg keys: Any?, load: suspend () -> T): Pair<Remote<T>, () -> Unit> {
    var attempt by remember { mutableIntStateOf(0) }
    val currentLoad by rememberUpdatedState(load)
    val remote by produceState<Remote<T>>(Remote.Loading, *keys, attempt) {
        value = Remote.Loading
        value = try {
            Remote.Ready(currentLoad())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Remote.Error(userMessage(e))
        }
    }
    return remote to { attempt++ }
}

fun userMessage(error: Throwable): String = when (error) {
    is ApiException -> {
        val detail = error.firstValidationMessage()
        if (!detail.isNullOrBlank() && detail != error.message) "${error.message}: $detail" else error.message
    }
    is SocketTimeoutException ->
        "El servidor está tardando en responder. Probá de nuevo en unos segundos."
    is IOException -> "No se pudo conectar con el servidor. Revisá tu conexión."
    else -> "Algo salió mal. Probá de nuevo."
}

fun Double.kgText(): String =
    if (this == toLong().toDouble()) toLong().toString() else String.format("%.1f", this)

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    var slow by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(5_000)
        slow = true
    }
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(color = EvolutionLime)
        Text(
            if (slow) "Despertando el servidor…" else "Cargando…",
            style = MaterialTheme.typography.bodyMedium,
            color = SteelGray
        )
    }
}

@Composable
fun ErrorBox(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        TextButton(onClick = onRetry) {
            Text("Reintentar", color = EvolutionLime)
        }
    }
}

@Composable
fun EmptyMessage(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = SteelGray,
        modifier = modifier.fillMaxWidth().padding(vertical = 28.dp)
    )
}
