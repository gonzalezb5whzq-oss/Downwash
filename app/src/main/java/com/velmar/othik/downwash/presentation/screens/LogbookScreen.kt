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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velmar.othik.downwash.domain.ProgressRepository
import com.velmar.othik.downwash.domain.model.CargoKind
import com.velmar.othik.downwash.domain.model.Missions
import com.velmar.othik.downwash.presentation.kit.GameFonts
import com.velmar.othik.downwash.presentation.kit.IconChip
import com.velmar.othik.downwash.presentation.kit.OrnateButton
import com.velmar.othik.downwash.presentation.kit.Palette
import com.velmar.othik.downwash.presentation.kit.Panel
import com.velmar.othik.downwash.presentation.kit.ScreenTitle
import com.velmar.othik.downwash.presentation.kit.ValleyBackdrop

@Composable
fun LogbookScreen(repo: ProgressRepository, backdrop: Int, onBack: () -> Unit) {
    BackHandler { onBack() }
    val book = repo.logbook()
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
                ScreenTitle("Logbook", size = 24)
                IconChip("ic_star", "${book.stars}")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Panel(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Line("Missions flown", "${book.flights}")
                            Line("Missions cleared", "${book.cleared} of ${Missions.count}")
                            Line("Stars earned", "${book.stars} of ${Missions.count * 3}")
                            Line("Loads delivered", "${book.delivered}")
                            Line("Machines lost", "${book.wrecks}")
                            Line("Fuel burned", "${book.fuelBurned}")
                            Line("Longest cable", "${book.longestCable} m")
                        }
                    }
                }
                item {
                    Text(
                        text = "Cargo manifest",
                        color = Palette.Gold,
                        fontFamily = GameFonts.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                items(CargoKind.entries.toList()) { kind ->
                    val count = repo.deliveries(kind)
                    Panel(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = kind.label,
                                    color = if (count > 0) Palette.Cream else Palette.Steel,
                                    fontFamily = GameFonts.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                )
                                Text(
                                    text = "${kind.mass.toInt()} kg on the hook",
                                    color = Palette.Steel,
                                    fontFamily = GameFonts.hud,
                                    fontSize = 12.sp,
                                )
                            }
                            Text(
                                text = "x$count",
                                color = if (count > 0) Palette.Grass else Palette.Steel,
                                fontFamily = GameFonts.hud,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
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
