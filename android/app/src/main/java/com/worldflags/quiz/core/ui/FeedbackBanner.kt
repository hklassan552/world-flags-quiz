package com.worldflags.quiz.core.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.res.stringResource
import com.worldflags.quiz.R
import com.worldflags.quiz.core.theme.AtlasPaper

@Composable
fun FeedbackBanner(message: String, onRetry: (() -> Unit)? = null) {
    AtlasCard(Modifier.semantics { liveRegion = LiveRegionMode.Polite; stateDescription = message }, color = AtlasPaper) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
        if (onRetry != null) SecondaryButton(stringResource(R.string.retry), onRetry)
    }
}
