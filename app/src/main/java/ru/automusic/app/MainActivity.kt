package ru.automusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val bg = Color(0xFF101820)
private val surface = Color(0xFF263440)
private val gold = Color(0xFFF2BD4A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AutoMusicScreen() } }
    }
}

@Composable
private fun AutoMusicScreen() {
    var compact by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var liked by remember { mutableStateOf(false) }
    var disliked by remember { mutableStateOf(false) }
    var index by remember { mutableIntStateOf(0) }
    var section by remember { mutableStateOf("Моя волна") }
    val tracks = listOf("Демонстрационный трек 1", "Демонстрационный трек 2", "Демонстрационный трек 3")
    BoxWithConstraints(Modifier.fillMaxSize().background(bg)) {
        val narrow = maxWidth < 700.dp
        Column(Modifier.fillMaxSize().padding(if (narrow) 12.dp else 22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("♫  AutoMusic", color = gold, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                ActionButton(if (compact) "РАЗВЕРНУТЬ" else "КОМПАКТНО", onClick = { compact = !compact })
            }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (!compact && !narrow) {
                    Box(Modifier.fillMaxHeight().weight(0.85f).background(surface, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Text("♫", fontSize = 100.sp, color = gold)
                    }
                }
                Column(Modifier.weight(1.15f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceEvenly) {
                    Text(section.uppercase(), color = gold, fontSize = 15.sp)
                    Text(tracks[index], color = Color.White, fontSize = if (narrow) 22.sp else 30.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Демонстрационный исполнитель", color = Color.LightGray, fontSize = 17.sp, maxLines = 1)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        ActionButton("⏮", { index = (index + tracks.size - 1) % tracks.size }, big = true)
                        ActionButton(if (playing) "Ⅱ" else "▶", { playing = !playing }, big = true, accent = true)
                        ActionButton("⏭", { index = (index + 1) % tracks.size }, big = true)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) { ActionButton(if (liked) "♥ НРАВИТСЯ" else "♡ ЛАЙК", { liked = !liked; if (liked) disliked = false }, full = true) }
                        Box(Modifier.weight(1f)) { ActionButton(if (disliked) "✕ УБРАТЬ" else "⊘ ДИЗЛАЙК", { disliked = !disliked; if (disliked) liked = false }, full = true) }
                    }
                }
            }
            if (!compact) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("Избранное", "Моя волна", "Плейлисты").forEach { name ->
                        Box(Modifier.weight(1f)) { ActionButton(name, { section = name }, full = true, accent = section == name) }
                    }
                }
            }
            Text("ПРОТОТИП • без подключения к Яндекс Музыке", color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit, big: Boolean = false, accent: Boolean = false, full: Boolean = false) {
    Box(
        Modifier.then(if (full) Modifier.fillMaxWidth() else Modifier)
            .height(if (big) 76.dp else 58.dp)
            .background(if (accent) gold else surface, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (accent) Color.Black else Color.White, fontSize = if (big) 28.sp else 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
