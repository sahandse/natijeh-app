package com.natijeh.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.natijeh.BuildConfig
import com.natijeh.data.model.MatchEntity
import com.natijeh.data.model.PlayerEntity
import com.natijeh.data.model.TeamEntity
import com.natijeh.data.model.TeamResultMatch
import com.natijeh.ui.viewmodel.SportsViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import java.io.FileOutputStream

private val utilityMoshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
private val utilityRecentAdapter = utilityMoshi.adapter<List<TeamResultMatch>>(Types.newParameterizedType(List::class.java, TeamResultMatch::class.java))

@Composable
fun ComparisonCenterContent(viewModel: SportsViewModel, onTeam: (String) -> Unit, onPlayer: (String) -> Unit) {
    val teams by viewModel.allTeams.collectAsStateWithLifecycle()
    val players by viewModel.allPlayers.collectAsStateWithLifecycle()
    var mode by rememberSaveable { mutableStateOf("team") }
    var firstId by rememberSaveable { mutableStateOf("") }
    var secondId by rememberSaveable { mutableStateOf("") }
    var query by rememberSaveable { mutableStateOf("") }
    val source = if (mode == "team") teams.map { it.id to it.name } else players.map { it.id to it.name }
    val filtered = source.filter { query.isBlank() || it.second.contains(query, true) }.take(12)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { UtilityHero("مقایسه حرفه‌ای", "دو ${if (mode == "team") "تیم" else "بازیکن"} را روبه‌روی هم قرار دهید") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(mode == "team", { mode = "team"; firstId = ""; secondId = "" }, { Text("تیم‌ها") })
                FilterChip(mode == "player", { mode = "player"; firstId = ""; secondId = "" }, { Text("بازیکنان") })
            }
        }
        item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) }, placeholder = { Text("جستجوی نام…") }, singleLine = true, shape = RoundedCornerShape(18.dp)) }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.first }) { item ->
                    FilterChip(
                        selected = item.first == firstId || item.first == secondId,
                        onClick = { if (firstId.isBlank() || firstId == item.first) firstId = item.first else secondId = item.first },
                        label = { Text(item.second, maxLines = 1) }
                    )
                }
            }
        }
        if (mode == "team") {
            val first = teams.firstOrNull { it.id == firstId }
            val second = teams.firstOrNull { it.id == secondId }
            if (first != null && second != null) item { TeamComparisonCard(first, second, onTeam) }
        } else {
            val first = players.firstOrNull { it.id == firstId }
            val second = players.firstOrNull { it.id == secondId }
            if (first != null && second != null) item { PlayerComparisonCard(first, second, onPlayer) }
        }
    }
}

@Composable
private fun TeamComparisonCard(a: TeamEntity, b: TeamEntity, onTeam: (String) -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CompareHeaders(a.name, a.logo, b.name, b.logo, { onTeam(a.id) }, { onTeam(b.id) })
            CompareMetric("امتیاز", a.points, b.points)
            CompareMetric("برد", a.won, b.won)
            CompareMetric("گل زده", a.goalsFor, b.goalsFor)
            CompareMetric("تفاضل گل", a.goalsFor - a.goalsAgainst, b.goalsFor - b.goalsAgainst)
            CompareMetric("بازی", a.played, b.played)
        }
    }
}

@Composable
private fun PlayerComparisonCard(a: PlayerEntity, b: PlayerEntity, onPlayer: (String) -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CompareHeaders(a.name, a.portrait, b.name, b.portrait, { onPlayer(a.id) }, { onPlayer(b.id) })
            CompareMetric("گل", a.goals, b.goals)
            CompareMetric("سن", a.age, b.age, higherIsBetter = false)
            CompareMetric("شماره پیراهن", a.shirtNumber, b.shirtNumber, higherIsBetter = false)
            Text("${a.teamName}  |  ${b.teamName}", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CompareHeaders(a: String, aImage: String, b: String, bImage: String, onA: () -> Unit, onB: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        CompareIdentity(a, aImage, onA)
        Text("VS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        CompareIdentity(b, bImage, onB)
    }
}

@Composable
private fun CompareIdentity(name: String, image: String, onClick: () -> Unit) {
    Column(Modifier.width(112.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        AsyncImage(image, name, contentScale = ContentScale.Crop, modifier = Modifier.size(62.dp).clip(CircleShape))
        Text(name, maxLines = 2, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CompareMetric(label: String, a: Int, b: Int, higherIsBetter: Boolean = true) {
    val max = maxOf(a, b, 1)
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(a.toString(), fontWeight = if ((a >= b) == higherIsBetter) FontWeight.Black else FontWeight.Normal)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(b.toString(), fontWeight = if ((b >= a) == higherIsBetter) FontWeight.Black else FontWeight.Normal)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            LinearProgressIndicator(progress = { a.toFloat() / max }, modifier = Modifier.weight(1f).height(5.dp), color = MaterialTheme.colorScheme.primary)
            LinearProgressIndicator(progress = { b.toFloat() / max }, modifier = Modifier.weight(1f).height(5.dp), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun FormAndSeasonContent(viewModel: SportsViewModel, onTeam: (String) -> Unit) {
    val teams by viewModel.allTeams.collectAsStateWithLifecycle()
    val sorted = teams.sortedWith(compareByDescending<TeamEntity> { it.points }.thenByDescending { it.goalsFor - it.goalsAgainst })
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { UtilityHero("فرم و آمار فصل", "عملکرد، قدرت حمله و دفاع تیم‌ها") }
        items(sorted, key = { "form-${it.id}" }) { team ->
            val recent = remember(team.recentJson) { runCatching { utilityRecentAdapter.fromJson(team.recentJson).orEmpty() }.getOrDefault(emptyList()).take(5) }
            Card(Modifier.fillMaxWidth().clickable { onTeam(team.id) }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AsyncImage(team.logo, team.name, modifier = Modifier.size(38.dp))
                        Column(Modifier.weight(1f)) { Text(team.name, fontWeight = FontWeight.Black); Text(team.leagueName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }
                        Text("${team.points} امتیاز", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MiniStat("برد", team.won.toString()); MiniStat("مساوی", team.drawn.toString()); MiniStat("باخت", team.lost.toString()); MiniStat("گل", team.goalsFor.toString())
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        if (recent.isEmpty()) Text("فرم اخیر هنوز دریافت نشده", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        recent.forEach { game ->
                            val home = game.homeTeamId == team.id || game.homeTeam == team.name
                            val own = if (home) game.homeScore else game.awayScore
                            val rival = if (home) game.awayScore else game.homeScore
                            when {
                                own == null || rival == null -> FormDot("-", MaterialTheme.colorScheme.onSurfaceVariant)
                                own > rival -> FormDot("W", MaterialTheme.colorScheme.primary)
                                own == rival -> FormDot("D", MaterialTheme.colorScheme.onSurfaceVariant)
                                else -> FormDot("L", Color(0xFFE24B4B))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun MiniStat(label: String, value: String) = Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, fontWeight = FontWeight.Black); Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
@Composable private fun FormDot(label: String, color: Color) = Surface(shape = CircleShape, color = color.copy(alpha = .14f)) { Text(label, color = color, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)) }

@Composable
fun ReminderManagerContent(viewModel: SportsViewModel, onSettings: () -> Unit, onMatch: (String) -> Unit) {
    val matches by viewModel.favoriteMatches.collectAsStateWithLifecycle()
    val history by viewModel.notificationHistory.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { UtilityHero("مدیریت یادآورها", "اعلان بازی‌های دنبال‌شده و رخدادهای مهم") }
        item { SummaryStrip(listOf("بازی فعال" to matches.size.toString(), "اعلان ثبت‌شده" to history.size.toString())) }
        item { Button(onSettings, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Notifications, null); Spacer(Modifier.width(8.dp)); Text("تنظیم نوع اعلان‌ها") } }
        if (matches.isEmpty()) item { EmptyState("برای ساخت یادآور، یک مسابقه را محبوب کنید.") }
        items(matches, key = { "reminder-${it.id}" }) { match ->
            Card(Modifier.fillMaxWidth().clickable { onMatch(match.id) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text("${match.homeTeamName} - ${match.awayTeamName}", fontWeight = FontWeight.Bold); Text("${match.date} · ${match.time}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
                    Switch(true, { viewModel.toggleMatchFavorite(match.id, true) })
                }
            }
        }
    }
}

@Composable
fun OfflineCenterContent(viewModel: SportsViewModel) {
    val state by viewModel.maintenance.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { UtilityHero("مرکز آفلاین", "داده‌ها و تصاویر را برای استفاده بدون اینترنت مدیریت کنید") }
        item { SummaryStrip(listOf("حجم ذخیره" to formatUtilityBytes(state.offlineBytes), "پیشرفت" to "${state.downloadProgress ?: 0}٪")) }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(state.downloadMessage ?: "آماده دانلود", fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(progress = { (state.downloadProgress ?: 0) / 100f }, modifier = Modifier.fillMaxWidth())
                    Button({ if (state.downloading) viewModel.cancelOfflineDownload() else viewModel.downloadOfflineData() }, Modifier.fillMaxWidth()) { Text(if (state.downloading) "توقف دانلود" else "دانلود یا بروزرسانی محتوا") }
                    TextButton({ viewModel.clearOfflineImages() }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Delete, null); Text("پاک‌کردن تصاویر آفلاین") }
                }
            }
        }
    }
}

@Composable
fun RecentlyViewedContent(onOpen: (RecentEntry) -> Unit) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf(loadRecentEntries(context)) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { UtilityHero("اخیراً دیده‌شده", "ادامه مسیر از آخرین صفحات بازشده") }
        if (entries.isEmpty()) item { EmptyState("هنوز صفحه‌ای در تاریخچه ثبت نشده است.") }
        items(entries, key = { "recent-${it.type}-${it.id}" }) { entry ->
            Card(Modifier.fillMaxWidth().clickable { onOpen(entry) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .12f), modifier = Modifier.size(42.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.SportsSoccer, null) } }
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(entry.name, fontWeight = FontWeight.Bold); Text(recentTypeLabel(entry.type), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }
                    Text(SimpleDateFormat("MM/dd HH:mm", Locale("fa")).format(Date(entry.time)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (entries.isNotEmpty()) item { TextButton({ clearRecentEntries(context); entries = emptyList() }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Delete, null); Text("پاک‌کردن تاریخچه") } }
    }
}

@Composable
fun StadiumCoachContent(viewModel: SportsViewModel, onTeam: (String) -> Unit) {
    val teams by viewModel.allTeams.collectAsStateWithLifecycle()
    var mode by rememberSaveable { mutableStateOf("stadium") }
    val shown = teams.filter { if (mode == "stadium") it.stadium.isNotBlank() else it.coach.isNotBlank() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { UtilityHero("ورزشگاه و مربی", "اطلاعات تکمیلی باشگاه‌ها") }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(mode == "stadium", { mode = "stadium" }, { Text("ورزشگاه‌ها") }); FilterChip(mode == "coach", { mode = "coach" }, { Text("مربیان") }) } }
        items(shown, key = { "venue-${mode}-${it.id}" }) { team ->
            Card(Modifier.fillMaxWidth().clickable { onTeam(team.id) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                    AsyncImage(team.logo, team.name, modifier = Modifier.size(48.dp))
                    Column(Modifier.weight(1f)) { Text(if (mode == "stadium") team.stadium else team.coach, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium); Text(team.name, color = MaterialTheme.colorScheme.primary); Text(listOf(team.leagueName, if (mode == "stadium") "تأسیس ${team.founded}" else team.formation).filter { it.isNotBlank() }.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}

@Composable
fun ShareMatchCardContent(viewModel: SportsViewModel, onMatch: (String) -> Unit) {
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selected by rememberSaveable { mutableStateOf("") }
    val match = matches.firstOrNull { it.id == selected } ?: matches.firstOrNull()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { UtilityHero("کارت اشتراک مسابقه", "یک کارت مینیمال برای نتیجه یا بازی بعدی بسازید") }
        item { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(matches.take(15), key = { "share-${it.id}" }) { item -> FilterChip(selected == item.id || (selected.isBlank() && item == match), { selected = item.id }, { Text("${item.homeTeamName} - ${item.awayTeamName}") }) } } }
        match?.let { game ->
            item { SharePreview(game, { onMatch(game.id) }) }
            item { Button({ shareMatchText(context, game) }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Share, null); Spacer(Modifier.width(8.dp)); Text("اشتراک‌گذاری کارت") } }
        }
    }
}

@Composable
private fun SharePreview(match: MatchEntity, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF090909))) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("نتیجه", color = Color.White.copy(alpha = .7f), fontWeight = FontWeight.Black)
            Text(match.leagueName, color = Color.White.copy(alpha = .6f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                ShareTeam(match.homeTeamName, match.homeTeamLogo)
                Text(if (match.status == "SCHEDULED") match.time else "${match.homeScore}  :  ${match.awayScore}", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                ShareTeam(match.awayTeamName, match.awayTeamLogo)
            }
            Text("${match.date} · ${match.statusTitle}", color = Color.White.copy(alpha = .65f))
        }
    }
}

@Composable private fun ShareTeam(name: String, logo: String) = Column(Modifier.width(100.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) { AsyncImage(logo, name, modifier = Modifier.size(56.dp)); Text(name, color = Color.White, maxLines = 2, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) }

@Composable
fun DataHealthContent(viewModel: SportsViewModel) {
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    val teams by viewModel.allTeams.collectAsStateWithLifecycle()
    val players by viewModel.allPlayers.collectAsStateWithLifecycle()
    val leagues by viewModel.allLeagues.collectAsStateWithLifecycle()
    val refreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val error by viewModel.errorMessage.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { UtilityHero("وضعیت داده‌ها", "سلامت اتصال و محتوای ذخیره‌شده") }
        item { SummaryStrip(listOf("مسابقه" to matches.size.toString(), "تیم" to teams.size.toString(), "بازیکن" to players.size.toString(), "لیگ" to leagues.size.toString())) }
        item { StatusCard("اتصال داده", if (error == null) "سالم" else "نیاز به بررسی", error ?: "آخرین دریافت بدون خطا انجام شده است", error == null) }
        item { StatusCard("بروزرسانی", if (refreshing) "در حال انجام" else "آماده", "اطلاعات زنده و کش محلی هماهنگ می‌شوند", !refreshing) }
        item { Button({ viewModel.refresh(true) }, Modifier.fillMaxWidth(), enabled = !refreshing) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(8.dp)); Text("بروزرسانی همه داده‌ها") } }
    }
}

@Composable
fun CollectionsContent(viewModel: SportsViewModel, onTeam: (String) -> Unit, onPlayer: (String) -> Unit) {
    val context = LocalContext.current
    val teams by viewModel.favoriteTeams.collectAsStateWithLifecycle()
    val players by viewModel.favoritePlayers.collectAsStateWithLifecycle()
    val prefs = remember { context.getSharedPreferences("natijeh_collections", 0) }
    var names by remember { mutableStateOf(prefs.getStringSet("names", setOf("منتخب من")).orEmpty().toSet()) }
    var newName by rememberSaveable { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { UtilityHero("مجموعه‌های من", "تیم‌ها و بازیکنان محبوب را در پوشه‌های شخصی نگه دارید") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(newName, { newName = it }, Modifier.weight(1f), placeholder = { Text("نام مجموعه") }, singleLine = true, shape = RoundedCornerShape(16.dp))
                IconButton({ if (newName.isNotBlank()) { names = names + newName.trim(); prefs.edit().putStringSet("names", names).apply(); newName = "" } }) { Icon(Icons.Default.Add, "افزودن") }
            }
        }
        items(names.toList(), key = { it }) { name ->
            var selectedKeys by remember(name) { mutableStateOf(prefs.getStringSet("items_$name", emptySet()).orEmpty().toSet()) }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary); Text(name, Modifier.weight(1f).padding(horizontal = 10.dp), fontWeight = FontWeight.Black); Text("${selectedKeys.size} مورد", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Text("برای افزودن یا حذف، روی نام بزنید.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(teams, key = { "collection-team-${it.id}" }) { team ->
                            val key = "team:${team.id}"
                            FilterChip(key in selectedKeys, { selectedKeys = if (key in selectedKeys) selectedKeys - key else selectedKeys + key; prefs.edit().putStringSet("items_$name", selectedKeys).apply() }, { Text(team.name) })
                        }
                        items(players, key = { "collection-player-${it.id}" }) { player ->
                            val key = "player:${player.id}"
                            FilterChip(key in selectedKeys, { selectedKeys = if (key in selectedKeys) selectedKeys - key else selectedKeys + key; prefs.edit().putStringSet("items_$name", selectedKeys).apply() }, { Text(player.name) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReleaseChangesContent(viewModel: SportsViewModel) {
    val state by viewModel.maintenance.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { UtilityHero("تغییرات نسخه", "نسخه نصب‌شده ${BuildConfig.VERSION_NAME}") }
        item { StatusCard("نسخه فعلی", BuildConfig.VERSION_NAME, "طراحی جدید، مرکز مقایسه، آفلاین و ابزارهای شخصی", true) }
        state.releaseNotes?.takeIf { it.isNotBlank() }?.let { notes -> item { StatusCard("نسخه ${state.updateVersion.orEmpty()}", "نسخه جدید", notes, true) } }
        item { Button({ viewModel.checkForUpdate() }, Modifier.fillMaxWidth(), enabled = !state.checkingUpdate) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(8.dp)); Text(state.updateMessage ?: "بررسی نسخه جدید") } }
    }
}

@Composable private fun UtilityHero(title: String, subtitle: String) = Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable private fun SummaryStrip(items: List<Pair<String, String>>) = Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) { items.forEach { (label, value) -> MiniStat(label, value) } } }
@Composable private fun StatusCard(title: String, value: String, subtitle: String, healthy: Boolean) = Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Surface(shape = CircleShape, color = (if (healthy) MaterialTheme.colorScheme.primary else Color(0xFFE24B4B)).copy(alpha = .14f), modifier = Modifier.size(42.dp)) { Box(contentAlignment = Alignment.Center) { Icon(if (healthy) Icons.Default.Check else Icons.Default.Refresh, null, tint = if (healthy) MaterialTheme.colorScheme.primary else Color(0xFFE24B4B)) } }; Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Black); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }; Text(value, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } }

data class RecentEntry(val type: String, val id: String, val name: String, val time: Long)

fun recordRecentEntry(context: Context, type: String, id: String, name: String) {
    if (id.isBlank() || name.isBlank()) return
    val prefs = context.getSharedPreferences("natijeh_recent", 0)
    val current = prefs.getStringSet("entries", emptySet()).orEmpty().mapNotNull(::decodeRecent).filterNot { it.type == type && it.id == id }.toMutableList()
    current.add(0, RecentEntry(type, id, name, System.currentTimeMillis()))
    prefs.edit().putStringSet("entries", current.take(30).map(::encodeRecent).toSet()).apply()
}

private fun loadRecentEntries(context: Context): List<RecentEntry> = context.getSharedPreferences("natijeh_recent", 0).getStringSet("entries", emptySet()).orEmpty().mapNotNull(::decodeRecent).sortedByDescending { it.time }
private fun clearRecentEntries(context: Context) { context.getSharedPreferences("natijeh_recent", 0).edit().remove("entries").apply() }
private fun encodeRecent(item: RecentEntry) = listOf(item.type, item.id, Uri.encode(item.name), item.time.toString()).joinToString("|")
private fun decodeRecent(raw: String): RecentEntry? { val p = raw.split('|'); return if (p.size == 4) RecentEntry(p[0], p[1], Uri.decode(p[2]), p[3].toLongOrNull() ?: 0) else null }
private fun recentTypeLabel(type: String) = when (type) { "team" -> "باشگاه"; "player" -> "بازیکن"; "league" -> "لیگ"; else -> "مسابقه" }
private fun formatUtilityBytes(bytes: Long) = if (bytes < 1024 * 1024) "${bytes / 1024} KB" else String.format(Locale.US, "%.1f MB", bytes / 1048576.0)
private fun shareMatchText(context: Context, match: MatchEntity) {
    runCatching {
        val bitmap = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(AndroidColor.rgb(8, 8, 8))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD) }
        paint.color = AndroidColor.rgb(35, 185, 85); paint.textSize = 54f
        canvas.drawText("NATIJEH", 540f, 130f, paint)
        paint.color = AndroidColor.LTGRAY; paint.textSize = 38f
        canvas.drawText(match.leagueName.take(34), 540f, 220f, paint)
        paint.color = AndroidColor.WHITE; paint.textSize = 48f
        canvas.drawText(match.homeTeamName.take(22), 270f, 500f, paint)
        canvas.drawText(match.awayTeamName.take(22), 810f, 500f, paint)
        paint.color = AndroidColor.rgb(35, 185, 85); paint.textSize = 96f
        val score = if (match.status == "SCHEDULED") match.time else "${match.homeScore}  :  ${match.awayScore}"
        canvas.drawText(score, 540f, 520f, paint)
        paint.color = AndroidColor.GRAY; paint.textSize = 36f
        canvas.drawText("${match.date}  •  ${match.statusTitle}", 540f, 650f, paint)
        paint.color = AndroidColor.DKGRAY; paint.strokeWidth = 3f
        canvas.drawLine(110f, 750f, 970f, 750f, paint)
        paint.color = AndroidColor.LTGRAY; paint.textSize = 30f
        canvas.drawText("نتیجه زنده فوتبال", 540f, 850f, paint)
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "match-${match.id.hashCode()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "اشتراک‌گذاری مسابقه"))
    }
}
