package com.evolve.app.data.api

import android.content.Context
import com.evolve.app.data.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** OkHttp + Retrofit con bearer y refresh single-flight. */
class ApiClient(context: Context) {

    val tokenStore: TokenStore = com.evolve.app.data.EncryptedTokenStore(context)

    private fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .client(client)
        .addConverterFactory(AppJson.asConverterFactory("application/json".toMediaType()))
        .build()

    // Cliente sin authenticator para que un refresh no se llame a sí mismo.
    private val refreshApi: EvolveApi = retrofit(baseClient()).create(EvolveApi::class.java)

    val api: EvolveApi = retrofit(
        baseClient()
            .newBuilder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .authenticator(
                TokenAuthenticator(tokenStore) { refreshToken ->
                    try {
                        runBlocking {
                            val response = refreshApi.refresh(RefreshBody(refreshToken))
                            if (response.isSuccessful) response.body() else null
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
            )
            .build()
    ).create(EvolveApi::class.java)

    private fun baseClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS) // Render free puede tardar ~60 s en despertar
        .retryOnConnectionFailure(true)
        .build()
}
