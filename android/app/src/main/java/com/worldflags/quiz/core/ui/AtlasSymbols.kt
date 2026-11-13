package com.worldflags.quiz.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.worldflags.quiz.R

@Composable
fun AtlasSymbols() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Image(painterResource(R.drawable.sprite_1), null, Modifier.size(40.dp), contentScale = ContentScale.Fit)
        Image(painterResource(R.drawable.sprite_2), null, Modifier.size(40.dp), contentScale = ContentScale.Fit)
        Image(painterResource(R.drawable.sprite_3), null, Modifier.size(40.dp), contentScale = ContentScale.Fit)
        Image(painterResource(R.drawable.sprite_4), null, Modifier.size(40.dp), contentScale = ContentScale.Fit)
        Image(painterResource(R.drawable.sprite_5), null, Modifier.size(40.dp), contentScale = ContentScale.Fit)
    }
}
