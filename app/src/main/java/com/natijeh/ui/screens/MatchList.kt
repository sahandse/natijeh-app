package com.natijeh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.natijeh.data.util.MatchAlertFormatter
import com.natijeh.ui.theme.LiveRed

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
    onOnlyFavoritesChange: (Boolean) -> Unit
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
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        grouped.forEach { (league, leagueMatches) ->
                            item(key = "h-${league.id}-${league.name}") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = league.id.isNotBlank()) { onLeagueClick(league.id) }
                                        .padding(vertical = 4.dp),
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
                                    onLeagueClick = { onLeagueClick(match.leagueId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(
    match: MatchEntity,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onHomeTeamClick: () -> Unit = {},
    onAwayTeamClick: () -> Unit = {},
    onLeagueClick: () -> Unit = {},
    showLeague: Boolean = true
) {
    val live = match.status == "LIVE"
    val accent = if (live) LiveRed else MaterialTheme.colorScheme.primary
    val statusText = when (match.status) {
        "LIVE" -> match.liveTime.ifBlank { "${match.minute}'" }.ifBlank { "زنده" }
        "FINISHED" -> match.statusTitle.ifBlank { "پایان" }
        else -> match.time.ifBlank { match.date }
    }
    val homeScore = if (match.status == "SCHEDULED") "–" else match.homeScore.toString()
    val awayScore = if (match.status == "SCHEDULED") "–" else match.awayScore.toString()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min).fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (live) LiveRed else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (showLeague) {
                        Text(
                            text = match.leagueName,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).clickable { onLeagueClick() }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (live) LiveRed.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (live) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(LiveRed)
                                )
                            }
                            Text(
                                text = if (live) "زنده $statusText" else statusText,
                                color = if (live) LiveRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (match.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "علاقه‌مندی",
                            tint = if (match.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamMark(
                        name = match.homeTeamName,
                        logo = match.homeTeamLogo,
                        alignEnd = false,
                        modifier = Modifier.weight(1f).clickable { onHomeTeamClick() }
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (live) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                homeScore,
                                color = if (live) accent else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(":", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Text(
                                awayScore,
                                color = if (live) accent else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    TeamMark(
                        name = match.awayTeamName,
                        logo = match.awayTeamLogo,
                        alignEnd = true,
                        modifier = Modifier.weight(1f).clickable { onAwayTeamClick() }
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AsyncImage(model = logo, contentDescription = name, modifier = Modifier.size(32.dp))
        Text(
            text = name,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
        )
    }
}
