package com.evolve.app.data.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Envoltura estándar de la API: los datos de negocio van en `data`. */
@Serializable
data class ApiData<T>(val data: T)

@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Int = 900,
    val user: UserProfile? = null
)

@Serializable
data class UserProfile(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val weightKg: Double? = null,
    val heightCm: Int? = null,
    val goal: String? = null,
    val level: String? = null,
    val timezone: String = "UTC",
    val createdAt: String = ""
)

@Serializable
data class RegisterBody(
    val email: String,
    val password: String,
    val name: String,
    val timezone: String
)

@Serializable
data class LoginBody(val email: String, val password: String)

@Serializable
data class RefreshBody(val refreshToken: String)

@Serializable
data class LogoutBody(val refreshToken: String)

@Serializable
data class ProfileUpdateBody(
    val name: String? = null,
    val weightKg: Double? = null,
    val heightCm: Int? = null,
    val goal: String? = null,
    val level: String? = null,
    val timezone: String? = null
)

@Serializable
data class ExerciseSummary(
    val id: String,
    val name: String,
    val bodyPart: String = "",
    val equipment: String = "",
    val target: String = "",
    val muscleGroup: String = "",
    val secondaryMuscles: List<String> = emptyList(),
    val imageUrl: String = "",
    val gifUrl: String = "",
    val attribution: String = ""
)

@Serializable
data class ExerciseDetail(
    val id: String,
    val name: String,
    val bodyPart: String = "",
    val equipment: String = "",
    val target: String = "",
    val muscleGroup: String = "",
    val secondaryMuscles: List<String> = emptyList(),
    val imageUrl: String = "",
    val gifUrl: String = "",
    val attribution: String = "",
    val language: String = "es",
    val instructions: String = "",
    val steps: List<String> = emptyList()
)

@Serializable
data class Meta(
    val total: Int = 0,
    val bodyParts: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
    val targets: List<String> = emptyList(),
    val muscleGroups: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val attribution: String = ""
)

@Serializable
data class Pagination(
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0,
    val hasMore: Boolean = false
)

/** `/exercises` devuelve `data` + `pagination` en la raíz, no envuelto en `data`. */
@Serializable
data class ExercisePage(
    val data: List<ExerciseSummary> = emptyList(),
    val pagination: Pagination = Pagination()
)

@Serializable
data class PlanSummary(
    val id: String,
    val name: String,
    val goal: String = "",
    val isActive: Boolean = false,
    val exerciseCount: Int = 0,
    val days: List<Int> = emptyList()
)

@Serializable
data class PlanDetail(
    val id: String,
    val name: String,
    val goal: String = "",
    val isActive: Boolean = false,
    val days: List<PlanDay> = emptyList()
)

@Serializable
data class PlanDay(
    val id: String,
    val weekday: Int,
    val name: String,
    val exercises: List<PlanExercise> = emptyList()
)

@Serializable
data class PlanExercise(
    val id: String,
    val exercise: ExerciseSummary,
    val sets: Int = 0,
    val reps: String = "",
    val restSeconds: Int? = null,
    val position: Int = 0
)

@Serializable
data class PlanBody(
    val name: String,
    val goal: String,
    val days: List<PlanDayBody>
)

@Serializable
data class PlanDayBody(
    val weekday: Int,
    val name: String,
    val exercises: List<PlanExerciseBody>
)

@Serializable
data class PlanExerciseBody(
    val exerciseId: String,
    val sets: Int,
    val reps: String,
    val restSeconds: Int? = null
)

/** `/workouts/today` puede devolver `data: null`, por eso el campo es nullable. */
@Serializable
data class TodayResponse(val data: TodayWorkout? = null)

@Serializable
data class TodayWorkout(
    val planId: String = "",
    val planName: String = "",
    val dayId: String = "",
    val dayName: String = "",
    val workoutId: String? = null,
    val exercises: List<TodayExercise> = emptyList()
)

@Serializable
data class TodayExercise(
    val id: String,
    val exercise: ExerciseSummary,
    val sets: Int = 0,
    val reps: String = "",
    val restSeconds: Int? = null,
    val position: Int = 0,
    val lastWeightKg: Double? = null
)

@Serializable
data class Workout(
    val id: String,
    val startedAt: String = "",
    val finishedAt: String? = null,
    val planDayId: String? = null
)

@Serializable
data class WorkoutDetail(
    val id: String,
    val startedAt: String = "",
    val finishedAt: String? = null,
    val notes: String? = null,
    val planDayId: String? = null,
    val sets: List<WorkoutSet> = emptyList()
)

@Serializable
data class WorkoutSet(
    val id: String,
    val exercise: ExerciseSummary,
    val setNumber: Int,
    val weightKg: Double = 0.0,
    val reps: Int = 0,
    val completedAt: String = ""
)

@Serializable
data class StartWorkoutBody(val planDayId: String? = null)

@Serializable
data class WorkoutSetBody(
    val exerciseId: String,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int
)

@Serializable
data class ProgressSummary(
    val streakDays: Int = 0,
    val workoutsThisWeek: Int = 0,
    val weeklyVolumeKg: Int = 0,
    val newPrs: Int = 0
)

@Serializable
data class VolumeDay(
    val date: String = "",
    val label: String = "",
    val kg: Int = 0
)

@Serializable
data class RecordItem(
    val exerciseId: String = "",
    val exerciseName: String = "",
    val weightKg: Double = 0.0,
    val reps: Int = 0,
    val date: String = ""
)

@Serializable
data class ErrorEnvelope(val error: ErrorBody = ErrorBody())

@Serializable
data class ErrorBody(
    val code: String = "",
    val message: String = "",
    val details: JsonElement? = null
)
