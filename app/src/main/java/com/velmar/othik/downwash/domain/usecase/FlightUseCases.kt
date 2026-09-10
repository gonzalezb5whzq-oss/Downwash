package com.velmar.othik.downwash.domain.usecase

import com.velmar.othik.downwash.domain.FlightEngine
import com.velmar.othik.downwash.domain.Phase
import com.velmar.othik.downwash.domain.ProgressRepository
import com.velmar.othik.downwash.domain.model.Award
import com.velmar.othik.downwash.domain.model.Awards
import com.velmar.othik.downwash.domain.model.CargoKind
import com.velmar.othik.downwash.domain.model.Mission
import com.velmar.othik.downwash.domain.model.Missions
import com.velmar.othik.downwash.domain.model.Regions

class LaunchMission {
    operator fun invoke(mission: Mission): FlightEngine = FlightEngine(mission, System.nanoTime())
}

class AdvanceFlight {
    operator fun invoke(engine: FlightEngine, dt: Float) = engine.update(dt)
}

data class Debrief(
    val cleared: Boolean,
    val stars: Int,
    val delivered: Int,
    val total: Int,
    val fuelLeft: Int,
    val damage: Int,
    val awards: List<Award>,
)

class SettleMission(private val repo: ProgressRepository) {

    operator fun invoke(engine: FlightEngine): Debrief {
        val mission = engine.mission
        val cleared = engine.phase == Phase.CLEARED
        val stars = engine.stars()
        val burned = (mission.fuel - engine.fuel).toInt()
        repo.logFlight(engine.delivered, !cleared, burned, engine.longestCarry.toInt())
        if (cleared) {
            repo.saveClear(mission.index, stars)
            val counts = HashMap<CargoKind, Int>()
            engine.loads.filter { it.placed }.forEach { counts[it.kind] = (counts[it.kind] ?: 0) + 1 }
            repo.addDeliveries(counts)
        }
        return Debrief(
            cleared = cleared,
            stars = stars,
            delivered = engine.delivered,
            total = engine.total,
            fuelLeft = engine.fuel.toInt(),
            damage = (engine.damage * 100f).toInt(),
            awards = grant(engine, cleared, stars),
        )
    }

    private fun grant(engine: FlightEngine, cleared: Boolean, stars: Int): List<Award> {
        val mission = engine.mission
        val hits = ArrayList<String>()
        if (engine.delivered > 0 || engine.loads.any { it.carried }) hits.add("first_hook")
        if (engine.delivered > 0) hits.add("first_drop")
        if (repo.clearedCount() >= 5) hits.add("five_runs")
        if (Missions.byRegion(mission.region).all { repo.starsFor(it.index) > 0 }) hits.add("region_done")
        if (cleared && engine.damage < 0.02f) hits.add("no_damage")
        if (cleared && engine.fuel > mission.fuel * 0.5f) hits.add("thrifty")
        if (cleared && engine.longestCarry >= 12f) hits.add("long_line")
        if (cleared && mission.cargo.size == 2) hits.add("double")
        if (cleared && mission.cargo.size >= 3) hits.add("triple")
        if (cleared && engine.loads.any { it.kind == CargoKind.BASKET && it.placed && it.damage < 0.02f }) {
            hits.add("basket")
        }
        if (cleared && mission.wind >= 3.8f) hits.add("gale")
        if ((0 until Missions.count).count { repo.starsFor(it) >= 3 } >= 10) hits.add("all_stars")
        if (cleared && mission.region == Regions.all.lastIndex) hits.add("night")
        if (repo.clearedCount() >= Missions.count) hits.add("grand")
        if (stars >= 3) hits.add("no_damage")
        return hits.distinct().filter { repo.unlockAward(it) }.mapNotNull { Awards.byId(it) }
    }
}
