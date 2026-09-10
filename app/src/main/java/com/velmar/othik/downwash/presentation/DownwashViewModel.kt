package com.velmar.othik.downwash.presentation

import androidx.lifecycle.ViewModel
import com.velmar.othik.downwash.data.audio.Haptics
import com.velmar.othik.downwash.data.audio.Sfx
import com.velmar.othik.downwash.data.audio.SoundManager
import com.velmar.othik.downwash.domain.FlightEvent
import com.velmar.othik.downwash.domain.Options
import com.velmar.othik.downwash.domain.Phase
import com.velmar.othik.downwash.domain.ProgressRepository
import com.velmar.othik.downwash.domain.model.Missions
import com.velmar.othik.downwash.domain.usecase.AdvanceFlight
import com.velmar.othik.downwash.domain.usecase.LaunchMission
import com.velmar.othik.downwash.domain.usecase.SettleMission
import com.velmar.othik.downwash.presentation.state.FlightSession
import com.velmar.othik.downwash.presentation.state.Hud
import com.velmar.othik.downwash.presentation.state.Screen
import com.velmar.othik.downwash.presentation.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DownwashViewModel(
    val repo: ProgressRepository,
    private val sound: SoundManager,
    private val haptics: Haptics,
    private val launch: LaunchMission,
    private val advance: AdvanceFlight,
    private val settle: SettleMission,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UiState(options = repo.options(), stars = repo.totalStars())
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var lastLift = 0f

    fun boot() {
        val next = if (repo.tutorialSeen()) Screen.Hangar else Screen.Tutorial
        _state.value = _state.value.copy(screen = next, stack = emptyList(), stars = repo.totalStars())
    }

    fun go(screen: Screen) {
        sound.play(Sfx.TAP)
        haptics.tick()
        _state.value = _state.value.copy(
            screen = screen,
            stack = _state.value.stack + _state.value.screen,
        )
    }

    fun back() {
        val current = _state.value
        if (current.stack.isEmpty()) {
            _state.value = current.copy(screen = Screen.Hangar, session = null)
            return
        }
        val target = current.stack[current.stack.lastIndex]
        sound.stopRotor()
        _state.value = current.copy(
            screen = target,
            stack = current.stack.subList(0, current.stack.lastIndex),
            stars = repo.totalStars(),
            session = if (target is Screen.Flight) current.session else null,
        )
    }

    fun openMission(index: Int) {
        val mission = Missions.all[index.coerceIn(0, Missions.count - 1)]
        val engine = launch(mission)
        sound.play(Sfx.TAP)
        sound.startRotor()
        lastLift = 0f
        _state.value = _state.value.copy(
            screen = Screen.Flight(mission.index),
            stack = listOf(Screen.Hangar, Screen.Chart),
            session = FlightSession(mission, engine, snapshot(engine)),
        )
    }

    fun finishTutorial() {
        repo.markTutorialSeen()
        _state.value = _state.value.copy(screen = Screen.Hangar, stack = emptyList())
    }

    fun steer(x: Float, z: Float) {
        _state.value.session?.engine?.steer(x, z)
    }

    fun lift(value: Float) {
        lastLift = value
        _state.value.session?.engine?.lift(value)
        sound.rotorPower(value)
    }

    fun winch(rate: Float) {
        val engine = _state.value.session?.engine ?: return
        engine.winch(rate)
        if (rate != 0f) haptics.tick()
    }

    fun release() {
        val engine = _state.value.session?.engine ?: return
        if (!engine.canRelease()) return
        engine.release()
    }

    fun togglePause() {
        val session = _state.value.session ?: return
        sound.play(Sfx.TAP)
        if (session.paused) sound.startRotor() else sound.stopRotor()
        _state.value = _state.value.copy(session = session.copy(paused = !session.paused))
    }

    fun leave() {
        sound.play(Sfx.TAP)
        sound.stopRotor()
        _state.value = _state.value.copy(
            screen = Screen.Chart,
            stack = listOf(Screen.Hangar),
            session = null,
        )
    }

    fun retry() {
        val index = _state.value.session?.mission?.index ?: 0
        _state.value = _state.value.copy(session = null)
        openMission(index)
    }

    fun nextMission() {
        val index = (_state.value.session?.mission?.index ?: 0) + 1
        if (index >= Missions.count) {
            leave()
        } else {
            _state.value = _state.value.copy(session = null)
            openMission(index)
        }
    }

    fun applyOptions(options: Options) {
        repo.saveOptions(options)
        sound.play(Sfx.TAP)
        haptics.tick()
        _state.value = _state.value.copy(options = options)
    }

    fun dismissBanner() {
        _state.value = _state.value.copy(banner = null)
    }

    fun tick(dt: Float) {
        val current = _state.value
        val session = current.session ?: return
        if (session.paused || session.debrief != null) return
        val engine = session.engine
        advance(engine, dt)
        var event = engine.poll()
        while (event != null) {
            react(event)
            event = engine.poll()
        }
        if (engine.phase != Phase.FLYING) {
            sound.stopRotor()
            val debrief = settle(engine)
            if (debrief.awards.isNotEmpty()) sound.play(Sfx.UNLOCK)
            _state.value = current.copy(
                screen = Screen.Debrief,
                stack = listOf(Screen.Hangar, Screen.Chart),
                stars = repo.totalStars(),
                session = session.copy(debrief = debrief, hud = snapshot(engine)),
                banner = debrief.awards.firstOrNull(),
            )
            return
        }
        val hud = snapshot(engine)
        if (hud != session.hud) {
            _state.value = current.copy(session = session.copy(hud = hud))
        }
    }

    private fun react(event: FlightEvent) {
        when (event) {
            FlightEvent.HOOK -> {
                sound.play(Sfx.HOOK)
                haptics.knock()
            }
            FlightEvent.RELEASE -> sound.play(Sfx.HOOK, 0.8f)
            FlightEvent.SETTLE -> {
                sound.play(Sfx.LAND)
                haptics.success()
            }
            FlightEvent.BUMP -> {
                sound.play(Sfx.LAND, 1.4f)
                haptics.knock()
            }
            FlightEvent.LOW_FUEL -> {
                sound.play(Sfx.WARN)
                haptics.error()
            }
            FlightEvent.CLEARED -> sound.play(Sfx.WIN)
            FlightEvent.CRASH -> {
                sound.play(Sfx.LOSE)
                haptics.error()
            }
        }
    }

    private fun snapshot(engine: com.velmar.othik.downwash.domain.FlightEngine): Hud = Hud(
        fuel = engine.fuel.toInt(),
        fuelPart = (engine.fuel / engine.mission.fuel).coerceIn(0f, 1f),
        delivered = engine.delivered,
        total = engine.total,
        cable = engine.cable.toInt(),
        hooked = engine.held != null,
        altitude = engine.hy.toInt(),
        damage = (engine.damage * 100f).toInt(),
        phase = engine.phase,
    )

    override fun onCleared() {
        sound.release()
        super.onCleared()
    }
}
