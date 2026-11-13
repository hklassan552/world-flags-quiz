package com.worldflags.quiz.presentation.app

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.view.WindowCompat
import com.worldflags.quiz.core.di.ServiceLocator
import com.worldflags.quiz.core.navigation.Screen
import com.worldflags.quiz.core.theme.LocalReducedMotion
import com.worldflags.quiz.presentation.continents.ContinentSelectRoute
import com.worldflags.quiz.presentation.game.GameRoute
import com.worldflags.quiz.presentation.game.GameViewModel
import com.worldflags.quiz.presentation.gameover.GameOverRoute
import com.worldflags.quiz.presentation.menu.MenuRoute
import com.worldflags.quiz.presentation.settings.SettingsRoute
import com.worldflags.quiz.presentation.splash.SplashRoute
import com.worldflags.quiz.presentation.study.StudyRoute

@Composable
fun AppRoot() {
    val model: AppViewModel = viewModel(factory = ServiceLocator.factory)
    val gameModel: GameViewModel = viewModel(factory = ServiceLocator.factory)
    val screen by model.screen.collectAsStateWithLifecycle()
    val state by model.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            model.events.collect { event -> if (event == AppEvent.Finish) (context as? Activity)?.finish() }
        }
    }
    BackHandler { if (screen is Screen.Game) gameModel.pause(); model.back() }
    DisposableEffect(screen, context) {
        (context as? Activity)?.window?.let { window ->
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = screen != Screen.Splash
            controller.isAppearanceLightNavigationBars = screen != Screen.Splash
        }
        onDispose { }
    }
    CompositionLocalProvider(LocalReducedMotion provides state.reducedMotion) {
        AnimatedContent(targetState = screen, transitionSpec = {
            fadeIn(tween(if (state.reducedMotion) 150 else 220)) togetherWith fadeOut(tween(if (state.reducedMotion) 150 else 220))
        }, label = "destination") { destination ->
            when (destination) {
                Screen.Splash -> SplashRoute(model::splashFinished)
                Screen.Menu -> MenuRoute(state.busy, state.error, { model.play() }, model::resume,
                    { model.navigate(Screen.ContinentSelect) }, { model.navigate(Screen.Study()) }, { model.navigate(Screen.Settings) })
                Screen.ContinentSelect -> ContinentSelectRoute(state.busy, model::back, { model.play() }, model::resume,
                    { model.navigate(Screen.Study(it)) })
                is Screen.Game -> GameRoute(destination.sessionId, model::back, model::menu, model::results)
                is Screen.GameOver -> GameOverRoute(destination.resultId, state.busy, model::back, model::menu,
                    { continent, difficulty -> model.play(continent, difficulty, replay = true) },
                    { continent, country -> model.navigate(Screen.Study(continent, country)) })
                is Screen.Study -> StudyRoute(destination.continent, destination.countryId, model::back)
                Screen.Settings -> SettingsRoute(model::back, model::menu)
            }
        }
    }
}
