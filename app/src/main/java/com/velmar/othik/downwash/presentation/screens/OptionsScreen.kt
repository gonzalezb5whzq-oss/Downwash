package com.velmar.othik.downwash.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velmar.othik.downwash.domain.Options
import com.velmar.othik.downwash.presentation.kit.Backdrop
import com.velmar.othik.downwash.presentation.kit.GameFonts
import com.velmar.othik.downwash.presentation.kit.OrnateButton
import com.velmar.othik.downwash.presentation.kit.Palette
import com.velmar.othik.downwash.presentation.kit.Panel
import com.velmar.othik.downwash.presentation.kit.ScreenTitle
import com.velmar.othik.downwash.presentation.kit.SwitchPlate
import com.velmar.othik.downwash.presentation.kit.ValleyBackdrop
import com.velmar.othik.downwash.presentation.kit.drawableId

@Composable
fun OptionsScreen(options: Options, onChange: (Options) -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }
    Box(Modifier.fillMaxSize()) {
        ValleyBackdrop(options.backdrop, dim = 0.68f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OrnateButton("Back", onBack, Modifier.height(44.dp), fillColor = Palette.Sky, fontSize = 15)
                ScreenTitle("Settings", size = 24)
                Spacer(Modifier.width(70.dp))
            }
            Text(
                text = "Home valley",
                color = Palette.Gold,
                fontFamily = GameFonts.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(Backdrop.entries.toList()) { entry ->
                    val index = Backdrop.entries.indexOf(entry)
                    val id = drawableId(entry.asset)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .width(98.dp)
                                .height(146.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = if (index == options.backdrop) 3.dp else 0.dp,
                                    color = if (index == options.backdrop) Palette.Amber else Palette.Night,
                                    shape = RoundedCornerShape(16.dp),
                                )
                                .clickable { onChange(options.copy(backdrop = index)) }
                        ) {
                            if (id != 0) {
                                Image(
                                    painter = painterResource(id),
                                    contentDescription = null,
                                    modifier = Modifier.matchParentSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                        Text(
                            text = entry.label,
                            color = if (index == options.backdrop) Palette.Cream else Palette.Steel,
                            fontFamily = GameFonts.hud,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 5.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            OptionRow("Sound effects", options.sound) { onChange(options.copy(sound = it)) }
            Spacer(Modifier.height(10.dp))
            OptionRow("Vibration", options.vibration) { onChange(options.copy(vibration = it)) }
            Spacer(Modifier.height(10.dp))
            OptionRow("Invert lift lever", options.invertLift) { onChange(options.copy(invertLift = it)) }
            Spacer(Modifier.weight(1f))
            Panel(Modifier.fillMaxWidth(), alpha = 0.62f) {
                Text(
                    text = "A heavier load swings wider and pulls the machine harder. Shorten the cable before you move, and let the swing die before you set down.",
                    color = Palette.Steel,
                    fontFamily = GameFonts.hud,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun OptionRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Palette.Night.copy(alpha = 0.76f))
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = Palette.Cream,
            fontFamily = GameFonts.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
        )
        SwitchPlate(checked, onChange)
    }
}
