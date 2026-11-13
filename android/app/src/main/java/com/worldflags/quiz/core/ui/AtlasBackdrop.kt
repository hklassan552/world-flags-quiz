package com.worldflags.quiz.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.worldflags.quiz.R
import com.worldflags.quiz.core.theme.*

enum class Backdrop { SPLASH, MENU, GAME, RESULT, LIBRARY }

@Composable
fun AtlasBackdrop(tone: Backdrop, content: @Composable BoxScope.() -> Unit) {
    val colors = when (tone) {
        Backdrop.SPLASH -> listOf(AtlasPurple.copy(alpha = 0.94f), AtlasInk.copy(alpha = 0.96f))
        Backdrop.MENU -> listOf(AtlasPaper.copy(alpha = 0.94f), AtlasWhite.copy(alpha = 0.90f), AtlasLilac.copy(alpha = 0.94f))
        Backdrop.GAME -> listOf(AtlasPaper.copy(alpha = 0.96f), AtlasWhite.copy(alpha = 0.92f), AtlasPaper.copy(alpha = 0.96f))
        Backdrop.RESULT -> listOf(AtlasLilac.copy(alpha = 0.94f), AtlasPaper.copy(alpha = 0.94f), AtlasWhite.copy(alpha = 0.94f))
        Backdrop.LIBRARY -> listOf(AtlasWhite.copy(alpha = 0.94f), AtlasPaper.copy(alpha = 0.93f), AtlasLilac.copy(alpha = 0.90f))
    }
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_game), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
        content()
    }
}

@Composable
fun AtlasPage(tone: Backdrop = Backdrop.LIBRARY, content: @Composable ColumnScope.() -> Unit) {
    AtlasBackdrop(tone) {
        Column(
            Modifier.align(Alignment.TopCenter).safeDrawingPadding().widthIn(max = 600.dp).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}
