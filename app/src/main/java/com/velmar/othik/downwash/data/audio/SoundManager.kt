package com.velmar.othik.downwash.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.velmar.othik.downwash.R
import com.velmar.othik.downwash.domain.ProgressRepository

enum class Sfx { TAP, HOOK, WINCH, LAND, WARN, WIN, LOSE, UNLOCK }

class SoundManager(context: Context, private val repo: ProgressRepository) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids = mapOf(
        Sfx.TAP to pool.load(context, R.raw.tap, 1),
        Sfx.HOOK to pool.load(context, R.raw.hook, 1),
        Sfx.WINCH to pool.load(context, R.raw.winch, 1),
        Sfx.LAND to pool.load(context, R.raw.land, 1),
        Sfx.WARN to pool.load(context, R.raw.warn, 1),
        Sfx.WIN to pool.load(context, R.raw.win, 1),
        Sfx.LOSE to pool.load(context, R.raw.lose, 1),
        Sfx.UNLOCK to pool.load(context, R.raw.unlock, 1),
    )
    private val rotorId = pool.load(context, R.raw.rotor, 1)
    private var rotorStream = 0

    fun play(sfx: Sfx, rate: Float = 1f) {
        if (!repo.options().sound) return
        ids[sfx]?.let { pool.play(it, 0.95f, 0.95f, 1, 0, rate) }
    }

    fun startRotor() {
        if (!repo.options().sound || rotorStream != 0) return
        rotorStream = pool.play(rotorId, 0.55f, 0.55f, 0, -1, 1f)
    }

    fun rotorPower(power: Float) {
        if (rotorStream == 0) return
        val rate = (0.82f + power * 0.42f).coerceIn(0.5f, 2f)
        pool.setRate(rotorStream, rate)
        pool.setVolume(rotorStream, 0.35f + power * 0.35f, 0.35f + power * 0.35f)
    }

    fun stopRotor() {
        if (rotorStream == 0) return
        pool.stop(rotorStream)
        rotorStream = 0
    }

    fun release() {
        stopRotor()
        pool.release()
    }
}
