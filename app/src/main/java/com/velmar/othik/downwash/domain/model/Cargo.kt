package com.velmar.othik.downwash.domain.model

enum class CargoKind(
    val label: String,
    val asset: String,
    val size: Float,
    val radius: Float,
    val mass: Float,
    val fragility: Float,
) {
    CRATE("Supply Crate", "models/crate.glb", 1.90f, 0.95f, 620f, 0.55f),
    DRUM("Fuel Drum", "models/drum.glb", 1.80f, 0.72f, 780f, 1.00f),
    BASKET("Rescue Basket", "models/basket.glb", 2.00f, 0.80f, 460f, 1.35f);

    val rest: Float get() = size * 0.5f
}

enum class PropKind(val asset: String, val size: Float, val radius: Float) {
    PINE("models/pine.glb", 7.5f, 1.5f),
    ROCK("models/rock.glb", 4.4f, 2.2f),
    PYLON("models/pylon.glb", 15.0f, 1.6f),
}

data class Prop(
    val kind: PropKind,
    val x: Float,
    val z: Float,
    val yaw: Float,
) {
    val radius: Float get() = kind.radius
    val height: Float get() = kind.size
}
