package com.velmar.othik.downwash.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velmar.othik.downwash.domain.FlightEngine
import com.velmar.othik.downwash.domain.model.Mission
import com.velmar.othik.downwash.presentation.kit.GameFonts
import com.velmar.othik.downwash.presentation.kit.IconChip
import com.velmar.othik.downwash.presentation.kit.OrnateButton
import com.velmar.othik.downwash.presentation.kit.Palette
import com.velmar.othik.downwash.presentation.kit.PlateFace
import com.velmar.othik.downwash.presentation.kit.ScreenTitle
import com.velmar.othik.downwash.presentation.scene.ValleyScene
import com.velmar.othik.downwash.presentation.state.Hud
import kotlin.math.abs

@Composable
fun FlightScreen(
    mission: Mission,
    engine: FlightEngine,
    hud: Hud,
    paused: Boolean,
    invertLift: Boolean,
    onStep: (Float) -> Unit,
    onSteer: (Float, Float) -> Unit,
    onLift: (Float) -> Unit,
    onWinch: (Float) -> Unit,
    onRelease: () -> Unit,
    onPause: () -> Unit,
    onQuit: () -> Unit,
) {
    var ready by remember { mutableStateOf(false) }
    var stickX by remember { mutableFloatStateOf(0f) }
    var stickY by remember { mutableFloatStateOf(0f) }
    var collective by remember { mutableFloatStateOf(0.595f) }
    val density = LocalDensity.current
    val reach = with(density) { 96.dp.toPx() }

    LaunchedEffect(engine) { onLift(collective) }

    BackHandler { onPause() }

    Box(Modifier.fillMaxSize().background(Palette.Night)) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2B4C61), Color(0xFF13232E), Color(0xFF0B141B))
                )
            )
        )
        ValleyScene(
            engine = engine,
            isPaused = { paused },
            onStep = { dt ->
                if (!ready) ready = true
                onStep(dt)
            },
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxWidth(0.62f)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .pointerInput(engine) {
                    detectDragGestures(
                        onDragStart = { },
                        onDragEnd = {
                            stickX = 0f
                            stickY = 0f
                            onSteer(0f, 0f)
                        },
                        onDragCancel = {
                            stickX = 0f
                            stickY = 0f
                            onSteer(0f, 0f)
                        },
                    ) { change, drag ->
                        change.consume()
                        stickX = (stickX + drag.x).coerceIn(-reach, reach)
                        stickY = (stickY + drag.y).coerceIn(-reach, reach)
                        onSteer(stickX / reach, stickY / reach)
                    }
                }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            HudBar(mission, hud, onPause)
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StickPad(stickX / reach, stickY / reach)
                Lever(
                    value = collective,
                    invert = invertLift,
                    onChange = {
                        collective = it
                        onLift(it)
                    },
                )
            }
            Controls(hud, onWinch, onRelease)
        }
        if (!ready) {
            Box(
                Modifier.fillMaxSize().background(Palette.Night.copy(alpha = 0.94f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Spooling up",
                    color = Palette.Amber,
                    fontFamily = GameFonts.primary,
                    fontSize = 20.sp,
                )
            }
        }
        if (paused) {
            PauseCurtain(mission, onPause, onQuit)
        }
    }
}

@Composable
private fun HudBar(mission: Mission, hud: Hud, onPause: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Palette.Night.copy(alpha = 0.92f), Color.Transparent))
            )
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = mission.title,
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
                Text(
                    text = mission.brief(),
                    color = Palette.Amber,
                    fontFamily = GameFonts.hud,
                    fontSize = 12.sp,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                IconChip("ic_fuel", "${hud.fuel}", tint = if (hud.fuelPart < 0.2f) Palette.Alert else Palette.Cream)
                OrnateButton("II", onPause, Modifier.height(40.dp), fillColor = Palette.Sky, fontSize = 14)
            }
        }
        Spacer(Modifier.height(7.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Palette.Night.copy(alpha = 0.80f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(hud.fuelPart)
                    .background(
                        Brush.horizontalGradient(
                            if (hud.fuelPart < 0.2f) listOf(Palette.Alert, Palette.Amber)
                            else listOf(Palette.Grass, Palette.Sky)
                        )
                    )
            )
        }
        Spacer(Modifier.height(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Readout("LOADS", "${hud.delivered}/${hud.total}")
            Readout("CABLE", "${hud.cable} m")
            Readout("ALT", "${hud.altitude} m")
            Readout("DMG", "${hud.damage}%", hud.damage > 40)
        }
    }
}

@Composable
private fun Readout(label: String, value: String, alert: Boolean = false) {
    Column {
        Text(
            text = label,
            color = Palette.Steel,
            fontFamily = GameFonts.hud,
            fontSize = 10.sp,
        )
        Text(
            text = value,
            color = if (alert) Palette.Alert else Palette.Cream,
            fontFamily = GameFonts.hud,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun StickPad(x: Float, y: Float) {
    Canvas(Modifier.width(110.dp).height(110.dp)) {
        drawCircle(
            color = Palette.Night.copy(alpha = 0.45f),
            radius = size.minDimension / 2f,
            center = center,
        )
        drawCircle(
            color = Palette.Steel.copy(alpha = 0.40f),
            radius = size.minDimension / 2f - 3f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f),
        )
        drawCircle(
            color = Palette.Amber.copy(alpha = 0.92f),
            radius = size.minDimension * 0.17f,
            center = Offset(
                center.x + x * size.minDimension * 0.31f,
                center.y + y * size.minDimension * 0.31f,
            ),
        )
    }
}

@Composable
private fun Lever(value: Float, invert: Boolean, onChange: (Float) -> Unit) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .width(74.dp)
            .height(190.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Palette.Night.copy(alpha = 0.55f))
            .pointerInput(invert) {
                detectDragGestures { change, drag ->
                    change.consume()
                    val span = with(density) { 190.dp.toPx() }
                    val step = -drag.y / span * (if (invert) -1f else 1f)
                    onChange((value + step).coerceIn(0f, 1f))
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val trackTop = size.height * 0.08f
            val trackHeight = size.height * 0.84f
            drawRoundRect(
                color = Palette.Deep.copy(alpha = 0.85f),
                topLeft = Offset(size.width * 0.36f, trackTop),
                size = Size(size.width * 0.28f, trackHeight),
                cornerRadius = CornerRadius(size.width * 0.14f),
            )
            val knobY = trackTop + trackHeight * (1f - value)
            drawRoundRect(
                color = Palette.Grass,
                topLeft = Offset(size.width * 0.36f, knobY),
                size = Size(size.width * 0.28f, trackTop + trackHeight - knobY),
                cornerRadius = CornerRadius(size.width * 0.14f),
            )
            drawRoundRect(
                color = Palette.Cream,
                topLeft = Offset(size.width * 0.16f, knobY - size.height * 0.026f),
                size = Size(size.width * 0.68f, size.height * 0.052f),
                cornerRadius = CornerRadius(6f),
            )
        }
        Text(
            text = "LIFT",
            color = Palette.Steel,
            fontFamily = GameFonts.hud,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp),
        )
    }
}

@Composable
private fun Controls(hud: Hud, onWinch: (Float) -> Unit, onRelease: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color.Transparent, Palette.Night.copy(alpha = 0.94f)))
            )
            .padding(start = 14.dp, end = 14.dp, top = 18.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            HoldButton("CABLE +", Palette.Sky, Modifier.weight(1f)) { onWinch(if (it) 1f else 0f) }
            HoldButton("CABLE -", Palette.Sky, Modifier.weight(1f)) { onWinch(if (it) -1f else 0f) }
            OrnateButton(
                text = "RELEASE",
                onClick = onRelease,
                modifier = Modifier.weight(1.1f).height(54.dp),
                fillColor = if (hud.hooked) Palette.Amber else Palette.Grass,
                enabled = hud.hooked,
                fontSize = 15,
            )
        }
        Text(
            text = if (hud.hooked) "Bring it over the pad and let it touch down slowly"
            else "Lower the hook onto the load to pick it up",
            color = Palette.Steel,
            fontFamily = GameFonts.hud,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}

@Composable
private fun HoldButton(
    text: String,
    fill: Color,
    modifier: Modifier,
    onHold: (Boolean) -> Unit,
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onHold(true)
                    waitForUpOrCancellation()
                    onHold(false)
                }
            }
    ) {
        PlateFace(text, Modifier.fillMaxSize(), fill, fontSize = 14)
    }
}

@Composable
private fun PauseCurtain(mission: Mission, onResume: () -> Unit, onQuit: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Palette.Night.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(30.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenTitle("Holding", size = 30)
            Text(
                text = mission.title,
                color = Palette.Amber,
                fontFamily = GameFonts.hud,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 22.dp),
            )
            OrnateButton(
                text = "Resume",
                onClick = onResume,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                fillColor = Palette.Grass,
            )
            Spacer(Modifier.height(11.dp))
            OrnateButton(
                text = "Abort Mission",
                onClick = onQuit,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                fillColor = Palette.Alert,
            )
        }
    }
}
