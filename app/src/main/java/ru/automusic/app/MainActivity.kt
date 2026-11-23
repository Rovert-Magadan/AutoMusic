package ru.automusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val navy = Color(0xFF08182F)
private val panel = Color(0xFF163153)
private val blue = Color(0xFF448BEB)
private val pale = Color(0xFFA9C5E8)
private val yellow = Color(0xFFF6C945)

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
    var liked by remember { mutableStateOf(false) }
    var disliked by remember { mutableStateOf(false) }
    var index by remember { mutableIntStateOf(0) }
    val tracks = remember { (1..24).map { "Демонстрационный трек $it" } }
    BoxWithConstraints(Modifier.fillMaxSize().background(navy)) {
        val narrow = maxWidth < 720.dp
        val short = maxHeight < 430.dp
        val gap = if (short) 7.dp else 14.dp
        Column(Modifier.fillMaxSize().padding(if (narrow) 10.dp else 20.dp), verticalArrangement = Arrangement.spacedBy(gap)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("♫ AutoMusic", color = yellow, fontWeight = FontWeight.Bold, fontSize = if (narrow) 18.sp else 25.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Control(if (source == "Избранное") "Моя волна" else "Избранное", { source = if (source == "Избранное") "Моя волна" else "Избранное"; index = 0 }, height = 42.dp, small = true)
                    Control(if (showList) "Плеер" else "Список", { showList = !showList }, height = 42.dp, small = true)
                }
            }
            Text(source.uppercase() + if (showList) "  •  СПИСОК" else "  •  СЕЙЧАС ИГРАЕТ", color = pale, fontSize = 12.sp)
            if (showList) {
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    itemsIndexed(tracks) { i, title ->
                        Row(Modifier.fillMaxWidth().background(if (i == index) Color(0xFF244B7B) else panel, RoundedCornerShape(9.dp)).clickable { index = i; playing = true; showList = false }.padding(horizontal = 12.dp, vertical = if (short) 8.dp else 13.dp), verticalAlignment = Alignment.CenterVertically) {
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
                    Row(Modifier.fillMaxWidth().weight(1f, fill = false), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Cover(if (short) 62.dp else 90.dp)
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Box(Modifier.weight(1f)) { Control("⏮", { index = (index + tracks.size - 1) % tracks.size }, full = true, height = if (short) 51.dp else 68.dp, fontSize = 27) }
                    Box(Modifier.weight(1f)) { Control(if (playing) "Ⅱ" else "▶", { playing = !playing }, full = true, accent = true, height = if (short) 51.dp else 68.dp, fontSize = 27) }
                    Box(Modifier.weight(1f)) { Control("⏭", { index = (index + 1) % tracks.size }, full = true, height = if (short) 51.dp else 68.dp, fontSize = 27) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Box(Modifier.weight(1f)) { Control(if (liked) "♥ ЛАЙК" else "♡ ЛАЙК", { liked = !liked; if (liked) disliked = false }, full = true, height = if (short) 48.dp else 60.dp, accent = liked) }
                    Box(Modifier.weight(1f)) { Control(if (disliked) "⊘ ДИЗЛАЙК" else "⊘ ДИЗЛАЙК", { disliked = !disliked; if (disliked) liked = false }, full = true, height = if (short) 48.dp else 60.dp, accent = disliked) }
                }
            }
            if (!short) Text("ПРОТОТИП • без подключения к Яндекс Музыке", color = pale, fontSize = 10.sp)
        }
    }
}

@Composable
private fun Cover(size: androidx.compose.ui.unit.Dp) {
    Box(Modifier.size(size).background(panel, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        Text("♫", color = blue, fontSize = (size.value * 0.52f).sp)
    }
}

@Composable
private fun Control(label: String, onClick: () -> Unit, full: Boolean = false, accent: Boolean = false, small: Boolean = false, height: androidx.compose.ui.unit.Dp = 58.dp, fontSize: Int = 15) {
    Box(Modifier.then(if (full) Modifier.fillMaxWidth() else Modifier).height(height).background(if (accent) blue else panel, RoundedCornerShape(11.dp)).clickable(onClick = onClick).padding(horizontal = if (small) 9.dp else 12.dp), contentAlignment = Alignment.Center) {
        Text(label, color = Color.White, fontSize = if (small) 12.sp else fontSize.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
