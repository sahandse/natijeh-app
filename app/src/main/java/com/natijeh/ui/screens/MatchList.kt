package com.natijeh.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.natijeh.data.mapper.SportsMapper
import com.natijeh.data.model.MatchEntity
import com.natijeh.data.util.MatchAlertFormatter

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
                selectedContainerColor = Color(0xFF18C964),
                selectedLabelColor = Color(0xFF0B0E14),
                containerColor = Color(0xFF161B22),
                labelColor = Color.White
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
                                        color = Color(0xFF18C964),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            items(leagueMatches, key = { it.id }) { match ->
                                MatchCard(
                                    match = match,
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
    onLeagueClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.leagueName,
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onLeagueClick() }
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (match.status == "LIVE") Color(0xFFEF4444) else Color(0xFF21262D)
                ) {
                    val statusText = when (match.status) {
                        "LIVE" -> "زنده ${match.liveTime.ifBlank { "${match.minute}'" }}"
                        "FINISHED" -> match.statusTitle.ifBlank { "پایان" }
                        else -> match.time.ifBlank { match.date }
                    }
                    Text(
                        text = statusText,
                        color = if (match.status == "LIVE") Color.White else Color(0xFF94A3B8),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onHomeTeamClick() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(model = match.homeTeamLogo, contentDescription = match.homeTeamName, modifier = Modifier.size(28.dp))
                    Text(
                        text = match.homeTeamName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    val scoreColor = if (match.status == "LIVE") Color(0xFF18C964) else Color.White
                    Text(text = if (match.status == "SCHEDULED") "-" else match.homeScore.toString(), color = scoreColor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(text = ":", color = Color(0xFF475569), style = MaterialTheme.typography.headlineMedium)
                    Text(text = if (match.status == "SCHEDULED") "-" else match.awayScore.toString(), color = scoreColor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAwayTeamClick() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Text(
                        text = match.awayTeamName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    AsyncImage(model = match.awayTeamLogo, contentDescription = match.awayTeamName, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.date.ifBlank { match.venue },
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (match.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "علاقه‌مندی",
                        tint = if (match.isFavorite) Color(0xFF18C964) else Color(0xFF64748B)
                    )
                }
            }
        }
    }
}
