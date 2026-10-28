package com.evolve.app.data

/**
 * Punto de conexión para la API de ejercicios.
 * Cuando tengas la API, crea una clase (por ejemplo ApiExerciseRepository con Retrofit)
 * que implemente esta interfaz y cámbiala en EvolveApp.kt. Las pantallas no cambian.
 */
interface ExerciseRepository {
    fun todayExercises(): List<Exercise>
    fun plans(): List<WorkoutPlan>
    fun records(): List<PersonalRecord>
    fun weeklyVolume(): List<WeeklyVolume>
    fun profile(): UserProfile
}

/** Datos de ejemplo para ver las pantallas mientras no hay API. */
class SampleExerciseRepository : ExerciseRepository {

    override fun todayExercises() = listOf(
        Exercise("1", "Press de banca", "Pecho", 4, "8-10", 80f),
        Exercise("2", "Press inclinado con mancuernas", "Pecho", 3, "10-12", 28f),
        Exercise("3", "Press militar", "Hombro", 3, "8-10", 45f),
        Exercise("4", "Elevaciones laterales", "Hombro", 3, "12-15", 10f),
        Exercise("5", "Fondos en paralelas", "Tríceps", 3, "10-12", null),
        Exercise("6", "Extensión de tríceps en polea", "Tríceps", 3, "12-15", 25f)
    )

    override fun plans() = listOf(
        WorkoutPlan("p1", "Push Pull Legs", "Hipertrofia", listOf("L", "M", "V", "S"), 24, isActive = true),
        WorkoutPlan("p2", "Full Body", "Fuerza general", listOf("L", "X", "V"), 18),
        WorkoutPlan("p3", "Torso / Pierna", "Fuerza e hipertrofia", listOf("L", "M", "J", "V"), 22)
    )

    override fun records() = listOf(
        PersonalRecord("Press de banca", 90f, 5, "21 oct"),
        PersonalRecord("Sentadilla", 120f, 5, "18 oct"),
        PersonalRecord("Peso muerto", 150f, 3, "14 oct"),
        PersonalRecord("Press militar", 55f, 6, "09 oct")
    )

    override fun weeklyVolume() = listOf(
        WeeklyVolume("L", 8400), WeeklyVolume("M", 0), WeeklyVolume("X", 9100),
        WeeklyVolume("J", 0), WeeklyVolume("V", 10250), WeeklyVolume("S", 6800),
        WeeklyVolume("D", 0)
    )

    override fun profile() = UserProfile(
        name = "Gerardo",
        email = "gerardo@correo.com",
        weightKg = 78f,
        heightCm = 175,
        goal = "Ganar masa muscular",
        level = "Intermedio"
    )
}
