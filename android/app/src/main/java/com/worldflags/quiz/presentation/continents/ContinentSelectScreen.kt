package com.worldflags.quiz.presentation.continents

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worldflags.quiz.R
import com.worldflags.quiz.core.di.ServiceLocator
import com.worldflags.quiz.core.ui.*
import com.worldflags.quiz.domain.model.*

@Composable
fun ContinentSelectRoute(busy: Boolean, onBack: () -> Unit, onPlay: () -> Unit, onResume: () -> Unit, onStudy: (Continent) -> Unit) {
    val model: ContinentSelectViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    ContinentSelectScreen(state, busy, onBack, onPlay, onResume, onStudy, model::select, model::retry)
}

@Composable
fun ContinentSelectScreen(state: ContinentSelectUiState, busy: Boolean, onBack: () -> Unit, onPlay: () -> Unit, onResume: () -> Unit, onStudy: (Continent) -> Unit, onSelect: (Continent) -> Unit, onRetry: () -> Unit) {
    val snapshot = state.snapshot
    val unlocked = snapshot.unlocked(state.countries)
    AtlasPage {
        ScreenHeader(stringResource(R.string.continents_title), onBack)
        Text(stringResource(R.string.choose_continent), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.complete_country_hint), style = MaterialTheme.typography.bodyLarge)
        if (state.loading) CircularProgressIndicator()
        if (state.saveError) FeedbackBanner(stringResource(R.string.save_error), onRetry)
        Continent.entries.forEach { continent ->
            val countries = state.countries.filter { it.continent == continent }
            val previous = Continent.entries.getOrNull(continent.ordinal - 1)
            val remaining = if (previous == null) 0 else (3 - state.countries.count { it.continent == previous && snapshot.knowledge[it.id] == 3 }).coerceAtLeast(0)
            val requirement = if (continent !in unlocked && previous != null) stringResource(R.string.unlock_requirement, remaining, continentName(previous)) else null
            ContinentCard(continentName(continent), countries.count { snapshot.knowledge[it.id] == 3 }, countries.size,
                snapshot.settings.selected == continent, continent in unlocked, requirement, { onSelect(continent) }, { onStudy(continent) })
        }
        Text(stringResource(R.string.selected_expedition, continentName(snapshot.settings.selected), difficultyName(snapshot.settings.difficulty)), style = MaterialTheme.typography.bodyMedium)
        if (snapshot.session?.let { it.phase != AnswerPhase.COMPLETED && it.continent == snapshot.settings.selected } == true)
            SecondaryButton(stringResource(R.string.resume), onResume, Modifier.fillMaxWidth())
        PrimaryButton(stringResource(R.string.play), onPlay, enabled = !busy && !state.loading)
    }
}
