package com.worldflags.quiz.presentation.continents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldflags.quiz.domain.model.Continent
import com.worldflags.quiz.domain.repository.QuizRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ContinentSelectViewModel(private val repository: QuizRepository) : ViewModel() {
    private val _state = MutableStateFlow(ContinentSelectUiState(countries = repository.countries))
    val state: StateFlow<ContinentSelectUiState> = _state.asStateFlow()
    init { viewModelScope.launch { repository.state.collect { store -> _state.update { it.copy(loading = store.loading, snapshot = store.snapshot, saveError = store.saveError) } } } }
    fun select(continent: Continent) { viewModelScope.launch { repository.select(continent) } }
    fun retry() { viewModelScope.launch { repository.retry() } }
}
