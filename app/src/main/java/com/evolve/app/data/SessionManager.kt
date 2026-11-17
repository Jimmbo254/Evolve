package com.evolve.app.data

import com.evolve.app.data.api.AuthSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.time.ZoneId

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data object LoggedIn : SessionState
}

/** Dueño del estado de sesión: bootstrap, login, registro y logout. */
class SessionManager(
    private val tokens: TokenStore,
    val repository: EvolveRepository
) {

    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private var bootstrapped = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        // Si un refresh terminal limpia los tokens, la app vuelve al login sola.
        scope.launch {
            tokens.hasSession.drop(1).collect { hasSession ->
                if (!hasSession) _state.value = SessionState.LoggedOut
            }
        }
    }

    suspend fun bootstrap() {
        if (bootstrapped) return
        bootstrapped = true
        val startedAt = System.currentTimeMillis()

        _state.value = when {
            tokens.refreshToken == null -> SessionState.LoggedOut
            repository.refreshSession() -> SessionState.LoggedIn
            else -> SessionState.LoggedOut
        }

        // Mantiene el splash un instante aunque el refresco sea instantáneo.
        val elapsed = System.currentTimeMillis() - startedAt
        if (elapsed < SPLASH_MILLIS) delay(SPLASH_MILLIS - elapsed)
    }

    suspend fun login(email: String, password: String) {
        apply(repository.login(email.trim(), password))
    }

    suspend fun signUp(name: String, email: String, password: String) {
        val session = repository.register(
            email = email.trim(),
            password = password,
            name = name.trim(),
            timezone = ZoneId.systemDefault().id
        )
        apply(session)
    }

    private fun apply(session: AuthSession) {
        tokens.save(session.accessToken, session.refreshToken)
        _state.value = SessionState.LoggedIn
    }

    suspend fun logout() {
        repository.logout()
        _state.value = SessionState.LoggedOut
    }

    private companion object {
        const val SPLASH_MILLIS = 900L
    }
}
