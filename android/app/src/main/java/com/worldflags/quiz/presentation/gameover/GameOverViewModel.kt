package com.worldflags.quiz.presentation.gameover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldflags.quiz.domain.repository.QuizRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameOverViewModel(private val repository: QuizRepository) : ViewModel() {
    private val _state = MutableStateFlow(GameOverUiState(countries = repository.countries))
    val state: StateFlow<GameOverUiState> = _state.asStateFlow()
    private var resultId: String? = null
    init { viewModelScope.launch { repository.state.collect { refresh() } } }
    fun open(id: String) { resultId = id; refresh() }
    private fun refresh() {
        val store = repository.state.value
        _state.update { it.copy(loading = store.loading, result = store.snapshot.results.firstOrNull { result -> result.id == resultId }, saveError = store.saveError) }
    }
    fun retry() { viewModelScope.launch { repository.retry() } }
}
