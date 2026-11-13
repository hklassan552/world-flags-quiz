package com.worldflags.quiz.presentation.study

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worldflags.quiz.R
import com.worldflags.quiz.core.di.ServiceLocator
import com.worldflags.quiz.core.ui.*
import com.worldflags.quiz.domain.model.Continent

@Composable
fun StudyRoute(continent: Continent?, countryId: String?, onBack: () -> Unit) {
    val model: StudyViewModel = viewModel(key = "study:$continent:$countryId", factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    DisposableEffect(model) { model.open(continent, countryId); onDispose { } }
    StudyScreen(state, onBack, model::query, model::filter, model::toggle, model::retry)
}

@Composable
fun StudyScreen(state: StudyUiState, onBack: () -> Unit, onQuery: (String) -> Unit, onFilter: (Continent?) -> Unit, onToggle: (String) -> Unit, onRetry: () -> Unit) {
    AtlasBackdrop(Backdrop.LIBRARY) {
        Column(Modifier.align(Alignment.TopCenter).safeDrawingPadding().widthIn(max = 600.dp).fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScreenHeader(stringResource(R.string.study_title), onBack)
            OutlinedTextField(value = state.query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text(stringResource(R.string.search_label)) })
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = state.filter == null, onClick = { onFilter(null) }, label = { Text(stringResource(R.string.all)) }, modifier = Modifier.heightIn(min = 48.dp))
                Continent.entries.forEach { continent ->
                    FilterChip(selected = state.filter == continent, onClick = { onFilter(continent) }, label = { Text(continentName(continent)) }, modifier = Modifier.heightIn(min = 48.dp))
                }
            }
            Text(stringResource(R.string.search_count, state.countries.size), style = MaterialTheme.typography.labelSmall)
            LazyColumn(Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.loading) item { CircularProgressIndicator() }
                if (state.error) item { FeedbackBanner(stringResource(R.string.catalog_error), onRetry) }
                if (!state.loading && state.countries.isEmpty()) item {
                    AtlasCard {
                        Image(painterResource(R.drawable.sprite_5), null, Modifier.size(120.dp).align(Alignment.CenterHorizontally), contentScale = ContentScale.Fit)
                        Text(stringResource(R.string.no_countries), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.no_matches, state.query), style = MaterialTheme.typography.bodyMedium)
                        SecondaryButton(stringResource(R.string.clear_search), { onQuery("") })
                        if (state.filter != null) SecondaryButton(stringResource(R.string.all_continents), { onFilter(null) })
                    }
                }
                items(state.countries, key = { it.id }) { country ->
                    CountryStudyCard(country, state.knowledge[country.id] ?: 0, state.expanded == country.id) { onToggle(country.id) }
                }
            }
        }
    }
}
