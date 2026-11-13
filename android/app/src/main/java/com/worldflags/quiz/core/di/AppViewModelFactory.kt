package com.worldflags.quiz.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.worldflags.quiz.core.audio.TonePlayer
import com.worldflags.quiz.domain.repository.QuizRepository
import com.worldflags.quiz.presentation.app.AppViewModel
import com.worldflags.quiz.presentation.continents.ContinentSelectViewModel
import com.worldflags.quiz.presentation.game.GameViewModel
import com.worldflags.quiz.presentation.gameover.GameOverViewModel
import com.worldflags.quiz.presentation.menu.MenuViewModel
import com.worldflags.quiz.presentation.settings.SettingsViewModel
import com.worldflags.quiz.presentation.splash.SplashViewModel
import com.worldflags.quiz.presentation.study.StudyViewModel

class AppViewModelFactory(private val repository: QuizRepository, private val tones: TonePlayer, private val reducedMotion: Boolean) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        AppViewModel::class.java -> AppViewModel(repository, reducedMotion)
        SplashViewModel::class.java -> SplashViewModel(repository, reducedMotion)
        MenuViewModel::class.java -> MenuViewModel(repository)
        GameViewModel::class.java -> GameViewModel(repository, tones)
        GameOverViewModel::class.java -> GameOverViewModel(repository)
        ContinentSelectViewModel::class.java -> ContinentSelectViewModel(repository)
        StudyViewModel::class.java -> StudyViewModel(repository)
        SettingsViewModel::class.java -> SettingsViewModel(repository, tones)
        else -> error(modelClass.name)
    } as T
}
