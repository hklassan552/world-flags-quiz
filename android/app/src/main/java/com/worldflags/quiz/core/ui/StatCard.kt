package com.worldflags.quiz.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.worldflags.quiz.R
import com.worldflags.quiz.core.theme.AtlasPurple

@Composable
fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    AtlasCard(modifier) {
        Text(stringResource(R.string.number_value, value), style = MaterialTheme.typography.headlineMedium, color = AtlasPurple)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
