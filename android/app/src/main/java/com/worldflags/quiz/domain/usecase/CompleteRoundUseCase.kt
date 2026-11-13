package com.worldflags.quiz.domain.usecase

import com.worldflags.quiz.domain.model.*

class CompleteRoundUseCase(private val countries: List<Country>) {
    operator fun invoke(snapshot: AtlasSnapshot, sessionId: String): AtlasSnapshot {
        if (snapshot.results.any { it.id == sessionId }) return snapshot
        val session = snapshot.session?.takeIf { it.id == sessionId } ?: return snapshot
        if (session.phase != AnswerPhase.INCORRECT && !(session.phase == AnswerPhase.CORRECT && session.index == session.questions.lastIndex)) return snapshot
        val completed = session.copy(phase = AnswerPhase.COMPLETED)
        val before = snapshot.copy(knowledge = session.initialKnowledge)
        val result = QuizResult(sessionId, completed, snapshot.facts() - before.facts(),
            countries.filter { snapshot.knowledge[it.id] == 3 && session.initialKnowledge[it.id] != 3 }.map { it.id },
            (snapshot.unlocked(countries) - before.unlocked(countries)).toList())
        val key = "${session.continent.name}_${session.difficulty.name}"
        return snapshot.copy(session = completed, results = (listOf(result) + snapshot.results).take(20),
            bestScores = snapshot.bestScores + (key to maxOf(session.score, snapshot.bestScores[key] ?: 0)))
    }
}
