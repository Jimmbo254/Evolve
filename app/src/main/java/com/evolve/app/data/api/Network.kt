package com.evolve.app.data.api

import com.evolve.app.data.TokenStore
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private fun Request.withBearer(token: String): Request =
    newBuilder().header("Authorization", "Bearer $token").build()

/** Agrega `Authorization: Bearer` a todo lo que no sea auth. */
class AuthInterceptor(private val tokens: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val access = tokens.accessToken
        if (access == null || request.url.encodedPath.contains("/auth/")) {
            return chain.proceed(request)
        }
        return chain.proceed(request.withBearer(access))
    }
}

/**
 * Renueva el access token ante un 401. Un solo refresh a la vez: si otro hilo ya
 * rotó el token, reintenta con el nuevo. Un solo reintento por petición.
 */
class TokenAuthenticator(
    private val tokens: TokenStore,
    private val refresh: (refreshToken: String) -> AuthSession?
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null
        if (response.request.url.encodedPath.contains("/auth/")) return null
        // OkHttp vuelve a llamar al authenticator si el reintento también da 401.
        if (response.priorResponse != null) return null

        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")
        synchronized(lock) {
            val current = tokens.accessToken
            if (current != null && current != failedToken) {
                return response.request.withBearer(current)
            }
            val refreshToken = tokens.refreshToken ?: return null
            val session = refresh(refreshToken)
            if (session == null) {
                tokens.clear()
                return null
            }
            tokens.save(session.accessToken, session.refreshToken)
            return response.request.withBearer(session.accessToken)
        }
    }
}
