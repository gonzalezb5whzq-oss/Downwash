package com.velmar.othik.downwash.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velmar.othik.downwash.domain.model.Missions
import com.velmar.othik.downwash.presentation.kit.GameFonts
import com.velmar.othik.downwash.presentation.kit.IconChip
import com.velmar.othik.downwash.presentation.kit.OrnateButton
import com.velmar.othik.downwash.presentation.kit.Palette
import com.velmar.othik.downwash.presentation.kit.backdropFor
import com.velmar.othik.downwash.presentation.kit.drawableId

@Composable
fun HangarScreen(
    backdrop: Int,
    stars: Int,
    cleared: Int,
    onFly: () -> Unit,
    onChart: () -> Unit,
    onAwards: () -> Unit,
    onLogbook: () -> Unit,
    onOptions: () -> Unit,
    onExit: () -> Unit,
) {
    val hero = drawableId(backdropFor(backdrop).asset)
    val logo = drawableId("logo")
    Box(Modifier.fillMaxSize().background(Palette.Night)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().weight(0.46f)) {
                if (hero != 0) {
                    Image(
                        painter = painterResource(hero),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(
                                Palette.Night.copy(alpha = 0.55f),
                                Color.Transparent,
                                Palette.Night,
                            )
                        )
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (logo != 0) {
                            Image(painterResource(logo), null, Modifier.size(52.dp))
                        }
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(
                                text = "DOWNWASH",
                                color = Palette.Cream,
                                fontFamily = GameFonts.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                            )
                            Text(
                                text = "Sling load operations",
                                color = Palette.Amber,
                                fontFamily = GameFonts.hud,
                                fontSize = 12.sp,
                            )
                        }
                    }
                    IconChip("ic_star", "$stars")
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.54f)
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Text(
                    text = "$cleared of ${Missions.count} missions flown",
                    color = Palette.Steel,
                    fontFamily = GameFonts.hud,
                    fontSize = 13.sp,
                )
                RailButton("Fly", onFly, 1.00f, Palette.Amber, 24)
                RailButton("Mission Chart", onChart, 0.82f, Palette.Sky, 18)
                RailButton("Awards", onAwards, 0.70f, Palette.Grass, 17)
                RailButton("Logbook", onLogbook, 0.63f, Palette.Grass, 17)
                RailButton("Settings", onOptions, 0.56f, Palette.Grass, 16)
                Spacer(Modifier.weight(1f))
                OrnateButton(
                    text = "Exit",
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth(0.44f).height(44.dp),
                    fillColor = Palette.Alert,
                    fontSize = 15,
                )
            }
        }
    }
}

@Composable
private fun RailButton(text: String, onClick: () -> Unit, width: Float, fill: Color, size: Int) {
    OrnateButton(
        text = text,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(width).height(if (size >= 24) 62.dp else 50.dp),
        fillColor = fill,
        fontSize = size,
    )
}
