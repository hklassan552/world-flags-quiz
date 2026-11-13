package com.worldflags.quiz.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldflags.quiz.domain.repository.QuizRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MenuViewModel(private val repository: QuizRepository) : ViewModel() {
    private val _state = MutableStateFlow(MenuUiState(countries = repository.countries))
    val state: StateFlow<MenuUiState> = _state.asStateFlow()
    init {
        viewModelScope.launch { repository.state.collect { store -> _state.update { it.copy(loading = store.loading, snapshot = store.snapshot, saveError = store.saveError, recovered = store.recovered) } } }
    }
    fun retry() { viewModelScope.launch { repository.retry() } }
}
