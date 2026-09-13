package com.natijeh.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.natijeh.data.mapper.SportsMapper
import com.natijeh.data.model.MatchEntity
import com.natijeh.data.model.OfficialStreamResolver
import com.natijeh.data.util.MatchAlertFormatter
import com.natijeh.ui.theme.LiveRed
import com.natijeh.ui.theme.PulseDot
import com.natijeh.ui.theme.natijehCardElevation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupedMatchList(
    matches: List<MatchEntity>,
    isRefreshing: Boolean,
    emptyMessage: String,
    onlyFavorites: Boolean,
    favoriteTeamIds: Set<String>,
    favoriteLeagueIds: Set<String>,
    onRefresh: () -> Unit,
    onMatchClick: (String) -> Unit,
    onTeamClick: (String) -> Unit,
    onLeagueClick: (String) -> Unit,
    onFavoriteToggle: (MatchEntity) -> Unit,
    onOnlyFavoritesChange: (Boolean) -> Unit,
    compactCards: Boolean = true,
    onStreamClick: (String, String) -> Unit = { _, _ -> }
) {
    val visible = remember(matches, onlyFavorites, favoriteTeamIds, favoriteLeagueIds) {
        if (!onlyFavorites) matches else matches.filter {
            MatchAlertFormatter.isWatched(it, favoriteTeamIds, favoriteLeagueIds)
        }
    }
    val grouped = remember(visible) { SportsMapper.groupMatchesByLeague(visible) }

    Column(modifier = Modifier.fillMaxSize()) {
        FilterChip(
            selected = onlyFavorites,
            onClick = { onOnlyFavoritesChange(!onlyFavorites) },
            label = { Text("فقط محبوب‌ها", style = MaterialTheme.typography.labelMedium) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
        )
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                visible.isEmpty() && isRefreshing -> LoadingState()
                visible.isEmpty() -> EmptyState(message = emptyMessage)
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        grouped.forEach { (league, leagueMatches) ->
                            item(key = "h-${league.id}-${league.name}") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = league.id.isNotBlank()) { onLeagueClick(league.id) }
                                        .padding(horizontal = 4.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (league.logo.isNotBlank()) {
                                        AsyncImage(model = league.logo, contentDescription = league.name, modifier = Modifier.size(22.dp))
                                    }
                                    Text(
                                        text = league.name,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            items(leagueMatches, key = { it.id }) { match ->
                                MatchCard(
                                    match = match,
                                    showLeague = false,
                                    onClick = { onMatchClick(match.id) },
                                    onFavoriteToggle = { onFavoriteToggle(match) },
                                    onHomeTeamClick = { onTeamClick(match.homeTeamId) },
                                    onAwayTeamClick = { onTeamClick(match.awayTeamId) },
                                    onLeagueClick = { onLeagueClick(match.leagueId) },
                                    compact = compactCards,
                                    onStreamClick = { provider -> onStreamClick(match.id, provider) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MatchCard(
    match: MatchEntity,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onHomeTeamClick: () -> Unit = {},
    onAwayTeamClick: () -> Unit = {},
    onLeagueClick: () -> Unit = {},
    showLeague: Boolean = true,
    compact: Boolean = true,
    onStreamClick: (String) -> Unit = {}
) {
    val live = match.status == "LIVE"
    val statusText = when (match.status) {
        "LIVE" -> match.liveTime.ifBlank { "${match.minute}'" }.ifBlank { "زنده" }
        "FINISHED" -> listOf(match.statusTitle.ifBlank { "پایان" }, match.date).filter { it.isNotBlank() }.joinToString(" · ")
        else -> listOf(match.date, match.time).filter { it.isNotBlank() }.joinToString(" · ")
    }
    val homeScore = if (match.status == "SCHEDULED") "–" else match.homeScore.toString()
    val awayScore = if (match.status == "SCHEDULED") "–" else match.awayScore.toString()
    val scoreColor = if (live) LiveRed else MaterialTheme.colorScheme.onSurface

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (live) LiveRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = natijehCardElevation(),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            .combinedClickable(onClick = onClick, onLongClick = onFavoriteToggle)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (live) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(if (compact) 124.dp else 156.dp)
                        .background(LiveRed)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = if (compact) 10.dp else 16.dp)) {
                    if (showLeague) {
                        Text(
                            text = match.leagueName,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLeagueClick() }
                                .padding(bottom = 8.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TeamMark(
                            name = match.homeTeamName,
                            logo = match.homeTeamLogo,
                            alignEnd = false,
                            modifier = Modifier.weight(1f).clickable { onHomeTeamClick() },
                            compact = compact
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AnimatedContent(targetState = homeScore, label = "home-score") { score ->
                                    Text(score, color = scoreColor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                }
                                Text(":", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                AnimatedContent(targetState = awayScore, label = "away-score") { score ->
                                    Text(score, color = scoreColor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (live) PulseDot()
                                Text(
                                    text = if (live) "زنده $statusText" else statusText,
                                    color = if (live) LiveRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        TeamMark(
                            name = match.awayTeamName,
                            logo = match.awayTeamLogo,
                            alignEnd = true,
                            modifier = Modifier.weight(1f).clickable { onAwayTeamClick() },
                            compact = compact
                        )
                    }
                    if (live) {
                        val streams = remember(match) { OfficialStreamResolver.forMatch(match) }
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(streams, key = { it.id }) { stream ->
                                FilterChip(
                                    selected = false,
                                    onClick = { onStreamClick(stream.id) },
                                    label = { Text(stream.title, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp)) }
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = if (match.isFavorite) Icons.Outlined.Notifications else Icons.Outlined.NotificationsNone,
                        contentDescription = if (match.isFavorite) "اعلان این بازی فعال است" else "فعال‌کردن اعلان این بازی",
                        tint = if (match.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onFavoriteToggle)
                            .padding(6.dp)
                            .size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamMark(
    name: String,
    logo: String,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 38.dp else 52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(model = logo, contentDescription = name, modifier = Modifier.size(if (compact) 28.dp else 40.dp))
        }
        Text(
            text = name,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
        )
    }
}
