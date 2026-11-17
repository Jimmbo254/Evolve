package com.evolve.app.data.api

import kotlinx.serialization.json.Json

/** Configuración única de JSON: tolera campos nuevos y no envía nulos en los cuerpos. */
internal val AppJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
}

const val API_BASE_URL = "https://back-evolve-yn3t.onrender.com/api/v1/"
