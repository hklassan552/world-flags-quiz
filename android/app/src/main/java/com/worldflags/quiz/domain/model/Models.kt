package com.worldflags.quiz.domain.model

enum class Continent { EUROPE, ASIA, AFRICA, NORTH_AMERICA, SOUTH_AMERICA, OCEANIA }
enum class Difficulty(val length: Int) { LEARNER(5), EXPLORER(5), NAVIGATOR(8) }
enum class QuestionKind { FLAG, CAPITAL }
enum class AnswerPhase { AWAITING, CORRECT, INCORRECT, COMPLETED }
enum class CapitalRole { CAPITAL, ADMINISTRATIVE, CONSTITUTIONAL, LEGISLATIVE, SEAT }

data class Country(
    val id: String,
    val name: String,
    val capital: String,
    val continent: Continent,
    val latitude: Float,
    val longitude: Float,
    val role: CapitalRole = CapitalRole.CAPITAL,
    val acceptedCapitals: Set<String> = setOf(capital),
    val similarity: String = ""
)

data class Settings(
    val sound: Boolean = true,
    val reducedMotion: Boolean = false,
    val difficulty: Difficulty = Difficulty.EXPLORER,
    val selected: Continent = Continent.EUROPE
)

data class Question(val countryId: String, val kind: QuestionKind, val options: List<String>)
data class Answer(val questionIndex: Int, val selectedId: String, val correct: Boolean)

data class QuizSession(
    val id: String,
    val continent: Continent,
    val difficulty: Difficulty,
    val questions: List<Question>,
    val index: Int = 0,
    val answers: List<Answer> = emptyList(),
    val phase: AnswerPhase = AnswerPhase.AWAITING,
    val initialKnowledge: Map<String, Int> = emptyMap()
) {
    val question: Question get() = questions[index]
    val score: Int get() = answers.count { it.correct } * 100
    val correctCount: Int get() = answers.count { it.correct }
    val selectedId: String? get() = answers.lastOrNull { it.questionIndex == index }?.selectedId
}

data class QuizResult(
    val id: String,
    val session: QuizSession,
    val newFacts: Int,
    val newlyLearned: List<String>,
    val unlocked: List<Continent>
) {
    val score: Int get() = session.score
    val answered: Int get() = session.answers.size
    val correct: Int get() = session.correctCount
    val perfect: Boolean get() = correct == session.questions.size
    val accuracy: Int get() = if (answered == 0) 0 else kotlin.math.round(correct * 100f / answered).toInt()
}

data class AtlasSnapshot(
    val settings: Settings = Settings(),
    val knowledge: Map<String, Int> = emptyMap(),
    val bestScores: Map<String, Int> = emptyMap(),
    val session: QuizSession? = null,
    val results: List<QuizResult> = emptyList()
) {
    fun learned(countries: List<Country>): Int = countries.count { knowledge[it.id] == 3 }
    fun facts(): Int = knowledge.values.sumOf { (it and 1) + ((it shr 1) and 1) }
    fun best(continent: Continent, difficulty: Difficulty): Int = bestScores["${continent.name}_${difficulty.name}"] ?: 0
    fun unlocked(countries: List<Country>): Set<Continent> {
        val unlocked = linkedSetOf(Continent.EUROPE)
        Continent.entries.drop(1).forEach { continent ->
            val previous = Continent.entries[continent.ordinal - 1]
            if (previous in unlocked && countries.count { it.continent == previous && knowledge[it.id] == 3 } >= 3) unlocked.add(continent)
        }
        return unlocked
    }
}

data class StoreState(
    val loading: Boolean = true,
    val snapshot: AtlasSnapshot = AtlasSnapshot(),
    val saveError: Boolean = false,
    val recovered: Boolean = false,
    val readError: Boolean = false
)
