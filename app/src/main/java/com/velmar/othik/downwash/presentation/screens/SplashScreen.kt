package com.velmar.othik.downwash.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velmar.othik.downwash.presentation.kit.GameFonts
import com.velmar.othik.downwash.presentation.kit.Palette
import com.velmar.othik.downwash.presentation.kit.drawableId
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    val fade by animateFloatAsState(if (shown) 1f else 0f, label = "splash")
    LaunchedEffect(Unit) {
        shown = true
        delay(1500)
        onDone()
    }
    val backdrop = drawableId("bg_splash")
    val logo = drawableId("logo")
    Box(Modifier.fillMaxSize().background(Palette.Night)) {
        if (backdrop != 0) {
            Image(
                painter = painterResource(backdrop),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Palette.Night.copy(alpha = 0.25f), Palette.Night.copy(alpha = 0.86f))
                )
            )
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (logo != 0) {
                Image(
                    painter = painterResource(logo),
                    contentDescription = null,
                    modifier = Modifier.size(180.dp),
                    alpha = fade,
                )
            }
            Text(
                text = "DOWNWASH",
                color = Palette.Cream.copy(alpha = fade),
                fontFamily = GameFonts.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 46.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Sling it, fly it, set it down",
                color = Palette.Amber.copy(alpha = fade),
                fontFamily = GameFonts.hud,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
        }
    }
}
