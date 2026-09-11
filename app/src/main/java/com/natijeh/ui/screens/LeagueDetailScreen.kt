package com.natijeh.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.natijeh.data.model.FixtureRound
import com.natijeh.data.model.ScorerRow
import com.natijeh.data.model.StandingRow
import com.natijeh.ui.viewmodel.SportsViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
private val standingsAdapter = moshi.adapter<List<StandingRow>>(
    Types.newParameterizedType(List::class.java, StandingRow::class.java)
)
private val scorersAdapter = moshi.adapter<List<ScorerRow>>(
    Types.newParameterizedType(List::class.java, ScorerRow::class.java)
)
private val fixturesAdapter = moshi.adapter<List<FixtureRound>>(
    Types.newParameterizedType(List::class.java, FixtureRound::class.java)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueDetailScreen(
    leagueId: String,
    viewModel: SportsViewModel,
    onBack: () -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToMatch: (String) -> Unit
) {
    val league by viewModel.getLeagueFlow(leagueId).collectAsStateWithLifecycle(initialValue = null)
    var selectedTab by remember { mutableStateOf("table") }

    LaunchedEffect(leagueId) {
        viewModel.loadLeagueDetails(leagueId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(league?.name ?: "جدول لیگ", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = Color.White)
                    }
                },
                actions = {
                    league?.let { l ->
                        IconButton(onClick = { viewModel.toggleLeagueFavorite(l.id, l.isFavorite) }) {
                            Icon(
                                imageVector = if (l.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "محبوب",
                                tint = if (l.isFavorite) Color(0xFF18C964) else Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0E14))
            )
        },
        containerColor = Color(0xFF0B0E14)
    ) { innerPadding ->
        league?.let { l ->
            val standings = standingsAdapter.fromJson(l.standingsJson).orEmpty()
            val scorers = scorersAdapter.fromJson(l.scorersJson).orEmpty()
            val fixtures = fixturesAdapter.fromJson(l.fixturesJson).orEmpty()
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(shape = CircleShape, color = Color(0xFF0B0E14), modifier = Modifier.size(56.dp)) {
                            AsyncImage(model = l.logo, contentDescription = l.name, modifier = Modifier.padding(12.dp).fillMaxSize())
                        }
                        Text(l.name, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                ScrollableTabRow(
                    selectedTabIndex = when (selectedTab) {
                        "table" -> 0
                        "scorers" -> 1
                        else -> 2
                    },
                    containerColor = Color(0xFF0B0E14),
                    contentColor = Color(0xFF18C964),
                    edgePadding = 0.dp
                ) {
                    Tab(selected = selectedTab == "table", onClick = { selectedTab = "table" }) {
                        Text("جدول", modifier = Modifier.padding(16.dp), color = if (selectedTab == "table") Color(0xFF18C964) else Color(0xFF64748B))
                    }
                    Tab(selected = selectedTab == "scorers", onClick = { selectedTab = "scorers" }) {
                        Text("گلزنان", modifier = Modifier.padding(16.dp), color = if (selectedTab == "scorers") Color(0xFF18C964) else Color(0xFF64748B))
                    }
                    Tab(selected = selectedTab == "week", onClick = { selectedTab = "week" }) {
                        Text("برنامه هفته", modifier = Modifier.padding(16.dp), color = if (selectedTab == "week") Color(0xFF18C964) else Color(0xFF64748B))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                when (selectedTab) {
                    "scorers" -> ScorersTab(scorers, onNavigateToTeam)
                    "week" -> FixturesTab(fixtures, onNavigateToMatch, onNavigateToTeam)
                    else -> StandingsTab(standings, onNavigateToTeam)
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
private fun StandingsTab(standings: List<StandingRow>, onNavigateToTeam: (String) -> Unit) {
    if (standings.isEmpty()) {
        EmptyState(message = "جدول این رقابت هنوز در دسترس نیست.")
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("#", color = Color(0xFF64748B), modifier = Modifier.width(24.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Text("تیم", color = Color(0xFF64748B), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("بازی", color = Color(0xFF64748B), modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("برد", color = Color(0xFF64748B), modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("مساوی", color = Color(0xFF64748B), modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("باخت", color = Color(0xFF64748B), modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("تفاضل", color = Color(0xFF64748B), modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("امتیاز", color = Color(0xFF18C964), modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(standings, key = { it.teamId }) { row ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF21262D)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToTeam(row.teamId) }
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        val rankColor = when (row.rank) {
                            1 -> Color(0xFF18C964)
                            2 -> Color(0xFF38BDF8)
                            3 -> Color(0xFFFBBF24)
                            4 -> Color(0xFFA78BFA)
                            else -> Color.White
                        }
                        Text(row.rank.toString(), color = rankColor, modifier = Modifier.width(24.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        AsyncImage(model = row.teamLogo, contentDescription = row.teamName, modifier = Modifier.size(22.dp).padding(end = 6.dp))
                        Text(row.teamName, color = Color.White, modifier = Modifier.weight(1f), maxLines = 1, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(row.played.toString(), color = Color.White, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                            Text(row.won.toString(), color = Color.White, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                            Text(row.drawn.toString(), color = Color(0xFFFBBF24), modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                            Text(row.lost.toString(), color = Color(0xFFEF4444), modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                            val gd = row.goalsFor - row.goalsAgainst
                            val gdPrefix = if (gd > 0) "+$gd" else gd.toString()
                            Text(gdPrefix, color = if (gd >= 0) Color(0xFF18C964) else Color(0xFFEF4444), modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                            Text(row.points.toString(), color = Color(0xFF18C964), modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScorersTab(scorers: List<ScorerRow>, onNavigateToTeam: (String) -> Unit) {
    if (scorers.isEmpty()) {
        EmptyState(message = "جدول گلزنان هنوز منتشر نشده است.")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(scorers, key = { it.playerId }) { row ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable(enabled = row.teamId.isNotBlank() && row.teamId != "0") {
                    onNavigateToTeam(row.teamId)
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (row.portrait.isNotBlank()) {
                        AsyncImage(model = row.portrait, contentDescription = row.name, modifier = Modifier.size(40.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(row.name, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(row.teamName, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                    }
                    Text("${row.goals} گل", color = Color(0xFF18C964), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FixturesTab(
    rounds: List<FixtureRound>,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit
) {
    if (rounds.isEmpty()) {
        EmptyState(message = "برنامه این لیگ هنوز در دسترس نیست.")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rounds.forEach { round ->
            item(key = "r-${round.round}") {
                Text(round.round, color = Color(0xFF18C964), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }
            items(round.matches, key = { it.id }) { match ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF21262D)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToMatch(match.id) }
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${match.date}  ${match.time}", color = Color(0xFF64748B), style = MaterialTheme.typography.labelSmall)
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                match.homeTeamName,
                                color = Color.White,
                                modifier = Modifier.weight(1f).clickable { onNavigateToTeam(match.homeTeamId) },
                                maxLines = 1
                            )
                            val score = if (match.homeScore != null && match.awayScore != null) {
                                "${match.homeScore} - ${match.awayScore}"
                            } else {
                                "—"
                            }
                            Text(score, color = Color(0xFF18C964), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                            Text(
                                match.awayTeamName,
                                color = Color.White,
                                modifier = Modifier.weight(1f).clickable { onNavigateToTeam(match.awayTeamId) },
                                maxLines = 1,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}
