package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.viewmodel.SportsViewModelimport com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
private val eventAdapter = moshi.adapter<List<MatchEvent>>(
    Types.newParameterizedType(List::class.java, MatchEvent::class.java)
)
private val statsAdapter = moshi.adapter(MatchStats::class.java)
private val lineupsAdapter = moshi.adapter(MatchLineups::class.java)
private val h2hAdapter = moshi.adapter(HeadToHeadData::class.java)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    matchId: String,
    sportsViewModel: SportsViewModel,
    onBack: () -> Unit
) {
    val match by sportsViewModel.getMatchFlow(matchId).collectAsStateWithLifecycle(initialValue = null)
    var selectedTab by remember { mutableStateOf("timeline") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("جزئیات مسابقه", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "بازگشت", tint = Color.White)
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
                // Header Score Card
                MatchHeaderCard(m)

                // Tabs
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

                // Tab Content
                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                    when (selectedTab) {
                        "timeline" -> {
                            val events = eventAdapter.fromJson(m.eventsJson) ?: emptyList()
                            TimelineTab(events)
                        }
                        "stats" -> {
                            val stats = statsAdapter.fromJson(m.statsJson) ?: createDefaultStats()
                            StatsTab(stats)
                        }
                        "lineups" -> {
                            val lineups = lineupsAdapter.fromJson(m.lineupsJson)
                            if (lineups != null) {
                                LineupsTab(lineups)
                            } else {
                                EmptyState(message = "ترکیب رسمی هنوز اعلام نشده است.")
                            }
                        }
                        "h2h" -> {
                            val h2h = h2hAdapter.fromJson(m.h2hJson)
                            if (h2h != null) {
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

// --- SUB-COMPONENTS FOR MATCH DETAIL ---

@Composable
fun MatchHeaderCard(match: MatchEntity) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = match.leagueName, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home Team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = match.homeTeamName, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }

                // Scores
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = if (match.status == "SCHEDULED") "-" else match.homeScore.toString(), color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                        Text(text = ":", color = Color(0xFF64748B), style = MaterialTheme.typography.displayMedium)
                        Text(text = if (match.status == "SCHEDULED") "-" else match.awayScore.toString(), color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (match.status == "LIVE") Color(0xFFEF4444) else Color(0xFF21262D)
                    ) {
                        val statusText = when (match.status) {
                            "LIVE" -> "زنده ${match.minute}'"
                            "FINISHED" -> "پایان"
                            else -> match.time
                        }
                        Text(text = statusText, color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                    }
                }

                // Away Team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = match.awayTeamName, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📡 پوشش لایو اسکور زنده و اتفاقات بازی",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("داور", color = Color(0xFF64748B), style = MaterialTheme.typography.labelSmall)
                    Text(match.referee, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ورزشگاه", color = Color(0xFF64748B), style = MaterialTheme.typography.labelSmall)
                    Text(match.venue, color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun TimelineTab(events: List<MatchEvent>) {
    if (events.isEmpty()) {
        EmptyState(message = "رویداد خاصی در این بازی رخ نداده است.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(events.reversed()) { event ->
                val cardColor = if (event.isHome) Color(0xFF161B22) else Color(0xFF21262D)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0B0E14)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${event.minute}'", color = Color(0xFF18C964), fontWeight = FontWeight.Bold)
                            }
                            Column {
                                val typeLabel = when (event.type) {
                                    "GOAL" -> "⚽ گل!"
                                    "CARD_YELLOW" -> "🟨 کارت زرد"
                                    "CARD_RED" -> "🟥 کارت قرمز"
                                    "PENALTY" -> "🎯 پنالتی"
                                    "VAR_REVIEW" -> "🔍 بررسی VAR"
                                    else -> "رویداد"
                                }
                                Text(typeLabel, color = Color(0xFF18C964), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text(event.playerName, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                                Text(event.detail, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
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
fun StatsTab(stats: MatchStats) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "اطلاعات",
                        tint = Color(0xFF18C964),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "آمار پیشرفته مسابقه شامل مالکیت توپ، شوت‌ها و امید گل (xG).",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item { StatRow("مالکیت توپ (%)", stats.possessionHome, stats.possessionAway) }
        item { StatRow("مجموع شوت‌ها", stats.shotsHome, stats.shotsAway) }
        item { StatRow("شوت در چارچوب", stats.shotsOnTargetHome, stats.shotsOnTargetAway) }
        item { StatRow("امید گل (xG)", (stats.expectedGoalsHome * 100).toInt(), (stats.expectedGoalsAway * 100).toInt(), displayFormat = { "${(it.toDouble() / 100)}" }) }
        item { StatRow("دقت پاس (%)", stats.passAccuracyHome, stats.passAccuracyAway) }
        item { StatRow("ضربات کرنر", stats.cornersHome, stats.cornersAway) }
        item { StatRow("خطاها", stats.foulsHome, stats.foulsAway) }
        item { StatRow("آفسایدها", stats.offsidesHome, stats.offsidesAway) }
    }
}

@Composable
fun StatRow(
    title: String,
    homeValue: Int,
    awayValue: Int,
    displayFormat: (Int) -> String = { it.toString() }
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(displayFormat(homeValue), color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(title, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
            Text(displayFormat(awayValue), color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF21262D))
        ) {
            val total = (homeValue + awayValue).coerceAtLeast(1)
            val homeRatio = homeValue.toFloat() / total
            val awayRatio = awayValue.toFloat() / total
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(homeRatio.coerceAtLeast(0.01f))
                    .background(Color(0xFF18C964))
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(awayRatio.coerceAtLeast(0.01f))
                    .background(Color(0xFFFBBF24))
            )
        }
    }
}

@Composable
fun LineupsTab(lineups: MatchLineups) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("سیستم بازی (مربی)", color = Color(0xFF18C964), style = MaterialTheme.typography.labelSmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${lineups.homeFormation} (${lineups.homeCoach})", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${lineups.awayFormation} (${lineups.awayCoach})", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item { Text("بازیکنان اصلی", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        items(lineups.homeStarting.zip(lineups.awayStarting)) { (homeP, awayP) ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = CircleShape, color = Color(0xFF18C964).copy(alpha = 0.2f), modifier = Modifier.size(24.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text(homeP.number.toString(), color = Color(0xFF18C964), style = MaterialTheme.typography.labelSmall) }
                    }
                    Text(homeP.name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                    Text(awayP.name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(shape = CircleShape, color = Color(0xFFFBBF24).copy(alpha = 0.2f), modifier = Modifier.size(24.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text(awayP.number.toString(), color = Color(0xFFFBBF24), style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }

        // Bench players
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("رویارویی‌های اخیر", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

        // Past matches
        if (h2h.pastMatches.isNotEmpty()) {
            item {
                Text("بازی‌های قبلی", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(h2h.pastMatches) { match ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF21262D)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

private fun createDefaultStats(): MatchStats {
    return MatchStats(
        possessionHome = 50, possessionAway = 50,
        shotsHome = 10, shotsAway = 10,
        shotsOnTargetHome = 4, shotsOnTargetAway = 4,
        expectedGoalsHome = 1.0, expectedGoalsAway = 1.0,
        passAccuracyHome = 80, passAccuracyAway = 80,
        cornersHome = 5, cornersAway = 5,
        foulsHome = 10, foulsAway = 10,
        offsidesHome = 2, offsidesAway = 2
    )
}
