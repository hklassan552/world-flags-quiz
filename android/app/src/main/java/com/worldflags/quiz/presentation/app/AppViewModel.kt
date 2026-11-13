package com.worldflags.quiz.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldflags.quiz.core.navigation.Screen
import com.worldflags.quiz.domain.model.*
import com.worldflags.quiz.domain.repository.QuizRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AppEvent { data object Finish : AppEvent }

class AppViewModel(private val repository: QuizRepository, private val systemReducedMotion: Boolean) : ViewModel() {
    private val _screen = MutableStateFlow<Screen>(Screen.Splash)
    val screen: StateFlow<Screen> = _screen.asStateFlow()
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()
    private var backStack: List<Screen> = emptyList()
    private val _events = Channel<AppEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch { repository.load() }
        viewModelScope.launch { repository.state.collect { store -> _state.update { it.copy(reducedMotion = systemReducedMotion || store.snapshot.settings.reducedMotion) } } }
    }

    fun splashFinished() {
        if (_screen.value == Screen.Splash) { backStack = emptyList(); _screen.value = Screen.Menu }
    }

    fun navigate(destination: Screen) {
        if (destination == _screen.value) return
        if (_screen.value != Screen.Splash) backStack = backStack + _screen.value
        _screen.value = destination
    }

    fun back() {
        if (_screen.value == Screen.Menu || _screen.value == Screen.Splash || backStack.isEmpty()) {
            viewModelScope.launch { _events.send(AppEvent.Finish) }
        } else {
            _screen.value = backStack.last()
            backStack = backStack.dropLast(1)
        }
    }

    fun menu() { backStack = emptyList(); _screen.value = Screen.Menu }

    fun play(continent: Continent? = null, difficulty: Difficulty? = null, replay: Boolean = false) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.start(continent ?: repository.state.value.snapshot.settings.selected, difficulty) }
                .onSuccess { id ->
                    if (replay) {
                        backStack = backStack.takeWhile { it !is Screen.Game && it !is Screen.GameOver }
                        if (backStack.isEmpty()) backStack = listOf(Screen.Menu)
                        _screen.value = Screen.Game(id)
                    } else navigate(Screen.Game(id))
                }.onFailure { _state.update { it.copy(error = true) } }
            _state.update { it.copy(busy = false) }
        }
    }

    fun resume() { repository.state.value.snapshot.session?.takeIf { it.phase != AnswerPhase.COMPLETED }?.let { navigate(Screen.Game(it.id)) } }
    fun results(id: String) { if (_screen.value == Screen.Game(id)) navigate(Screen.GameOver(id)) }
}
