package com.velmar.othik.downwash.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velmar.othik.downwash.domain.model.Mission
import com.velmar.othik.downwash.domain.model.Missions
import com.velmar.othik.downwash.domain.usecase.Debrief
import com.velmar.othik.downwash.presentation.kit.GameFonts
import com.velmar.othik.downwash.presentation.kit.OrnateButton
import com.velmar.othik.downwash.presentation.kit.Palette
import com.velmar.othik.downwash.presentation.kit.Panel
import com.velmar.othik.downwash.presentation.kit.ScreenTitle
import com.velmar.othik.downwash.presentation.kit.StarStrip
import com.velmar.othik.downwash.presentation.kit.ValleyBackdrop

@Composable
fun DebriefScreen(
    mission: Mission,
    debrief: Debrief,
    backdrop: Int,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onChart: () -> Unit,
) {
    BackHandler { onChart() }
    Box(Modifier.fillMaxSize()) {
        ValleyBackdrop(backdrop, dim = 0.70f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            ScreenTitle(if (debrief.cleared) "Load Delivered" else "Mission Lost", size = 31)
            Text(
                text = mission.title,
                color = Palette.Amber,
                fontFamily = GameFonts.hud,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
            StarStrip(debrief.stars, star = 40)
            Spacer(Modifier.height(20.dp))
            Panel(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Line("Loads set down", "${debrief.delivered} of ${debrief.total}")
                    Line("Fuel remaining", "${debrief.fuelLeft}")
                    Line("Load damage", "${debrief.damage}%")
                }
            }
            if (debrief.awards.isNotEmpty()) {
                Spacer(Modifier.height(13.dp))
                Text(
                    text = "New award: " + debrief.awards.joinToString { it.title },
                    color = Palette.Gold,
                    fontFamily = GameFonts.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.weight(1f))
            if (debrief.cleared && mission.index + 1 < Missions.count) {
                OrnateButton(
                    text = "Next Mission",
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    fillColor = Palette.Amber,
                    fontSize = 20,
                )
                Spacer(Modifier.height(10.dp))
            }
            OrnateButton(
                text = if (debrief.cleared) "Fly It Again" else "Try Again",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                fillColor = Palette.Grass,
            )
            Spacer(Modifier.height(10.dp))
            OrnateButton(
                text = "Mission Chart",
                onClick = onChart,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                fillColor = Palette.Sky,
                fontSize = 16,
            )
        }
    }
}

@Composable
private fun Line(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Palette.Steel, fontFamily = GameFonts.hud, fontSize = 14.sp)
        Text(
            text = value,
            color = Palette.Cream,
            fontFamily = GameFonts.hud,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}
