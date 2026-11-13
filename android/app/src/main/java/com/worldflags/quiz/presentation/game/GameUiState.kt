package com.worldflags.quiz.presentation.game

import com.worldflags.quiz.domain.model.*

data class GameUiState(val loading: Boolean = true, val session: QuizSession? = null, val countries: List<Country> = emptyList(),
    val submitting: Boolean = false, val saveError: Boolean = false, val error: Boolean = false, val pauseDialog: Boolean = false)
