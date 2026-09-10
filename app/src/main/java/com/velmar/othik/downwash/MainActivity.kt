package com.velmar.othik.downwash

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.velmar.othik.downwash.presentation.DownwashViewModel
import com.velmar.othik.downwash.presentation.kit.Palette
import com.velmar.othik.downwash.presentation.screens.AwardsScreen
import com.velmar.othik.downwash.presentation.screens.ChartScreen
import com.velmar.othik.downwash.presentation.screens.DebriefScreen
import com.velmar.othik.downwash.presentation.screens.FlightScreen
import com.velmar.othik.downwash.presentation.screens.HangarScreen
import com.velmar.othik.downwash.presentation.screens.LogbookScreen
import com.velmar.othik.downwash.presentation.screens.OptionsScreen
import com.velmar.othik.downwash.presentation.screens.SplashScreen
import com.velmar.othik.downwash.presentation.screens.TutorialScreen
import com.velmar.othik.downwash.presentation.state.Screen
import com.velmar.othik.downwash.ui.theme.DownwashTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        setContent {
            DownwashTheme {
                Box(Modifier.fillMaxSize().background(Palette.Night)) {
                    DownwashRoot()
                }
            }
        }
    }
}

@Composable
private fun DownwashRoot(viewModel: DownwashViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val activity = LocalContext.current as Activity

    when (val screen = state.screen) {
        Screen.Splash -> SplashScreen { viewModel.boot() }

        Screen.Tutorial -> TutorialScreen { viewModel.finishTutorial() }

        Screen.Hangar -> {
            BackHandler { activity.finish() }
            HangarScreen(
                backdrop = state.options.backdrop,
                stars = state.stars,
                cleared = viewModel.repo.clearedCount(),
                onFly = { viewModel.openMission(viewModel.repo.furthestOpen()) },
                onChart = { viewModel.go(Screen.Chart) },
                onAwards = { viewModel.go(Screen.Awards) },
                onLogbook = { viewModel.go(Screen.Logbook) },
                onOptions = { viewModel.go(Screen.Options) },
                onExit = { activity.finish() },
            )
        }

        Screen.Chart -> ChartScreen(
            repo = viewModel.repo,
            onOpen = { viewModel.openMission(it) },
            onBack = { viewModel.back() },
        )

        is Screen.Flight -> {
            val session = state.session
            if (session == null) {
                LaunchedEffect(screen) { viewModel.leave() }
            } else {
                FlightScreen(
                    mission = session.mission,
                    engine = session.engine,
                    hud = session.hud,
                    paused = session.paused,
                    invertLift = state.options.invertLift,
                    onStep = { viewModel.tick(it) },
                    onSteer = { x, z -> viewModel.steer(x, z) },
                    onLift = { viewModel.lift(it) },
                    onWinch = { viewModel.winch(it) },
                    onRelease = { viewModel.release() },
                    onPause = { viewModel.togglePause() },
                    onQuit = { viewModel.leave() },
                )
            }
        }

        Screen.Debrief -> {
            val session = state.session
            val debrief = session?.debrief
            if (session == null || debrief == null) {
                LaunchedEffect(screen) { viewModel.leave() }
            } else {
                DebriefScreen(
                    mission = session.mission,
                    debrief = debrief,
                    backdrop = state.options.backdrop,
                    onRetry = { viewModel.retry() },
                    onNext = { viewModel.nextMission() },
                    onChart = { viewModel.leave() },
                )
            }
        }

        Screen.Awards -> AwardsScreen(
            repo = viewModel.repo,
            backdrop = state.options.backdrop,
            onBack = { viewModel.back() },
        )

        Screen.Logbook -> LogbookScreen(
            repo = viewModel.repo,
            backdrop = state.options.backdrop,
            onBack = { viewModel.back() },
        )

        Screen.Options -> OptionsScreen(
            options = state.options,
            onChange = { viewModel.applyOptions(it) },
            onBack = { viewModel.back() },
        )
    }
}
