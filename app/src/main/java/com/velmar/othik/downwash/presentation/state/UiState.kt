package com.velmar.othik.downwash.presentation.state

import com.velmar.othik.downwash.domain.FlightEngine
import com.velmar.othik.downwash.domain.Options
import com.velmar.othik.downwash.domain.Phase
import com.velmar.othik.downwash.domain.model.Award
import com.velmar.othik.downwash.domain.model.Mission
import com.velmar.othik.downwash.domain.usecase.Debrief

sealed interface Screen {
    data object Splash : Screen
    data object Tutorial : Screen
    data object Hangar : Screen
    data object Chart : Screen
    data class Flight(val index: Int) : Screen
    data object Debrief : Screen
    data object Logbook : Screen
    data object Awards : Screen
    data object Options : Screen
}

data class Hud(
    val fuel: Int = 0,
    val fuelPart: Float = 1f,
    val delivered: Int = 0,
    val total: Int = 1,
    val cable: Int = 6,
    val hooked: Boolean = false,
    val altitude: Int = 0,
    val damage: Int = 0,
    val phase: Phase = Phase.FLYING,
)

data class FlightSession(
    val mission: Mission,
    val engine: FlightEngine,
    val hud: Hud,
    val paused: Boolean = false,
    val debrief: Debrief? = null,
)

data class UiState(
    val screen: Screen = Screen.Splash,
    val stack: List<Screen> = emptyList(),
    val options: Options = Options(),
    val stars: Int = 0,
    val session: FlightSession? = null,
    val banner: Award? = null,
)
