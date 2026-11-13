package com.worldflags.quiz.presentation.study

import com.worldflags.quiz.domain.model.*

data class StudyUiState(val loading: Boolean = true, val query: String = "", val filter: Continent? = null, val expanded: String? = null,
    val countries: List<Country> = emptyList(), val knowledge: Map<String, Int> = emptyMap(), val error: Boolean = false)
