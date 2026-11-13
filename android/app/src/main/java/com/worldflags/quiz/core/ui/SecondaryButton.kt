package com.worldflags.quiz.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.worldflags.quiz.core.theme.*
import com.worldflags.quiz.R

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: Int? = null) {
    OutlinedButton(onClick = onClick, enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (enabled) AtlasPurple.copy(alpha = 0.35f) else AtlasLilac),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = AtlasWhite.copy(alpha = 0.92f), contentColor = AtlasPurple,
            disabledContainerColor = AtlasPaper, disabledContentColor = AtlasInk.copy(alpha = 0.45f))) {
        if (icon != null) {
            Image(painterResource(R.drawable.icon_home), null, Modifier.size(24.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
