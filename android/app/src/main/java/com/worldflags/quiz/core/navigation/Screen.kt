package com.worldflags.quiz.core.navigation

import com.worldflags.quiz.domain.model.Continent

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data object ContinentSelect : Screen
    data class Game(val sessionId: String) : Screen
    data class GameOver(val resultId: String) : Screen
    data class Study(val continent: Continent? = null, val countryId: String? = null) : Screen
    data object Settings : Screen
}
