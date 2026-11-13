package com.worldflags.quiz.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import com.worldflags.quiz.R
import com.worldflags.quiz.core.theme.*

@Composable
fun ContinentCard(name: String, learned: Int, total: Int, selected: Boolean, unlocked: Boolean, requirement: String?, onSelect: () -> Unit, onStudy: () -> Unit) {
    val status = stringResource(if (selected) R.string.selected else if (unlocked) R.string.unlocked else R.string.locked)
    AtlasCard(Modifier.semantics { stateDescription = status; if (selected) liveRegion = LiveRegionMode.Polite }, color = if (selected) AtlasLilac.copy(alpha = 0.5f) else AtlasWhite.copy(alpha = 0.94f)) {
        Text(name, style = MaterialTheme.typography.titleLarge, color = AtlasPurple)
        Text(stringResource(R.string.continent_progress, learned, total), style = MaterialTheme.typography.bodyMedium)
        LinearProgressIndicator(progress = { if (total == 0) 0f else learned.toFloat() / total }, modifier = Modifier.fillMaxWidth(), color = AtlasPurple, trackColor = AtlasPaper)
        Text(status, style = MaterialTheme.typography.labelSmall)
        if (requirement != null) Text(requirement, style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            if (unlocked) SecondaryButton(stringResource(R.string.select), onSelect)
            SecondaryButton(stringResource(R.string.study), onStudy)
        }
    }
}
