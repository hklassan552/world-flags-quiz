package com.worldflags.quiz.presentation.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldflags.quiz.domain.model.*
import com.worldflags.quiz.domain.repository.QuizRepository
import java.text.Normalizer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudyViewModel(private val repository: QuizRepository) : ViewModel() {
    private val _state = MutableStateFlow(StudyUiState())
    val state: StateFlow<StudyUiState> = _state.asStateFlow()
    private var initialized = false
    init { viewModelScope.launch { repository.state.collect { store ->
        _state.update { it.copy(loading = store.loading, knowledge = store.snapshot.knowledge) }
        filterCountries()
    } } }
    fun open(continent: Continent?, countryId: String?) {
        if (initialized) return
        initialized = true
        _state.update { it.copy(filter = continent, expanded = countryId) }
        filterCountries()
    }
    fun query(value: String) { _state.update { it.copy(query = value) }; filterCountries() }
    fun filter(value: Continent?) { _state.update { it.copy(filter = value) }; filterCountries() }
    fun toggle(id: String) { _state.update { it.copy(expanded = if (it.expanded == id) null else id) } }
    fun retry() { viewModelScope.launch { repository.load(); filterCountries() } }
    private fun normalize(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase()
    private fun filterCountries() {
        val current = _state.value
        val query = normalize(current.query.trim())
        val countries = repository.countries.filter { country ->
            (current.filter == null || country.continent == current.filter) &&
                (query.isEmpty() || normalize(country.name).contains(query) || normalize(country.capital).contains(query))
        }.sortedWith(compareBy<Country> { if (it.id == current.expanded) 0 else 1 }.thenBy { it.name })
        _state.update { it.copy(countries = countries, error = repository.countries.isEmpty()) }
    }
}
