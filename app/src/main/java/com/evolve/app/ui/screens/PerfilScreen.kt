package com.evolve.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.evolve.app.R
import com.evolve.app.data.ExerciseRepository
import com.evolve.app.ui.components.EvolveCard
import com.evolve.app.ui.components.InfoRow
import com.evolve.app.ui.components.SectionTitle
import com.evolve.app.ui.theme.ForestCharcoal
import com.evolve.app.ui.theme.ObsidianBlack
import com.evolve.app.ui.theme.SteelGray

@Composable
fun PerfilScreen(repo: ExerciseRepository) {
    val user = repo.profile()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineLarge)

        EvolveCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(ObsidianBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_evolve_icon),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(user.name.uppercase(), style = MaterialTheme.typography.titleLarge)
                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = SteelGray)
                    Text(user.level, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        SectionTitle("Datos físicos")
        EvolveCard {
            InfoRow("Peso", "${user.weightKg} kg")
            InfoRow("Estatura", "${user.heightCm} cm")
            InfoRow("Objetivo", user.goal)
        }

        SectionTitle("Ajustes")
        EvolveCard {
            InfoRow("Unidades", "Kilogramos")
            InfoRow("Notificaciones", "Activadas")
            InfoRow("Versión", "1.0")
        }
        Spacer(Modifier.height(8.dp))
    }
}
