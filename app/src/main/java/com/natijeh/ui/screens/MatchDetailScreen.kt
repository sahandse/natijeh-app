package com.natijeh.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.natijeh.data.model.HeadToHeadData
import com.natijeh.data.model.MatchEntity
import com.natijeh.data.model.MatchEvent
import com.natijeh.data.model.MatchLineups
import com.natijeh.data.model.StatItem
import com.natijeh.data.util.JalaliDate
import com.natijeh.ui.viewmodel.SportsViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

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
    onBack: () -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit
) {
    val match by sportsViewModel.getMatchFlow(matchId).collectAsStateWithLifecycle(initialValue = null)
    var selectedTab by remember { mutableStateOf("timeline") }
    val view = LocalView.current

    LaunchedEffect(matchId) {
        sportsViewModel.loadMatchDetails(matchId)
    }
    DisposableEffect(match?.status) {
        view.keepScreenOn = match?.status == "LIVE"
        onDispose { view.keepScreenOn = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("جزئیات مسابقه", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0E14))
            )
        },
        containerColor = Color(0xFF0B0E14)
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
                ScrollableTabRow(
                    selectedTabIndex = when (selectedTab) {
                        "timeline" -> 0
                        "stats" -> 1
                        "lineups" -> 2
                        "h2h" -> 3
                        else -> 0
                    },
                    containerColor = Color(0xFF0B0E14),
                    contentColor = Color(0xFF18C964),
                    edgePadding = 16.dp
                ) {
                    Tab(selected = selectedTab == "timeline", onClick = { selectedTab = "timeline" }) {
                        Text("رویدادها", modifier = Modifier.padding(16.dp), color = if (selectedTab == "timeline") Color(0xFF18C964) else Color(0xFF64748B), style = MaterialTheme.typography.labelLarge)
                    }
                    Tab(selected = selectedTab == "stats", onClick = { selectedTab = "stats" }) {
                        Text("آمار بازی", modifier = Modifier.padding(16.dp), color = if (selectedTab == "stats") Color(0xFF18C964) else Color(0xFF64748B), style = MaterialTheme.typography.labelLarge)
                    }
                    Tab(selected = selectedTab == "lineups", onClick = { selectedTab = "lineups" }) {
                        Text("ترکیب‌ها", modifier = Modifier.padding(16.dp), color = if (selectedTab == "lineups") Color(0xFF18C964) else Color(0xFF64748B), style = MaterialTheme.typography.labelLarge)
                    }
                    Tab(selected = selectedTab == "h2h", onClick = { selectedTab = "h2h" }) {
                        Text("رویارویی‌ها", modifier = Modifier.padding(16.dp), color = if (selectedTab == "h2h") Color(0xFF18C964) else Color(0xFF64748B), style = MaterialTheme.typography.labelLarge)
                    }
                }
                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                    when (selectedTab) {
                        "timeline" -> TimelineTab(eventAdapter.fromJson(m.eventsJson).orEmpty())
                        "stats" -> StatsTab(statsAdapter.fromJson(m.statsJson).orEmpty())
                        "lineups" -> {
                            val lineups = lineupsAdapter.fromJson(m.lineupsJson)
                            if (lineups != null && (lineups.homeStarting.isNotEmpty() || lineups.awayStarting.isNotEmpty())) {
                                LineupsTab(lineups)
                            } else {
                                EmptyState(message = "ترکیب رسمی هنوز اعلام نشده است.")
                            }
                        }
                        "h2h" -> {
                            val h2h = h2hAdapter.fromJson(m.h2hJson)
                            if (h2h != null && h2h.pastMatches.isNotEmpty()) {
                                H2HTab(h2h, m.homeTeamName, m.awayTeamName)
                            } else {
                                EmptyState(message = "اطلاعات رویارویی‌های قبلی موجود نیست.")
                            }
                        }
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF18C964))
            }
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
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = match.leagueName,
                color = Color(0xFF94A3B8),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable(enabled = match.leagueId.isNotBlank()) { onLeagueClick() }
            )
            if (match.lastUpdatedMillis > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = JalaliDate.relative(match.lastUpdatedMillis),
                    color = Color(0xFF18C964),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).clickable { onHomeTeamClick() }
                ) {
                    AsyncImage(model = match.homeTeamLogo, contentDescription = match.homeTeamName, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = match.homeTeamName, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = if (match.status == "SCHEDULED") "-" else match.homeScore.toString(), color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                        Text(text = ":", color = Color(0xFF64748B), style = MaterialTheme.typography.displayMedium)
                        Text(text = if (match.status == "SCHEDULED") "-" else match.awayScore.toString(), color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = if (match.status == "LIVE") Color(0xFFEF4444) else Color(0xFF21262D)) {
                        val statusText = when (match.status) {
                            "LIVE" -> "زنده ${match.liveTime.ifBlank { "${match.minute}'" }}"
                            "FINISHED" -> match.statusTitle.ifBlank { "پایان" }
                            else -> match.time
                        }
                        Text(text = statusText, color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).clickable { onAwayTeamClick() }
                ) {
                    AsyncImage(model = match.awayTeamLogo, contentDescription = match.awayTeamName, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = match.awayTeamName, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("داور", color = Color(0xFF64748B), style = MaterialTheme.typography.labelSmall)
                    Text(match.referee.ifBlank { "—" }, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ورزشگاه", color = Color(0xFF64748B), style = MaterialTheme.typography.labelSmall)
                    Text(match.venue.ifBlank { "—" }, color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun TimelineTab(events: List<MatchEvent>) {
    if (events.isEmpty()) {
        EmptyState(message = "رویداد خاصی در این بازی ثبت نشده است.")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(events.reversed()) { event ->
                val cardColor = if (event.isHome) Color(0xFF161B22) else Color(0xFF21262D)
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = cardColor), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF0B0E14)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${event.minute}'", color = Color(0xFF18C964), fontWeight = FontWeight.Bold)
                            }
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
                                Text(typeLabel, color = Color(0xFF18C964), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text(event.playerName, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                                if (event.detail.isNotBlank()) {
                                    Text(event.detail, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        Text(
                            text = if (event.isHome) "میزبان" else "میهمان",
                            color = if (event.isHome) Color(0xFF38BDF8) else Color(0xFFFBBF24),
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
fun StatsTab(stats: List<StatItem>) {
    if (stats.isEmpty()) {
        EmptyState(message = "آمار این بازی هنوز منتشر نشده است.")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFF18C964), modifier = Modifier.size(20.dp))
                    Text("آمار زنده مسابقه از منبع رسمی فارسی.", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
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
            Text(homeValue.ifBlank { "0" }, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(title, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
            Text(awayValue.ifBlank { "0" }, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color(0xFF21262D))) {
            val homeRatio = homePercent.coerceAtLeast(1).toFloat()
            val awayRatio = awayPercent.coerceAtLeast(1).toFloat()
            Box(modifier = Modifier.fillMaxHeight().weight(homeRatio).background(Color(0xFF18C964)))
            Box(modifier = Modifier.fillMaxHeight().weight(awayRatio).background(Color(0xFFFBBF24)))
        }
    }
}

@Composable
fun LineupsTab(lineups: MatchLineups) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("سیستم بازی (مربی)", color = Color(0xFF18C964), style = MaterialTheme.typography.labelSmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${lineups.homeFormation} (${lineups.homeCoach.ifBlank { "—" }})", color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${lineups.awayFormation} (${lineups.awayCoach.ifBlank { "—" }})", color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item { Text("بازیکنان اصلی", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        val maxStart = maxOf(lineups.homeStarting.size, lineups.awayStarting.size)
        items(maxStart) { index ->
            val homeP = lineups.homeStarting.getOrNull(index)
            val awayP = lineups.awayStarting.getOrNull(index)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    homeP?.let { p ->
                        Surface(shape = CircleShape, color = Color(0xFF18C964).copy(alpha = 0.2f), modifier = Modifier.size(24.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text(p.number.toString(), color = Color(0xFF18C964), style = MaterialTheme.typography.labelSmall) }
                        }
                        Text(p.name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                    awayP?.let { p ->
                        Text(p.name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = CircleShape, color = Color(0xFFFBBF24).copy(alpha = 0.2f), modifier = Modifier.size(24.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text(p.number.toString(), color = Color(0xFFFBBF24), style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            }
        }
        if (lineups.homeBench.isNotEmpty() || lineups.awayBench.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("بازیکنان ذخیره", color = Color(0xFF64748B), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            val maxBench = maxOf(lineups.homeBench.size, lineups.awayBench.size)
            items(maxBench) { index ->
                val homeBench = lineups.homeBench.getOrNull(index)
                val awayBench = lineups.awayBench.getOrNull(index)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        homeBench?.let { p ->
                            Surface(shape = CircleShape, color = Color(0xFF18C964).copy(alpha = 0.1f), modifier = Modifier.size(22.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text(p.number.toString(), color = Color(0xFF18C964).copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall) }
                            }
                            Text(p.name, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                        awayBench?.let { p ->
                            Text(p.name, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = CircleShape, color = Color(0xFFFBBF24).copy(alpha = 0.1f), modifier = Modifier.size(22.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text(p.number.toString(), color = Color(0xFFFBBF24).copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun H2HTab(h2h: HeadToHeadData, homeTeam: String, awayTeam: String) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("رویارویی‌های اخیر", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${h2h.homeWins}", color = Color(0xFF38BDF8), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                            Text(homeTeam, color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${h2h.draws}", color = Color(0xFFFBBF24), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                            Text("تساوی", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${h2h.awayWins}", color = Color(0xFFEF4444), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                            Text(awayTeam, color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        }
        if (h2h.pastMatches.isNotEmpty()) {
            item { Text("بازی‌های قبلی", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(h2h.pastMatches) { match ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF21262D)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(match.homeTeam, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(match.score, color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(match.date, color = Color(0xFF64748B), style = MaterialTheme.typography.labelSmall)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(match.awayTeam, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
