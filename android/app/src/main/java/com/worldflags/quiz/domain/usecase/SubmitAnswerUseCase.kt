package com.worldflags.quiz.domain.usecase

import com.worldflags.quiz.domain.model.*

class SubmitAnswerUseCase {
    operator fun invoke(snapshot: AtlasSnapshot, sessionId: String, optionId: String): AtlasSnapshot {
        val session = snapshot.session?.takeIf { it.id == sessionId && it.phase == AnswerPhase.AWAITING } ?: return snapshot
        if (optionId !in session.question.options) return snapshot
        val correct = optionId == session.question.countryId
        val answer = Answer(session.index, optionId, correct)
        val knowledge = if (correct) snapshot.knowledge + (session.question.countryId to ((snapshot.knowledge[session.question.countryId] ?: 0) or if (session.question.kind == QuestionKind.FLAG) 1 else 2)) else snapshot.knowledge
        return snapshot.copy(knowledge = knowledge, session = session.copy(
            answers = session.answers + answer,
            phase = if (correct) AnswerPhase.CORRECT else AnswerPhase.INCORRECT
        ))
    }
}
