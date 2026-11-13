package com.worldflags.quiz.presentation.gameover

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worldflags.quiz.R
import com.worldflags.quiz.core.di.ServiceLocator
import com.worldflags.quiz.core.theme.*
import com.worldflags.quiz.core.ui.*
import com.worldflags.quiz.domain.model.*

@Composable
fun GameOverRoute(resultId: String, busy: Boolean, onBack: () -> Unit, onMenu: () -> Unit, onAgain: (Continent, Difficulty) -> Unit, onReview: (Continent, String) -> Unit) {
    val model: GameOverViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    DisposableEffect(resultId) { model.open(resultId); onDispose { } }
    GameOverScreen(state, busy, onBack, onMenu, onAgain, onReview, model::retry)
}

@Composable
fun GameOverScreen(state: GameOverUiState, busy: Boolean, onBack: () -> Unit, onMenu: () -> Unit, onAgain: (Continent, Difficulty) -> Unit, onReview: (Continent, String) -> Unit, onRetry: () -> Unit) {
    val result = state.result
    AtlasActionPage(Backdrop.RESULT, actions = {
        if (result != null) {
            Text(stringResource(R.string.again_hint), style = MaterialTheme.typography.bodyMedium)
            PrimaryButton(stringResource(R.string.play_again), { onAgain(result.session.continent, result.session.difficulty) }, enabled = !busy)
        }
        SecondaryButton(stringResource(R.string.menu), onMenu, Modifier.fillMaxWidth(), icon = R.drawable.icon_home)
    }) {
        ScreenHeader(stringResource(R.string.results_title), onBack)
        if (state.loading) CircularProgressIndicator()
        else if (result == null) FeedbackBanner(stringResource(R.string.result_missing))
        else {
            val session = result.session
            Column(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(painterResource(R.drawable.sprite_4), null, Modifier.size(88.dp), contentScale = ContentScale.Fit)
                Text(stringResource(if (result.perfect) R.string.expedition_complete else R.string.more_to_discover),
                    style = MaterialTheme.typography.headlineMedium, color = AtlasPurple, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text(stringResource(R.string.points, result.score), style = MaterialTheme.typography.displaySmall, color = AtlasInk)
                Text(stringResource(R.string.correct_summary, result.correct, result.answered), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.accuracy, result.accuracy), style = MaterialTheme.typography.labelLarge, color = AtlasPurple)
            }
            if (!result.perfect) Text(stringResource(R.string.ended_early, session.index + 1, session.questions.size), style = MaterialTheme.typography.bodyMedium)
            if (result.score > 0 && result.correct > 0) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(stringResource(R.string.score), result.score, Modifier.weight(1f))
                StatCard(stringResource(R.string.correct_answers), result.correct, Modifier.weight(1f))
            }
            AtlasCard {
                Text(stringResource(if (result.newFacts > 0) R.string.new_facts else R.string.practice_hint, result.newFacts), style = MaterialTheme.typography.titleMedium, color = AtlasPurple)
                result.newlyLearned.forEach { id -> Text(stringResource(R.string.country_completed, countryName(id)), style = MaterialTheme.typography.bodyMedium) }
                result.unlocked.forEach { continent -> Text(stringResource(R.string.continent_unlocked, continentName(continent)), style = MaterialTheme.typography.bodyLarge) }
            }
            if (result.perfect) {
                AtlasCard {
                    Text(stringResource(R.string.every_answer_correct), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.perfect_hint), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Text(stringResource(R.string.field_notes), style = MaterialTheme.typography.titleLarge, color = AtlasPurple)
                session.answers.filter { !it.correct }.forEach { answer ->
                    val question = session.questions[answer.questionIndex]
                    val country = state.countries.firstOrNull { it.id == question.countryId }
                    if (country != null) AtlasCard {
                        FlagImage(country.id, stringResource(R.string.flag_of, countryName(country.id)), 112.dp, 70.dp)
                        Text(if (question.kind == QuestionKind.FLAG) stringResource(R.string.flag_question) else capitalQuestion(country), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.your_answer, if (question.kind == QuestionKind.FLAG) countryName(answer.selectedId) else capitalName(answer.selectedId)), style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(R.string.correct_answer_value, if (question.kind == QuestionKind.FLAG) countryName(country.id) else capitalName(country.id)), style = MaterialTheme.typography.bodyLarge, color = AtlasPurple)
                        Text(capitalFact(country), style = MaterialTheme.typography.bodyMedium)
                        SecondaryButton(stringResource(R.string.review), { onReview(country.continent, country.id) })
                    }
                }
            }
            if (state.saveError) FeedbackBanner(stringResource(R.string.save_error), onRetry)
        }
    }
}
