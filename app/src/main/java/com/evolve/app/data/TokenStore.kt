package com.evolve.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Los tokens nunca se guardan en claro. El access token se persiste (cifrado) para
 * poder rehidratar la sesión; igualmente se refresca al arrancar.
 */
interface TokenStore {
    val accessToken: String?
    val refreshToken: String?
    val hasSession: StateFlow<Boolean>
    fun save(accessToken: String, refreshToken: String)
    fun clear()
}

class EncryptedTokenStore(context: Context) : TokenStore {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs = EncryptedSharedPreferences.create(
        "evolve_tokens",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    @Volatile
    private var access: String? = prefs.getString(KEY_ACCESS, null)

    @Volatile
    private var refresh: String? = prefs.getString(KEY_REFRESH, null)

    private val _hasSession = MutableStateFlow(refresh != null)
    override val hasSession: StateFlow<Boolean> = _hasSession.asStateFlow()

    override val accessToken: String? get() = access
    override val refreshToken: String? get() = refresh

    override fun save(accessToken: String, refreshToken: String) {
        access = accessToken
        refresh = refreshToken
        prefs.edit().putString(KEY_ACCESS, accessToken).putString(KEY_REFRESH, refreshToken).apply()
        _hasSession.value = true
    }

    override fun clear() {
        access = null
        refresh = null
        prefs.edit().clear().apply()
        _hasSession.value = false
    }

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
    }
}
