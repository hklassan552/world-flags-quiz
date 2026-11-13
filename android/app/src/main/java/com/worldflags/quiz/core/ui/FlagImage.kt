package com.worldflags.quiz.core.ui

import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import com.worldflags.quiz.core.theme.*
import com.worldflags.quiz.data.sample.FlagVectors
import java.util.Base64
import java.util.zip.GZIPInputStream
import kotlin.math.min

private data class FlagLayer(val path: Path, val fill: Int?, val stroke: Int?, val width: Float, val cap: Paint.Cap, val join: Paint.Join, val clips: List<Path>)
private data class PreparedFlag(val width: Float, val height: Float, val layers: List<FlagLayer>)
private val flagCache = object : LinkedHashMap<String, PreparedFlag>(24, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, PreparedFlag>?): Boolean = size > 24
}

private fun prepareFlag(id: String): PreparedFlag = synchronized(flagCache) {
    flagCache[id] ?: run {
        val art = FlagVectors.get(id)
        val raw = GZIPInputStream(Base64.getUrlDecoder().decode(art.encoded).inputStream()).bufferedReader().use { it.readText() }
        val layers = raw.lineSequence().filter { it.isNotBlank() }.map { row ->
            val values = row.split('\t')
            val path = PathParser.createPathFromPathData(values[6]) ?: Path()
            path.fillType = if (values[3] == "evenodd") Path.FillType.EVEN_ODD else Path.FillType.WINDING
            FlagLayer(path, values[0].takeUnless { it == "-" }?.toLong(16)?.toInt(),
                values[1].takeUnless { it == "-" }?.toLong(16)?.toInt(), values[2].toFloat(),
                when (values[4]) { "round" -> Paint.Cap.ROUND; "square" -> Paint.Cap.SQUARE; else -> Paint.Cap.BUTT },
                when (values[5]) { "round" -> Paint.Join.ROUND; "bevel" -> Paint.Join.BEVEL; else -> Paint.Join.MITER },
                values.drop(7).mapNotNull { PathParser.createPathFromPathData(it) })
        }.toList()
        PreparedFlag(art.width, art.height, layers).also { flagCache[id] = it }
    }
}

@Composable
fun FlagImage(countryId: String, description: String, width: Dp = 208.dp, height: Dp = 112.dp) {
    val art = remember(countryId) { prepareFlag(countryId) }
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }
    Surface(Modifier.width(width).height(height).semantics { contentDescription = description; role = Role.Image },
        shape = RoundedCornerShape(8.dp), color = AtlasWhite, border = BorderStroke(1.dp, AtlasPurple.copy(alpha = 0.16f))) {
        Canvas(Modifier.fillMaxSize().padding(5.dp)) {
            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                val scale = min(size.width / art.width, size.height / art.height)
                native.save()
                native.translate((size.width - art.width * scale) / 2, (size.height - art.height * scale) / 2)
                native.scale(scale, scale)
                native.clipRect(0f, 0f, art.width, art.height)
                art.layers.forEach { layer ->
                    native.save()
                    layer.clips.forEach { native.clipPath(it) }
                    layer.fill?.let { color -> paint.color = color; paint.style = Paint.Style.FILL; native.drawPath(layer.path, paint) }
                    layer.stroke?.let { color ->
                        paint.color = color; paint.style = Paint.Style.STROKE; paint.strokeWidth = layer.width
                        paint.strokeCap = layer.cap; paint.strokeJoin = layer.join; native.drawPath(layer.path, paint)
                    }
                    native.restore()
                }
                native.restore()
            }
        }
    }
}
