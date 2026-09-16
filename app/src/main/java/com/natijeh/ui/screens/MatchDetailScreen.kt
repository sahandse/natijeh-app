package com.natijeh.ui.screens

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.natijeh.data.model.HeadToHeadData
import com.natijeh.data.model.MatchEntity
import com.natijeh.data.model.MatchEvent
import com.natijeh.data.model.MatchLineups
import com.natijeh.data.model.PlayerLineup
import com.natijeh.data.model.StatItem
import com.natijeh.data.util.JalaliDate
import com.natijeh.ui.theme.LiveRed
import com.natijeh.ui.theme.NatijehGreen
import com.natijeh.ui.theme.PulseDot
import com.natijeh.ui.viewmodel.SportsViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
private val eventAdapter = moshi.adapter<List<MatchEvent>>(
    Types.newParameterizedType(List::class.java, MatchEvent::class.java)
)
private val statsAdapter = moshi.adapter<List<StatItem>>(
    Types.newParameterizedType(List::class.java, StatItem::class.java)
)
private val lineupsAdapter = moshi.adapter(MatchLineups::class.java)
private val h2hAdapter = moshi.adapter(HeadToHeadData::class.java)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    matchId: String,
    sportsViewModel: SportsViewModel,
    keepScreenOnLive: Boolean = true,
    onBack: () -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit = {}
) {
    val match by sportsViewModel.getMatchFlow(matchId).collectAsStateWithLifecycle(initialValue = null)
    var selectedTab by remember { mutableStateOf("summary") }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val view = LocalView.current
    val context = LocalContext.current

    LaunchedEffect(matchId) {
        sportsViewModel.loadMatchDetails(matchId)
    }
    LaunchedEffect(matchId, match?.homeTeamName, match?.awayTeamName) {
        match?.let { recordRecentEntry(context, "match", matchId, "${it.homeTeamName} - ${it.awayTeamName}") }
    }
    LaunchedEffect(match?.status) {
        while (match?.status == "SCHEDULED") {
            nowMillis = System.currentTimeMillis()
            delay(30_000)
        }
    }
    DisposableEffect(match?.status, keepScreenOnLive) {
        view.keepScreenOn = keepScreenOnLive && match?.status == "LIVE"
        onDispose { view.keepScreenOn = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("جزئیات مسابقه", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    match?.let { current ->
                        IconButton(onClick = {
                            val score = if (current.status == "SCHEDULED") current.time else "${current.homeScore} - ${current.awayScore}"
                            val text = "${current.homeTeamName} $score ${current.awayTeamName}\n${current.leagueName}\nنتیجه"
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, text)
                                    },
                                    "اشتراک مسابقه"
                                )
                            )
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "اشتراک مسابقه")
                        }
                        IconButton(onClick = { sportsViewModel.toggleMatchFavorite(current.id, current.isFavorite) }) {
                            Icon(
                                imageVector = if (current.isFavorite) Icons.Outlined.Notifications else Icons.Outlined.NotificationsNone,
                                contentDescription = if (current.isFavorite) "اعلان مسابقه فعال است" else "فعال‌کردن اعلان مسابقه"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        match?.let { m ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                MatchHeaderCard(
                    match = m,
                    onHomeTeamClick = { onNavigateToTeam(m.homeTeamId) },
                    onAwayTeamClick = { onNavigateToTeam(m.awayTeamId) },
                    onLeagueClick = { onNavigateToLeague(m.leagueId) }
                )
                matchCountdown(m, nowMillis)?.let { countdown ->
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp)) {
                        Text("شروع مسابقه · $countdown", modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
                val topPerformer = remember(m.lineupsJson) {
                    runCatching { lineupsAdapter.fromJson(m.lineupsJson) }.getOrNull()?.let { lineups ->
                        (lineups.homeStarting + lineups.awayStarting).filter { it.rating > 0.0 }.maxByOrNull { it.rating }
                    }
                }
                topPerformer?.let { player ->
                    TopPerformerBanner(player, onNavigateToPlayer)
                }
                ScrollableTabRow(
                    selectedTabIndex = when (selectedTab) {
                        "summary" -> 0
                        "timeline" -> 1
                        "stats" -> 2
                        "lineups" -> 3
                        "h2h" -> 4
                        "pregame" -> 5
                        "momentum" -> 6
                        else -> 0
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp
                ) {
                    Tab(selected = selectedTab == "summary", onClick = { selectedTab = "summary" }) {
                        Text("خلاصه", modifier = Modifier.padding(16.dp), color = if (selectedTab == "summary") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                    }
                    Tab(selected = selectedTab == "timeline", onClick = { selectedTab = "timeline" }) {
                        Text("رویدادها", modifier = Modifier.padding(16.dp), color = if (selectedTab == "timeline") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                    }
                    Tab(selected = selectedTab == "stats", onClick = { selectedTab = "stats" }) {
                        Text("آمار بازی", modifier = Modifier.padding(16.dp), color = if (selectedTab == "stats") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                    }
                    Tab(selected = selectedTab == "lineups", onClick = { selectedTab = "lineups" }) {
                        Text("ترکیب‌ها", modifier = Modifier.padding(16.dp), color = if (selectedTab == "lineups") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                    }
                    Tab(selected = selectedTab == "h2h", onClick = { selectedTab = "h2h" }) {
                        Text("رویارویی‌ها", modifier = Modifier.padding(16.dp), color = if (selectedTab == "h2h") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                    }
                    Tab(selected = selectedTab == "pregame", onClick = { selectedTab = "pregame" }) {
                        Text("قبل بازی", modifier = Modifier.padding(16.dp), color = if (selectedTab == "pregame") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                    }
                    Tab(selected = selectedTab == "momentum", onClick = { selectedTab = "momentum" }) {
                        Text("فشار بازی", modifier = Modifier.padding(16.dp), color = if (selectedTab == "momentum") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                    }
                }
                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                    when (selectedTab) {
                        "summary" -> MatchSummaryTab(
                            match = m,
                            events = runCatching { eventAdapter.fromJson(m.eventsJson).orEmpty() }.getOrDefault(emptyList()),
                            topPerformer = topPerformer,
                            onNavigateToPlayer = onNavigateToPlayer
                        )
                        "timeline" -> TimelineTab(runCatching { eventAdapter.fromJson(m.eventsJson).orEmpty() }.getOrDefault(emptyList()), onNavigateToPlayer)
                        "stats" -> StatsTab(runCatching { statsAdapter.fromJson(m.statsJson).orEmpty() }.getOrDefault(emptyList()))
                        "lineups" -> {
                            val lineups = runCatching { lineupsAdapter.fromJson(m.lineupsJson) }.getOrNull()
                            if (lineups != null && (lineups.homeStarting.isNotEmpty() || lineups.awayStarting.isNotEmpty())) {
                                LineupsTab(lineups, m.homeTeamName, m.awayTeamName, onNavigateToPlayer)
                            } else {
                                EmptyState(message = "ترکیب رسمی هنوز اعلام نشده است.")
                            }
                        }
                        "h2h" -> {
                            val h2h = runCatching { h2hAdapter.fromJson(m.h2hJson) }.getOrNull()
                            if (h2h != null && h2h.pastMatches.isNotEmpty()) {
                                H2HTab(h2h, m.homeTeamName, m.awayTeamName)
                            } else {
                                EmptyState(message = "اطلاعات رویارویی‌های قبلی موجود نیست.")
                            }
                        }
                        "pregame" -> PreMatchCenter(
                            match = m,
                            h2h = runCatching { h2hAdapter.fromJson(m.h2hJson) }.getOrNull(),
                            lineups = runCatching { lineupsAdapter.fromJson(m.lineupsJson) }.getOrNull()
                        )
                        "momentum" -> MomentumCenter(
                            match = m,
                            events = runCatching { eventAdapter.fromJson(m.eventsJson).orEmpty() }.getOrDefault(emptyList())
                        )
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun PreMatchCenter(match: MatchEntity, h2h: HeadToHeadData?, lineups: MatchLineups?) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("natijeh_predictions", 0) }
    var prediction by remember(match.id) { mutableStateOf(prefs.getString("match_${match.id}", "").orEmpty()) }
    val confirmed = lineups != null && (lineups.homeStarting.isNotEmpty() || lineups.awayStarting.isNotEmpty())
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("مرکز پیش از مسابقه", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    SummaryInfoRow("زمان", "${match.date} · ${match.time}")
                    SummaryInfoRow("ورزشگاه", match.venue.ifBlank { "اعلام نشده" })
                    SummaryInfoRow("داور", match.referee.ifBlank { "اعلام نشده" })
                    SummaryInfoRow("ترکیب", if (confirmed) "رسمی و تأییدشده" else "هنوز تأیید نشده")
                }
            }
        }
        h2h?.let { data ->
            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("جمع‌بندی رودررو", fontWeight = FontWeight.Black)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            SummaryMetric(data.homeWins.toString(), "برد ${match.homeTeamName}", Modifier.weight(1f))
                            SummaryMetric(data.draws.toString(), "مساوی", Modifier.weight(1f))
                            SummaryMetric(data.awayWins.toString(), "برد ${match.awayTeamName}", Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("پیش‌بینی شخصی من", fontWeight = FontWeight.Black)
                    Text("پیش‌بینی روی همین دستگاه ذخیره می‌شود و بعد از شروع مسابقه قابل تغییر نیست.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf("home" to "برد میزبان", "draw" to "مساوی", "away" to "برد مهمان").forEach { (key, label) ->
                            FilterChip(
                                selected = prediction == key,
                                enabled = match.status == "SCHEDULED",
                                onClick = { prediction = key; prefs.edit().putString("match_${match.id}", key).apply() },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }
        if (match.status == "SCHEDULED") item {
            Button(onClick = { addMatchToCalendar(context, match) }, modifier = Modifier.fillMaxWidth()) { Text("افزودن به تقویم گوشی") }
        }
    }
}

@Composable
private fun MomentumCenter(match: MatchEntity, events: List<MatchEvent>) {
    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = .5f)
    val buckets = remember(events, match.minute) {
        val limit = maxOf(match.minute, events.maxOfOrNull { it.minute } ?: 0, 15)
        (0..limit step 5).map { start ->
            val section = events.filter { it.minute in start until start + 5 }
            start to (section.count { it.isHome } - section.count { !it.isHome })
        }
    }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("فشار و جریان مسابقه", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text("نمودار از رخدادهای ثبت‌شده هر بازه پنج‌دقیقه‌ای ساخته می‌شود.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
            Canvas(Modifier.fillMaxWidth().height(230.dp).padding(18.dp)) {
                val middle = size.height / 2f
                drawLine(dividerColor, Offset(0f, middle), Offset(size.width, middle), strokeWidth = 2f)
                val width = size.width / maxOf(buckets.size, 1)
                buckets.forEachIndexed { index, (_, pressure) ->
                    val magnitude = (kotlin.math.abs(pressure).coerceAtLeast(1) * 16f).coerceAtMost(middle - 8f)
                    val top = if (pressure >= 0) middle - magnitude else middle
                    drawRoundRect(
                        color = if (pressure >= 0) NatijehGreen else LiveRed,
                        topLeft = Offset(index * width + 3f, top),
                        size = Size(width - 6f, magnitude),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(match.homeTeamName, color = NatijehGreen, fontWeight = FontWeight.Bold)
            Text(match.awayTeamName, color = LiveRed, fontWeight = FontWeight.Bold)
        }
        if (events.isEmpty()) EmptyState("هنوز رخدادی برای ترسیم فشار مسابقه ثبت نشده است.")
    }
}

private fun addMatchToCalendar(context: android.content.Context, match: MatchEntity) {
    val start = listOf("yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ss'Z'")
        .firstNotNullOfOrNull { pattern -> runCatching { SimpleDateFormat(pattern, Locale.US).parse(match.utcStart)?.time }.getOrNull() }
        ?: System.currentTimeMillis() + 60 * 60 * 1000
    runCatching {
        context.startActivity(Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + 2 * 60 * 60 * 1000)
            putExtra(CalendarContract.Events.TITLE, "${match.homeTeamName} - ${match.awayTeamName}")
            putExtra(CalendarContract.Events.DESCRIPTION, match.leagueName)
            putExtra(CalendarContract.Events.EVENT_LOCATION, match.venue)
        })
    }
}

private fun matchCountdown(match: MatchEntity, nowMillis: Long): String? {
    if (match.status != "SCHEDULED" || match.utcStart.isBlank()) return null
    val formats = listOf("yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ss'Z'")
    val start = formats.firstNotNullOfOrNull { pattern -> runCatching { SimpleDateFormat(pattern, Locale.US).parse(match.utcStart)?.time }.getOrNull() } ?: return null
    val remaining = start - nowMillis
    if (remaining <= 0) return "تا لحظاتی دیگر"
    val totalMinutes = remaining / 60_000
    val days = totalMinutes / (24 * 60)
    val hours = (totalMinutes % (24 * 60)) / 60
    val minutes = totalMinutes % 60
    return when {
        days > 0 -> "$days روز و $hours ساعت"
        hours > 0 -> "$hours ساعت و $minutes دقیقه"
        else -> "$minutes دقیقه"
    }
}

@Composable
private fun MatchSummaryTab(match: MatchEntity, events: List<MatchEvent>, topPerformer: PlayerLineup?, onNavigateToPlayer: (String) -> Unit) {
    val goals = events.filter { it.type == "GOAL" || it.type == "PENALTY" }
    val cards = events.count { it.type == "CARD_RED" || it.type == "CARD_YELLOW" }
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("اطلاعات مسابقه", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    SummaryInfoRow("زمان", listOf(match.date, match.time).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "—" })
                    SummaryInfoRow("رقابت", match.leagueName.ifBlank { "—" })
                    SummaryInfoRow("ورزشگاه", match.venue.ifBlank { "—" })
                    SummaryInfoRow("داور", match.referee.ifBlank { "—" })
                    if (match.attendance.isNotBlank()) SummaryInfoRow("تماشاگر", match.attendance)
                }
            }
        }
        if (goals.isNotEmpty()) {
            item { Text("گل‌ها", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium) }
            items(goals, key = { "goal-${it.minute}-${it.playerId}-${it.isHome}" }) { event ->
                Surface(shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${event.minute}'", fontWeight = FontWeight.Black, modifier = Modifier.width(44.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            EventPlayerName(event.playerName, event.playerId, onNavigateToPlayer)
                            Text(if (event.isHome) match.homeTeamName else match.awayTeamName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                        Text("گل", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        topPerformer?.let { player -> item { TopPerformerBanner(player, onNavigateToPlayer) } }
        if (events.isNotEmpty()) item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryMetric(goals.size.toString(), "گل", Modifier.weight(1f))
                SummaryMetric(cards.toString(), "کارت", Modifier.weight(1f))
                SummaryMetric(events.count { it.type == "SUBSTITUTION" }.toString(), "تعویض", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryInfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 12.dp))
    }
}

@Composable
private fun SummaryMetric(value: String, label: String, modifier: Modifier) {
    Surface(shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.surface, modifier = modifier) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun MatchHeaderCard(
    match: MatchEntity,
    onHomeTeamClick: () -> Unit = {},
    onAwayTeamClick: () -> Unit = {},
    onLeagueClick: () -> Unit = {}
) {
    val live = match.status == "LIVE"
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        colors.surfaceVariant,
                        colors.surface,
                        colors.surfaceVariant
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = match.leagueName,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clickable(enabled = match.leagueId.isNotBlank()) { onLeagueClick() }
        )
        if (match.lastUpdatedMillis > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = JalaliDate.relative(match.lastUpdatedMillis),
                color = colors.primary,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).clickable { onHomeTeamClick() }
            ) {
                Surface(shape = CircleShape, color = colors.surface.copy(alpha = 0.7f), modifier = Modifier.size(72.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(model = match.homeTeamLogo, contentDescription = match.homeTeamName, modifier = Modifier.size(48.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = match.homeTeamName, color = colors.onSurface, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val scoreColor = if (live) LiveRed else colors.onSurface
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = if (match.status == "SCHEDULED") "–" else match.homeScore.toString(), color = scoreColor, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Text(text = ":", color = colors.onSurfaceVariant, style = MaterialTheme.typography.displayMedium)
                    Text(text = if (match.status == "SCHEDULED") "–" else match.awayScore.toString(), color = scoreColor, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (live) PulseDot()
                    val statusText = when (match.status) {
                        "LIVE" -> "زنده ${match.liveTime.ifBlank { "${match.minute}'" }}"
                        "FINISHED" -> match.statusTitle.ifBlank { "پایان" }
                        else -> match.time
                    }
                    Text(
                        text = statusText,
                        color = if (live) LiveRed else colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (match.date.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        listOf(match.date, match.time).filter { it.isNotBlank() }.joinToString(" · "),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).clickable { onAwayTeamClick() }
            ) {
                Surface(shape = CircleShape, color = colors.surface.copy(alpha = 0.7f), modifier = Modifier.size(72.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(model = match.awayTeamLogo, contentDescription = match.awayTeamName, modifier = Modifier.size(48.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = match.awayTeamName, color = colors.onSurface, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("داور", color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Text(match.referee.ifBlank { "—" }, color = colors.onSurface, style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ورزشگاه", color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Text(match.venue.ifBlank { "—" }, color = colors.onSurface, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            }
        }
    }
}

@Composable
private fun TopPerformerBanner(player: PlayerLineup, onNavigateToPlayer: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable(enabled = player.playerId.isNotBlank()) { onNavigateToPlayer(player.playerId) },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (player.portrait.isNotBlank()) {
                AsyncImage(model = player.portrait, contentDescription = player.name, modifier = Modifier.size(36.dp).clip(CircleShape))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text("بازیکن برتر", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(player.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            }
            Text(String.format("%.1f", player.rating), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun TimelineTab(events: List<MatchEvent>, onNavigateToPlayer: (String) -> Unit = {}) {
    if (events.isEmpty()) {
        EmptyState(message = "رویداد خاصی در این بازی ثبت نشده است.")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(events.reversed()) { event ->
                val cardColor = if (event.isHome) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                            EventMark(event)
                            Column {
                                val typeLabel = when (event.type) {
                                    "GOAL" -> "گل"
                                    "CARD_YELLOW" -> "کارت زرد"
                                    "CARD_RED" -> "کارت قرمز"
                                    "PENALTY" -> "پنالتی"
                                    "VAR_REVIEW" -> "بررسی VAR"
                                    "SUBSTITUTION" -> "تعویض"
                                    else -> "رویداد"
                                }
                                Text(
                                    typeLabel,
                                    color = when (event.type) {
                                        "CARD_RED" -> LiveRed
                                        "GOAL", "PENALTY" -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                EventPlayerName(event.playerName, event.playerId, onNavigateToPlayer)
                                if (event.type == "SUBSTITUTION" && event.extraPlayerName.isNotBlank()) {
                                    EventPlayerName("به‌جای ${event.extraPlayerName}", event.extraPlayerId, onNavigateToPlayer)
                                }
                                if (event.detail.isNotBlank()) {
                                    Text(event.detail, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        Text(
                            text = if (event.isHome) "میزبان" else "میهمان",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventPlayerName(label: String, playerId: String, onNavigateToPlayer: (String) -> Unit) {
    val enabled = playerId.isNotBlank() && playerId != "0"
    Text(
        text = label,
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = if (enabled) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.clickable(enabled = enabled) { onNavigateToPlayer(playerId) }
    )
}

@Composable
private fun EventMark(event: MatchEvent) {
    val minute = "${event.minute}'"
    when (event.type) {
        "GOAL", "PENALTY" -> {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(minute, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
        }
        "CARD_RED" -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(LiveRed)
                )
                Text(minute, color = LiveRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        "CARD_YELLOW" -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFF5C518))
                )
                Text(minute, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        else -> {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(minute, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun StatsTab(stats: List<StatItem>) {
    if (stats.isEmpty()) {
        EmptyState(message = "آمار این بازی هنوز منتشر نشده است.")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text("آمار زنده مسابقه از منبع رسمی فارسی.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        items(stats) { row ->
            StatRow(row.title, row.home, row.away, row.homePercent, row.awayPercent)
        }
    }
}

@Composable
fun StatRow(title: String, homeValue: String, awayValue: String, homePercent: Int, awayPercent: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(homeValue.ifBlank { "0" }, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Text(awayValue.ifBlank { "0" }, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
            val homeRatio = homePercent.coerceAtLeast(1).toFloat()
            val awayRatio = awayPercent.coerceAtLeast(1).toFloat()
            Box(modifier = Modifier.fillMaxHeight().weight(homeRatio).background(MaterialTheme.colorScheme.primary))
            Box(modifier = Modifier.fillMaxHeight().weight(awayRatio).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)))
        }
    }
}

@Composable
fun LineupsTab(
    lineups: MatchLineups,
    homeTeamName: String = "میزبان",
    awayTeamName: String = "میهمان",
    onNavigateToPlayer: (String) -> Unit = {}
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            PitchCard(
                title = homeTeamName,
                formation = lineups.homeFormation,
                coach = lineups.homeCoach,
                players = lineups.homeStarting,
                onNavigateToPlayer = onNavigateToPlayer
            )
        }
        item {
            PitchCard(
                title = awayTeamName,
                formation = lineups.awayFormation,
                coach = lineups.awayCoach,
                players = lineups.awayStarting,
                onNavigateToPlayer = onNavigateToPlayer
            )
        }
        if (lineups.homeBench.isNotEmpty() || lineups.awayBench.isNotEmpty()) {
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Text(
                    "بازیکنان ذخیره",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BenchColumn(lineups.homeBench, Modifier.weight(1f), onNavigateToPlayer)
                    BenchColumn(lineups.awayBench, Modifier.weight(1f), onNavigateToPlayer)
                }
            }
        }
    }
}

@Composable
private fun PitchCard(
    title: String,
    formation: String,
    coach: String,
    players: List<PlayerLineup>,
    onNavigateToPlayer: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                buildString {
                    append(formation.ifBlank { "—" })
                    if (coach.isNotBlank()) append(" · مربی $coach")
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            MiniPitch(players, onNavigateToPlayer)
        }
    }
}

@Composable
private fun MiniPitch(players: List<PlayerLineup>, onNavigateToPlayer: (String) -> Unit) {
    val grouped = remember(players) { players.groupBy { it.line }.toSortedMap() }
    val attackFirst = grouped.keys.sortedDescending()
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(248.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(NatijehGreen.copy(alpha = 0.28f))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val mark = Color.White.copy(alpha = 0.28f)
                val stroke = 2.dp.toPx()
                drawLine(mark, Offset(0f, size.height / 2f), Offset(size.width, size.height / 2f), strokeWidth = stroke)
                drawCircle(
                    color = mark,
                    radius = size.minDimension * 0.12f,
                    center = Offset(size.width / 2f, size.height / 2f),
                    style = Stroke(width = stroke)
                )
                val boxW = size.width * 0.56f
                val boxH = size.height * 0.16f
                val left = (size.width - boxW) / 2f
                drawRect(color = mark, topLeft = Offset(left, 0f), size = Size(boxW, boxH), style = Stroke(width = stroke))
                drawRect(color = mark, topLeft = Offset(left, size.height - boxH), size = Size(boxW, boxH), style = Stroke(width = stroke))
            }
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                attackFirst.forEach { line ->
                    val row = grouped[line].orEmpty()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { player ->
                            PitchPlayerChip(player, onNavigateToPlayer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PitchPlayerChip(player: PlayerLineup, onNavigateToPlayer: (String) -> Unit) {
    val enabled = player.playerId.isNotBlank()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable(enabled = enabled) { onNavigateToPlayer(player.playerId) }
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    if (player.number > 0) player.number.toString() else "—",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            player.name.substringBefore(" ").ifBlank { player.name },
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BenchColumn(players: List<PlayerLineup>, modifier: Modifier, onNavigateToPlayer: (String) -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        players.forEach { p ->
            val enabled = p.playerId.isNotBlank()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable(enabled = enabled) { onNavigateToPlayer(p.playerId) }
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), modifier = Modifier.size(22.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(p.number.toString(), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Text(
                    p.name,
                    color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun H2HTab(h2h: HeadToHeadData, homeTeam: String, awayTeam: String) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("رویارویی‌های اخیر", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${h2h.homeWins}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                            Text(homeTeam, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${h2h.draws}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                            Text("تساوی", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${h2h.awayWins}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                            Text(awayTeam, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val total = (h2h.homeWins + h2h.awayWins + h2h.draws).coerceAtLeast(1)
                    Row(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)) {
                        Box(Modifier.weight(h2h.homeWins.coerceAtLeast(0).toFloat() / total.toFloat() + 0.01f).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                        Box(Modifier.weight(h2h.draws.coerceAtLeast(0).toFloat() / total.toFloat() + 0.01f).fillMaxHeight().background(MaterialTheme.colorScheme.onSurfaceVariant))
                        Box(Modifier.weight(h2h.awayWins.coerceAtLeast(0).toFloat() / total.toFloat() + 0.01f).fillMaxHeight().background(LiveRed))
                    }
                }
            }
        }
        if (h2h.pastMatches.isNotEmpty()) {
            item { Text("بازی‌های قبلی", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(h2h.pastMatches) { match ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(match.homeTeam, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(match.score, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(match.date, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(match.awayTeam, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
