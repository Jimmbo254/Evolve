package com.evolve.app.data.api

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import retrofit2.Response

/** Código y mensaje tal como los devuelve la API en `{ "error": { ... } }`. */
class ApiException(
    val status: Int,
    val code: String,
    override val message: String,
    val details: JsonElement? = null
) : Exception(message)

/** Primer mensaje de `details[]` cuando la API responde VALIDATION_ERROR. */
fun ApiException.firstValidationMessage(): String? {
    val array = details as? JsonArray ?: return null
    return array.firstOrNull()
        ?.let { it as? JsonObject }
        ?.get("message")
        ?.let { it as? JsonPrimitive }
        ?.contentOrNull
}

/** `details.workoutId` del 409 WORKOUT_IN_PROGRESS. */
fun ApiException.workoutInProgressId(): String? {
    val objectValue = details as? JsonObject ?: return null
    return (objectValue["workoutId"] as? JsonPrimitive)?.contentOrNull
}

fun Response<*>.toApiException(): ApiException {
    val raw = try {
        errorBody()?.string()
    } catch (_: Exception) {
        null
    }
    val parsed = raw?.let {
        try {
            AppJson.decodeFromString<ErrorEnvelope>(it)
        } catch (_: Exception) {
            null
        }
    }
    return ApiException(
        status = code(),
        code = parsed?.error?.code ?: "HTTP_${code()}",
        message = parsed?.error?.message ?: "Error ${code()}",
        details = parsed?.error?.details
    )
}
