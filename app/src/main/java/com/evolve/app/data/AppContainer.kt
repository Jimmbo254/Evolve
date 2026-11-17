package com.evolve.app.data

import android.content.Context
import com.evolve.app.data.api.ApiClient

/** Inyección manual: una sola instancia por proceso. */
class AppContainer(context: Context) {
    private val client = ApiClient(context)
    val repository = EvolveRepository(client.api, client.tokenStore)
    val session = SessionManager(client.tokenStore, repository)
}
