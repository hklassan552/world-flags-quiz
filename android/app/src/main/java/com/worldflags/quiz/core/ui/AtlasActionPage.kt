package com.worldflags.quiz.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun AtlasActionPage(tone: Backdrop, actions: @Composable ColumnScope.() -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val fontScale = LocalDensity.current.fontScale
    val scroll = rememberScrollState()
    AtlasBackdrop(tone) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
            val anchored = maxHeight >= 560.dp && fontScale < 1.6f
            Column(Modifier.widthIn(max = 600.dp).fillMaxSize()
                .then(if (anchored) Modifier else Modifier.verticalScroll(scroll))
                .padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.fillMaxWidth().then(if (anchored) Modifier.weight(1f).verticalScroll(scroll) else Modifier),
                    verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
                actions()
            }
        }
    }
}
