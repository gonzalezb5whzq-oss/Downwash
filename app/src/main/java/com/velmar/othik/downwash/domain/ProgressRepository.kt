package com.velmar.othik.downwash.domain

import com.velmar.othik.downwash.domain.model.CargoKind

data class Options(
    val sound: Boolean = true,
    val vibration: Boolean = true,
    val backdrop: Int = 0,
    val invertLift: Boolean = false,
)

data class Logbook(
    val cleared: Int = 0,
    val stars: Int = 0,
    val flights: Int = 0,
    val delivered: Int = 0,
    val wrecks: Int = 0,
    val fuelBurned: Int = 0,
    val longestCable: Int = 0,
)

interface ProgressRepository {
    fun starsFor(index: Int): Int
    fun unlocked(index: Int): Boolean
    fun totalStars(): Int
    fun clearedCount(): Int
    fun furthestOpen(): Int
    fun saveClear(index: Int, stars: Int)
    fun logFlight(delivered: Int, wrecked: Boolean, fuelBurned: Int, longestCable: Int)
    fun logbook(): Logbook
    fun deliveries(kind: CargoKind): Int
    fun addDeliveries(counts: Map<CargoKind, Int>)
    fun awardUnlocked(id: String): Boolean
    fun unlockAward(id: String): Boolean
    fun options(): Options
    fun saveOptions(options: Options)
    fun tutorialSeen(): Boolean
    fun markTutorialSeen()
}
