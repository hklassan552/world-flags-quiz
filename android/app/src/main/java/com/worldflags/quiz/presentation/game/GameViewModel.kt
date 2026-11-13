package com.worldflags.quiz.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldflags.quiz.core.audio.TonePlayer
import com.worldflags.quiz.core.config.GameConfig
import com.worldflags.quiz.domain.model.*
import com.worldflags.quiz.domain.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface GameEvent { data class Results(val id: String) : GameEvent }

class GameViewModel(private val repository: QuizRepository, private val tones: TonePlayer) : ViewModel() {
    private val _state = MutableStateFlow(GameUiState(countries = repository.countries))
    val state: StateFlow<GameUiState> = _state.asStateFlow()
    private val _events = Channel<GameEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var sessionId: String? = null
    private var active = false
    private var correctionJob: Job? = null
    private var toneJob: Job? = null
    private var remaining = GameConfig.CORRECTION_DURATION_MS
    private var startedAt = 0L

    init { viewModelScope.launch { repository.state.collect { refresh() } } }

    fun attach(id: String) {
        if (sessionId != id) {
            pause()
            sessionId = id
            remaining = GameConfig.CORRECTION_DURATION_MS
            _state.update { it.copy(pauseDialog = false, submitting = false) }
        }
        active = !_state.value.pauseDialog
        refresh()
    }

    private fun refresh() {
        val store = repository.state.value
        val session = store.snapshot.session?.takeIf { it.id == sessionId }
            ?: store.snapshot.results.firstOrNull { it.id == sessionId }?.session
        _state.update { it.copy(loading = store.loading, session = session, saveError = store.saveError, error = !store.loading && sessionId != null && session == null) }
        if (active && session?.phase == AnswerPhase.INCORRECT) scheduleCorrection()
    }

    fun answer(optionId: String) {
        val session = _state.value.session ?: return
        if (!active || session.phase != AnswerPhase.AWAITING || _state.value.submitting) return
        _state.update { it.copy(submitting = true) }
        remaining = GameConfig.CORRECTION_DURATION_MS
        viewModelScope.launch {
            runCatching { repository.answer(session.id, optionId) }.onFailure { _state.update { it.copy(error = true) } }
            if (optionId == session.question.countryId && active) playTone(false)
            _state.update { it.copy(submitting = false) }
        }
    }

    private fun scheduleCorrection() {
        if (correctionJob?.isActive == true) return
        val id = sessionId ?: return
        startedAt = System.currentTimeMillis()
        correctionJob = viewModelScope.launch {
            delay(remaining.coerceAtLeast(0L))
            remaining = 0L
            repository.complete(id)?.let { _events.send(GameEvent.Results(it)) }
        }
    }

    fun next() {
        val session = _state.value.session ?: return
        if (_state.value.submitting || session.phase != AnswerPhase.CORRECT) return
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            if (session.index == session.questions.lastIndex) {
                repository.complete(session.id)?.let { id ->
                    playTone(true)
                    _events.send(GameEvent.Results(id))
                }
            } else repository.next(session.id)
            _state.update { it.copy(submitting = false) }
        }
    }

    fun viewResults() { _state.value.session?.let { session -> viewModelScope.launch { _events.send(GameEvent.Results(session.id)) } } }
    fun showPause() { pause(); _state.update { it.copy(pauseDialog = true) } }
    fun continueGame() { _state.update { it.copy(pauseDialog = false) }; resume() }
    fun resume() { active = !_state.value.pauseDialog; refresh() }
    fun pause() {
        active = false
        if (correctionJob?.isActive == true) remaining = (remaining - (System.currentTimeMillis() - startedAt)).coerceAtLeast(0L)
        correctionJob?.cancel(); correctionJob = null
        toneJob?.cancel(); toneJob = null
        tones.stop()
    }
    private fun playTone(completed: Boolean) {
        if (!repository.state.value.snapshot.settings.sound) return
        toneJob?.cancel()
        toneJob = viewModelScope.launch {
            if (completed) tones.complete() else tones.positive()
            delay(120L)
            tones.stop()
        }
    }
    fun retry() { viewModelScope.launch { repository.retry(); refresh() } }
    override fun onCleared() { tones.stop(); super.onCleared() }
}
