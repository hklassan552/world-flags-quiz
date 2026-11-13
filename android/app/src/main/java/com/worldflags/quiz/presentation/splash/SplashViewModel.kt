package com.worldflags.quiz.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldflags.quiz.core.config.GameConfig
import com.worldflags.quiz.domain.repository.QuizRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SplashViewModel(repository: QuizRepository, systemReducedMotion: Boolean) : ViewModel() {
    private val _state = MutableStateFlow(SplashUiState(reducedMotion = systemReducedMotion))
    val state: StateFlow<SplashUiState> = _state.asStateFlow()
    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    init {
        viewModelScope.launch { delay(GameConfig.LOADER_DURATION_MS); _finished.value = true; _state.update { it.copy(finished = true) } }
        viewModelScope.launch { repository.state.collect { store -> _state.update { it.copy(reducedMotion = systemReducedMotion || store.snapshot.settings.reducedMotion) } } }
    }
}
