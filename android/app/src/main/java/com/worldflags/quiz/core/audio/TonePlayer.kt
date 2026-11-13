package com.worldflags.quiz.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.ToneGenerator

class TonePlayer(context: Context) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var tone: ToneGenerator? = null
    private val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
        .setOnAudioFocusChangeListener { change -> if (change < 0) stop() }.build()

    fun positive() = play(ToneGenerator.TONE_PROP_ACK, 90)
    fun complete() = play(ToneGenerator.TONE_PROP_PROMPT, 100)
    private fun play(type: Int, duration: Int) {
        stop()
        if (audio.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        runCatching {
            if (audio.requestAudioFocus(focus) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                tone = ToneGenerator(AudioManager.STREAM_MUSIC, 22).also { it.startTone(type, duration) }
            }
        }
    }
    fun stop() {
        tone?.stopTone(); tone?.release(); tone = null
        runCatching { audio.abandonAudioFocusRequest(focus) }
    }
}
