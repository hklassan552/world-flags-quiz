package com.worldflags.quiz.presentation.continents

import com.worldflags.quiz.domain.model.*

data class ContinentSelectUiState(val loading: Boolean = true, val countries: List<Country> = emptyList(), val snapshot: AtlasSnapshot = AtlasSnapshot(), val saveError: Boolean = false)
