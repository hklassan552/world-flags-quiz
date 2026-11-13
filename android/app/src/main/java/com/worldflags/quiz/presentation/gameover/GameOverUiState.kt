package com.worldflags.quiz.presentation.gameover

import com.worldflags.quiz.domain.model.*

data class GameOverUiState(val loading: Boolean = true, val result: QuizResult? = null, val countries: List<Country> = emptyList(), val saveError: Boolean = false)
