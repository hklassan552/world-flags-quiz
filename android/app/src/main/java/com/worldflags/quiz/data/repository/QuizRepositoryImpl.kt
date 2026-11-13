package com.worldflags.quiz.data.repository

import com.worldflags.quiz.data.local.PreferencesStore
import com.worldflags.quiz.data.sample.SampleData
import com.worldflags.quiz.domain.model.*
import com.worldflags.quiz.domain.repository.QuizRepository
import com.worldflags.quiz.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class QuizRepositoryImpl(private val store: PreferencesStore) : QuizRepository {
    override val countries = SampleData.countries
    private val _state = MutableStateFlow(StoreState())
    override val state = _state.asStateFlow()
    private val mutex = Mutex()
    private val createRound = CreateRoundUseCase(countries)
    private val submitAnswer = SubmitAnswerUseCase()
    private val completeRound = CompleteRoundUseCase(countries)

    override suspend fun load() = mutex.withLock {
        if (!_state.value.loading && !_state.value.readError) return@withLock
        val loaded = runCatching { store.read() }
        loaded.onSuccess { (snapshot, recovered) -> _state.update { it.copy(loading = false, snapshot = snapshot, recovered = recovered, readError = false, saveError = false) } }
            .onFailure { _state.update { it.copy(loading = false, saveError = true, readError = true) } }
    }

    private suspend fun mutate(transform: (AtlasSnapshot) -> AtlasSnapshot) = mutex.withLock {
        val snapshot = transform(_state.value.snapshot)
        _state.update { it.copy(snapshot = snapshot) }
        val saved = store.write(snapshot)
        _state.update { it.copy(saveError = !saved) }
    }

    override suspend fun start(continent: Continent, difficulty: Difficulty?): String {
        var id = ""
        mutate { snapshot ->
            val region = continent.takeIf { it in snapshot.unlocked(countries) } ?: Continent.EUROPE
            val session = createRound(snapshot, region, difficulty ?: snapshot.settings.difficulty)
            id = session.id
            snapshot.copy(session = session, settings = snapshot.settings.copy(selected = region))
        }
        return id
    }

    override suspend fun answer(sessionId: String, optionId: String) = mutate { submitAnswer(it, sessionId, optionId) }

    override suspend fun next(sessionId: String) = mutate { snapshot ->
        val session = snapshot.session
        if (session?.id == sessionId && session.phase == AnswerPhase.CORRECT && session.index < session.questions.lastIndex)
            snapshot.copy(session = session.copy(index = session.index + 1, phase = AnswerPhase.AWAITING)) else snapshot
    }

    override suspend fun complete(sessionId: String): String? {
        mutate { completeRound(it, sessionId) }
        return sessionId.takeIf { id -> _state.value.snapshot.results.any { it.id == id } }
    }

    override suspend fun select(continent: Continent) = mutate {
        if (continent in it.unlocked(countries)) it.copy(settings = it.settings.copy(selected = continent)) else it
    }

    override suspend fun settings(transform: (Settings) -> Settings) = mutate { it.copy(settings = transform(it.settings)) }
    override suspend fun reset() = mutate { AtlasSnapshot(settings = it.settings.copy(selected = Continent.EUROPE)) }
    override suspend fun retry() { if (_state.value.readError) load() else mutate { it } }
}
