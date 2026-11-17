package com.evolve.app.data

import com.evolve.app.data.api.ApiData
import com.evolve.app.data.api.AuthSession
import com.evolve.app.data.api.EvolveApi
import com.evolve.app.data.api.ExerciseDetail
import com.evolve.app.data.api.ExercisePage
import com.evolve.app.data.api.LoginBody
import com.evolve.app.data.api.LogoutBody
import com.evolve.app.data.api.Meta
import com.evolve.app.data.api.PlanBody
import com.evolve.app.data.api.PlanDetail
import com.evolve.app.data.api.PlanSummary
import com.evolve.app.data.api.ProfileUpdateBody
import com.evolve.app.data.api.ProgressSummary
import com.evolve.app.data.api.RecordItem
import com.evolve.app.data.api.RefreshBody
import com.evolve.app.data.api.RegisterBody
import com.evolve.app.data.api.StartWorkoutBody
import com.evolve.app.data.api.TodayWorkout
import com.evolve.app.data.api.UserProfile
import com.evolve.app.data.api.VolumeDay
import com.evolve.app.data.api.Workout
import com.evolve.app.data.api.WorkoutDetail
import com.evolve.app.data.api.WorkoutSet
import com.evolve.app.data.api.WorkoutSetBody
import com.evolve.app.data.api.toApiException
import com.evolve.app.data.api.workoutInProgressId
import kotlinx.coroutines.CancellationException
import retrofit2.Response

class EvolveRepository(
    private val api: EvolveApi,
    private val tokens: TokenStore
) {

    private var metaCache: Meta? = null

    private suspend fun <T> call(block: suspend () -> Response<T>): T {
        val response = block()
        if (response.isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            return (response.body() ?: Unit) as T
        }
        throw response.toApiException()
    }

    // --- Auth ---

    suspend fun register(
        email: String,
        password: String,
        name: String,
        timezone: String
    ): AuthSession = call { api.register(RegisterBody(email, password, name, timezone)) }

    suspend fun login(email: String, password: String): AuthSession =
        call { api.login(LoginBody(email, password)) }

    /**
     * true = conservar la sesión. Solo un 401 la cierra: si Render está despertando
     * o no hay red, se mantienen los tokens para reintentar.
     */
    suspend fun refreshSession(): Boolean {
        val refreshToken = tokens.refreshToken ?: return false
        return try {
            val response = api.refresh(RefreshBody(refreshToken))
            val body = response.body()
            when {
                response.isSuccessful && body != null -> {
                    tokens.save(body.accessToken, body.refreshToken)
                    true
                }
                response.code() == 401 -> {
                    tokens.clear()
                    false
                }
                else -> true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            true
        }
    }

    suspend fun logout() {
        val refreshToken = tokens.refreshToken
        if (refreshToken != null) {
            try {
                api.logout(LogoutBody(refreshToken))
            } catch (_: Exception) {
                // El logout local debe funcionar aunque el servidor no responda.
            }
        }
        tokens.clear()
    }

    // --- Perfil ---

    suspend fun profile(): UserProfile = call { api.profile() }.data

    suspend fun updateProfile(body: ProfileUpdateBody): UserProfile =
        call { api.updateProfile(body) }.data

    // --- Planes ---

    suspend fun plans(): List<PlanSummary> = call { api.plans() }.data

    suspend fun plan(id: String): PlanDetail = call { api.plan(id) }.data

    suspend fun createPlan(body: PlanBody): PlanDetail = call { api.createPlan(body) }.data

    suspend fun updatePlan(id: String, body: PlanBody): PlanDetail =
        call { api.updatePlan(id, body) }.data

    suspend fun deletePlan(id: String) {
        call<Unit> { api.deletePlan(id) }
    }

    suspend fun activatePlan(id: String): PlanDetail = call { api.activatePlan(id) }.data

    // --- Catálogo ---

    suspend fun meta(force: Boolean = false): Meta {
        metaCache?.takeIf { !force }?.let { return it }
        val meta = call { api.meta() }.data
        metaCache = meta
        return meta
    }

    suspend fun exercises(
        search: String?,
        target: String?,
        offset: Int = 0,
        limit: Int = PAGE_SIZE
    ): ExercisePage = call {
        api.exercises(
            search = search?.trim()?.takeIf { it.isNotEmpty() },
            target = target,
            limit = limit,
            offset = offset
        )
    }

    suspend fun exercise(id: String): ExerciseDetail = call { api.exercise(id, "es") }.data

    // --- Entrenamientos ---

    suspend fun today(): TodayWorkout? = call { api.today() }.data

    suspend fun startWorkout(planDayId: String?): Workout =
        call { api.startWorkout(StartWorkoutBody(planDayId)) }.data

    /** Inicia o retoma el workout abierto de hoy (409 WORKOUT_IN_PROGRESS). */
    suspend fun startOrResumeWorkout(planDayId: String?): String {
        return try {
            startWorkout(planDayId).id
        } catch (e: com.evolve.app.data.api.ApiException) {
            val openId = if (e.code == "WORKOUT_IN_PROGRESS") e.workoutInProgressId() else null
            openId ?: throw e
        }
    }

    suspend fun workout(id: String): WorkoutDetail = call { api.workout(id) }.data

    suspend fun addSet(
        workoutId: String,
        exerciseId: String,
        setNumber: Int,
        weightKg: Double,
        reps: Int
    ): WorkoutSet = call {
        api.addSet(workoutId, WorkoutSetBody(exerciseId, setNumber, weightKg, reps))
    }.data

    suspend fun finishWorkout(id: String): Workout = call { api.finishWorkout(id) }.data

    // --- Progreso ---

    suspend fun progressSummary(): ProgressSummary = call { api.progressSummary() }.data

    suspend fun weeklyVolume(): List<VolumeDay> = call { api.weeklyVolume() }.data

    suspend fun records(limit: Int = 20): List<RecordItem> = call { api.records(limit) }.data

    private companion object {
        const val PAGE_SIZE = 40
    }
}
