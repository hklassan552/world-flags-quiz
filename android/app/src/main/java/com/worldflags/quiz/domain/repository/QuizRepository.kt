package com.worldflags.quiz.domain.repository

import com.worldflags.quiz.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface QuizRepository {
    val countries: List<Country>
    val state: StateFlow<StoreState>
    suspend fun load()
    suspend fun start(continent: Continent, difficulty: Difficulty? = null): String
    suspend fun answer(sessionId: String, optionId: String)
    suspend fun next(sessionId: String)
    suspend fun complete(sessionId: String): String?
    suspend fun select(continent: Continent)
    suspend fun settings(transform: (Settings) -> Settings)
    suspend fun reset()
    suspend fun retry()
}
