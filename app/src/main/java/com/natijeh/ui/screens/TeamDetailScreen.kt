package com.natijeh.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.natijeh.data.mapper.SportsMapper
import com.natijeh.data.model.SquadPlayer
import com.natijeh.data.model.TeamEntity
import com.natijeh.data.model.TeamResultMatch
import com.natijeh.ui.theme.LiveRed
import com.natijeh.ui.theme.natijehCardElevation
import com.natijeh.ui.viewmodel.SportsViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
private val squadAdapter = moshi.adapter<List<SquadPlayer>>(
    Types.newParameterizedType(List::class.java, SquadPlayer::class.java)
)
private val recentAdapter = moshi.adapter<List<TeamResultMatch>>(
    Types.newParameterizedType(List::class.java, TeamResultMatch::class.java)
)

private val squadFilters = listOf(
    "" to "همه",
    "GK" to "دروازه",
    "DF" to "دفاع",
    "MF" to "میانه",
    "FW" to "حمله"
)

private fun bucketLabel(bucket: String): String = when (bucket) {
    "GK" -> "دروازه‌بان‌ها"
    "DF" -> "مدافعان"
    "MF" -> "هافبک‌ها"
    "FW" -> "مهاجمان"
    else -> "سایر"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TeamDetailScreen(
    teamId: String,
    viewModel: SportsViewModel,
    onBack: () -> Unit,
    onNavigateToMatch: (String) -> Unit = {},
    onNavigateToPlayer: (String) -> Unit = {}
) {
    val team by viewModel.getTeamFlow(teamId).collectAsStateWithLifecycle(initialValue = null)
    val favoritePlayers by viewModel.favoritePlayers.collectAsStateWithLifecycle()
    val favoriteIds = remember(favoritePlayers) { favoritePlayers.map { it.id }.toSet() }
    var selectedTab by remember { mutableStateOf("matches") }

    LaunchedEffect(teamId) {
        viewModel.loadTeamDetails(teamId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(team?.name ?: "اطلاعات تیم", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        team?.let { t ->
            val squad = try {
                squadAdapter.fromJson(t.squadJson).orEmpty()
            } catch (_: Exception) {
                emptyList()
            }
            val recent = try {
                recentAdapter.fromJson(t.recentJson).orEmpty()
            } catch (_: Exception) {
                emptyList()
            }
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                TeamStadiumHeader(
                    team = t,
                    onLongPressFavorite = { viewModel.toggleTeamFavorite(t.id, t.isFavorite) }
                )
                ScrollableTabRow(
                    selectedTabIndex = when (selectedTab) {
                        "matches" -> 0
                        "squad" -> 1
                        else -> 2
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp
                ) {
                    Tab(selected = selectedTab == "matches", onClick = { selectedTab = "matches" }) {
                        Text("بازی‌ها", modifier = Modifier.padding(16.dp), color = if (selectedTab == "matches") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Tab(selected = selectedTab == "squad", onClick = { selectedTab = "squad" }) {
                        Text("ترکیب", modifier = Modifier.padding(16.dp), color = if (selectedTab == "squad") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Tab(selected = selectedTab == "stats", onClick = { selectedTab = "stats" }) {
                        Text("آمار", modifier = Modifier.padding(16.dp), color = if (selectedTab == "stats") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                when (selectedTab) {
                    "matches" -> TeamMatchesTab(teamId = t.id, matches = recent, onNavigateToMatch = onNavigateToMatch)
                    "squad" -> TeamSquadTab(
                        squad = squad,
                        favoriteIds = favoriteIds,
                        onNavigateToPlayer = onNavigateToPlayer,
                        onToggleFavorite = { id, isFav -> viewModel.togglePlayerFavorite(id, isFav) }
                    )
                    else -> TeamStatsTab(t)
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TeamStadiumHeader(team: TeamEntity, onLongPressFavorite: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val gd = team.goalsFor - team.goalsAgainst
    val gdText = if (gd > 0) "+$gd" else gd.toString()
    val subtitle = team.rankLabel.ifBlank {
        buildString {
            if (team.rank > 0) append("رتبه ${team.rank}")
            if (team.leagueName.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(team.leagueName)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.primary.copy(alpha = 0.22f),
                        colors.surface,
                    )
                )
            )
            .combinedClickable(onClick = {}, onLongClick = onLongPressFavorite)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(shape = CircleShape, color = colors.surface.copy(alpha = 0.85f), modifier = Modifier.size(92.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(model = team.logo, contentDescription = team.name, modifier = Modifier.size(68.dp))
                }
            }
            IconButton(
                onClick = onLongPressFavorite,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (team.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (team.isFavorite) "حذف تیم از محبوب‌ها" else "افزودن تیم به محبوب‌ها",
                    tint = if (team.isFavorite) colors.primary else colors.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(team.name, color = colors.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (subtitle.isNotBlank()) {
            Text(subtitle, color = colors.primary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            HeaderStat(label = "امتیاز", value = team.points.toString())
            HeaderStat(label = "برد", value = team.won.toString())
            HeaderStat(label = "تفاضل", value = gdText, emphasizePositive = gd >= 0)
        }
    }
}

@Composable
private fun HeaderStat(label: String, value: String, emphasizePositive: Boolean = true) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = if (emphasizePositive) MaterialTheme.colorScheme.primary else LiveRed,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun TeamMatchesTab(
    teamId: String,
    matches: List<TeamResultMatch>,
    onNavigateToMatch: (String) -> Unit
) {
    if (matches.isEmpty()) {
        EmptyState(message = "بازی‌های این تیم هنوز در دسترس نیست.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(matches, key = { it.id }) { match ->
            val result = SportsMapper.resultVersus(match, teamId)
            val tint = when (result) {
                "WIN" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                "LOSS" -> LiveRed.copy(alpha = 0.14f)
                "DRAW" -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
            val scoreColor = when (result) {
                "WIN" -> MaterialTheme.colorScheme.primary
                "LOSS" -> LiveRed
                else -> MaterialTheme.colorScheme.onSurface
            }
            val score = if (match.homeScore != null && match.awayScore != null) {
                "${match.homeScore} - ${match.awayScore}"
            } else {
                match.time.ifBlank { "—" }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = tint),
                elevation = natijehCardElevation(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().clickable(enabled = match.id.isNotBlank()) { onNavigateToMatch(match.id) }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(match.date, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        if (match.leagueName.isNotBlank()) {
                            Text(match.leagueName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(match.homeTeam, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                        Text(score, color = scoreColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp), style = MaterialTheme.typography.titleMedium)
                        Text(match.awayTeam, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TeamSquadTab(
    squad: List<SquadPlayer>,
    favoriteIds: Set<String>,
    onNavigateToPlayer: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    if (squad.isEmpty()) {
        EmptyState(message = "ترکیب این تیم هنوز منتشر نشده است.")
        return
    }
    var filter by remember { mutableStateOf("") }
    val visible = remember(squad, filter) {
        if (filter.isBlank()) squad else squad.filter { SportsMapper.squadBucket(it.position) == filter }
    }
    val grouped = remember(visible) {
        visible.groupBy { SportsMapper.squadBucket(it.position) }
            .toSortedMap(compareBy { key -> listOf("GK", "DF", "MF", "FW", "OT").indexOf(key).takeIf { it >= 0 } ?: 9 })
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            squadFilters.forEach { (key, label) ->
                FilterChip(
                    selected = filter == key,
                    onClick = { filter = key },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            grouped.forEach { (bucket, players) ->
                item(span = { GridItemSpan(maxLineSpan) }, key = "h-$bucket-$filter") {
                    Text(bucketLabel(bucket), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
                items(players, key = { it.id.ifBlank { it.name } }) { player ->
                    val fav = player.id in favoriteIds
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = natijehCardElevation(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.combinedClickable(
                            onClick = { if (player.id.isNotBlank()) onNavigateToPlayer(player.id) },
                            onLongClick = { if (player.id.isNotBlank()) onToggleFavorite(player.id, fav) }
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(64.dp)) {
                                    if (player.portrait.isNotBlank()) {
                                        AsyncImage(model = player.portrait, contentDescription = player.name, modifier = Modifier.size(64.dp).clip(CircleShape))
                                    }
                                }
                                if (fav) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                player.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        if (player.shirtNumber > 0) player.shirtNumber.toString() else "—",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamStatsTab(team: TeamEntity) {
    val gd = team.goalsFor - team.goalsAgainst
    val rows = listOf(
        "بازی" to team.played.toString(),
        "برد" to team.won.toString(),
        "مساوی" to team.drawn.toString(),
        "باخت" to team.lost.toString(),
        "گل زده" to team.goalsFor.toString(),
        "گل خورده" to team.goalsAgainst.toString(),
        "تفاضل" to if (gd > 0) "+$gd" else gd.toString(),
        "امتیاز" to team.points.toString(),
        "رتبه" to if (team.rank > 0) team.rank.toString() else "—"
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (team.coach.isNotBlank()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("مربی", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                        Text(team.coach, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (team.formation.isNotBlank() && team.formation != "-") {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("سیستم بازی", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                        Text(team.formation, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        items(rows) { (label, value) ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        value,
                        color = if (label == "باخت" && team.lost > 0) LiveRed else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
