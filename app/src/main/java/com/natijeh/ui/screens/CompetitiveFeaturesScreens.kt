package com.natijeh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.natijeh.data.model.MatchEntity
import com.natijeh.data.model.MatchEvent
import com.natijeh.data.model.StandingRow
import com.natijeh.ui.theme.LiveRed
import com.natijeh.ui.viewmodel.SportsViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

private val competitiveMoshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
private val competitiveStandingsAdapter = competitiveMoshi.adapter<List<StandingRow>>(Types.newParameterizedType(List::class.java, StandingRow::class.java))
private val competitiveEventAdapter = competitiveMoshi.adapter<List<MatchEvent>>(Types.newParameterizedType(List::class.java, MatchEvent::class.java))

@Composable
fun MultiMatchCenterContent(viewModel: SportsViewModel, onMatch: (String) -> Unit) {
    val context = LocalContext.current
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    val prefs = remember { context.getSharedPreferences("natijeh_multi_match", 0) }
    var selected by remember { mutableStateOf(prefs.getStringSet("ids", emptySet()).orEmpty().toSet()) }
    val active = matches.filter { it.id in selected }.sortedWith(compareByDescending<MatchEntity> { it.status == "LIVE" }.thenBy { it.time })
    val candidates = matches.filter { it.status == "LIVE" || it.dayOffset == 0 }.filterNot { it.id in selected }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("مرکز چندمسابقه‌ای", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("تا چهار بازی را هم‌زمان دنبال کنید", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(candidates.take(20), key = { "multi-candidate-${it.id}" }) { match ->
                    FilterChip(
                        selected = false,
                        enabled = selected.size < 4,
                        onClick = { selected = selected + match.id; prefs.edit().putStringSet("ids", selected).apply() },
                        leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) },
                        label = { Text("${match.homeTeamName} - ${match.awayTeamName}", maxLines = 1) }
                    )
                }
            }
        }
        if (active.isEmpty()) item { EmptyState("از بازی‌های امروز حداکثر چهار مسابقه انتخاب کنید.") }
        items(active, key = { "multi-active-${it.id}" }) { match ->
            MultiMatchCard(
                match = match,
                onOpen = { onMatch(match.id) },
                onRemove = { selected = selected - match.id; prefs.edit().putStringSet("ids", selected).apply() }
            )
        }
    }
}

@Composable
private fun MultiMatchCard(match: MatchEntity, onOpen: () -> Unit, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onOpen), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(match.leagueName, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                if (match.status == "LIVE") Text("● ${match.liveTime}", color = LiveRed, fontWeight = FontWeight.Black)
                IconButton(onRemove, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close, "حذف", modifier = Modifier.size(17.dp)) }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                MultiTeam(match.homeTeamName, match.homeTeamLogo)
                Text(if (match.status == "SCHEDULED") match.time else "${match.homeScore} : ${match.awayScore}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                MultiTeam(match.awayTeamName, match.awayTeamLogo)
            }
            val latest = runCatching { competitiveEventAdapter.fromJson(match.eventsJson).orEmpty().maxByOrNull { it.minute } }.getOrNull()
            latest?.let { Text("دقیقه ${it.minute} · ${it.playerName} ${it.detail}", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}

@Composable
private fun MultiTeam(name: String, logo: String) {
    Column(Modifier.fillMaxWidth(.32f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AsyncImage(logo, name, modifier = Modifier.size(42.dp))
        Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun LiveStandingsContent(viewModel: SportsViewModel, onTeam: (String) -> Unit) {
    val leagues by viewModel.allLeagues.collectAsStateWithLifecycle()
    val liveMatches by viewModel.liveMatches.collectAsStateWithLifecycle()
    var leagueId by remember { mutableStateOf(leagues.firstOrNull()?.id.orEmpty()) }
    val league = leagues.firstOrNull { it.id == leagueId } ?: leagues.firstOrNull()
    val base = remember(league?.standingsJson) { runCatching { competitiveStandingsAdapter.fromJson(league?.standingsJson.orEmpty()).orEmpty() }.getOrDefault(emptyList()) }
    val adjusted = remember(base, liveMatches, league?.id) { calculateLiveStandings(base, liveMatches.filter { it.leagueId == league?.id }) }

    Column(Modifier.fillMaxSize()) {
        LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(leagues, key = { it.id }) { item -> FilterChip(leagueId == item.id, { leagueId = item.id }, { Text(item.name) }) }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { Text("جدول زنده", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Text("نتایج در جریان به‌صورت موقت روی جدول اعمال شده‌اند", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
            items(adjusted, key = { "live-standing-${it.teamId}" }) { row ->
                Card(Modifier.fillMaxWidth().clickable { onTeam(row.teamId) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(14.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(row.rank.toString(), Modifier.size(28.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Black)
                        AsyncImage(row.teamLogo, row.teamName, modifier = Modifier.size(30.dp))
                        Text(row.teamName, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("${row.played} بازی", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)) { Text(row.points.toString(), Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black) }
                    }
                }
            }
        }
    }
}

private fun calculateLiveStandings(base: List<StandingRow>, live: List<MatchEntity>): List<StandingRow> {
    val changed = base.associateBy { it.teamId }.toMutableMap()
    live.forEach { match ->
        val home = changed[match.homeTeamId]
        val away = changed[match.awayTeamId]
        if (home != null) changed[home.teamId] = home.copy(played = home.played + 1, won = home.won + if (match.homeScore > match.awayScore) 1 else 0, drawn = home.drawn + if (match.homeScore == match.awayScore) 1 else 0, lost = home.lost + if (match.homeScore < match.awayScore) 1 else 0, goalsFor = home.goalsFor + match.homeScore, goalsAgainst = home.goalsAgainst + match.awayScore, points = home.points + if (match.homeScore > match.awayScore) 3 else if (match.homeScore == match.awayScore) 1 else 0)
        if (away != null) changed[away.teamId] = away.copy(played = away.played + 1, won = away.won + if (match.awayScore > match.homeScore) 1 else 0, drawn = away.drawn + if (match.homeScore == match.awayScore) 1 else 0, lost = away.lost + if (match.awayScore < match.homeScore) 1 else 0, goalsFor = away.goalsFor + match.awayScore, goalsAgainst = away.goalsAgainst + match.homeScore, points = away.points + if (match.awayScore > match.homeScore) 3 else if (match.homeScore == match.awayScore) 1 else 0)
    }
    return changed.values.sortedWith(compareByDescending<StandingRow> { it.points }.thenByDescending { it.goalsFor - it.goalsAgainst }.thenByDescending { it.goalsFor }).mapIndexed { index, row -> row.copy(rank = index + 1) }
}
