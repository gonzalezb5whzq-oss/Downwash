package com.velmar.othik.downwash.presentation.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.filament.Engine
import com.google.android.filament.View
import com.velmar.othik.downwash.domain.Field
import com.velmar.othik.downwash.domain.FlightEngine
import com.velmar.othik.downwash.domain.model.CargoKind
import com.velmar.othik.downwash.domain.model.Missions
import com.velmar.othik.downwash.domain.model.PropKind
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberView
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt

private const val TILE = 18.0f
private const val TILE_COLS = 4
private const val TILE_ROWS = 5
private const val TILE_SINK = TILE * 0.0933f
private const val HELI_SIZE = 6.5f
private const val HELI_YAW = 90f
private const val ROD_THICK = 0.055f
private const val HOOK_SIZE = 0.75f
private const val CAM_BACK = 24.0f
private const val CAM_UP = 5.0f

private class ScenePool {
    val tiles = ArrayList<ModelNode>()
    val pines = ArrayList<ModelNode>()
    val rocks = ArrayList<ModelNode>()
    val pylons = ArrayList<ModelNode>()
    val cargo = HashMap<CargoKind, ArrayList<ModelNode>>()
    var pad: ModelNode? = null
    var heli: ModelNode? = null
    var hook: ModelNode? = null
    var rod: ModelNode? = null
    var laid = false
    var clock = 0L
    var camX = 0f
    var camY = 20f
    var camZ = 40f
}

private fun cargoScale(kind: CargoKind): Float = when (kind) {
    CargoKind.CRATE -> 2.44f
    CargoKind.DRUM -> 1.80f
    CargoKind.BASKET -> 2.00f
}

private fun propLift(kind: PropKind): Float = when (kind) {
    PropKind.PINE -> kind.size * 0.5f
    PropKind.ROCK -> kind.size * 0.320f
    PropKind.PYLON -> kind.size * 0.5f
}

@Composable
fun ValleyScene(
    engine: FlightEngine,
    isPaused: () -> Boolean,
    onStep: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filament = rememberEngine(engineCreator = { egl ->
        Engine.Builder()
            .sharedContext(egl)
            .feature("backend.disable_parallel_shader_compile", true)
            .build()
    })
    val modelLoader = rememberModelLoader(filament)
    val pool = remember { ScenePool() }
    val camera = rememberCameraNode(filament)
    val mainLight = rememberMainLightNode(filament) { intensity = 118_000f }
    val bloomOff = remember { View.BloomOptions().apply { enabled = false } }
    val dynamicOff = remember { View.DynamicResolutionOptions().apply { enabled = false } }
    val view = rememberView(filament).apply {
        isPostProcessingEnabled = false
        bloomOptions = bloomOff
        dynamicResolutionOptions = dynamicOff
        setShadowingEnabled(false)
        setScreenSpaceRefractionEnabled(false)
    }

    SceneView(
        modifier = modifier,
        engine = filament,
        modelLoader = modelLoader,
        view = view,
        isOpaque = false,
        surfaceType = SurfaceType.TextureSurface,
        cameraNode = camera,
        mainLightNode = mainLight,
        onFrame = { nanos ->
            view.isPostProcessingEnabled = false
            view.bloomOptions = bloomOff
            view.dynamicResolutionOptions = dynamicOff
            view.setShadowingEnabled(false)
            view.setScreenSpaceRefractionEnabled(false)

            val raw = if (pool.clock == 0L) 0f else (nanos - pool.clock) / 1_000_000_000f
            pool.clock = nanos
            val dt = raw.coerceIn(0f, 0.05f)
            if (!isPaused()) onStep(dt)
            layout(engine, pool)
            sync(engine, pool)
            follow(engine, pool, camera, dt)
            val viewport = view.viewport
            if (viewport.height > 0) {
                camera.setLensProjection(
                    24.0,
                    viewport.width.toDouble() / viewport.height.toDouble(),
                    0.2,
                    400.0,
                )
            }
        },
        content = {
            build(modelLoader, "models/terrain.glb", TILE_COLS * TILE_ROWS, pool.tiles, TILE)
            build(modelLoader, "models/pine.glb", Missions.maxProps(PropKind.PINE), pool.pines, PropKind.PINE.size)
            build(modelLoader, "models/rock.glb", Missions.maxProps(PropKind.ROCK), pool.rocks, PropKind.ROCK.size)
            build(modelLoader, "models/pylon.glb", Missions.maxProps(PropKind.PYLON), pool.pylons, PropKind.PYLON.size)
            CargoKind.entries.forEach { kind ->
                val slots = pool.cargo.getOrPut(kind) { ArrayList() }
                build(modelLoader, kind.asset, Missions.poolSize(kind), slots, cargoScale(kind))
            }
            single(modelLoader, "models/pad.glb", 8f) { pool.pad = it }
            single(modelLoader, "models/heli.glb", HELI_SIZE) { pool.heli = it }
            single(modelLoader, "models/drum.glb", HOOK_SIZE) { pool.hook = it }
            single(modelLoader, "models/drum.glb", 1f) { pool.rod = it }
        },
    )
}

@Composable
private fun io.github.sceneview.SceneScope.build(
    modelLoader: ModelLoader,
    path: String,
    count: Int,
    out: ArrayList<ModelNode>,
    scaleToUnits: Float,
) {
    val instances: List<ModelInstance> = remember(path, count) {
        modelLoader.createInstancedModel(path, count)
    }
    instances.forEach { instance ->
        ModelNode(
            modelInstance = instance,
            scaleToUnits = scaleToUnits,
            isVisible = false,
            apply = { out.add(this) },
        )
    }
}

@Composable
private fun io.github.sceneview.SceneScope.single(
    modelLoader: ModelLoader,
    path: String,
    scaleToUnits: Float,
    keep: (ModelNode) -> Unit,
) {
    val instances: List<ModelInstance> = remember(path, scaleToUnits) {
        modelLoader.createInstancedModel(path, 1)
    }
    instances.forEach { instance ->
        ModelNode(
            modelInstance = instance,
            scaleToUnits = scaleToUnits,
            isVisible = false,
            apply = { keep(this) },
        )
    }
}

private fun layout(engine: FlightEngine, pool: ScenePool) {
    if (pool.laid || pool.tiles.size < TILE_COLS * TILE_ROWS) return
    val originX = -(TILE_COLS - 1) * TILE / 2f
    val originZ = -(TILE_ROWS - 1) * TILE / 2f
    for (index in 0 until TILE_COLS * TILE_ROWS) {
        val node = pool.tiles[index]
        node.worldPosition = Position(
            originX + (index % TILE_COLS) * TILE,
            -TILE_SINK,
            originZ + (index / TILE_COLS) * TILE,
        )
        node.isVisible = true
    }
    place(pool.pines, engine, PropKind.PINE)
    place(pool.rocks, engine, PropKind.ROCK)
    place(pool.pylons, engine, PropKind.PYLON)
    pool.pad?.let { node ->
        val span = engine.mission.padRadius * 2.2f
        val base = span / 1.901f
        node.worldPosition = Position(engine.padX, span * 0.041f, engine.padZ)
        node.scale = Scale(base, base, base)
        node.isVisible = true
    }
    pool.laid = true
}

private fun place(slots: ArrayList<ModelNode>, engine: FlightEngine, kind: PropKind) {
    var cursor = 0
    engine.props.forEach { prop ->
        if (prop.kind != kind || cursor >= slots.size) return@forEach
        val node = slots[cursor++]
        node.worldPosition = Position(prop.x, propLift(kind), prop.z)
        node.worldRotation = Rotation(0f, prop.yaw, 0f)
        node.isVisible = true
    }
    for (index in cursor until slots.size) slots[index].isVisible = false
}

private fun sync(engine: FlightEngine, pool: ScenePool) {
    val cursors = HashMap<CargoKind, Int>()
    engine.loads.forEach { load ->
        val slots = pool.cargo[load.kind] ?: return@forEach
        val cursor = cursors.getOrDefault(load.kind, 0)
        if (cursor >= slots.size) return@forEach
        cursors[load.kind] = cursor + 1
        val node = slots[cursor]
        node.worldPosition = Position(load.x, load.y, load.z)
        node.worldRotation = Rotation(0f, load.yaw, 0f)
        node.isVisible = true
    }
    pool.cargo.forEach { (kind, slots) ->
        val used = cursors.getOrDefault(kind, 0)
        for (index in used until slots.size) slots[index].isVisible = false
    }

    pool.heli?.let { node ->
        node.worldPosition = Position(engine.hx, engine.hy, engine.hz)
        node.worldRotation = Rotation(engine.tiltZ * 13f, HELI_YAW, -engine.tiltX * 17f)
        node.isVisible = true
    }

    val anchorY = engine.hy - Field.HOOK_DROP
    val dx = engine.bx - engine.hx
    val dy = engine.by - anchorY
    val dz = engine.bz - engine.hz
    val len = sqrt(dx * dx + dy * dy + dz * dz).coerceAtLeast(0.05f)

    pool.hook?.let { node ->
        node.worldPosition = Position(engine.bx, engine.by, engine.bz)
        node.isVisible = engine.held == null
    }

    pool.rod?.let { node ->
        val pitch = Math.toDegrees(acos((dy / len).toDouble().coerceIn(-1.0, 1.0))).toFloat()
        val yaw = Math.toDegrees(atan2(dx.toDouble(), dz.toDouble())).toFloat()
        node.worldPosition = Position(
            engine.hx + dx * 0.5f,
            anchorY + dy * 0.5f,
            engine.hz + dz * 0.5f,
        )
        node.worldRotation = Rotation(pitch, yaw, 0f)
        node.scale = Scale(ROD_THICK, len / 1.903f, ROD_THICK)
        node.isVisible = true
    }
}

private fun follow(engine: FlightEngine, pool: ScenePool, camera: io.github.sceneview.node.CameraNode, dt: Float) {
    val blend = (dt * 3.2f).coerceIn(0f, 1f)
    val sag = (engine.cable * 0.30f).coerceAtMost(4.2f)
    pool.camX += (engine.hx - pool.camX) * blend
    pool.camY += (engine.hy + CAM_UP - pool.camY) * blend
    pool.camZ += (engine.hz + CAM_BACK - pool.camZ) * blend
    camera.position = Position(
        pool.camX.coerceIn(-Field.HALF_X - 12f, Field.HALF_X + 12f),
        pool.camY.coerceAtLeast(6f),
        pool.camZ.coerceIn(-Field.HALF_Z - 6f, Field.HALF_Z + 24f),
    )
    camera.lookAt(Position(engine.hx, engine.hy - 3.4f - sag * 0.7f, engine.hz))
}

private fun drift(engine: FlightEngine): Float = hypot(engine.bx - engine.hx, engine.bz - engine.hz)
