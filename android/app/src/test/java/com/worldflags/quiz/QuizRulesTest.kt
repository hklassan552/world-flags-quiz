package com.worldflags.quiz

import com.worldflags.quiz.data.sample.FlagVectors
import com.worldflags.quiz.data.sample.MapGeometry
import com.worldflags.quiz.data.sample.SampleData
import com.worldflags.quiz.domain.model.*
import com.worldflags.quiz.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test
import java.util.Base64
import java.util.zip.GZIPInputStream

class QuizRulesTest {
    private val countries = SampleData.countries
    private val create = CreateRoundUseCase(countries)
    private val submit = SubmitAnswerUseCase()
    private val finish = CompleteRoundUseCase(countries)

    @Test
    fun completeCatalogHasOneGeometryAndVectorForEveryCountry() {
        assertEquals(195, countries.size)
        assertEquals(195, countries.map { it.id }.toSet().size)
        assertEquals(countries.map { it.id }.toSet(), MapGeometry.polygons.keys)
        countries.forEach { country ->
            assertTrue(country.capital.isNotBlank())
            assertTrue(country.capital in country.acceptedCapitals)
            assertTrue(MapGeometry.polygons.getValue(country.id).all { it.size >= 6 && it.size % 2 == 0 })
            val flag = FlagVectors.get(country.id)
            assertTrue(flag.width > 0 && flag.height > 0)
            val vector = GZIPInputStream(Base64.getUrlDecoder().decode(flag.encoded).inputStream()).bufferedReader().use { it.readText() }
            assertTrue(vector.lineSequence().all { it.split('\t').size >= 7 })
        }
        assertEquals(listOf(44, 48, 54, 23, 12, 14), Continent.entries.map { continent -> countries.count { it.continent == continent } })
    }

    @Test
    fun sampledRoundsHaveFourUnambiguousAnswersInEveryRegionAndDifficulty() {
        Continent.entries.forEach { continent -> Difficulty.entries.forEach { difficulty ->
            repeat(40) {
                val session = create(AtlasSnapshot(), continent, difficulty)
                assertEquals(difficulty.length, session.questions.size)
                assertEquals(QuestionKind.FLAG, session.questions.first().kind)
                assertEquals(session.questions.size, session.questions.map { it.countryId to it.kind }.toSet().size)
                session.questions.forEach { question ->
                    val country = countries.first { it.id == question.countryId }
                    assertEquals(continent, country.continent)
                    assertEquals(4, question.options.toSet().size)
                    assertTrue(country.id in question.options)
                    val options = question.options.map { id -> countries.first { it.id == id } }
                    if (question.kind == QuestionKind.CAPITAL) {
                        assertEquals(4, options.map { it.capital }.toSet().size)
                        assertEquals(1, options.count { it.capital in country.acceptedCapitals })
                    }
                }
            }
        } }
    }

    @Test
    fun firstPlayIsEuropeFranceFlagAndDuplicateSubmissionCannotScoreAgain() {
        val session = create(AtlasSnapshot(), Continent.EUROPE, Difficulty.EXPLORER)
        assertEquals("fr", session.question.countryId)
        val initial = AtlasSnapshot(session = session)
        val answered = submit(initial, session.id, "fr")
        assertEquals(100, answered.session?.score)
        assertEquals(1, answered.knowledge["fr"])
        assertEquals(answered, submit(answered, session.id, "fr"))
        assertEquals(initial, submit(initial, session.id, "missing"))
    }

    @Test
    fun wrongAnswerFinishesWithActualAccuracyAndDoesNotEraseLearning() {
        val knowledge = mapOf("de" to 3, "it" to 1)
        val session = create(AtlasSnapshot(knowledge = knowledge), Continent.EUROPE, Difficulty.EXPLORER)
        val wrong = session.question.options.first { it != session.question.countryId }
        val answered = submit(AtlasSnapshot(knowledge = knowledge, session = session), session.id, wrong)
        val completed = finish(answered, session.id)
        val result = completed.results.single()
        assertEquals(0, result.score)
        assertEquals(1, result.answered)
        assertEquals(0, result.accuracy)
        assertEquals(knowledge, completed.knowledge)
        assertFalse(result.perfect)
        assertEquals(completed, finish(completed, session.id))
    }

    @Test
    fun perfectRoundsScoreFiveOrEightAnswersAndOnlyLearnUniqueFacts() {
        Difficulty.entries.forEach { difficulty ->
            val session = create(AtlasSnapshot(), Continent.EUROPE, difficulty)
            var snapshot = AtlasSnapshot(session = session)
            session.questions.indices.forEach { index ->
                snapshot = snapshot.copy(session = snapshot.session?.copy(index = index, phase = AnswerPhase.AWAITING))
                snapshot = submit(snapshot, session.id, session.questions[index].countryId)
            }
            val completed = finish(snapshot, session.id)
            val result = completed.results.single()
            assertTrue(result.perfect)
            assertEquals(difficulty.length * 100, result.score)
            assertEquals(100, result.accuracy)
            assertEquals(difficulty.length, result.newFacts)
            assertEquals(completed, finish(completed, session.id))
        }
    }

    @Test
    fun learningRequiresBothFactsAndUnlocksInOrder() {
        val firstThree = countries.filter { it.continent == Continent.EUROPE }.take(3)
        val partial = AtlasSnapshot(knowledge = firstThree.associate { it.id to 1 })
        assertEquals(0, partial.learned(countries))
        assertEquals(setOf(Continent.EUROPE), partial.unlocked(countries))
        val complete = partial.copy(knowledge = firstThree.associate { it.id to 3 })
        assertEquals(3, complete.learned(countries))
        assertEquals(setOf(Continent.EUROPE, Continent.ASIA), complete.unlocked(countries))
        val wholeAtlas = AtlasSnapshot(knowledge = countries.associate { it.id to 3 })
        assertEquals(195, wholeAtlas.learned(countries))
        assertEquals(390, wholeAtlas.facts())
        assertEquals(Continent.entries.toSet(), wholeAtlas.unlocked(countries))
    }

    @Test
    fun unknownFactsArePreferredAndPracticeDoesNotInventNewLearning() {
        val knowledge = countries.associate { it.id to if (it.id == "de") 0 else 3 }
        val session = create(AtlasSnapshot(knowledge = knowledge), Continent.EUROPE, Difficulty.EXPLORER)
        assertEquals("de", session.question.countryId)
        assertEquals(session.questions.size, session.questions.map { it.countryId to it.kind }.toSet().size)
        val learned = countries.associate { it.id to 3 }
        val practice = create(AtlasSnapshot(knowledge = learned), Continent.EUROPE, Difficulty.EXPLORER)
        var snapshot = AtlasSnapshot(knowledge = learned, session = practice)
        practice.questions.indices.forEach { index ->
            snapshot = snapshot.copy(session = snapshot.session?.copy(index = index, phase = AnswerPhase.AWAITING))
            snapshot = submit(snapshot, practice.id, practice.questions[index].countryId)
        }
        assertEquals(0, finish(snapshot, practice.id).results.single().newFacts)
    }
}
