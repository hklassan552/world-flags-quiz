package com.worldflags.quiz.presentation.settings

import com.worldflags.quiz.domain.model.Settings

data class SettingsUiState(val settings: Settings = Settings(), val learned: Int = 0, val saving: Boolean = false, val saveError: Boolean = false, val resetConfirm: Boolean = false)
