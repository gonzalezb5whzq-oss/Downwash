package com.velmar.othik.downwash.di

import com.velmar.othik.downwash.data.PrefsProgressRepository
import com.velmar.othik.downwash.data.audio.Haptics
import com.velmar.othik.downwash.data.audio.SoundManager
import com.velmar.othik.downwash.domain.ProgressRepository
import com.velmar.othik.downwash.domain.usecase.AdvanceFlight
import com.velmar.othik.downwash.domain.usecase.LaunchMission
import com.velmar.othik.downwash.domain.usecase.SettleMission
import com.velmar.othik.downwash.presentation.DownwashViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<ProgressRepository> { PrefsProgressRepository(androidContext()) }
    single { SoundManager(androidContext(), get()) }
    single { Haptics(androidContext(), get()) }
    factory { LaunchMission() }
    factory { AdvanceFlight() }
    factory { SettleMission(get()) }
    viewModel { DownwashViewModel(get(), get(), get(), get(), get(), get()) }
}
