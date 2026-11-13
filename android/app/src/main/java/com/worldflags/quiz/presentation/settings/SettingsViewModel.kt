package com.worldflags.quiz.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldflags.quiz.core.audio.TonePlayer
import com.worldflags.quiz.domain.model.*
import com.worldflags.quiz.domain.repository.QuizRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface SettingsEvent { data object ResetComplete : SettingsEvent }

class SettingsViewModel(private val repository: QuizRepository, private val tones: TonePlayer) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()
    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    init { viewModelScope.launch { repository.state.collect { store -> _state.update { it.copy(settings = store.snapshot.settings, learned = store.snapshot.learned(repository.countries), saveError = store.saveError) } } } }
    fun sound(enabled: Boolean) { if (!enabled) tones.stop(); change { it.copy(sound = enabled) } }
    fun motion(enabled: Boolean) = change { it.copy(reducedMotion = enabled) }
    fun difficulty(value: Difficulty) = change { it.copy(difficulty = value) }
    private fun change(transform: (Settings) -> Settings) {
        viewModelScope.launch { repository.settings(transform) }
    }
    fun confirmReset(show: Boolean) { _state.update { it.copy(resetConfirm = show) } }
    fun reset() {
        if (_state.value.saving) return
        _state.update { it.copy(saving = true, resetConfirm = false) }
        viewModelScope.launch {
            repository.reset()
            _state.update { it.copy(saving = false) }
            _events.send(SettingsEvent.ResetComplete)
        }
    }
    fun retry() { viewModelScope.launch { repository.retry() } }
}
