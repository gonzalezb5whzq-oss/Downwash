package com.velmar.othik.downwash.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velmar.othik.downwash.domain.ProgressRepository
import com.velmar.othik.downwash.domain.model.Mission
import com.velmar.othik.downwash.domain.model.Missions
import com.velmar.othik.downwash.domain.model.Regions
import com.velmar.othik.downwash.presentation.kit.GameFonts
import com.velmar.othik.downwash.presentation.kit.IconChip
import com.velmar.othik.downwash.presentation.kit.OrnateButton
import com.velmar.othik.downwash.presentation.kit.Palette
import com.velmar.othik.downwash.presentation.kit.Panel
import com.velmar.othik.downwash.presentation.kit.ScreenTitle
import com.velmar.othik.downwash.presentation.kit.StarStrip
import com.velmar.othik.downwash.presentation.kit.backdropFor
import com.velmar.othik.downwash.presentation.kit.drawableId

private val lane = listOf(0.24f, 0.52f, 0.79f, 0.55f, 0.26f, 0.52f)

@Composable
fun ChartScreen(repo: ProgressRepository, onOpen: (Int) -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }
    Box(Modifier.fillMaxSize().background(Palette.Night)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OrnateButton("Back", onBack, Modifier.height(44.dp), fillColor = Palette.Sky, fontSize = 15)
                ScreenTitle("Mission Chart", size = 24)
                IconChip("ic_star", "${repo.totalStars()}")
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                Regions.all.forEachIndexed { index, region ->
                    item(key = "head_$index") { RegionHeader(index, repo) }
                    item(key = "leg_$index") { RegionLeg(index, repo, onOpen) }
                }
                item { Spacer(Modifier.height(18.dp)) }
            }
        }
    }
}

@Composable
private fun RegionHeader(region: Int, repo: ProgressRepository) {
    val missions = Missions.byRegion(region)
    val earned = missions.sumOf { repo.starsFor(it.index) }
    val info = Regions.all[region]
    Panel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        alpha = 0.80f,
    ) {
        Column {
            Text(
                text = info.title,
                color = Palette.Amber,
                fontFamily = GameFonts.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
            Text(
                text = info.brief,
                color = Palette.Cream,
                fontFamily = GameFonts.hud,
                fontSize = 13.sp,
            )
            Text(
                text = "$earned of ${missions.size * 3} stars",
                color = Palette.Steel,
                fontFamily = GameFonts.hud,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

@Composable
private fun RegionLeg(region: Int, repo: ProgressRepository, onOpen: (Int) -> Unit) {
    val missions = Missions.byRegion(region)
    val art = drawableId(backdropFor(Regions.all[region].backdrop).asset)
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp)
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(22.dp))
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val nodePx = with(density) { 62.dp.toPx() }
        val points = missions.indices.map { slot ->
            Offset(
                widthPx * lane[slot % lane.size],
                heightPx * (0.10f + 0.80f * slot / (missions.size - 1).coerceAtLeast(1)),
            )
        }
        if (art != 0) {
            Image(
                painter = painterResource(art),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.34f,
            )
        }
        Box(Modifier.fillMaxSize().background(Palette.Night.copy(alpha = 0.42f)))
        Canvas(Modifier.fillMaxSize()) {
            val path = Path()
            points.forEachIndexed { index, point ->
                if (index == 0) {
                    path.moveTo(point.x, point.y)
                } else {
                    val previous = points[index - 1]
                    val midY = (previous.y + point.y) / 2f
                    path.cubicTo(previous.x, midY, point.x, midY, point.x, point.y)
                }
            }
            drawPath(
                path = path,
                color = Palette.Amber.copy(alpha = 0.55f),
                style = Stroke(width = 7f),
            )
        }
        missions.forEachIndexed { slot, mission ->
            val point = points[slot]
            ChartNode(
                mission = mission,
                repo = repo,
                onOpen = onOpen,
                modifier = Modifier.offset(
                    x = with(density) { (point.x - nodePx / 2f).toDp() },
                    y = with(density) { (point.y - nodePx / 2f).toDp() },
                ),
            )
        }
    }
}

@Composable
private fun ChartNode(
    mission: Mission,
    repo: ProgressRepository,
    onOpen: (Int) -> Unit,
    modifier: Modifier,
) {
    val open = repo.unlocked(mission.index)
    val stars = repo.starsFor(mission.index)
    val node = drawableId("btn_node")
    val lock = drawableId("ic_lock")
    Column(modifier.width(96.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        StarStrip(stars, star = 11)
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .clickable(enabled = open) { onOpen(mission.index) },
            contentAlignment = Alignment.Center,
        ) {
            if (node != 0) {
                Image(
                    painter = painterResource(node),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Fit,
                    alpha = if (open) 1f else 0.32f,
                )
            }
            if (open) {
                Text(
                    text = "${mission.index % Regions.PER_REGION + 1}",
                    color = Palette.Night,
                    fontFamily = GameFonts.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                )
            } else if (lock != 0) {
                Image(painterResource(lock), null, Modifier.size(20.dp))
            }
        }
        Text(
            text = mission.title,
            color = if (open) Palette.Cream else Palette.Steel,
            fontFamily = GameFonts.hud,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
