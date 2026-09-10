package com.velmar.othik.downwash.data

import android.content.Context
import android.content.SharedPreferences
import com.velmar.othik.downwash.domain.Logbook
import com.velmar.othik.downwash.domain.Options
import com.velmar.othik.downwash.domain.ProgressRepository
import com.velmar.othik.downwash.domain.model.CargoKind
import com.velmar.othik.downwash.domain.model.Missions

class PrefsProgressRepository(context: Context) : ProgressRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("com.velmar.othik.downwash_game_v1", Context.MODE_PRIVATE)

    override fun starsFor(index: Int): Int = prefs.getInt("run_${index}_stars", 0)

    override fun unlocked(index: Int): Boolean =
        index == 0 || prefs.getBoolean("run_${index}_open", false)

    override fun totalStars(): Int = (0 until Missions.count).sumOf { starsFor(it) }

    override fun clearedCount(): Int = (0 until Missions.count).count { starsFor(it) > 0 }

    override fun furthestOpen(): Int = (0 until Missions.count).lastOrNull { unlocked(it) } ?: 0

    override fun saveClear(index: Int, stars: Int) {
        val editor = prefs.edit()
        if (stars > starsFor(index)) editor.putInt("run_${index}_stars", stars)
        if (index + 1 < Missions.count) editor.putBoolean("run_${index + 1}_open", true)
        editor.apply()
    }

    override fun logFlight(delivered: Int, wrecked: Boolean, fuelBurned: Int, longestCable: Int) {
        val book = logbook()
        prefs.edit()
            .putInt("log_flights", book.flights + 1)
            .putInt("log_delivered", book.delivered + delivered)
            .putInt("log_wrecks", book.wrecks + if (wrecked) 1 else 0)
            .putInt("log_fuel", book.fuelBurned + fuelBurned)
            .putInt("log_cable", maxOf(book.longestCable, longestCable))
            .apply()
    }

    override fun logbook(): Logbook = Logbook(
        cleared = clearedCount(),
        stars = totalStars(),
        flights = prefs.getInt("log_flights", 0),
        delivered = prefs.getInt("log_delivered", 0),
        wrecks = prefs.getInt("log_wrecks", 0),
        fuelBurned = prefs.getInt("log_fuel", 0),
        longestCable = prefs.getInt("log_cable", 0),
    )

    override fun deliveries(kind: CargoKind): Int = prefs.getInt("cargo_${kind.name}", 0)

    override fun addDeliveries(counts: Map<CargoKind, Int>) {
        val editor = prefs.edit()
        counts.forEach { (kind, amount) -> editor.putInt("cargo_${kind.name}", deliveries(kind) + amount) }
        editor.apply()
    }

    override fun awardUnlocked(id: String): Boolean = prefs.getBoolean("award_$id", false)

    override fun unlockAward(id: String): Boolean {
        if (awardUnlocked(id)) return false
        prefs.edit().putBoolean("award_$id", true).apply()
        return true
    }

    override fun options(): Options = Options(
        sound = prefs.getBoolean("sound", true),
        vibration = prefs.getBoolean("vibration", true),
        backdrop = prefs.getInt("backdrop", 0),
        invertLift = prefs.getBoolean("invert_lift", false),
    )

    override fun saveOptions(options: Options) {
        prefs.edit()
            .putBoolean("sound", options.sound)
            .putBoolean("vibration", options.vibration)
            .putInt("backdrop", options.backdrop)
            .putBoolean("invert_lift", options.invertLift)
            .apply()
    }

    override fun tutorialSeen(): Boolean = prefs.getBoolean("tutorial_seen", false)

    override fun markTutorialSeen() = prefs.edit().putBoolean("tutorial_seen", true).apply()
}
