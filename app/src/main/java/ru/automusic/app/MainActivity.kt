package ru.automusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val navy = Color(0xFF07162E)
private val panel = Color(0xDD142D50)
private val blue = Color(0xFF32A9F6)
private val pale = Color(0xFFA9C5E8)
private val yellow = Color(0xFFF6C945)
private const val duration = 247

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AutoMusicScreen() } }
    }
}

@Composable
private fun AutoMusicScreen() {
    var source by remember { mutableStateOf("Избранное") }
    var showList by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var shuffle by remember { mutableStateOf(false) }
    var liked by remember { mutableStateOf(false) }
    var disliked by remember { mutableStateOf(false) }
    var index by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableIntStateOf(0) }
    val tracks = remember { (1..24).map { "Демонстрационный трек $it" } }
    LaunchedEffect(playing, index) {
        while (playing) {
            delay(1000)
            if (seconds >= duration) {
                index = (index + 1) % tracks.size
                seconds = 0
            } else seconds++
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(navy)) {
        val narrow = maxWidth < 720.dp
        val short = maxHeight < 430.dp
        WaveBackground()
        val gap = if (short) 6.dp else 12.dp
        Column(Modifier.fillMaxSize().padding(horizontal = if (narrow) 10.dp else 20.dp, vertical = if (short) 8.dp else 18.dp), verticalArrangement = Arrangement.spacedBy(gap)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("♫ AutoMusic", color = yellow, fontWeight = FontWeight.Bold, fontSize = if (narrow) 18.sp else 25.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Control(if (shuffle) "⤨" else "⤨", { shuffle = !shuffle }, height = 42.dp, small = true, accent = shuffle)
                    Control(if (source == "Избранное") "Моя волна" else "Избранное", { source = if (source == "Избранное") "Моя волна" else "Избранное"; index = 0; seconds = 0 }, height = 42.dp, small = true)
                    Control(if (showList) "Плеер" else "Список", { showList = !showList }, height = 42.dp, small = true)
                }
            }
            Text(source.uppercase() + if (showList) "  •  СПИСОК" else "  •  СЕЙЧАС ИГРАЕТ", color = pale, fontSize = 12.sp)
            if (showList) {
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    itemsIndexed(tracks) { i, title ->
                        Row(Modifier.fillMaxWidth().background(if (i == index) Color(0xDD244B7B) else panel, RoundedCornerShape(9.dp)).clickable { index = i; seconds = 0; playing = true; showList = false }.padding(horizontal = 12.dp, vertical = if (short) 8.dp else 13.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${i + 1}", color = pale, modifier = Modifier.width(32.dp), fontSize = 13.sp)
                            Column(Modifier.weight(1f)) {
                                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, fontSize = 15.sp)
                                Text("Демонстрационный исполнитель", color = pale, fontSize = 12.sp, maxLines = 1)
                            }
                            Text(if (i == index && playing) "♫" else "▶", color = blue, fontSize = 19.sp)
                        }
                    }
                }
            } else {
                if (narrow) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Cover(if (short) 58.dp else 90.dp)
                        Column(Modifier.weight(1f)) {
                            Text(tracks[index], color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Демонстрационный исполнитель", color = pale, fontSize = 13.sp, maxLines = 1)
                        }
                    }
                } else {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
                        Cover(if (short) 100.dp else 210.dp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(tracks[index], color = Color.White, fontWeight = FontWeight.Bold, fontSize = 30.sp)
                            Text("Демонстрационный исполнитель", color = pale, fontSize = 19.sp)
                        }
                    }
                }
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Slider(value = seconds.toFloat(), onValueChange = { seconds = it.toInt() }, valueRange = 0f..duration.toFloat(), modifier = Modifier.fillMaxWidth().height(if (short) 26.dp else 34.dp), colors = SliderDefaults.colors(thumbColor = yellow, activeTrackColor = yellow, inactiveTrackColor = Color(0xFF284466)))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(time(seconds), color = pale, fontSize = 11.sp)
                        Text(time(duration), color = pale, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.weight(1f, fill = true))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(if (short) 82.dp else 115.dp)) { SymbolButton(true, liked, { liked = !liked; if (liked) disliked = false }, if (short) 48.dp else 62.dp) }
                    Spacer(Modifier.width(if (short) 20.dp else 34.dp))
                    Box(Modifier.width(if (short) 82.dp else 115.dp)) { SymbolButton(false, disliked, { disliked = !disliked; if (disliked) liked = false }, if (short) 48.dp else 62.dp) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Box(Modifier.weight(1f)) { Control("⏮", { index = (index + tracks.size - 1) % tracks.size; seconds = 0 }, full = true, height = if (short) 65.dp else 76.dp, fontSize = 30) }
                    Box(Modifier.weight(1f)) { Control(if (playing) "Ⅱ" else "▶", { playing = !playing }, full = true, accent = true, height = if (short) 65.dp else 76.dp, fontSize = 30) }
                    Box(Modifier.weight(1f)) { Control("⏭", { index = (index + 1) % tracks.size; seconds = 0 }, full = true, height = if (short) 65.dp else 76.dp, fontSize = 30) }
                }
            }
            if (!short) Text("ПРОТОТИП • без подключения к Яндекс Музыке", color = pale, fontSize = 10.sp)
        }
    }
}

private fun time(seconds: Int) = "%d:%02d".format(seconds / 60, seconds % 60)

@Composable
private fun WaveBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF040C20), Color(0xFF061B39), Color(0xFF040D20))))
        val w = size.width
        val h = size.height
        // One luminous flowing wave with fine parallel filaments, rather than wide horizontal stripes.
        for (i in 0..18) {
            val shift = (i - 9) * h * .007f
            val path = Path().apply {
                moveTo(-w * .08f, h * .53f + shift)
                cubicTo(w * .22f, h * .31f + shift, w * .38f, h * .77f + shift, w * .67f, h * .49f + shift)
                cubicTo(w * .83f, h * .33f + shift, w * .93f, h * .43f + shift, w * 1.1f, h * .50f + shift)
            }
            val distance = kotlin.math.abs(i - 9)
            val opacity = .26f * (1f - distance / 12f)
            drawPath(path, color = Color(0xFF0C8DFF).copy(alpha = opacity.coerceAtLeast(.025f)), style = Stroke(width = 1.1.dp.toPx()))
            if (i == 9) {
                drawPath(path, color = Color(0xFF008FFF).copy(alpha = .15f), style = Stroke(width = 13.dp.toPx()))
                drawPath(path, color = Color(0xFF00B8FF).copy(alpha = .28f), style = Stroke(width = 5.dp.toPx()))
                drawPath(path, color = Color(0xFF7EE7FF).copy(alpha = .85f), style = Stroke(width = 1.4.dp.toPx()))
            }
        }
    }
}

@Composable
private fun Cover(size: Dp) {
    Box(Modifier.size(size).background(Brush.linearGradient(listOf(Color(0xFF194B86), Color(0xFF0C1B3B))), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        Text("♫", color = blue, fontSize = (size.value * 0.52f).sp)
    }
}

@Composable
private fun SymbolButton(heart: Boolean, active: Boolean, onClick: () -> Unit, height: Dp) {
    Box(Modifier.fillMaxWidth().height(height).shadow(if (active) 8.dp else 3.dp, RoundedCornerShape(13.dp), ambientColor = if (heart) Color.Red else blue, spotColor = if (heart) Color.Red else blue).clip(RoundedCornerShape(13.dp)).background(Brush.verticalGradient(listOf(Color(0xFF18365D), Color(0xFF06152C)))).border(1.3.dp, if (active) (if (heart) Color(0xFFFF4057) else blue) else Color(0xFF5AABFF), RoundedCornerShape(13.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(34.dp)) {
            val w = size.width
            val h = size.height
            val path = Path()
            if (heart) {
                path.moveTo(w * .5f, h * .88f)
                path.cubicTo(w * .12f, h * .60f, -w * .04f, h * .35f, w * .18f, h * .18f)
                path.cubicTo(w * .34f, h * .04f, w * .47f, h * .16f, w * .5f, h * .25f)
                path.cubicTo(w * .62f, h * .04f, w * .84f, h * .06f, w * .91f, h * .24f)
                path.cubicTo(w * 1.04f, h * .48f, w * .77f, h * .71f, w * .5f, h * .88f)
                path.close()
            } else {
                path.moveTo(w * .23f, h * .12f)
                path.lineTo(w * .73f, h * .12f)
                path.cubicTo(w * .86f, h * .12f, w * .91f, h * .22f, w * .86f, h * .35f)
                path.lineTo(w * .70f, h * .63f)
                path.lineTo(w * .57f, h * .63f)
                path.lineTo(w * .57f, h * .87f)
                path.cubicTo(w * .57f, h * .98f, w * .41f, h * .97f, w * .36f, h * .87f)
                path.lineTo(w * .18f, h * .60f)
                path.lineTo(w * .12f, h * .60f)
                path.lineTo(w * .12f, h * .15f)
                path.close()
            }
            val color = if (active) (if (heart) Color(0xFFFF293D) else Color(0xFF159EFF)) else Color.White
            drawPath(path, color = color.copy(alpha = if (active) 1f else .9f), style = if (active) androidx.compose.ui.graphics.drawscope.Fill else Stroke(width = 2.5.dp.toPx()))
        }
    }
}

@Composable
private fun Control(label: String, onClick: () -> Unit, full: Boolean = false, accent: Boolean = false, small: Boolean = false, height: Dp = 58.dp, fontSize: Int = 15) {
    Box(Modifier.then(if (full) Modifier.fillMaxWidth() else Modifier).height(height).shadow(if (accent) 8.dp else 2.dp, RoundedCornerShape(12.dp), ambientColor = blue, spotColor = blue).clip(RoundedCornerShape(12.dp)).background(if (accent) Brush.verticalGradient(listOf(Color(0xFF1479D7), Color(0xFF06316D))) else Brush.verticalGradient(listOf(Color(0xFF173B6B), Color(0xFF06172F)))).border(if (small) 1.dp else 1.4.dp, if (accent) Color(0xFF4AE3FF) else Color(0xFF67AFFF), RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(horizontal = if (small) 9.dp else 12.dp), contentAlignment = Alignment.Center) {
        Text(label, color = Color.White, fontSize = if (small) 12.sp else fontSize.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
