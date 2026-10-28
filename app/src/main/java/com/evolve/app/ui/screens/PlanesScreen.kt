package com.evolve.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.evolve.app.data.ExerciseRepository
import com.evolve.app.ui.components.EvolveCard
import com.evolve.app.ui.theme.EvolutionLime
import com.evolve.app.ui.theme.ObsidianBlack
import com.evolve.app.ui.theme.SteelGray

@Composable
fun PlanesScreen(repo: ExerciseRepository) {
    val plans = repo.plans()
    val allDays = listOf("L", "M", "X", "J", "V", "S", "D")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Planes", style = MaterialTheme.typography.headlineLarge)
            Text("Tus rutinas personalizadas", style = MaterialTheme.typography.bodyMedium, color = SteelGray)
        }
        items(plans, key = { it.id }) { plan ->
            EvolveCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(plan.name.uppercase(), style = MaterialTheme.typography.titleLarge)
                        Text(plan.goal, style = MaterialTheme.typography.bodyMedium, color = SteelGray)
                    }
                    if (plan.isActive) {
                        Text(
                            "ACTIVO",
                            style = MaterialTheme.typography.labelMedium,
                            color = ObsidianBlack,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(EvolutionLime)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    allDays.forEach { d ->
                        val on = d in plan.days
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (on) EvolutionLime else ObsidianBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                d,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (on) ObsidianBlack else SteelGray
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "${plan.exerciseCount} ejercicios",
                    style = MaterialTheme.typography.labelMedium,
                    color = SteelGray
                )
            }
        }
    }
}
