package com.worldflags.quiz.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.worldflags.quiz.R
import com.worldflags.quiz.core.theme.*

enum class OptionState { IDLE, CORRECT, INCORRECT, LOCKED }

@Composable
fun AnswerOption(letter: String, label: String, state: OptionState, onClick: () -> Unit) {
    val status = stringResource(when (state) {
        OptionState.IDLE -> R.string.not_selected
        OptionState.CORRECT -> R.string.correct_answer
        OptionState.INCORRECT -> R.string.your_answer_incorrect
        OptionState.LOCKED -> R.string.answer_locked
    })
    val color by animateColorAsState(when (state) {
        OptionState.CORRECT -> AtlasLime
        OptionState.INCORRECT -> AtlasLilac
        OptionState.LOCKED -> AtlasPaper
        OptionState.IDLE -> AtlasWhite
    }, tween(180), label = "answer")
    OutlinedButton(onClick = onClick, enabled = state == OptionState.IDLE,
        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).semantics { stateDescription = status },
        shape = RoundedCornerShape(16.dp), border = BorderStroke(if (state == OptionState.IDLE) 1.dp else 2.dp, AtlasPurple.copy(alpha = if (state == OptionState.LOCKED) 0.16f else 0.45f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = color, contentColor = AtlasInk,
            disabledContainerColor = color, disabledContentColor = AtlasInk.copy(alpha = if (state == OptionState.LOCKED) 0.55f else 1f)),
        contentPadding = PaddingValues(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(8.dp), color = AtlasPurple.copy(alpha = 0.10f)) {
                Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) { Text(letter, style = MaterialTheme.typography.labelLarge) }
            }
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                if (state == OptionState.CORRECT || state == OptionState.INCORRECT) Text(status, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
