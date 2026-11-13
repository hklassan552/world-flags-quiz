package com.worldflags.quiz.domain.usecase

import com.worldflags.quiz.domain.model.*
import java.util.UUID
import kotlin.random.Random

class CreateRoundUseCase(private val countries: List<Country>) {
    operator fun invoke(snapshot: AtlasSnapshot, continent: Continent, difficulty: Difficulty): QuizSession {
        val random = Random(System.nanoTime())
        val pool = countries.filter { it.continent == continent }
        require(pool.size >= 4)
        val used = mutableSetOf<Pair<String, QuestionKind>>()
        val questions = mutableListOf<Question>()
        repeat(difficulty.length) { index ->
            val kind = if (index % 2 == 0) QuestionKind.FLAG else QuestionKind.CAPITAL
            val bit = if (kind == QuestionKind.FLAG) 1 else 2
            val unseen = pool.filter { it.id to kind !in used }
            val candidates = unseen.ifEmpty { pool }.shuffled(random).sortedWith(compareBy<Country>(
                { if ((snapshot.knowledge[it.id] ?: 0) and bit == 0) 0 else 1 },
                { if (questions.lastOrNull()?.countryId == it.id) 1 else 0 },
                { snapshot.knowledge[it.id]?.countOneBits() ?: 0 }
            ))
            val country = if (index == 0 && snapshot.knowledge.isEmpty() && continent == Continent.EUROPE) pool.first { it.id == "fr" } else candidates.first()
            used.add(country.id to kind)
            val regional = countries.filter { it.continent == continent }.shuffled(random)
            val similar = countries.filter {
                if (kind == QuestionKind.FLAG) it.similarity == country.similarity && it.similarity.isNotEmpty()
                else it.continent == continent
            }.shuffled(random)
            val distractors = when (difficulty) {
                Difficulty.LEARNER -> countries.shuffled(random)
                Difficulty.EXPLORER -> regional + countries.shuffled(random)
                Difficulty.NAVIGATOR -> similar + regional + countries.shuffled(random)
            }.filter { it.id != country.id && (kind != QuestionKind.CAPITAL || it.capital !in country.acceptedCapitals) }
                .distinctBy { if (kind == QuestionKind.FLAG) it.name else it.capital }.take(3)
            require(distractors.size == 3)
            questions.add(Question(country.id, kind, (distractors.map { it.id } + country.id).shuffled(random)))
        }
        return QuizSession(UUID.randomUUID().toString(), continent, difficulty, questions, initialKnowledge = snapshot.knowledge)
    }
}
