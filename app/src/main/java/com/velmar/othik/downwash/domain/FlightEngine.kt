package com.velmar.othik.downwash.domain

import com.velmar.othik.downwash.domain.model.CargoKind
import com.velmar.othik.downwash.domain.model.Mission
import com.velmar.othik.downwash.domain.model.Prop
import com.velmar.othik.downwash.domain.model.PropKind
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class Phase { FLYING, CLEARED, WRECKED, DRY }

enum class FlightEvent { HOOK, RELEASE, SETTLE, BUMP, LOW_FUEL, CLEARED, CRASH }

object Field {
    const val HALF_X = 26f
    const val HALF_Z = 34f
    const val CEILING = 30f
    const val HELI_RADIUS = 2.4f
    const val HOOK_DROP = 1.0f
    const val HOOK_REST = 0.4f
    const val HOOK_MASS = 90f
    const val HELI_MASS = 2600f
    const val GRAVITY = 9.81f
    const val LIFT = 16.5f
    const val SWAY = 7.6f
}

class Load(val kind: CargoKind) {
    var x = 0f
    var y = 0f
    var z = 0f
    var yaw = 0f
    var carried = false
    var placed = false
    var damage = 0f
}

class FlightEngine(val mission: Mission, seed: Long) {

    private val rng = Random(seed)
    private val queue = ArrayDeque<FlightEvent>()

    val loads: List<Load> = mission.cargo.map { Load(it) }
    val props = ArrayList<Prop>()

    var padX = 0f
        private set
    var padZ = 0f
        private set

    var hx = 0f
        private set
    var hy = 12f
        private set
    var hz = 0f
        private set
    var vx = 0f
        private set
    var vy = 0f
        private set
    var vz = 0f
        private set
    var tiltX = 0f
        private set
    var tiltZ = 0f
        private set
    var heading = 0f
        private set

    var bx = 0f
        private set
    var by = 0f
        private set
    var bz = 0f
        private set
    private var bvx = 0f
    private var bvy = 0f
    private var bvz = 0f

    var cable = 6f
        private set
    var fuel = mission.fuel
        private set
    var phase = Phase.FLYING
        private set
    var held: Load? = null
        private set
    var longestCarry = 0f
        private set

    private var wishX = 0f
    private var wishZ = 0f
    private var collective = Field.GRAVITY / Field.LIFT
    private var winchRate = 0f
    private var clock = 0f
    private var gustClock = 0f
    private var gustPower = 0f
    private var warned = false

    val delivered: Int get() = loads.count { it.placed }
    val total: Int get() = loads.size
    val damage: Float get() = loads.sumOf { it.damage.toDouble() }.toFloat() / loads.size
    val windX: Float get() = mission.wind + gustPower
    val windZ: Float get() = (mission.wind + gustPower) * 0.35f

    init {
        padX = -11f + rng.nextFloat() * 4f
        padZ = -18f - rng.nextFloat() * 6f
        val sourceX = 10f + rng.nextFloat() * 4f
        val sourceZ = 16f + rng.nextFloat() * 6f
        loads.forEachIndexed { index, load ->
            load.x = sourceX + (index - (loads.size - 1) * 0.5f) * 3.4f
            load.z = sourceZ + (index % 2) * 2.4f
            load.y = load.kind.rest
            load.yaw = rng.nextFloat() * 360f
        }
        hx = sourceX
        hz = sourceZ + 9f
        hy = 13f
        bx = hx
        by = hy - Field.HOOK_DROP - cable
        bz = hz
        scatter(PropKind.PINE, mission.pines)
        scatter(PropKind.ROCK, mission.rocks)
        scatter(PropKind.PYLON, mission.pylons)
    }

    fun poll(): FlightEvent? = queue.removeFirstOrNull()

    fun steer(x: Float, z: Float) {
        wishX = x.coerceIn(-1f, 1f)
        wishZ = z.coerceIn(-1f, 1f)
    }

    fun lift(value: Float) {
        collective = value.coerceIn(0f, 1f)
    }

    fun winch(rate: Float) {
        winchRate = rate.coerceIn(-1f, 1f)
    }

    fun canRelease(): Boolean = held != null

    fun release() {
        val load = held ?: return
        load.carried = false
        held = null
        queue.addLast(FlightEvent.RELEASE)
    }

    fun hookedCargo(): CargoKind? = held?.kind

    fun update(dt: Float) {
        if (phase != Phase.FLYING) return
        clock += dt
        stepGust(dt)
        stepCable(dt)
        stepHeli(dt)
        stepBob(dt)
        constrain()
        contact(dt)
        stepFuel(dt)
        if (loads.all { it.placed }) {
            phase = Phase.CLEARED
            queue.addLast(FlightEvent.CLEARED)
        }
    }

    fun stars(): Int {
        if (phase != Phase.CLEARED) return 0
        return when {
            fuel > mission.fuel * 0.5f && damage < 0.10f -> 3
            fuel > mission.fuel * 0.25f && damage < 0.45f -> 2
            else -> 1
        }
    }

    private fun scatter(kind: PropKind, count: Int) {
        var placed = 0
        var guard = 0
        while (placed < count && guard < count * 60 + 200) {
            guard++
            val x = (rng.nextFloat() - 0.5f) * 2f * (Field.HALF_X - 3f)
            val z = (rng.nextFloat() - 0.5f) * 2f * (Field.HALF_Z - 3f)
            if (hypot(x - padX, z - padZ) < mission.padRadius + kind.radius + 5f) continue
            if (loads.any { hypot(x - it.x, z - it.z) < kind.radius + 5f }) continue
            if (hypot(x - hx, z - hz) < kind.radius + 7f) continue
            if (props.any { hypot(x - it.x, z - it.z) < kind.radius + it.radius + 1.5f }) continue
            props.add(Prop(kind, x, z, rng.nextFloat() * 360f))
            placed++
        }
    }

    private fun stepGust(dt: Float) {
        if (mission.gust <= 0f) return
        gustClock += dt
        gustPower = mission.gust * sin(gustClock * 0.55f) * sin(gustClock * 0.19f + 1.1f)
    }

    private fun stepCable(dt: Float) {
        if (winchRate == 0f) return
        cable = (cable + winchRate * 3.2f * dt).coerceIn(2f, mission.cableMax)
    }

    private fun stepHeli(dt: Float) {
        val blend = (dt * 4.5f).coerceAtMost(1f)
        tiltX += (wishX - tiltX) * blend
        tiltZ += (wishZ - tiltZ) * blend
        vx += (tiltX * Field.SWAY + windX * 0.42f) * dt
        vz += (tiltZ * Field.SWAY + windZ * 0.42f) * dt
        vy += (collective * Field.LIFT - Field.GRAVITY) * dt
        val drag = 1f - (0.85f * dt).coerceAtMost(0.4f)
        vx *= drag
        vz *= drag
        vy *= 1f - (0.55f * dt).coerceAtMost(0.35f)
        hx += vx * dt
        hy += vy * dt
        hz += vz * dt
        heading += (tiltX * -18f - heading) * blend
        if (hx < -Field.HALF_X) { hx = -Field.HALF_X; vx = 0f }
        if (hx > Field.HALF_X) { hx = Field.HALF_X; vx = 0f }
        if (hz < -Field.HALF_Z) { hz = -Field.HALF_Z; vz = 0f }
        if (hz > Field.HALF_Z) { hz = Field.HALF_Z; vz = 0f }
        if (hy > Field.CEILING) { hy = Field.CEILING; vy = 0f }
    }

    private fun stepBob(dt: Float) {
        bvy -= Field.GRAVITY * dt
        bvx += windX * 0.30f * dt
        bvz += windZ * 0.30f * dt
        val drag = 1f - (0.60f * dt).coerceAtMost(0.35f)
        bvx *= drag
        bvz *= drag
        bvy *= 1f - (0.25f * dt).coerceAtMost(0.2f)
        bx += bvx * dt
        by += bvy * dt
        bz += bvz * dt
    }

    private fun constrain() {
        val ax = hx
        val ay = hy - Field.HOOK_DROP
        val az = hz
        var dx = bx - ax
        var dy = by - ay
        var dz = bz - az
        var len = sqrt(dx * dx + dy * dy + dz * dz)
        if (len < 1e-4f) {
            by -= 0.01f
            return
        }
        if (len <= cable) return
        val bobMass = Field.HOOK_MASS + (held?.kind?.mass ?: 0f)
        val wHeli = 1f / Field.HELI_MASS
        val wBob = 1f / bobMass
        repeat(2) {
            dx = bx - ax
            dy = by - ay
            dz = bz - az
            len = sqrt(dx * dx + dy * dy + dz * dz)
            if (len <= cable || len < 1e-4f) return@repeat
            val nx = dx / len
            val ny = dy / len
            val nz = dz / len
            val excess = len - cable
            val share = excess / (wHeli + wBob)
            hx += nx * share * wHeli
            hy += ny * share * wHeli
            hz += nz * share * wHeli
            bx -= nx * share * wBob
            by -= ny * share * wBob
            bz -= nz * share * wBob
            val relative = (bvx - vx) * nx + (bvy - vy) * ny + (bvz - vz) * nz
            if (relative > 0f) {
                val impulse = relative / (wHeli + wBob)
                vx += nx * impulse * wHeli
                vy += ny * impulse * wHeli
                vz += nz * impulse * wHeli
                bvx -= nx * impulse * wBob
                bvy -= ny * impulse * wBob
                bvz -= nz * impulse * wBob
            }
        }
    }

    private fun contact(dt: Float) {
        val load = held
        val restY = if (load != null) load.kind.rest else Field.HOOK_REST
        val radius = load?.kind?.radius ?: 0.4f

        props.forEach { prop ->
            val gap = hypot(bx - prop.x, bz - prop.z)
            val minimum = prop.radius + radius
            if (gap < minimum && by < prop.height && gap > 1e-4f) {
                val push = minimum - gap
                bx += (bx - prop.x) / gap * push
                bz += (bz - prop.z) / gap * push
                val speed = hypot(bvx, bvz)
                bvx *= -0.25f
                bvz *= -0.25f
                if (load != null && speed > 4f) {
                    load.damage = (load.damage + (speed - 4f) * 0.05f * load.kind.fragility).coerceAtMost(1.4f)
                    queue.addLast(FlightEvent.BUMP)
                }
            }
        }

        if (by < restY) {
            val impact = -bvy
            by = restY
            bvy = 0f
            bvx *= 0.55f
            bvz *= 0.55f
            if (load != null && impact > 4.5f) {
                load.damage = (load.damage + (impact - 4.5f) * 0.09f * load.kind.fragility).coerceAtMost(1.4f)
                queue.addLast(FlightEvent.BUMP)
            }
        }
        bx = bx.coerceIn(-Field.HALF_X, Field.HALF_X)
        bz = bz.coerceIn(-Field.HALF_Z, Field.HALF_Z)

        if (load != null) {
            load.x = bx
            load.y = by
            load.z = bz
            load.yaw += (hypot(bvx, bvz)) * dt * 22f
            longestCarry = max(longestCarry, cable)
            val onPad = hypot(bx - padX, bz - padZ) < mission.padRadius
            val settled = by <= restY + 0.06f && hypot(hypot(bvx, bvz), abs(bvy)) < mission.settleSpeed
            if (onPad && settled) {
                load.carried = false
                load.placed = true
                held = null
                queue.addLast(FlightEvent.SETTLE)
            }
        } else {
            val target = loads.firstOrNull { !it.placed && !it.carried }
            if (target != null) {
                val reach = hypot(bx - target.x, bz - target.z)
                if (reach < target.kind.radius + 1.3f && by < target.kind.rest + 1.5f) {
                    target.carried = true
                    held = target
                    bx = target.x
                    bz = target.z
                    by = max(by, target.kind.rest)
                    queue.addLast(FlightEvent.HOOK)
                }
            }
        }

        if (loads.any { it.damage >= 1f }) {
            phase = Phase.WRECKED
            queue.addLast(FlightEvent.CRASH)
            return
        }

        props.forEach { prop ->
            val gap = hypot(hx - prop.x, hz - prop.z)
            if (gap < prop.radius + Field.HELI_RADIUS && hy < prop.height) {
                phase = Phase.WRECKED
                queue.addLast(FlightEvent.CRASH)
            }
        }
        if (hy < 1.7f) {
            if (vy < -5f) {
                phase = Phase.WRECKED
                queue.addLast(FlightEvent.CRASH)
            } else {
                hy = 1.7f
                vy = 0f
                vx *= 0.72f
                vz *= 0.72f
            }
        }
    }

    private fun stepFuel(dt: Float) {
        fuel -= (0.35f + collective * 1.15f) * dt
        if (!warned && fuel < mission.fuel * 0.2f) {
            warned = true
            queue.addLast(FlightEvent.LOW_FUEL)
        }
        if (fuel <= 0f) {
            fuel = 0f
            phase = Phase.DRY
            queue.addLast(FlightEvent.CRASH)
        }
    }
}
