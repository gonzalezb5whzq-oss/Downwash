package com.velmar.othik.downwash.presentation.kit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Backdrop(val asset: String, val label: String) {
    BASIN("bg_menu_basin", "Pine Basin"),
    QUARRY("bg_menu_quarry", "The Quarry"),
    COAST("bg_menu_coast", "Coast Shelf"),
    RIDGE("bg_menu_ridge", "Night Ridge"),
}

fun backdropFor(index: Int): Backdrop = Backdrop.entries[index.coerceIn(0, Backdrop.entries.lastIndex)]

@Composable
fun drawableId(name: String): Int {
    val context = LocalContext.current
    return context.resources.getIdentifier(name, "drawable", context.packageName)
}

fun plateFor(fill: Color): String = when (fill) {
    Palette.Sky -> "btn_plate_blue"
    Palette.Amber, Palette.Gold -> "btn_plate_gold"
    Palette.Alert -> "btn_plate_red"
    else -> "btn_plate_green"
}

@Composable
fun OrnateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fillColor: Color = Palette.Grass,
    textColor: Color? = null,
    enabled: Boolean = true,
    fontSize: Int = 19,
) {
    val context = LocalContext.current
    val plateId = remember(fillColor) {
        context.resources.getIdentifier(plateFor(fillColor), "drawable", context.packageName)
    }
    val alpha = if (enabled) 1f else 0.42f
    val label = (textColor ?: if (fillColor == Palette.Gold) Palette.Night else Palette.Cream).copy(alpha = alpha)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (plateId != 0) {
            Image(
                painter = painterResource(plateId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds,
                alpha = alpha,
            )
        } else {
            Box(Modifier.matchParentSize().background(fillColor.copy(alpha = alpha)))
        }
        Text(
            text = text,
            color = label,
            fontFamily = GameFonts.primary,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
fun PlateFace(
    text: String,
    modifier: Modifier = Modifier,
    fillColor: Color = Palette.Grass,
    fontSize: Int = 16,
) {
    val context = LocalContext.current
    val plateId = remember(fillColor) {
        context.resources.getIdentifier(plateFor(fillColor), "drawable", context.packageName)
    }
    Box(modifier.clip(RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
        if (plateId != 0) {
            Image(
                painter = painterResource(plateId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds,
            )
        } else {
            Box(Modifier.matchParentSize().background(fillColor))
        }
        Text(
            text = text,
            color = if (fillColor == Palette.Gold) Palette.Night else Palette.Cream,
            fontFamily = GameFonts.primary,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ValleyBackdrop(index: Int, dim: Float = 0.55f) {
    val id = drawableId(backdropFor(index).asset)
    Box(Modifier.fillMaxSize().background(Palette.Night)) {
        if (id != 0) {
            Image(
                painter = painterResource(id),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Palette.Night.copy(alpha = dim + 0.20f),
                        Palette.Deep.copy(alpha = dim),
                        Palette.Night.copy(alpha = dim + 0.28f),
                    )
                )
            )
        )
    }
}

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier, size: Int = 30) {
    Text(
        text = text,
        color = Palette.Cream,
        fontFamily = GameFonts.primary,
        fontWeight = FontWeight.Bold,
        fontSize = size.sp,
        modifier = modifier,
    )
}

@Composable
fun StarStrip(earned: Int, total: Int = 3, star: Int = 18, modifier: Modifier = Modifier) {
    val id = drawableId("ic_star")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(total) { index ->
            if (id != 0) {
                Image(
                    painter = painterResource(id),
                    contentDescription = null,
                    modifier = Modifier.size(star.dp),
                    alpha = if (index < earned) 1f else 0.20f,
                )
            }
        }
    }
}

@Composable
fun IconChip(
    asset: String,
    label: String,
    modifier: Modifier = Modifier,
    tint: Color = Palette.Cream,
) {
    val id = drawableId(asset)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.Night.copy(alpha = 0.66f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (id != 0) {
            Image(painter = painterResource(id), contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Text(
            text = label,
            color = tint,
            fontFamily = GameFonts.hud,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
    }
}

@Composable
fun SwitchPlate(checked: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val slide by animateFloatAsState(if (checked) 1f else 0f, label = "switch")
    Canvas(
        modifier = modifier
            .width(62.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onChange(!checked) }
    ) {
        drawRoundRect(
            color = if (checked) Palette.Grass.copy(alpha = 0.85f) else Palette.Steel.copy(alpha = 0.35f),
            size = size,
            cornerRadius = CornerRadius(6f),
        )
        val knobWidth = size.width * 0.44f
        val travel = size.width - knobWidth - size.height * 0.16f
        drawRoundRect(
            color = Palette.Cream,
            topLeft = Offset(size.height * 0.08f + travel * slide, size.height * 0.12f),
            size = Size(knobWidth, size.height * 0.76f),
            cornerRadius = CornerRadius(5f),
        )
        drawRoundRect(
            color = if (checked) Palette.Amber else Palette.Steel,
            topLeft = Offset(size.height * 0.08f + travel * slide + knobWidth * 0.36f, size.height * 0.30f),
            size = Size(knobWidth * 0.28f, size.height * 0.40f),
            cornerRadius = CornerRadius(3f),
        )
    }
}

@Composable
fun Panel(modifier: Modifier = Modifier, alpha: Float = 0.72f, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Palette.Night.copy(alpha = alpha))
            .padding(horizontal = 15.dp, vertical = 13.dp)
    ) { content() }
}
