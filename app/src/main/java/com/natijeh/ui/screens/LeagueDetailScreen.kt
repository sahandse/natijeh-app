package com.natijeh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.natijeh.data.model.FixtureRound
import com.natijeh.data.model.ScorerRow
import com.natijeh.data.model.StandingRow
import com.natijeh.ui.theme.LiveRed
import com.natijeh.ui.theme.NatijehGreen
import com.natijeh.ui.theme.RankGold
import com.natijeh.ui.theme.RankSilver
import com.natijeh.ui.theme.natijehCardElevation
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
    onNavigateToMatch: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit = {}
) {
    val league by viewModel.getLeagueFlow(leagueId).collectAsStateWithLifecycle(initialValue = null)
    var selectedTab by remember { mutableStateOf("table") }

    LaunchedEffect(leagueId) {
        viewModel.loadLeagueDetails(leagueId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(league?.name ?: "جدول لیگ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    league?.let { l ->
                        IconButton(onClick = { viewModel.toggleLeagueFavorite(l.id, l.isFavorite) }) {
                            Icon(
                                imageVector = if (l.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "محبوب",
                                tint = if (l.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        league?.let { l ->
            val standings = standingsAdapter.fromJson(l.standingsJson).orEmpty()
            val scorers = scorersAdapter.fromJson(l.scorersJson).orEmpty()
            val fixtures = fixturesAdapter.fromJson(l.fixturesJson).orEmpty()
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = natijehCardElevation(),
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.20f), MaterialTheme.colorScheme.surface))).padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background, modifier = Modifier.size(56.dp)) {
                            AsyncImage(model = l.logo, contentDescription = l.name, modifier = Modifier.padding(12.dp).fillMaxSize())
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(l.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (l.country.isNotBlank()) {
                                Text(l.country, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            standings.firstOrNull()?.let { leader ->
                                Text("صدرنشین: ${leader.teamName} · ${leader.points} امتیاز", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LeagueBadge("${standings.size} تیم")
                                LeagueBadge("${scorers.size} گلزن")
                                LeagueBadge("${fixtures.size} هفته")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                ScrollableTabRow(
                    selectedTabIndex = when (selectedTab) {
                        "table" -> 0
                        "scorers" -> 1
                        else -> 2
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 0.dp
                ) {
                    Tab(selected = selectedTab == "table", onClick = { selectedTab = "table" }) {
                        Text("جدول", modifier = Modifier.padding(16.dp), color = if (selectedTab == "table") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Tab(selected = selectedTab == "scorers", onClick = { selectedTab = "scorers" }) {
                        Text("گلزنان", modifier = Modifier.padding(16.dp), color = if (selectedTab == "scorers") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Tab(selected = selectedTab == "week", onClick = { selectedTab = "week" }) {
                        Text("برنامه هفته", modifier = Modifier.padding(16.dp), color = if (selectedTab == "week") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                when (selectedTab) {
                    "scorers" -> ScorersTab(scorers, onNavigateToTeam, onNavigateToPlayer)
                    "week" -> FixturesTab(fixtures, onNavigateToMatch, onNavigateToTeam)
                    else -> StandingsTab(standings, onNavigateToTeam)
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
private fun LeagueBadge(text: String) {
    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.background.copy(alpha = 0.75f)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StandingsTab(standings: List<StandingRow>, onNavigateToTeam: (String) -> Unit) {
    if (standings.isEmpty()) {
        EmptyState(message = "جدول این رقابت هنوز در دسترس نیست.")
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StandingLegend(NatijehGreen, "سهمیه")
            StandingLegend(LiveRed, "سقوط")
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("#", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(24.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Text("تیم", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("بازی", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("برد", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("مساوی", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("باخت", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("تفاضل", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("امتیاز", color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(standings, key = { it.teamId }) { row ->
                val relegationStart = (standings.size - 1).coerceAtLeast(1)
                val rankBar = when {
                    row.rank <= 4 -> NatijehGreen
                    row.rank >= relegationStart -> LiveRed
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0f)
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = natijehCardElevation(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToTeam(row.teamId) }
                ) {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(rankBar)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                row.rank.toString(),
                                color = when (row.rank) {
                                    1 -> MaterialTheme.colorScheme.primary
                                    2 -> RankGold
                                    3 -> RankSilver
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.width(24.dp),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            AsyncImage(model = row.teamLogo, contentDescription = row.teamName, modifier = Modifier.size(22.dp).padding(end = 6.dp))
                            Text(row.teamName, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(row.played.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                                Text(row.won.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                                Text(row.drawn.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                                Text(row.lost.toString(), color = if (row.lost > 0) LiveRed else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                                val gd = row.goalsFor - row.goalsAgainst
                                val gdPrefix = if (gd > 0) "+$gd" else gd.toString()
                                Text(gdPrefix, color = if (gd >= 0) MaterialTheme.colorScheme.primary else LiveRed, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                                Text(row.points.toString(), color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StandingLegend(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ScorersTab(
    scorers: List<ScorerRow>,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit
) {
    if (scorers.isEmpty()) {
        EmptyState(message = "جدول گلزنان هنوز منتشر نشده است.")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(scorers, key = { _, row -> row.playerId }) { index, row ->
            val rank = index + 1
            val canOpenPlayer = row.playerId.isNotBlank() && row.playerId != "0"
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = natijehCardElevation(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().clickable(enabled = canOpenPlayer) {
                    onNavigateToPlayer(row.playerId)
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = rank.toString(),
                        color = when (rank) {
                            1 -> MaterialTheme.colorScheme.primary
                            2 -> RankGold
                            3 -> RankSilver
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (row.portrait.isNotBlank()) {
                            AsyncImage(model = row.portrait, contentDescription = row.name, modifier = Modifier.size(44.dp).clip(CircleShape))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(row.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text(
                            row.teamName,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.clickable(enabled = row.teamId.isNotBlank() && row.teamId != "0") {
                                onNavigateToTeam(row.teamId)
                            }
                        )
                    }
                    Text("${row.goals} گل", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
                Text(round.round, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }
            items(round.matches, key = { it.id }) { match ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToMatch(match.id) }
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${match.date}  ${match.time}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                match.homeTeamName,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f).clickable { onNavigateToTeam(match.homeTeamId) },
                                maxLines = 1
                            )
                            val score = if (match.homeScore != null && match.awayScore != null) {
                                "${match.homeScore} - ${match.awayScore}"
                            } else {
                                "—"
                            }
                            Text(score, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                            Text(
                                match.awayTeamName,
                                color = MaterialTheme.colorScheme.onSurface,
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
