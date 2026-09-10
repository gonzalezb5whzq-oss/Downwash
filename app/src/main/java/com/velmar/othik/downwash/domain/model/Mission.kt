package com.velmar.othik.downwash.domain.model

data class Region(val title: String, val brief: String, val backdrop: Int)

data class Mission(
    val index: Int,
    val region: Int,
    val title: String,
    val cargo: List<CargoKind>,
    val fuel: Float,
    val wind: Float,
    val gust: Float,
    val pines: Int,
    val rocks: Int,
    val pylons: Int,
    val padRadius: Float,
    val settleSpeed: Float,
    val cableMax: Float,
) {
    fun brief(): String {
        val loads = if (cargo.size == 1) cargo[0].label else "${cargo.size} loads"
        return "Sling $loads to the pad"
    }
}

object Regions {
    val all = listOf(
        Region("Pine Basin", "Still air over the treeline. Learn the swing.", 0),
        Region("The Quarry", "Tight benches and hard rock. No room to drift.", 1),
        Region("Powerline Run", "Thread the towers. The cable is longer than you think.", 0),
        Region("Coast Shelf", "A steady crosswind that never lets go.", 2),
        Region("Night Ridge", "Gusts in the dark, and the longest carries of the job.", 3),
    )
    const val PER_REGION = 6
}

object Missions {
    val all: List<Mission> = buildList {
        val rows = listOf(
            Row("First Lift", listOf(CargoKind.CRATE), 150f, 0f, 0f, 6, 2, 0, 5.5f, 4.0f, 10f),
            Row("Short Hop", listOf(CargoKind.CRATE), 140f, 0f, 0f, 9, 3, 0, 5.0f, 3.6f, 10f),
            Row("Over The Stand", listOf(CargoKind.CRATE), 135f, 1.1f, 0f, 14, 3, 0, 4.6f, 3.4f, 11f),
            Row("Basket Case", listOf(CargoKind.BASKET), 130f, 1.2f, 0f, 12, 4, 0, 4.4f, 3.0f, 11f),
            Row("Two Runs", listOf(CargoKind.CRATE, CargoKind.CRATE), 175f, 1.3f, 0.6f, 12, 4, 0, 4.6f, 3.4f, 11f),
            Row("Basin Sign-Off", listOf(CargoKind.BASKET, CargoKind.CRATE), 170f, 1.6f, 0.8f, 15, 5, 0, 4.2f, 3.0f, 12f),

            Row("Bench One", listOf(CargoKind.CRATE), 130f, 1.4f, 0.7f, 2, 12, 0, 4.2f, 3.2f, 11f),
            Row("Drum Duty", listOf(CargoKind.DRUM), 128f, 1.5f, 0.8f, 3, 12, 0, 4.2f, 2.6f, 11f),
            Row("Narrow Bench", listOf(CargoKind.CRATE), 120f, 1.8f, 1.0f, 2, 16, 0, 3.6f, 3.0f, 10f),
            Row("Two Drums", listOf(CargoKind.DRUM, CargoKind.DRUM), 165f, 1.8f, 1.0f, 3, 14, 0, 4.0f, 2.6f, 11f),
            Row("Scree Slope", listOf(CargoKind.BASKET), 118f, 2.0f, 1.2f, 4, 18, 0, 3.6f, 2.8f, 10f),
            Row("Quarry Sign-Off", listOf(CargoKind.DRUM, CargoKind.BASKET), 160f, 2.2f, 1.3f, 4, 18, 0, 3.4f, 2.5f, 11f),

            Row("Under The Wires", listOf(CargoKind.CRATE), 130f, 1.6f, 0.9f, 8, 3, 4, 4.2f, 3.2f, 12f),
            Row("Tower Gap", listOf(CargoKind.CRATE), 125f, 1.8f, 1.0f, 8, 3, 6, 4.0f, 3.0f, 12f),
            Row("Long Line", listOf(CargoKind.DRUM), 130f, 1.8f, 1.1f, 10, 4, 6, 4.0f, 2.6f, 14f),
            Row("Slalom", listOf(CargoKind.BASKET), 122f, 2.0f, 1.2f, 10, 4, 8, 3.8f, 2.8f, 13f),
            Row("Double Thread", listOf(CargoKind.CRATE, CargoKind.DRUM), 168f, 2.1f, 1.3f, 10, 5, 8, 3.8f, 2.6f, 13f),
            Row("Powerline Sign-Off", listOf(CargoKind.BASKET, CargoKind.DRUM), 162f, 2.3f, 1.5f, 12, 5, 9, 3.4f, 2.4f, 14f),

            Row("Onshore", listOf(CargoKind.CRATE), 126f, 3.0f, 1.2f, 5, 8, 2, 4.0f, 3.0f, 12f),
            Row("Spit Landing", listOf(CargoKind.BASKET), 122f, 3.2f, 1.4f, 5, 9, 2, 3.8f, 2.8f, 12f),
            Row("Crosswind Drum", listOf(CargoKind.DRUM), 120f, 3.4f, 1.5f, 6, 10, 3, 3.6f, 2.4f, 12f),
            Row("Squall", listOf(CargoKind.CRATE), 116f, 3.6f, 2.0f, 6, 10, 3, 3.4f, 2.8f, 11f),
            Row("Shelf Pair", listOf(CargoKind.DRUM, CargoKind.BASKET), 160f, 3.6f, 1.8f, 7, 11, 4, 3.4f, 2.4f, 12f),
            Row("Coast Sign-Off", listOf(CargoKind.BASKET, CargoKind.BASKET), 155f, 3.9f, 2.1f, 8, 12, 4, 3.2f, 2.2f, 13f),

            Row("Last Light", listOf(CargoKind.CRATE), 124f, 3.2f, 1.8f, 14, 8, 4, 3.6f, 2.8f, 13f),
            Row("Cold Winch", listOf(CargoKind.DRUM), 118f, 3.4f, 2.0f, 15, 8, 5, 3.4f, 2.4f, 14f),
            Row("Ridge Carry", listOf(CargoKind.BASKET), 116f, 3.6f, 2.2f, 16, 9, 6, 3.2f, 2.4f, 15f),
            Row("Blackout", listOf(CargoKind.CRATE, CargoKind.BASKET), 158f, 3.8f, 2.4f, 16, 10, 6, 3.2f, 2.4f, 14f),
            Row("Three Loads", listOf(CargoKind.CRATE, CargoKind.DRUM, CargoKind.BASKET), 200f, 3.8f, 2.4f, 16, 10, 7, 3.2f, 2.3f, 14f),
            Row("Downwash", listOf(CargoKind.DRUM, CargoKind.BASKET, CargoKind.BASKET), 195f, 4.2f, 2.8f, 18, 11, 8, 3.0f, 2.1f, 15f),
        )
        rows.forEachIndexed { index, row ->
            add(
                Mission(
                    index = index,
                    region = index / Regions.PER_REGION,
                    title = row.title,
                    cargo = row.cargo,
                    fuel = row.fuel,
                    wind = row.wind,
                    gust = row.gust,
                    pines = row.pines,
                    rocks = row.rocks,
                    pylons = row.pylons,
                    padRadius = row.padRadius,
                    settleSpeed = row.settleSpeed,
                    cableMax = row.cableMax,
                )
            )
        }
    }

    val count: Int get() = all.size

    fun byRegion(region: Int): List<Mission> = all.filter { it.region == region }

    fun poolSize(kind: CargoKind): Int = maxOf(all.maxOf { m -> m.cargo.count { it == kind } }, 1)

    fun maxProps(kind: PropKind): Int = when (kind) {
        PropKind.PINE -> all.maxOf { it.pines }
        PropKind.ROCK -> all.maxOf { it.rocks }
        PropKind.PYLON -> all.maxOf { it.pylons }
    }

    private data class Row(
        val title: String,
        val cargo: List<CargoKind>,
        val fuel: Float,
        val wind: Float,
        val gust: Float,
        val pines: Int,
        val rocks: Int,
        val pylons: Int,
        val padRadius: Float,
        val settleSpeed: Float,
        val cableMax: Float,
    )
}
