package com.worldflags.quiz.core.di

import android.content.Context
import android.provider.Settings
import com.worldflags.quiz.core.audio.TonePlayer
import com.worldflags.quiz.data.local.PreferencesStore
import com.worldflags.quiz.data.repository.QuizRepositoryImpl
import com.worldflags.quiz.data.sample.SampleData

object ServiceLocator {
    private var initialized = false
    private var storedFactory: AppViewModelFactory? = null
    val factory: AppViewModelFactory get() = checkNotNull(storedFactory)

    fun initialize(context: Context) {
        if (initialized) return
        val repository = QuizRepositoryImpl(PreferencesStore(context, SampleData.countries.map { it.id }.toSet()))
        val reducedMotion = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        storedFactory = AppViewModelFactory(repository, TonePlayer(context), reducedMotion)
        initialized = true
    }
}
