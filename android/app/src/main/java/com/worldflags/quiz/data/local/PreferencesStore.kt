package com.worldflags.quiz.data.local

import android.content.Context
import com.worldflags.quiz.domain.model.*
import com.worldflags.quiz.data.sample.SampleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class PreferencesStore(context: Context, private val validIds: Set<String>) {
    private val appContext = context.applicationContext
    private val preferences by lazy { appContext.getSharedPreferences("world_atlas_v1", Context.MODE_PRIVATE) }

    suspend fun read(): Pair<AtlasSnapshot, Boolean> = withContext(Dispatchers.IO) {
        val raw = preferences.getString("snapshot", null) ?: return@withContext AtlasSnapshot() to false
        runCatching {
            val json = JSONObject(raw)
            var recovered = false
            val settings = runCatching {
                val s = json.optJSONObject("settings") ?: JSONObject()
                Settings(s.optBoolean("sound", true), s.optBoolean("motion", false),
                    Difficulty.valueOf(s.optString("difficulty", "EXPLORER")), Continent.valueOf(s.optString("selected", "EUROPE")))
            }.getOrElse { recovered = true; Settings() }
            val knowledge = readBits(json.optJSONObject("knowledge"))
            if (readInts(json.optJSONObject("knowledge")) != knowledge) recovered = true
            val best = readInts(json.optJSONObject("best")).filterValues { it in 0..800 }
            val session = json.optJSONObject("session")?.let { value ->
                runCatching { readSession(value) }.getOrElse { recovered = true; null }
            }
            val results = mutableListOf<QuizResult>()
            val array = json.optJSONArray("results") ?: JSONArray()
            repeat(minOf(20, array.length())) { index ->
                runCatching {
                    val r = array.getJSONObject(index)
                    val s = readSession(r.getJSONObject("session"))
                    QuizResult(s.id, s, r.optInt("facts"), strings(r.optJSONArray("learned")).filter { it in validIds },
                        strings(r.optJSONArray("unlocks")).map { Continent.valueOf(it) })
                }.onSuccess { results.add(it) }.onFailure { recovered = true }
            }
            AtlasSnapshot(settings, knowledge, best, session, results) to recovered
        }.getOrElse { AtlasSnapshot() to true }
    }

    suspend fun write(snapshot: AtlasSnapshot): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val s = snapshot.settings
            val json = JSONObject().put("schema", 1)
                .put("settings", JSONObject().put("sound", s.sound).put("motion", s.reducedMotion)
                    .put("difficulty", s.difficulty.name).put("selected", s.selected.name))
                .put("knowledge", JSONObject(snapshot.knowledge)).put("best", JSONObject(snapshot.bestScores))
                .put("results", JSONArray().apply {
                    snapshot.results.forEach { result -> put(JSONObject().put("session", sessionJson(result.session))
                        .put("facts", result.newFacts).put("learned", JSONArray(result.newlyLearned))
                        .put("unlocks", JSONArray(result.unlocked.map { it.name }))) }
                })
            snapshot.session?.let { json.put("session", sessionJson(it)) }
            preferences.edit().putString("snapshot", json.toString()).commit()
        }.getOrDefault(false)
    }

    private fun readInts(json: JSONObject?): Map<String, Int> = json?.keys()?.asSequence()?.associateWith { json.optInt(it) } ?: emptyMap()
    private fun readBits(json: JSONObject?): Map<String, Int> = readInts(json).filter { it.key in validIds && it.value in 1..3 }
    private fun strings(array: JSONArray?): List<String> = if (array == null) emptyList() else List(array.length()) { array.getString(it) }

    private fun sessionJson(s: QuizSession): JSONObject = JSONObject().put("id", s.id).put("continent", s.continent.name)
        .put("difficulty", s.difficulty.name).put("index", s.index).put("phase", s.phase.name)
        .put("initial", JSONObject(s.initialKnowledge)).put("questions", JSONArray().apply {
            s.questions.forEach { put(JSONObject().put("country", it.countryId).put("kind", it.kind.name).put("options", JSONArray(it.options))) }
        }).put("answers", JSONArray().apply {
            s.answers.forEach { put(JSONObject().put("index", it.questionIndex).put("selected", it.selectedId).put("correct", it.correct)) }
        })

    private fun readSession(json: JSONObject): QuizSession {
        val questionsJson = json.getJSONArray("questions")
        require(questionsJson.length() in 1..8)
        val questions = List(questionsJson.length()) { i ->
            val q = questionsJson.getJSONObject(i)
            val country = q.getString("country")
            val options = strings(q.getJSONArray("options"))
            require(country in validIds && options.size == 4 && options.toSet().size == 4 && country in options && options.all { it in validIds })
            Question(country, QuestionKind.valueOf(q.getString("kind")), options)
        }
        val index = json.getInt("index")
        require(index in questions.indices)
        val continent = Continent.valueOf(json.getString("continent"))
        val difficulty = Difficulty.valueOf(json.getString("difficulty"))
        val catalog = SampleData.countries.associateBy { it.id }
        require(questions.size == difficulty.length)
        questions.forEachIndexed { position, question ->
            val country = catalog.getValue(question.countryId)
            require(country.continent == continent)
            require(question.kind == if (position % 2 == 0) QuestionKind.FLAG else QuestionKind.CAPITAL)
            if (question.kind == QuestionKind.CAPITAL) {
                val capitals = question.options.map { catalog.getValue(it).capital }
                require(capitals.toSet().size == 4 && capitals.count { it in country.acceptedCapitals } == 1)
            }
        }
        val answersJson = json.optJSONArray("answers") ?: JSONArray()
        val answers = List(answersJson.length()) { i ->
            val a = answersJson.getJSONObject(i)
            val qi = a.getInt("index")
            val selected = a.getString("selected")
            require(qi in questions.indices && selected in questions[qi].options)
            Answer(qi, selected, selected == questions[qi].countryId)
        }
        require(answers.map { it.questionIndex }.toSet().size == answers.size)
        require(answers.map { it.questionIndex } == answers.indices.toList())
        val phase = AnswerPhase.valueOf(json.getString("phase"))
        require(answers.dropLast(1).all { it.correct })
        when (phase) {
            AnswerPhase.AWAITING -> require(answers.size == index && answers.all { it.correct })
            AnswerPhase.CORRECT -> require(answers.size == index + 1 && answers.all { it.correct })
            AnswerPhase.INCORRECT -> require(answers.size == index + 1 && answers.lastOrNull()?.correct == false)
            AnswerPhase.COMPLETED -> require(answers.size == index + 1 && (answers.lastOrNull()?.correct == false || index == questions.lastIndex))
        }
        val id = json.getString("id")
        require(id.isNotBlank())
        return QuizSession(id, continent, difficulty, questions, index, answers, phase, readBits(json.optJSONObject("initial")))
    }
}
