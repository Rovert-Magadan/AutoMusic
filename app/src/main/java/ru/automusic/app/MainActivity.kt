package ru.automusic.app

import android.os.Bundle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Intent
import android.provider.Settings
import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.media.session.MediaController
import android.os.SystemClock
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalDensity
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
    val context = LocalContext.current
    var report by remember { mutableStateOf<String?>(null) }
    report?.let { snapshot ->
        AlertDialog(onDismissRequest = { report = null },
            title = { Text("AutoMusic — диагностика") },
            text = { Column(Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) { Text(snapshot, fontSize = 12.sp) } },
            confirmButton = { TextButton(onClick = { val result = runCatching { saveDiagnostic(context, snapshot) }; android.widget.Toast.makeText(context, result.getOrElse { "Ошибка: " + it.javaClass.simpleName }, android.widget.Toast.LENGTH_LONG).show() }) { Text("Сохранить TXT") } },
            dismissButton = { Row {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("AutoMusic diagnostic", snapshot))
                }) { Text("Копировать") }
                TextButton(onClick = { report = null }) { Text("Закрыть") }
            } }
        )
    }
    var controller by remember { mutableStateOf<MediaController?>(null) }
    var metadata by remember { mutableStateOf<MediaMetadata?>(null) }
    var state by remember { mutableStateOf<PlaybackState?>(null) }
    var queue by remember { mutableStateOf<List<android.media.session.MediaSession.QueueItem>>(emptyList()) }
    var showList by remember { mutableStateOf(false) }
    var seconds by remember { mutableIntStateOf(0) }
    val duration = ((metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L) / 1000).toInt().coerceAtLeast(1)
    val playing = state?.state == PlaybackState.STATE_PLAYING
    val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Запусти Яндекс Музыку"
    val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Нажми AutoMusic для разрешения доступа"
    val artwork = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
    val index = queue.indexOfFirst { it.queueId == state?.activeQueueItemId }
    val tracks = queue.map { it.description.title?.toString() ?: "Трек" }
    val actions = state?.customActions.orEmpty()
    val source = "Яндекс Музыка"
    val shuffle = false
    val liked = false
    val disliked = false
    fun custom(vararg names: String) {
        val action = actions.firstOrNull { a -> names.any { n -> a.name.toString().equals(n, true) } }
        if (action != null) controller?.transportControls?.sendCustomAction(action, action.extras)
        else android.widget.Toast.makeText(context, "Команда не предоставлена Яндекс Музыкой", android.widget.Toast.LENGTH_SHORT).show()
    }
    LaunchedEffect(Unit) {
        while (true) {
            val c = yandexController(context)
            controller = c
            metadata = c?.metadata
            state = c?.playbackState
            queue = c?.queue.orEmpty()
            val p = state
            val elapsed = if (p?.state == PlaybackState.STATE_PLAYING && p.lastPositionUpdateTime > 0) ((SystemClock.elapsedRealtime() - p.lastPositionUpdateTime) * p.playbackSpeed).toLong() else 0L
            seconds = (((p?.position ?: 0L) + elapsed).coerceAtLeast(0L) / 1000).toInt()
            delay(500)
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(navy)) {
        val narrow = maxWidth < 720.dp
        val short = maxHeight < 430.dp
        Image(painter = painterResource(R.drawable.automusic_wave), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
        val gap = if (short) 6.dp else 12.dp
        Column(Modifier.fillMaxSize().padding(horizontal = if (narrow) 10.dp else 20.dp, vertical = if (short) 8.dp else 18.dp), verticalArrangement = Arrangement.spacedBy(gap)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("♫ AutoMusic", modifier = Modifier.pointerInput(Unit) { detectTapGestures(onTap = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }, onLongPress = { report = runCatching { diagnosticReport(context) }.getOrElse { "Diagnostic error: " + it.javaClass.simpleName } }) }, color = yellow, fontWeight = FontWeight.Bold, fontSize = if (narrow) 18.sp else 25.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Spacer(Modifier.width(if (narrow) 48.dp else 64.dp))
                    ShuffleButton(shuffle, { custom("Перемешать", "Shuffle") })
                    Control("Моя волна", { custom("Моя волна", "My Wave") }, height = 42.dp, small = true)
                    Control(if (showList) "Плеер" else "Список", { showList = !showList }, height = 42.dp, small = true)
                }
            }
            Text(source.uppercase() + if (showList) "  •  СПИСОК" else "  •  СЕЙЧАС ИГРАЕТ", color = pale, fontSize = 12.sp)
            if (showList) {
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    if (tracks.isEmpty()) item { Text("Очередь не предоставлена Яндекс Музыкой", color = pale) }
                    itemsIndexed(tracks) { i, title ->
                        Row(Modifier.fillMaxWidth().background(if (i == index) Color(0xDD244B7B) else panel, RoundedCornerShape(9.dp)).clickable { controller?.transportControls?.skipToQueueItem(queue[i].queueId); showList = false }.padding(horizontal = 12.dp, vertical = if (short) 8.dp else 13.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${i + 1}", color = pale, modifier = Modifier.width(32.dp), fontSize = 13.sp)
                            Column(Modifier.weight(1f)) {
                                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, fontSize = 15.sp)
                                Text(queue[i].description.subtitle?.toString().orEmpty(), color = pale, fontSize = 12.sp, maxLines = 1)
                            }
                            Text(if (i == index && playing) "♫" else "▶", color = blue, fontSize = 19.sp)
                        }
                    }
                }
            } else {
                if (narrow) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Cover(if (short) 58.dp else 90.dp, artwork)
                        Column(Modifier.weight(1f)) {
                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(artist, color = pale, fontSize = 13.sp, maxLines = 1)
                        }
                    }
                } else {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
                        Cover(if (short) 100.dp else 210.dp, artwork)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 30.sp)
                            Text(artist, color = pale, fontSize = 19.sp)
                        }
                    }
                }
                BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                    val transportGap = 9.dp
                    val buttonWidth = (maxWidth - transportGap * 2) / 3
                    val leftCenter = buttonWidth + transportGap / 2
                    val rightCenter = maxWidth - leftCenter
                    // The seek line is at 13.5 dp. Transport starts after the
                    // column gap and half of the unused height in its row.
                    val transportTop = maxHeight + gap + (if (short) 5.5.dp else 12.dp)
                    val reactionCenterY = (13.5.dp + transportTop) / 2
                    val reactionHeight = minOf(if (short) 58.dp else 72.dp, (transportTop - 13.5.dp - 8.dp).coerceAtLeast(1.dp))
                    val reactionWidth = minOf(if (short) 112.dp else 150.dp, buttonWidth)
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        ProgressSeek(seconds, duration, { controller?.transportControls?.seekTo(it * 1000L) })
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(time(seconds), color = pale, fontSize = 11.sp)
                            Text(time(duration), color = pale, fontSize = 11.sp)
                        }
                    }
                    Box(Modifier.offset(x = leftCenter - reactionWidth / 2, y = reactionCenterY - reactionHeight / 2).width(reactionWidth)) {
                        SymbolButton(true, liked, { custom("Лайк", "Like", "Нравится") }, reactionHeight)
                    }
                    Box(Modifier.offset(x = rightCenter - reactionWidth / 2, y = reactionCenterY - reactionHeight / 2).width(reactionWidth)) {
                        SymbolButton(false, disliked, { custom("Дизлайк", "Dislike", "Не нравится") }, reactionHeight)
                    }
                }
                Row(Modifier.fillMaxWidth().height(if (short) 75.dp else 100.dp), horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { Control("⏮", { controller?.transportControls?.skipToPrevious() }, full = true, height = if (short) 64.dp else 76.dp, fontSize = 30) }
                    Box(Modifier.weight(1f)) { Control(if (playing) "Ⅱ" else "▶", { if (playing) controller?.transportControls?.pause() else controller?.transportControls?.play() }, full = true, accent = true, height = if (short) 64.dp else 76.dp, fontSize = 30) }
                    Box(Modifier.weight(1f)) { Control("⏭", { controller?.transportControls?.skipToNext() }, full = true, height = if (short) 64.dp else 76.dp, fontSize = 30) }
                }
            }
            if (!short) Text("v0.9.1 • Diagnostic", color = pale, fontSize = 10.sp)
        }
    }
}

private fun time(seconds: Int) = "%d:%02d".format(seconds / 60, seconds % 60)

@Composable
private fun ProgressSeek(seconds: Int, duration: Int, onSeek: (Int) -> Unit) {
    var widthPx by remember { mutableFloatStateOf(1f) }
    val fraction = (seconds.toFloat() / duration).coerceIn(0f, 1f)
    Canvas(
        Modifier.fillMaxWidth().height(27.dp)
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(duration) { detectTapGestures { onSeek(((it.x / widthPx).coerceIn(0f, 1f) * duration).toInt()) } }
            .pointerInput(duration) { detectDragGestures(onDragStart = { onSeek(((it.x / widthPx).coerceIn(0f, 1f) * duration).toInt()) }) { change, _ ->
                onSeek(((change.position.x / widthPx).coerceIn(0f, 1f) * duration).toInt())
                change.consume()
            } }
    ) {
        val y = size.height / 2f
        val inset = 10.dp.toPx()
        val start = inset
        val end = size.width - inset
        val x = start + (end - start) * fraction
        drawLine(Color(0xFF273F62), Offset(start, y), Offset(end, y), 5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(yellow, Offset(start, y), Offset(x, y), 5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(yellow.copy(alpha = .13f), 15.dp.toPx(), Offset(x, y))
        drawCircle(yellow.copy(alpha = .24f), 10.dp.toPx(), Offset(x, y))
        drawCircle(yellow, 7.dp.toPx(), Offset(x, y))
    }
}

@Composable
private fun ShuffleButton(active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(width = 70.dp, height = 42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF17365C), Color(0xFF06152D))))
            .border(1.dp, if (active) Color(0xFF6DE6FF) else Color(0xFF5DAAFF), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(32.dp)) {
            val c = if (active) Color(0xFF4BD9FF) else Color.White
            val sw = 2.7.dp.toPx()
            val w = size.width
            val h = size.height
            // Two crossing shuffle paths, each with a proper arrow head.
            drawLine(c, Offset(w * .10f, h * .25f), Offset(w * .28f, h * .25f), sw, cap = StrokeCap.Round)
            drawLine(c, Offset(w * .28f, h * .25f), Offset(w * .72f, h * .76f), sw, cap = StrokeCap.Round)
            drawLine(c, Offset(w * .72f, h * .76f), Offset(w * .90f, h * .76f), sw, cap = StrokeCap.Round)
            drawLine(c, Offset(w * .10f, h * .76f), Offset(w * .28f, h * .76f), sw, cap = StrokeCap.Round)
            drawLine(c, Offset(w * .28f, h * .76f), Offset(w * .72f, h * .25f), sw, cap = StrokeCap.Round)
            drawLine(c, Offset(w * .72f, h * .25f), Offset(w * .90f, h * .25f), sw, cap = StrokeCap.Round)
            for (y in listOf(.25f, .76f)) {
                drawLine(c, Offset(w * .90f, h * y), Offset(w * .76f, h * (y - .13f)), sw, cap = StrokeCap.Round)
                drawLine(c, Offset(w * .90f, h * y), Offset(w * .76f, h * (y + .13f)), sw, cap = StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun Cover(size: Dp, artwork: Bitmap? = null) {
    if (artwork != null) {
        Image(artwork.asImageBitmap(), contentDescription = null, modifier = Modifier.size(size).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
        return
    }
    Box(Modifier.size(size).background(Brush.linearGradient(listOf(Color(0xFF194B86), Color(0xFF0C1B3B))), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        Text("♫", color = blue, fontSize = (size.value * 0.52f).sp)
    }
}

@Composable
private fun SymbolButton(heart: Boolean, active: Boolean, onClick: () -> Unit, height: Dp) {
    Box(Modifier.fillMaxWidth().height(height).shadow(if (active) 11.dp else 5.dp, RoundedCornerShape(16.dp), ambientColor = if (heart) Color.Red else blue, spotColor = if (heart) Color.Red else blue).clip(RoundedCornerShape(16.dp)).background(Brush.verticalGradient(listOf(Color(0xFF173B68), Color(0xFF081C38), Color(0xFF030C20)))).border(1.5.dp, if (active) (if (heart) Color(0xFFFF5C6A) else Color(0xFF35B9FF)) else Color(0xFF65C9FF), RoundedCornerShape(16.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(44.dp)) {
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
    Box(Modifier.then(if (full) Modifier.fillMaxWidth() else Modifier).height(height).shadow(if (accent) 8.dp else 2.dp, RoundedCornerShape(12.dp), ambientColor = blue, spotColor = blue).clip(RoundedCornerShape(12.dp)).background(if (accent) Brush.verticalGradient(listOf(Color(0xFF187DE2), Color(0xFF064B9D), Color(0xFF05224D))) else Brush.verticalGradient(listOf(Color(0xFF173B6B), Color(0xFF081C38), Color(0xFF030C20)))).border(if (small) 1.dp else 1.4.dp, if (accent) Color(0xFF4AE3FF) else Color(0xFF67AFFF), RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(horizontal = if (small) 9.dp else 12.dp), contentAlignment = Alignment.Center) {
        Text(label, color = Color.White, fontSize = if (small) 12.sp else fontSize.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
