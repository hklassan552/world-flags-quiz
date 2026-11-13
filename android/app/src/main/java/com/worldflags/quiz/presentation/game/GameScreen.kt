package com.worldflags.quiz.presentation.game

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worldflags.quiz.R
import com.worldflags.quiz.core.di.ServiceLocator
import com.worldflags.quiz.core.theme.*
import com.worldflags.quiz.core.ui.*
import com.worldflags.quiz.domain.model.*

@Composable
fun GameRoute(sessionId: String, onBack: () -> Unit, onMenu: () -> Unit, onResults: (String) -> Unit) {
    val model: GameViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    val resultsAction by rememberUpdatedState(onResults)
    LaunchedEffect(Unit) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            model.events.collect { event -> if (event is GameEvent.Results) resultsAction(event.id) }
        }
    }
    DisposableEffect(sessionId, owner) {
        model.attach(sessionId)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) model.pause()
            if (event == Lifecycle.Event.ON_START) model.resume()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); model.pause() }
    }
    GameScreen(state, { model.pause(); onBack() }, { model.pause(); onMenu() }, model::answer,
        model::next, model::viewResults, model::showPause, model::continueGame, model::retry)
}

@Composable
fun GameScreen(state: GameUiState, onBack: () -> Unit, onMenu: () -> Unit, onAnswer: (String) -> Unit,
    onNext: () -> Unit, onResults: () -> Unit, onPause: () -> Unit, onContinue: () -> Unit, onRetry: () -> Unit) {
    AtlasBackdrop(Backdrop.GAME) {
        Column(Modifier.align(Alignment.TopCenter).safeDrawingPadding().widthIn(max = 600.dp).fillMaxSize()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val session = state.session
            val country = state.countries.firstOrNull { it.id == session?.question?.countryId }
            if (state.loading) {
                Text(stringResource(R.string.preparing_expedition), style = MaterialTheme.typography.headlineMedium)
                CircularProgressIndicator()
            } else if (session == null || country == null || state.error) {
                ScreenHeader(stringResource(R.string.expedition), onBack)
                FeedbackBanner(stringResource(R.string.prepare_error), onRetry)
                SecondaryButton(stringResource(R.string.menu), onMenu, Modifier.fillMaxWidth(), icon = R.drawable.icon_home)
            } else {
                AtlasCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp), modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) { Text(stringResource(R.string.back)) }
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.expedition_name, continentName(session.continent)), style = MaterialTheme.typography.titleMedium, color = AtlasPurple)
                            Text(stringResource(R.string.question_position, session.index + 1, session.questions.size), style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                        }
                        TextButton(onClick = onPause, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp), contentPadding = PaddingValues(0.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(painterResource(R.drawable.icon_pause), null, Modifier.size(24.dp), contentScale = ContentScale.Fit)
                                Text(stringResource(R.string.pause), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    val scoreStatus = stringResource(R.string.live_score, session.score)
                    Text(scoreStatus, style = MaterialTheme.typography.labelSmall, color = AtlasPurple,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite; stateDescription = scoreStatus })
                }
                LinearProgressIndicator(progress = { session.answers.size.toFloat() / session.questions.size }, modifier = Modifier.fillMaxWidth(), color = AtlasPurple, trackColor = AtlasLilac)
                AtlasCard {
                    if (session.question.kind == QuestionKind.FLAG) {
                        Text(stringResource(R.string.flag_question), style = MaterialTheme.typography.titleLarge)
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { FlagImage(country.id, flagDescription(country.id)) }
                    } else {
                        Text(stringResource(R.string.capital_eyebrow), style = MaterialTheme.typography.labelSmall, color = AtlasPurple)
                        Text(countryName(country.id), style = MaterialTheme.typography.headlineMedium)
                        Text(capitalQuestion(country), style = MaterialTheme.typography.titleLarge)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val letters = listOf(R.string.option_a, R.string.option_b, R.string.option_c, R.string.option_d)
                    session.question.options.forEachIndexed { index, id ->
                        val optionState = when {
                            session.phase == AnswerPhase.AWAITING && !state.submitting -> OptionState.IDLE
                            session.phase != AnswerPhase.AWAITING && id == country.id -> OptionState.CORRECT
                            session.phase != AnswerPhase.AWAITING && id == session.selectedId -> OptionState.INCORRECT
                            else -> OptionState.LOCKED
                        }
                        AnswerOption(stringResource(letters[index]), if (session.question.kind == QuestionKind.FLAG) countryName(id) else capitalName(id), optionState) { onAnswer(id) }
                    }
                }
                when (session.phase) {
                    AnswerPhase.AWAITING -> Text(stringResource(R.string.choose_answer), style = MaterialTheme.typography.bodyMedium)
                    AnswerPhase.CORRECT -> {
                        FeedbackBanner(capitalFact(country))
                        PrimaryButton(stringResource(if (session.index == session.questions.lastIndex) R.string.results else R.string.next), onNext, enabled = !state.submitting)
                    }
                    AnswerPhase.INCORRECT -> FeedbackBanner(stringResource(R.string.good_try,
                        if (session.question.kind == QuestionKind.FLAG) countryName(country.id) else capitalName(country.id)))
                    AnswerPhase.COMPLETED -> {
                        FeedbackBanner(stringResource(R.string.completed_review))
                        PrimaryButton(stringResource(R.string.view_results), onResults)
                        SecondaryButton(stringResource(R.string.menu), onMenu, Modifier.fillMaxWidth(), icon = R.drawable.icon_home)
                    }
                }
                if (state.saveError) FeedbackBanner(stringResource(R.string.save_error), onRetry)
            }
        }
    }
    if (state.pauseDialog) AlertDialog(onDismissRequest = onContinue,
        title = { Text(stringResource(R.string.paused_title)) }, text = { Text(stringResource(R.string.paused_message)) },
        confirmButton = { TextButton(onClick = onContinue, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.resume)) } },
        dismissButton = { TextButton(onClick = onMenu, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.menu)) } })
}
