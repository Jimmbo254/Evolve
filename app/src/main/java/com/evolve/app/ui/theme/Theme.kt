package com.evolve.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EvolveColors = darkColorScheme(
    primary = EvolutionLime,
    onPrimary = ObsidianBlack,
    secondary = PerformanceCoral,
    onSecondary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = TitaniumWhite,
    surface = ForestCharcoal,
    onSurface = TitaniumWhite,
    surfaceVariant = ForestCharcoal,
    onSurfaceVariant = SteelGray,
    outline = SteelGray,
    error = PerformanceCoral
)

// EVOLVE siempre usa tema oscuro (70% oscuro, 20% superficies, 10% acentos)
@Composable
fun EvolveTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EvolveColors,
        typography = EvolveTypography,
        content = content
    )
}
