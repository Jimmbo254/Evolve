package com.evolve.app.data

/** Ejercicio. imageUrl vendrá de la API (imagen o GIF); por ahora es null. */
data class Exercise(
    val id: String,
    val name: String,
    val muscle: String,
    val sets: Int,
    val reps: String,
    val lastWeightKg: Float?,
    val imageUrl: String? = null
)

data class WorkoutPlan(
    val id: String,
    val name: String,
    val goal: String,
    val days: List<String>,
    val exerciseCount: Int,
    val isActive: Boolean = false
)

data class PersonalRecord(
    val exercise: String,
    val weightKg: Float,
    val reps: Int,
    val date: String
)

data class UserProfile(
    val name: String,
    val email: String,
    val weightKg: Float,
    val heightCm: Int,
    val goal: String,
    val level: String
)

data class WeeklyVolume(val day: String, val kg: Int)
