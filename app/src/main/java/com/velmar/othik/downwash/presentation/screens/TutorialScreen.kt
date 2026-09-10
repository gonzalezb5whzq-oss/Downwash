package com.velmar.othik.downwash.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velmar.othik.downwash.presentation.kit.GameFonts
import com.velmar.othik.downwash.presentation.kit.OrnateButton
import com.velmar.othik.downwash.presentation.kit.Palette
import com.velmar.othik.downwash.presentation.kit.ValleyBackdrop
import com.velmar.othik.downwash.presentation.kit.drawableId

private data class Card(val title: String, val body: String, val sketch: Int)

private val cards = listOf(
    Card(
        "The Job",
        "You fly a rescue helicopter with a load hanging on a steel cable. Pick it up, carry it across the valley, set it down on the pad. The flying is the easy half.",
        0,
    ),
    Card(
        "Flying",
        "Drag the left half of the screen to tilt the machine — it accelerates the way you lean. The lever on the right is the collective: hold it up to climb, ease off to sink.",
        1,
    ),
    Card(
        "The Swing",
        "The load is a pendulum, and it pulls the helicopter back. Chase a swinging load and you make it worse. Fly smoothly, let the cable settle, then move.",
        2,
    ),
    Card(
        "The Winch",
        "WINCH shortens or lengthens the cable. A short cable swings fast but stays under you; a long one hangs steady but reaches into gullies. Lower the hook onto a load to pick it up.",
        3,
    ),
    Card(
        "Setting Down",
        "Bring the load over the pad and let it touch down slowly — drop it hard and it takes damage. Fuel burns the whole time, and burning it on a hover is the usual way to lose.",
        4,
    ),
)

@Composable
fun TutorialScreen(onBegin: () -> Unit) {
    val logo = drawableId("logo")
    Box(Modifier.fillMaxSize()) {
        ValleyBackdrop(0, dim = 0.66f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (logo != 0) {
                    Image(painterResource(logo), null, Modifier.size(76.dp))
                }
                Text(
                    text = "Flight Notes",
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                )
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(cards) { card -> NoteCard(card) }
                item { Spacer(Modifier.height(6.dp)) }
            }
            Spacer(Modifier.height(12.dp))
            OrnateButton(
                text = "Begin!",
                onClick = onBegin,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                fillColor = Palette.Amber,
                fontSize = 22,
            )
        }
    }
}

@Composable
private fun NoteCard(card: Card) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Palette.Night.copy(alpha = 0.76f))
            .padding(16.dp)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Palette.Deep.copy(alpha = 0.85f))
            ) {
                Sketch(card.sketch)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = card.title,
                color = Palette.Amber,
                fontFamily = GameFonts.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
            )
            Text(
                text = card.body,
                color = Palette.Cream,
                fontFamily = GameFonts.hud,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

@Composable
private fun Sketch(kind: Int) {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val body = Palette.Cream
        val accent = Palette.Amber
        val wire = Palette.Steel

        fun heli(cx: Float, cy: Float, scale: Float) {
            drawLine(body, Offset(cx - 30f * scale, cy - 14f * scale), Offset(cx + 30f * scale, cy - 14f * scale), 4f)
            drawRoundRect(
                color = accent,
                topLeft = Offset(cx - 14f * scale, cy - 10f * scale),
                size = androidx.compose.ui.geometry.Size(28f * scale, 16f * scale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f),
            )
            drawLine(body, Offset(cx + 12f * scale, cy - 4f * scale), Offset(cx + 30f * scale, cy - 6f * scale), 3f)
        }

        when (kind) {
            0 -> {
                heli(w * 0.5f, h * 0.30f, 1.3f)
                drawLine(wire, Offset(w * 0.5f, h * 0.36f), Offset(w * 0.5f, h * 0.72f), 3f)
                drawRoundRect(
                    color = Palette.Sky,
                    topLeft = Offset(w * 0.5f - 15f, h * 0.72f),
                    size = androidx.compose.ui.geometry.Size(30f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f),
                )
                drawLine(Palette.Grass, Offset(w * 0.2f, h * 0.94f), Offset(w * 0.8f, h * 0.94f), 5f)
            }
            1 -> {
                heli(w * 0.35f, h * 0.45f, 1.2f)
                drawCircle(Palette.Sky.copy(alpha = 0.35f), 26f, Offset(w * 0.72f, h * 0.55f))
                drawCircle(Palette.Cream, 10f, Offset(w * 0.78f, h * 0.45f))
                drawRoundRect(
                    color = Palette.Grass.copy(alpha = 0.6f),
                    topLeft = Offset(w * 0.90f, h * 0.18f),
                    size = androidx.compose.ui.geometry.Size(16f, h * 0.64f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f),
                )
            }
            2 -> {
                heli(w * 0.5f, h * 0.22f, 1.1f)
                listOf(-0.22f to 0.30f, 0f to 0.34f, 0.22f to 0.30f).forEachIndexed { index, pair ->
                    val alpha = if (index == 1) 1f else 0.35f
                    drawLine(
                        wire.copy(alpha = alpha),
                        Offset(w * 0.5f, h * 0.28f),
                        Offset(w * (0.5f + pair.first), h * (0.28f + pair.second * 2f)),
                        3f,
                    )
                    drawCircle(
                        Palette.Sky.copy(alpha = alpha),
                        12f,
                        Offset(w * (0.5f + pair.first), h * (0.28f + pair.second * 2f)),
                    )
                }
            }
            3 -> {
                heli(w * 0.28f, h * 0.28f, 1.0f)
                drawLine(wire, Offset(w * 0.28f, h * 0.34f), Offset(w * 0.28f, h * 0.60f), 3f)
                drawCircle(Palette.Sky, 11f, Offset(w * 0.28f, h * 0.62f))
                heli(w * 0.72f, h * 0.28f, 1.0f)
                drawLine(wire, Offset(w * 0.72f, h * 0.34f), Offset(w * 0.72f, h * 0.86f), 3f)
                drawCircle(Palette.Sky, 11f, Offset(w * 0.72f, h * 0.88f))
                drawLine(accent, Offset(w * 0.46f, h * 0.5f), Offset(w * 0.54f, h * 0.5f), 4f)
            }
            else -> {
                heli(w * 0.5f, h * 0.20f, 1.0f)
                drawLine(wire, Offset(w * 0.5f, h * 0.26f), Offset(w * 0.5f, h * 0.60f), 3f)
                drawRoundRect(
                    color = Palette.Sky,
                    topLeft = Offset(w * 0.5f - 14f, h * 0.60f),
                    size = androidx.compose.ui.geometry.Size(28f, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f),
                )
                drawCircle(Color.Transparent, 1f, Offset.Zero)
                drawOval(
                    color = accent.copy(alpha = 0.55f),
                    topLeft = Offset(w * 0.5f - 46f, h * 0.82f),
                    size = androidx.compose.ui.geometry.Size(92f, 22f),
                )
            }
        }
    }
}
