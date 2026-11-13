package com.worldflags.quiz.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.worldflags.quiz.core.theme.*
import com.worldflags.quiz.data.sample.MapGeometry
import com.worldflags.quiz.domain.model.Country

@Composable
fun WorldAtlas(countries: List<Country>, knowledge: Map<String, Int>, description: String) {
    Canvas(Modifier.fillMaxWidth().height(188.dp).semantics { contentDescription = description; stateDescription = description }) {
        fun point(lon: Float, lat: Float) = Offset((lon + 180f) / 360f * size.width, (85f - lat) / 160f * size.height)
        for (longitude in -180..180 step 30) {
            val x = point(longitude.toFloat(), 0f).x
            drawLine(AtlasPurple.copy(alpha = 0.08f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
        }
        for (latitude in -60..80 step 20) {
            val y = point(0f, latitude.toFloat()).y
            drawLine(AtlasPurple.copy(alpha = 0.08f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        }
        countries.forEach { country ->
            val bits = knowledge[country.id] ?: 0
            val fill = when (bits) { 3 -> AtlasLime; 0 -> AtlasLilac.copy(alpha = 0.74f); else -> AtlasPurple }
            MapGeometry.polygons[country.id]?.forEach { polygon ->
                val path = Path()
                var previousX: Float? = null
                polygon.asList().chunked(2).forEachIndexed { index, pair ->
                    val p = point(pair[0], pair[1])
                    if (index == 0 || previousX?.let { kotlin.math.abs(p.x - it) > size.width / 2 } == true) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                    previousX = p.x
                }
                path.close()
                drawPath(path, fill)
                drawPath(path, AtlasPurple.copy(alpha = 0.30f), style = Stroke(0.45.dp.toPx()))
                if (bits in 1..2) drawCircle(AtlasWhite, 1.dp.toPx(), point(country.longitude, country.latitude))
            }
            val polygons = MapGeometry.polygons[country.id].orEmpty()
            val coordinates = polygons.flatMap { it.toList().chunked(2) }
            if (coordinates.isNotEmpty() && ((coordinates.maxOf { it[0] } - coordinates.minOf { it[0] }) / 360 * size.width < 3.dp.toPx())) {
                drawCircle(fill, 1.8.dp.toPx(), point(country.longitude, country.latitude))
                drawCircle(AtlasPurple.copy(alpha = 0.55f), 1.8.dp.toPx(), point(country.longitude, country.latitude), style = Stroke(0.5.dp.toPx()))
            }
        }
        val antarctica = Path().apply {
            moveTo(0f, size.height * 0.98f)
            lineTo(size.width * 0.12f, size.height * 0.94f); lineTo(size.width * 0.28f, size.height * 0.97f)
            lineTo(size.width * 0.55f, size.height * 0.93f); lineTo(size.width * 0.80f, size.height * 0.95f)
            lineTo(size.width, size.height * 0.96f); lineTo(size.width, size.height); lineTo(0f, size.height); close()
        }
        drawPath(antarctica, AtlasLilac.copy(alpha = 0.45f))
    }
}
