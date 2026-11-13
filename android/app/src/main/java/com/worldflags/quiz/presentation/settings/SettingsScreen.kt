package com.worldflags.quiz.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worldflags.quiz.R
import com.worldflags.quiz.core.di.ServiceLocator
import com.worldflags.quiz.core.ui.*
import com.worldflags.quiz.domain.model.Difficulty
import com.worldflags.quiz.presentation.dialog.ResetProgressDialog

@Composable
fun SettingsRoute(onBack: () -> Unit, onMenu: () -> Unit) {
    val model: SettingsViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    val menuAction by rememberUpdatedState(onMenu)
    LaunchedEffect(Unit) { owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { model.events.collect { menuAction() } } }
    SettingsScreen(state, onBack, onMenu, model::sound, model::motion, model::difficulty, model::confirmReset, model::reset, model::retry)
}

@Composable
fun SettingsScreen(state: SettingsUiState, onBack: () -> Unit, onMenu: () -> Unit, onSound: (Boolean) -> Unit, onMotion: (Boolean) -> Unit, onDifficulty: (Difficulty) -> Unit, onConfirm: (Boolean) -> Unit, onReset: () -> Unit, onRetry: () -> Unit) {
    AtlasPage {
        ScreenHeader(stringResource(R.string.settings_title), onBack)
        Text(stringResource(R.string.make_it_yours), style = MaterialTheme.typography.headlineMedium)
        AtlasCard {
            SettingSwitch(stringResource(R.string.sound_effects), state.settings.sound, onSound)
            HorizontalDivider()
            SettingSwitch(stringResource(R.string.reduced_motion), state.settings.reducedMotion, onMotion)
        }
        AtlasCard {
            Text(stringResource(R.string.difficulty), style = MaterialTheme.typography.titleLarge)
            Column(Modifier.selectableGroup()) {
                Difficulty.entries.forEach { difficulty ->
                    val selected = state.settings.difficulty == difficulty
                    val description = stringResource(if (selected) R.string.selected else R.string.not_selected)
                    OutlinedButton(onClick = { onDifficulty(difficulty) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { stateDescription = description; this.selected = selected; role = Role.RadioButton }) {
                        RadioButton(selected = selected, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(difficultyName(difficulty), style = MaterialTheme.typography.labelLarge)
                            Text(stringResource(R.string.questions_count, difficulty.length), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            Text(stringResource(R.string.difficulty_hint), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.next_expedition_hint), style = MaterialTheme.typography.bodyMedium)
        }
        AtlasCard {
            Text(stringResource(R.string.local_progress), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(if (state.saveError) R.string.save_error else R.string.saved_locally), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.local_learned, state.learned), style = MaterialTheme.typography.bodyMedium)
            SecondaryButton(stringResource(R.string.reset_progress), { onConfirm(true) }, enabled = !state.saving)
        }
        if (state.saveError) FeedbackBanner(stringResource(R.string.save_error), onRetry)
        SecondaryButton(stringResource(R.string.menu), onMenu, Modifier.fillMaxWidth(), icon = R.drawable.icon_home)
        Text(stringResource(R.string.credits), style = MaterialTheme.typography.labelSmall)
    }
    if (state.resetConfirm) ResetProgressDialog({ onConfirm(false) }, onReset)
}

@Composable
private fun SettingSwitch(label: String, enabled: Boolean, onChange: (Boolean) -> Unit) {
    val status = stringResource(if (enabled) R.string.on else R.string.off)
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = enabled, onCheckedChange = onChange, modifier = Modifier.semantics { contentDescription = label; stateDescription = status })
    }
}
