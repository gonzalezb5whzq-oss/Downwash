package com.velmar.othik.downwash.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velmar.othik.downwash.domain.ProgressRepository
import com.velmar.othik.downwash.domain.model.Awards
import com.velmar.othik.downwash.presentation.kit.GameFonts
import com.velmar.othik.downwash.presentation.kit.IconChip
import com.velmar.othik.downwash.presentation.kit.OrnateButton
import com.velmar.othik.downwash.presentation.kit.Palette
import com.velmar.othik.downwash.presentation.kit.ScreenTitle
import com.velmar.othik.downwash.presentation.kit.ValleyBackdrop
import com.velmar.othik.downwash.presentation.kit.drawableId

@Composable
fun AwardsScreen(repo: ProgressRepository, backdrop: Int, onBack: () -> Unit) {
    BackHandler { onBack() }
    val medal = drawableId("ic_medal")
    val unlocked = Awards.all.count { repo.awardUnlocked(it.id) }
    Box(Modifier.fillMaxSize()) {
        ValleyBackdrop(backdrop, dim = 0.70f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OrnateButton("Back", onBack, Modifier.height(44.dp), fillColor = Palette.Sky, fontSize = 15)
                ScreenTitle("Awards", size = 24)
                IconChip("ic_medal", "$unlocked/${Awards.all.size}")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(Awards.all) { award ->
                    val open = repo.awardUnlocked(award.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Palette.Night.copy(alpha = if (open) 0.80f else 0.55f))
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (medal != 0) {
                            Image(
                                painter = painterResource(medal),
                                contentDescription = null,
                                modifier = Modifier.size(42.dp),
                                alpha = if (open) 1f else 0.20f,
                            )
                        }
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(
                                text = award.title,
                                color = if (open) Palette.Gold else Palette.Steel,
                                fontFamily = GameFonts.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                            )
                            Text(
                                text = award.detail,
                                color = if (open) Palette.Cream else Palette.Steel,
                                fontFamily = GameFonts.hud,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
