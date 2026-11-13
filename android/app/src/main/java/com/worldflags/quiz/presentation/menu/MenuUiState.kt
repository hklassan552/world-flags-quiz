package com.worldflags.quiz.presentation.menu

import com.worldflags.quiz.domain.model.*

data class MenuUiState(val loading: Boolean = true, val snapshot: AtlasSnapshot = AtlasSnapshot(), val countries: List<Country> = emptyList(), val saveError: Boolean = false, val recovered: Boolean = false)
