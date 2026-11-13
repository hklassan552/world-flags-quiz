package com.worldflags.quiz.presentation.menu

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
fun MenuRoute(busy: Boolean, launchError: Boolean, onPlay: () -> Unit, onResume: () -> Unit, onContinents: () -> Unit, onStudy: () -> Unit, onSettings: () -> Unit) {
    val model: MenuViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    MenuScreen(state, busy, launchError, onPlay, onResume, onContinents, onStudy, onSettings, model::retry)
}

@Composable
fun MenuScreen(state: MenuUiState, busy: Boolean, launchError: Boolean, onPlay: () -> Unit, onResume: () -> Unit, onContinents: () -> Unit, onStudy: () -> Unit, onSettings: () -> Unit, onRetry: () -> Unit) {
    val snapshot = state.snapshot
    val learned = snapshot.learned(state.countries)
    val settings = snapshot.settings
    val best = snapshot.best(settings.selected, settings.difficulty)
    val progress = stringResource(R.string.atlas_progress, learned, state.countries.size)
    AtlasActionPage(Backdrop.MENU, actions = {
        Text(stringResource(R.string.selected_expedition, continentName(settings.selected), difficultyName(settings.difficulty)), style = MaterialTheme.typography.labelSmall, color = AtlasPurple)
        Text(stringResource(R.string.round_hint, settings.difficulty.length), style = MaterialTheme.typography.bodyMedium)
        PrimaryButton(stringResource(if (busy) R.string.preparing else R.string.play), onPlay, enabled = !busy && !state.loading)
    }) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.your_atlas), Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium, color = AtlasPurple)
            TextButton(onClick = onSettings, modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(4.dp)) { Text(stringResource(R.string.settings)) }
        }
        Text(stringResource(R.string.one_country), style = MaterialTheme.typography.labelSmall, color = AtlasPurple)
        AtlasCard(color = AtlasPurple) {
            Text(stringResource(if (learned == state.countries.size && learned > 0) R.string.every_country else if (learned == 0) R.string.first_country else R.string.countries_learned), color = AtlasWhite, style = MaterialTheme.typography.titleLarge)
            if (learned > 0) Text(stringResource(R.string.learned_hero, learned), color = AtlasLime, style = MaterialTheme.typography.displaySmall)
            else Text(stringResource(R.string.first_atlas_hint), color = AtlasPaper, style = MaterialTheme.typography.bodyMedium)
            AtlasSymbols()
        }
        AtlasCard(color = AtlasPaper.copy(alpha = 0.86f)) {
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = AtlasPurple)
            WorldAtlas(state.countries, snapshot.knowledge, progress)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.legend_started), style = MaterialTheme.typography.labelSmall, color = AtlasPurple)
                Text(stringResource(R.string.legend_learned), style = MaterialTheme.typography.labelSmall, color = AtlasInk)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.semantics { stateDescription = progress; liveRegion = LiveRegionMode.Polite }) {
            Text(progress, style = MaterialTheme.typography.bodyLarge)
            LinearProgressIndicator(progress = { if (state.countries.isEmpty()) 0f else learned.toFloat() / state.countries.size }, modifier = Modifier.fillMaxWidth(), color = AtlasPurple, trackColor = AtlasLilac)
        }
        if (learned > 0 && best > 0) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(stringResource(R.string.countries_learned), learned, Modifier.weight(1f))
            StatCard(stringResource(R.string.best_score), best, Modifier.weight(1f))
        }
        if (state.saveError) FeedbackBanner(stringResource(R.string.save_error), onRetry)
        if (state.recovered) FeedbackBanner(stringResource(R.string.recovered_data))
        if (launchError) FeedbackBanner(stringResource(R.string.prepare_error), onPlay)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(stringResource(R.string.continents), onContinents, Modifier.weight(1f))
            SecondaryButton(stringResource(R.string.study), onStudy, Modifier.weight(1f))
        }
        if (snapshot.session?.phase != null && snapshot.session.phase != AnswerPhase.COMPLETED) {
            Text(stringResource(R.string.resume_hint), style = MaterialTheme.typography.bodyMedium)
            SecondaryButton(stringResource(R.string.resume), onResume, Modifier.fillMaxWidth())
        }
    }
}
