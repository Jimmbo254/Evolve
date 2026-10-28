package com.evolve.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.evolve.app.R
import com.evolve.app.ui.theme.ObsidianBlack
import kotlinx.coroutines.delay

/** Pantalla de entrada con el logo completo de EVOLVE. */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onFinished()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo_evolve_full),
            contentDescription = "EVOLVE Fitness & Performance",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
