package com.worldflags.quiz.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashRoute(onFinished: () -> Unit) {
    val model: SplashViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    val finished by model.finished.collectAsStateWithLifecycle()
    val finishAction by rememberUpdatedState(onFinished)
    LaunchedEffect(finished) { if (finished) finishAction() }
    SplashScreen(state)
}

@Composable
fun SplashScreen(state: SplashUiState) {
    val loop = rememberInfiniteTransition(label = "atlasOrbit")
    val drift by loop.animateFloat(-6f, 6f, infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "globeFloat")
    val angle by loop.animateFloat(0f, 6.283185f, infiniteRepeatable(tween(4800, easing = LinearEasing)), label = "orbit")
    val glow by loop.animateFloat(0.72f, 1f, infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "glow")
    val announcement = stringResource(R.string.splash_announcement)
    AtlasBackdrop(Backdrop.SPLASH) {
        Column(Modifier.align(Alignment.Center).safeDrawingPadding().widthIn(max = 600.dp).fillMaxWidth()
            .verticalScroll(rememberScrollState()).padding(28.dp).semantics { contentDescription = announcement },
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(stringResource(R.string.splash_eyebrow), color = AtlasLilac, style = MaterialTheme.typography.labelSmall)
            Box(Modifier.size(232.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val radius = size.minDimension * 0.45f
                    drawCircle(AtlasWhite.copy(alpha = 0.17f), radius, style = Stroke(1.dp.toPx()))
                    repeat(3) { index ->
                        val a = (if (state.reducedMotion) 0f else angle) + index * 2.0944f
                        drawCircle(if (index == 0) AtlasLime else AtlasLilac, (4 + index).dp.toPx(), Offset(center.x + cos(a) * radius, center.y + sin(a) * radius))
                    }
                }
                Image(painterResource(R.drawable.logo), null, Modifier.size(176.dp)
                    .offset(y = if (state.reducedMotion) 0.dp else drift.dp).alpha(glow), contentScale = ContentScale.Fit)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.splash_title), color = AtlasWhite, style = MaterialTheme.typography.displaySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text(stringResource(R.string.splash_subtitle), color = AtlasPaper, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            CircularProgressIndicator(Modifier.size(40.dp), color = AtlasLime, strokeWidth = 4.dp, trackColor = AtlasWhite.copy(alpha = 0.12f))
            Text(stringResource(R.string.preparing_atlas), color = AtlasLilac, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
