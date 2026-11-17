package com.evolve.app.ui

/** `target` del catálogo (inglés) a etiqueta de UI. Desconocido -> capitalizado. */
private val targetLabels = mapOf(
    "pectorals" to "Pecho",
    "biceps" to "Bíceps",
    "triceps" to "Tríceps",
    "delts" to "Hombros",
    "abs" to "Abdomen",
    "glutes" to "Glúteos",
    "quads" to "Cuádriceps",
    "hamstrings" to "Isquiotibiales",
    "calves" to "Pantorrillas",
    "lats" to "Dorsales",
    "upper back" to "Espalda alta",
    "spine" to "Espalda baja",
    "traps" to "Trapecios",
    "forearms" to "Antebrazos",
    "adductors" to "Aductores",
    "abductors" to "Abductores",
    "serratus anterior" to "Serrato",
    "levator scapulae" to "Cuello",
    "cardiovascular system" to "Cardio"
)

private val targetOrder = listOf(
    "pectorals", "biceps", "triceps", "delts", "abs", "glutes", "quads",
    "hamstrings", "calves", "lats", "upper back", "spine", "traps",
    "forearms", "adductors", "abductors", "serratus anterior",
    "levator scapulae", "cardiovascular system"
)

fun targetLabel(target: String): String =
    targetLabels[target] ?: target.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

fun sortedTargets(targets: List<String>): List<String> =
    targets.sortedBy { targetOrder.indexOf(it).let { index -> if (index < 0) targetOrder.size else index } }

val weekdayNames = listOf(
    "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
)

fun weekdayName(weekday: Int): String = weekdayNames.getOrElse(weekday - 1) { "Día $weekday" }

val weekdayLetters = listOf("L", "M", "X", "J", "V", "S", "D")

fun weekdayLetter(weekday: Int): String = weekdayLetters.getOrElse(weekday - 1) { "?" }
