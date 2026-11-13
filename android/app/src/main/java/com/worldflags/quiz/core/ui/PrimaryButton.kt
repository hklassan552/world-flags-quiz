package com.worldflags.quiz.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.worldflags.quiz.R
import com.worldflags.quiz.core.theme.*

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val reducedMotion = LocalReducedMotion.current
    val scale by animateFloatAsState(if (pressed && !reducedMotion) 0.98f else 1f, spring(dampingRatio = 0.78f, stiffness = 500f), label = "button")
    val shape = RoundedCornerShape(18.dp)
    val status = stringResource(if (enabled) R.string.action_ready else R.string.action_wait)
    Button(onClick = onClick, enabled = enabled, interactionSource = source, shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = AtlasInk,
            disabledContainerColor = AtlasLilac.copy(alpha = 0.6f), disabledContentColor = AtlasInk.copy(alpha = 0.5f)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 60.dp).scale(scale).semantics { stateDescription = status }
            .shadow(if (enabled) 6.dp else 0.dp, shape, ambientColor = AtlasPurple, spotColor = AtlasPurple)
            .background(Brush.linearGradient(if (enabled) listOf(AtlasLime, AtlasLilac) else listOf(AtlasPaper, AtlasLilac)), shape)) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
