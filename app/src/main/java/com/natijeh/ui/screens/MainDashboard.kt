package com.natijeh.ui.screens

import android.content.Intent
import android.net.Uri

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.natijeh.R
import com.natijeh.data.mapper.SportsMapper
import com.natijeh.data.model.MatchEntity
import com.natijeh.data.model.MatchEvent
import com.natijeh.data.model.LeagueEntity
import com.natijeh.data.model.StandingRow
import com.natijeh.data.model.ScorerRow
import com.natijeh.data.model.FixtureRound
import com.natijeh.data.model.NewsEntity
import com.natijeh.data.model.PlayerEntity
import com.natijeh.data.model.StatItem
import com.natijeh.data.model.TeamEntity
import com.natijeh.data.util.MatchAlertFormatter
import com.natijeh.ui.theme.LiveRed
import com.natijeh.ui.theme.PulseDot
import com.natijeh.ui.theme.natijehCardElevation
import com.natijeh.ui.theme.shimmer
import com.natijeh.ui.viewmodel.SportsViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dashboardMoshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
private val dashboardEventAdapter = dashboardMoshi.adapter<List<MatchEvent>>(Types.newParameterizedType(List::class.java, MatchEvent::class.java))
private val dashboardStatAdapter = dashboardMoshi.adapter<List<StatItem>>(Types.newParameterizedType(List::class.java, StatItem::class.java))
private val dashboardStandingsAdapter = dashboardMoshi.adapter<List<StandingRow>>(Types.newParameterizedType(List::class.java, StandingRow::class.java))
private val dashboardScorersAdapter = dashboardMoshi.adapter<List<ScorerRow>>(Types.newParameterizedType(List::class.java, ScorerRow::class.java))
private val dashboardFixturesAdapter = dashboardMoshi.adapter<List<FixtureRound>>(Types.newParameterizedType(List::class.java, FixtureRound::class.java))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    sportsViewModel: SportsViewModel,
    openLiveTab: Boolean = true,
    compactCards: Boolean = true,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
    onNavigateToLeagueTab: (String, String) -> Unit = { id, _ -> onNavigateToLeague(id) },
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    var activeTab by remember { mutableStateOf("today") }
    var moreSection by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val isRefreshing by sportsViewModel.isRefreshing.collectAsStateWithLifecycle()
    val errorMessage by sportsViewModel.errorMessage.collectAsStateWithLifecycle()
    val liveMatches by sportsViewModel.liveMatches.collectAsStateWithLifecycle()
    var openedLiveTab by remember { mutableStateOf(false) }
    LaunchedEffect(liveMatches, openLiveTab) {
        if (openLiveTab && !openedLiveTab && liveMatches.isNotEmpty()) {
            activeTab = "live"
            openedLiveTab = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(R.drawable.ic_natijeh_mark)
                                    .build(),
                                contentDescription = "لوگو نتیجه",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column {
                            Text(
                                text = "نتیجه",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "جستجو",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "تنظیمات",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
                NavigationBarItem(
                    selected = activeTab == "today",
                    onClick = { activeTab = "today"; showSearch = false },
                    icon = { SlimTabIcon(Icons.Default.Home, activeTab == "today") },
                    label = { Text("بازی‌ها", style = MaterialTheme.typography.labelSmall) },
                    colors = itemColors,
                    modifier = Modifier.testTag("today_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "live",
                    onClick = { activeTab = "live"; showSearch = false },
                    icon = { SlimTabIcon(Icons.Default.PlayArrow, activeTab == "live") },
                    label = { Text("زنده", style = MaterialTheme.typography.labelSmall) },
                    colors = itemColors,
                    modifier = Modifier.testTag("live_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "leagues",
                    onClick = { activeTab = "leagues"; showSearch = false },
                    icon = { SlimTabIcon(Icons.Default.EmojiEvents, activeTab == "leagues") },
                    label = { Text("لیگ‌ها", style = MaterialTheme.typography.labelSmall) },
                    colors = itemColors
                )
                NavigationBarItem(
                    selected = activeTab == "favorites",
                    onClick = { activeTab = "favorites"; showSearch = false },
                    icon = { SlimTabIcon(Icons.Default.Favorite, activeTab == "favorites") },
                    label = { Text("محبوب‌ها", style = MaterialTheme.typography.labelSmall) },
                    colors = itemColors
                )
                NavigationBarItem(
                    selected = activeTab == "more",
                    onClick = {
                        if (activeTab == "more") moreSection = null else activeTab = "more"
                        showSearch = false
                    },
                    icon = { SlimTabIcon(Icons.Default.MoreHoriz, activeTab == "more") },
                    label = { Text("بیشتر", style = MaterialTheme.typography.labelSmall) },
                    colors = itemColors,
                    modifier = Modifier.testTag("more_tab")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = LiveRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            AnimatedVisibility(
                visible = showSearch,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("نام تیم، لیگ یا مربی را جستجو کنید...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "جستجو", tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "پاک کردن", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            if (showSearch) {
                SearchResultsContent(
                    query = searchQuery,
                    sportsViewModel = sportsViewModel,
                    onNavigateToMatch = onNavigateToMatch,
                    onNavigateToTeam = onNavigateToTeam,
                    onNavigateToLeague = onNavigateToLeague,
                    onNavigateToPlayer = onNavigateToPlayer
                )
            } else {
                when (activeTab) {
                    "today" -> TodayTabContent(sportsViewModel, onNavigateToMatch, onNavigateToTeam, onNavigateToLeague, onNavigateToPlayer, isRefreshing, compactCards)
                    "live" -> LiveTabContent(sportsViewModel, onNavigateToMatch, onNavigateToTeam, onNavigateToLeague, isRefreshing, compactCards)
                    "leagues" -> LeaguesTabContent(sportsViewModel, onNavigateToLeague, onNavigateToLeagueTab)
                    "favorites" -> FavoritesTabContent(sportsViewModel, onNavigateToMatch, onNavigateToTeam, onNavigateToLeague, onNavigateToPlayer)
                    else -> MoreTabContent(
                        moreSection = moreSection,
                        onSelectSection = { moreSection = it },
                        sportsViewModel = sportsViewModel,
                        onNavigateToMatch = onNavigateToMatch,
                        onNavigateToTeam = onNavigateToTeam,
                        onNavigateToLeague = onNavigateToLeague,
                        onNavigateToPlayer = onNavigateToPlayer,
                        onNavigateToSettings = onNavigateToSettings
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTabContent(
    viewModel: SportsViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    isRefreshing: Boolean,
    compactCards: Boolean = true
) {
    val selectedOffset by viewModel.selectedOffset.collectAsStateWithLifecycle()
    val matches by viewModel.matchesForSelectedDate.collectAsStateWithLifecycle()
    val favoriteTeams by viewModel.favoriteTeams.collectAsStateWithLifecycle()
    val favoriteLeagues by viewModel.favoriteLeagues.collectAsStateWithLifecycle()
    val favoritePlayers by viewModel.favoritePlayers.collectAsStateWithLifecycle()
    val allLeagues by viewModel.allLeagues.collectAsStateWithLifecycle()
    val news by viewModel.newsFeed.collectAsStateWithLifecycle()
    var onlyFavorites by remember { mutableStateOf(false) }
    val liveCount = matches.count { it.status == "LIVE" }
    val finishedCount = matches.count { it.status == "FINISHED" }
    val favoriteTeamIds = favoriteTeams.map { it.id }.toSet()
    val favoriteLeagueIds = favoriteLeagues.map { it.id }.toSet()
    val visibleMatches = remember(matches, onlyFavorites, favoriteTeamIds, favoriteLeagueIds) {
        if (!onlyFavorites) matches else matches.filter { MatchAlertFormatter.isWatched(it, favoriteTeamIds, favoriteLeagueIds) }
    }
    val featured = remember(matches, favoriteTeamIds, favoriteLeagueIds) {
        matches.firstOrNull { it.status == "LIVE" && (it.homeTeamId in favoriteTeamIds || it.awayTeamId in favoriteTeamIds || it.leagueId in favoriteLeagueIds) }
            ?: matches.firstOrNull { it.status == "LIVE" }
            ?: matches.firstOrNull { it.status == "SCHEDULED" && (it.homeTeamId in favoriteTeamIds || it.awayTeamId in favoriteTeamIds || it.leagueId in favoriteLeagueIds) }
            ?: matches.firstOrNull { it.status == "SCHEDULED" }
            ?: matches.firstOrNull()
    }
    val grouped = remember(visibleMatches, featured) {
        SportsMapper.groupMatchesByLeague(visibleMatches.filterNot { it.id == featured?.id })
    }
    val leagueShortcuts = remember(favoriteLeagues, allLeagues) { (favoriteLeagues + allLeagues).distinctBy { it.id }.take(8) }

    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { viewModel.refresh() }, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(if (selectedOffset == 0) "فوتبال امروز" else "مسابقات این روز", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        Text("${matches.size} مسابقه · $liveCount زنده · $finishedCount پایان‌یافته", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    if (liveCount > 0) Surface(shape = RoundedCornerShape(12.dp), color = LiveRed.copy(alpha = 0.12f)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PulseDot(size = 7.dp)
                            Text("زنده", color = LiveRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.datesList) { (label, offset) ->
                        FilterChip(
                            selected = selectedOffset == offset,
                            onClick = { viewModel.selectDate(offset) },
                            label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
            featured?.let { match ->
                item(key = "featured-${match.id}") {
                    FeaturedHomeMatch(match, onNavigateToMatch, onNavigateToTeam, onNavigateToLeague) {
                        viewModel.toggleMatchFavorite(match.id, match.isFavorite)
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    HomeSectionTitle("مسابقات", "برنامه و نتایج")
                    FilterChip(selected = onlyFavorites, onClick = { onlyFavorites = !onlyFavorites }, label = { Text("محبوب‌ها") })
                }
            }
            if (visibleMatches.isEmpty()) {
                item { EmptyState(if (onlyFavorites) "مسابقه‌ای از محبوب‌ها در این روز نیست." else "مسابقه‌ای برای این روز پیدا نشد.") }
            } else {
                grouped.forEach { (league, leagueMatches) ->
                    item(key = "home-league-${league.id}-${league.name}") {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable(enabled = league.id.isNotBlank()) { onNavigateToLeague(league.id) }.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (league.logo.isNotBlank()) AsyncImage(league.logo, league.name, modifier = Modifier.size(20.dp))
                            Text(league.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                    items(leagueMatches, key = { "home-match-${it.id}" }) { match ->
                        MatchCard(
                            match = match,
                            showLeague = false,
                            compact = compactCards,
                            onClick = { onNavigateToMatch(match.id) },
                            onFavoriteToggle = { viewModel.toggleMatchFavorite(match.id, match.isFavorite) },
                            onHomeTeamClick = { onNavigateToTeam(match.homeTeamId) },
                            onAwayTeamClick = { onNavigateToTeam(match.awayTeamId) },
                            onLeagueClick = { onNavigateToLeague(match.leagueId) }
                        )
                    }
                }
            }
            if (leagueShortcuts.isNotEmpty()) {
                item { HomeSectionTitle("لیگ‌های منتخب", "دسترسی سریع") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(leagueShortcuts, key = { "shortcut-${it.id}" }) { league ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.width(104.dp).clickable { onNavigateToLeague(league.id) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AsyncImage(league.logo, league.name, modifier = Modifier.size(38.dp))
                                    Text(league.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            favoriteTeams.firstOrNull()?.let { team ->
                item { HomeSectionTitle("تیم محبوب", "نمای سریع باشگاه") }
                item { FavoriteTeamHomeCard(team, onNavigateToTeam) }
            }
            if (favoritePlayers.isNotEmpty()) {
                item { HomeSectionTitle("بازیکنان محبوب", "عملکرد فصل") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(favoritePlayers.take(8), key = { "home-player-${it.id}" }) { player ->
                            FavoritePlayerHomeCard(player) { onNavigateToPlayer(player.id) }
                        }
                    }
                }
            }
            if (news.isNotEmpty()) {
                item { HomeSectionTitle("تازه‌ترین خبرها", "خلاصه کوتاه") }
                items(news.distinctBy { it.title }.take(3), key = { "home-news-${it.id}" }) { article ->
                    HomeNewsCard(article)
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun HomeSectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun FeaturedHomeMatch(
    match: MatchEntity,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val live = match.status == "LIVE"
    val score = if (match.status == "SCHEDULED") match.time.ifBlank { "– : –" } else "${match.homeScore}  -  ${match.awayScore}"
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().border(1.dp, if (live) LiveRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)).clickable { onNavigateToMatch(match.id) }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(
                Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), Color.Transparent))
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(
                        modifier = Modifier.clickable(enabled = match.leagueId.isNotBlank()) { onNavigateToLeague(match.leagueId) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (match.leagueLogo.isNotBlank()) AsyncImage(match.leagueLogo, match.leagueName, modifier = Modifier.size(22.dp))
                        Text(match.leagueName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (live) {
                            PulseDot(size = 7.dp)
                            Text(match.liveTime.ifBlank { "زنده" }, color = LiveRed, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        } else {
                            Text(match.date, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(40.dp)) {
                            Icon(if (match.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "محبوب", tint = if (match.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HomeFeaturedTeam(match.homeTeamName, match.homeTeamLogo, Modifier.weight(1f)) { onNavigateToTeam(match.homeTeamId) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(score, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = if (live) LiveRed else MaterialTheme.colorScheme.onSurface)
                        Text(if (match.status == "SCHEDULED") "ساعت شروع" else match.statusTitle.ifBlank { if (live) "در جریان" else "پایان" }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                    HomeFeaturedTeam(match.awayTeamName, match.awayTeamLogo, Modifier.weight(1f)) { onNavigateToTeam(match.awayTeamId) }
                }
            }
        }
    }
}

@Composable
private fun HomeFeaturedTeam(name: String, logo: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background, modifier = Modifier.size(62.dp)) {
            AsyncImage(logo, name, modifier = Modifier.padding(9.dp).fillMaxSize())
        }
        Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FavoriteTeamHomeCard(team: TeamEntity, onNavigateToTeam: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable { onNavigateToTeam(team.id) }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background, modifier = Modifier.size(58.dp)) {
                AsyncImage(team.logo, team.name, modifier = Modifier.padding(8.dp).fillMaxSize())
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(team.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text(team.leagueName.ifBlank { "باشگاه محبوب شما" }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeTeamMetric(if (team.rank > 0) "${team.rank}" else "–", "رتبه")
                HomeTeamMetric("${team.points}", "امتیاز")
                HomeTeamMetric("${team.won}", "برد")
            }
        }
    }
}

@Composable
private fun HomeTeamMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun FavoritePlayerHomeCard(player: PlayerEntity, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.width(132.dp).clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(58.dp)) {
                AsyncImage(player.portrait.ifBlank { player.teamLogo }, player.name, contentScale = ContentScale.Crop)
            }
            Text(player.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            Text(player.teamName, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            Text("${player.goals} گل", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun HomeNewsCard(article: NewsEntity) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (article.imageUrl.isNotBlank()) {
                AsyncImage(article.imageUrl, article.title, contentScale = ContentScale.Crop, modifier = Modifier.size(76.dp).clip(RoundedCornerShape(13.dp)))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(article.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("${article.source} · ${article.date}", maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTabContent(
    viewModel: SportsViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
    isRefreshing: Boolean,
    compactCards: Boolean = true
) {
    val darkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val liveCanvas = if (darkMode) Color(0xFF070809) else MaterialTheme.colorScheme.background
    val liveMatches by viewModel.liveMatches.collectAsStateWithLifecycle()
    val allMatches by viewModel.allMatches.collectAsStateWithLifecycle()
    val favoriteTeams by viewModel.favoriteTeams.collectAsStateWithLifecycle()
    val favoriteLeagues by viewModel.favoriteLeagues.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf("all") }
    var collapsedLeagues by remember { mutableStateOf(setOf<String>()) }
    val favoriteTeamIds = favoriteTeams.map { it.id }.toSet()
    val favoriteLeagueIds = favoriteLeagues.map { it.id }.toSet()
    val eventMap = remember(liveMatches) {
        liveMatches.associate { match -> match.id to runCatching { dashboardEventAdapter.fromJson(match.eventsJson).orEmpty() }.getOrDefault(emptyList()) }
    }
    val visible = remember(liveMatches, filter, favoriteTeamIds, favoriteLeagueIds, eventMap) {
        liveMatches.filter { match ->
            when (filter) {
                "favorites" -> MatchAlertFormatter.isWatched(match, favoriteTeamIds, favoriteLeagueIds)
                "goals" -> eventMap[match.id].orEmpty().any { it.type == "GOAL" || it.type == "PENALTY" }
                "first" -> match.minute in 1..45 || match.statusTitle.contains("نیمه اول")
                "second" -> match.minute in 46..90 || match.statusTitle.contains("نیمه دوم")
                "extra" -> match.minute > 90 || match.statusTitle.contains("اضافه")
                else -> true
            }
        }
    }
    val featured = remember(liveMatches, favoriteTeamIds, favoriteLeagueIds) {
        liveMatches.firstOrNull { it.homeTeamId in favoriteTeamIds || it.awayTeamId in favoriteTeamIds }
            ?: liveMatches.firstOrNull { it.leagueId in favoriteLeagueIds }
            ?: liveMatches.firstOrNull()
    }
    val latestEvents = remember(liveMatches, eventMap) {
        liveMatches.flatMap { match -> eventMap[match.id].orEmpty().map { match to it } }
            .filter { (_, event) -> event.type in setOf("GOAL", "PENALTY", "CARD_RED", "VAR_REVIEW") }
            .sortedByDescending { (_, event) -> event.minute }
            .take(10)
    }
    val grouped = remember(visible, featured) { SportsMapper.groupMatchesByLeague(visible.filterNot { it.id == featured?.id }) }
    val nextMatch = remember(allMatches) { allMatches.filter { it.status == "SCHEDULED" }.minByOrNull { it.utcStart.ifBlank { it.time } } }
    val lastUpdated = liveMatches.maxOfOrNull { it.lastUpdatedMillis }?.takeIf { it > 0 }
        ?.let { SimpleDateFormat("HH:mm", Locale("fa")).format(Date(it)) } ?: "—"

    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { viewModel.refresh() }, modifier = Modifier.fillMaxSize().background(liveCanvas)) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PulseDot(size = 10.dp)
                            Text("زنده", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        }
                        Text("بروزرسانی خودکار · آخرین دریافت $lastUpdated", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(shape = CircleShape, color = if (darkMode) Color(0xFF191A1D) else MaterialTheme.colorScheme.surface) {
                            Text("${liveMatches.size}", modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                        }
                        IconButton(onClick = { viewModel.refresh() }, enabled = !isRefreshing) {
                            if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.Refresh, contentDescription = "بروزرسانی زنده")
                        }
                    }
                }
            }
            featured?.let { match ->
                item(key = "live-featured-${match.id}") {
                    FeaturedLiveMatch(match, eventMap[match.id].orEmpty(), onNavigateToMatch, onNavigateToTeam, onNavigateToLeague) {
                        viewModel.toggleMatchFavorite(match.id, match.isFavorite)
                    }
                }
            }
            if (latestEvents.isNotEmpty()) {
                item { HomeSectionTitle("اتفاقات فوری", "آخرین لحظه‌های مهم") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(latestEvents, key = { "ticker-${it.first.id}-${it.second.hashCode()}" }) { (match, event) ->
                            LiveEventChip(match, event) { onNavigateToMatch(match.id) }
                        }
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("all" to "همه", "favorites" to "محبوب‌ها", "goals" to "دارای گل", "first" to "نیمه اول", "second" to "نیمه دوم", "extra" to "وقت اضافه")) { (key, label) ->
                        FilterChip(
                            selected = filter == key,
                            onClick = { filter = key },
                            label = { Text(label, fontWeight = if (filter == key) FontWeight.Bold else FontWeight.Medium) },
                            shape = CircleShape,
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filter == key,
                                borderColor = if (darkMode) Color(0xFF26282C) else MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = Color.Transparent
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (darkMode) Color(0xFF101114) else MaterialTheme.colorScheme.surface,
                                selectedContainerColor = MaterialTheme.colorScheme.onBackground,
                                selectedLabelColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                }
            }
            if (liveMatches.isEmpty()) {
                item { LiveEmptyState(nextMatch, onNavigateToMatch) }
            } else if (visible.isEmpty()) {
                item { EmptyState("مسابقه‌ای با این فیلتر پیدا نشد.") }
            } else {
                grouped.forEach { (league, leagueMatches) ->
                    val collapseKey = league.id.ifBlank { league.name }
                    item(key = "live-league-$collapseKey") {
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable {
                                collapsedLeagues = if (collapseKey in collapsedLeagues) collapsedLeagues - collapseKey else collapsedLeagues + collapseKey
                            }.padding(horizontal = 4.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            if (league.logo.isNotBlank()) AsyncImage(league.logo, league.name, modifier = Modifier.size(22.dp))
                            Text(league.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Surface(shape = CircleShape, color = LiveRed.copy(alpha = 0.12f)) {
                                Text("${leagueMatches.size} زنده", modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp), color = LiveRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Text(if (collapseKey in collapsedLeagues) "+" else "−", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    if (collapseKey !in collapsedLeagues) {
                        items(leagueMatches, key = { "live-panel-${it.id}" }) { match ->
                            LiveMatchPanel(
                                match = match,
                                events = eventMap[match.id].orEmpty(),
                                compact = compactCards,
                                onMatch = { onNavigateToMatch(match.id) },
                                onHomeTeam = { onNavigateToTeam(match.homeTeamId) },
                                onAwayTeam = { onNavigateToTeam(match.awayTeamId) },
                                onLeague = { onNavigateToLeague(match.leagueId) },
                                onNotify = { viewModel.toggleMatchFavorite(match.id, match.isFavorite) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedLiveMatch(
    match: MatchEntity,
    events: List<MatchEvent>,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
    onNotify: () -> Unit
) {
    val latestEvent = events.lastOrNull()
    val darkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardColor = if (darkMode) Color(0xFF111215) else MaterialTheme.colorScheme.surface
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.fillMaxWidth().border(1.dp, if (darkMode) Color(0xFF292B30) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(26.dp)).clickable { onNavigateToMatch(match.id) }
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(if (darkMode) Color(0xFF18191D) else MaterialTheme.colorScheme.surface, cardColor))).padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.clickable { onNavigateToLeague(match.leagueId) }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (match.leagueLogo.isNotBlank()) AsyncImage(match.leagueLogo, match.leagueName, modifier = Modifier.size(22.dp))
                    Text(match.leagueName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PulseDot(size = 8.dp)
                    Surface(shape = CircleShape, color = LiveRed) {
                        Text(match.liveTime.ifBlank { "${match.minute}'" }.ifBlank { "زنده" }, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                    }
                    IconButton(onClick = onNotify, modifier = Modifier.size(38.dp)) {
                        Icon(if (match.isFavorite) Icons.Outlined.Notifications else Icons.Outlined.Notifications, "اعلان مسابقه", tint = if (match.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HomeFeaturedTeam(match.homeTeamName, match.homeTeamLogo, Modifier.weight(1f)) { onNavigateToTeam(match.homeTeamId) }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    AnimatedContent(targetState = "${match.homeScore} - ${match.awayScore}", label = "live-score") { score ->
                        Text(score, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                    }
                    Text(match.statusTitle.ifBlank { "در جریان" }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
                HomeFeaturedTeam(match.awayTeamName, match.awayTeamLogo, Modifier.weight(1f)) { onNavigateToTeam(match.awayTeamId) }
            }
            latestEvent?.let { event ->
                Surface(shape = RoundedCornerShape(16.dp), color = if (darkMode) Color(0xFF090A0C) else MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text(liveEventIcon(event.type), style = MaterialTheme.typography.titleMedium)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(liveEventTitle(event), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(if (event.isHome) match.homeTeamName else match.awayTeamName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                        Text("${event.minute}'", color = LiveRed, fontWeight = FontWeight.Black)
                    }
                }
            }
            if (events.isNotEmpty()) LiveEventTimeline(events, match.minute)
        }
    }
}

@Composable
private fun LiveEventChip(match: MatchEntity, event: MatchEvent, onClick: () -> Unit) {
    val darkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(shape = RoundedCornerShape(18.dp), color = if (darkMode) Color(0xFF111215) else MaterialTheme.colorScheme.surface, modifier = Modifier.width(200.dp).border(1.dp, if (darkMode) Color(0xFF24262A) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)).clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(liveEventIcon(event.type), style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.weight(1f)) {
                Text(liveEventTitle(event), maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text("${match.homeTeamName} - ${match.awayTeamName}", maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
            Text("${event.minute}'", color = LiveRed, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun LiveMatchPanel(
    match: MatchEntity,
    events: List<MatchEvent>,
    compact: Boolean,
    onMatch: () -> Unit,
    onHomeTeam: () -> Unit,
    onAwayTeam: () -> Unit,
    onLeague: () -> Unit,
    onNotify: () -> Unit
) {
    val stats = remember(match.statsJson) { runCatching { dashboardStatAdapter.fromJson(match.statsJson).orEmpty() }.getOrDefault(emptyList()) }
    val quickStats = stats.filter { stat -> stat.title.contains("مالکیت") || stat.title.contains("شوت") }.take(2)
    val darkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val panelColor = if (darkMode) Color(0xFF101114) else MaterialTheme.colorScheme.surface
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = panelColor), modifier = Modifier.fillMaxWidth().border(1.dp, if (darkMode) Color(0xFF24262A) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))) {
        Column {
            MatchCard(match, onMatch, onNotify, onHomeTeam, onAwayTeam, onLeague, showLeague = false, compact = compact)
            val latest = events.lastOrNull()
            if (latest != null || quickStats.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().background(if (darkMode) Color(0xFF0B0C0E) else MaterialTheme.colorScheme.background.copy(alpha = 0.55f)).padding(horizontal = 14.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    latest?.let {
                        Text("آخرین اتفاق: ${liveEventTitle(it)} · ${it.minute}'", maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (it.type == "CARD_RED") LiveRed else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    }
                    quickStats.forEach { stat ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stat.home, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Text(stat.title, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            Text(stat.away, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveEventTimeline(events: List<MatchEvent>, currentMinute: Int) {
    val important = events.filter { it.type in setOf("GOAL", "PENALTY", "CARD_RED", "CARD_YELLOW", "VAR_REVIEW") }.takeLast(7)
    if (important.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxWidth((currentMinute.coerceIn(0, 120) / 120f).coerceAtLeast(0.02f)).height(4.dp).background(LiveRed))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            important.forEach { event ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(liveEventIcon(event.type), style = MaterialTheme.typography.labelMedium)
                    Text("${event.minute}'", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun LiveEmptyState(nextMatch: MatchEntity?, onNavigateToMatch: (String) -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(64.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.SportsSoccer, null, modifier = Modifier.size(30.dp)) }
            }
            Text("فعلاً مسابقه‌ای زنده نیست", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            nextMatch?.let { match ->
                Text("نزدیک‌ترین بازی", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth().clickable { onNavigateToMatch(match.id) }) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${match.homeTeamName} - ${match.awayTeamName}", modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                        Text(match.time.ifBlank { match.date }, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

private fun liveEventIcon(type: String): String = when (type) {
    "GOAL", "PENALTY" -> "⚽"
    "CARD_RED" -> "🟥"
    "CARD_YELLOW" -> "🟨"
    "VAR_REVIEW" -> "VAR"
    else -> "•"
}

private fun liveEventTitle(event: MatchEvent): String = when (event.type) {
    "GOAL" -> "گل ${event.playerName}"
    "PENALTY" -> "پنالتی ${event.playerName}"
    "CARD_RED" -> "کارت قرمز ${event.playerName}"
    "CARD_YELLOW" -> "کارت زرد ${event.playerName}"
    "VAR_REVIEW" -> "بررسی VAR"
    "SUBSTITUTION" -> "تعویض ${event.playerName}"
    else -> event.detail.ifBlank { "اتفاق مسابقه" }
}

@Composable
private fun LiveMetric(value: String, label: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun LeaguesTabContent(viewModel: SportsViewModel, onNavigateToLeague: (String) -> Unit, onNavigateToLeagueTab: (String, String) -> Unit = { id, _ -> onNavigateToLeague(id) }) {
    val leagues by viewModel.allLeagues.collectAsStateWithLifecycle()
    val liveMatches by viewModel.liveMatches.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var countryFilter by rememberSaveable { mutableStateOf("همه") }
    var recentIds by rememberSaveable { mutableStateOf(listOf<String>()) }
    var collapsedGroups by rememberSaveable { mutableStateOf(listOf<String>()) }
    val darkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f
    if (leagues.isEmpty()) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(5) {
                Box(Modifier.fillMaxWidth().height(if (it == 0) 154.dp else 86.dp).clip(RoundedCornerShape(22.dp)).shimmer())
            }
        }
        return
    }
    val openLeague: (String) -> Unit = { id ->
        recentIds = (listOf(id) + recentIds.filterNot { it == id }).take(5)
        onNavigateToLeague(id)
    }
    val countries = remember(leagues) { listOf("همه") + leagues.map { it.country.ifBlank { "سایر" } }.distinct().sorted() }
    val filtered = remember(leagues, query, countryFilter) {
        leagues.filter {
            (query.isBlank() || it.name.contains(query, true) || it.country.contains(query, true)) &&
                (countryFilter == "همه" || it.country.ifBlank { "سایر" } == countryFilter)
        }.sortedByDescending { it.isFavorite }
    }
    val grouped = remember(filtered) { filtered.groupBy { leagueRegion(it.country) } }
    val favoriteLeague = leagues.firstOrNull { it.isFavorite }
    val liveCounts = remember(liveMatches) { liveMatches.groupingBy { it.leagueId }.eachCount() }
    val recent = remember(leagues, recentIds) { recentIds.mapNotNull { id -> leagues.firstOrNull { it.id == id } } }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(if (darkMode) Color(0xFF070809) else MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("رقابت‌ها", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text("لیگ‌ها، جدول‌ها و برنامه مسابقات", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("جستجوی لیگ یا کشور…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { if (query.isNotBlank()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Clear, "پاک‌کردن") } },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (darkMode) Color(0xFF111215) else MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = if (darkMode) Color(0xFF111215) else MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = if (darkMode) Color(0xFF25272B) else MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
        favoriteLeague?.let { league ->
            item { FeaturedLeagueCard(league, liveCounts[league.id] ?: 0, { openLeague(league.id) }) { viewModel.toggleLeagueFavorite(league.id, league.isFavorite) } }
        }
        val hot = leagues.filter { (liveCounts[it.id] ?: 0) > 0 }.sortedByDescending { liveCounts[it.id] }.take(5)
        if (hot.isNotEmpty()) {
            item { HomeSectionTitle("لیگ‌های داغ", "رقابت‌های دارای بازی زنده") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(hot, key = { "hot-${it.id}" }) { league ->
                        HotLeagueCard(league, liveCounts[league.id] ?: 0) { openLeague(league.id) }
                    }
                }
            }
        }
        if (recent.isNotEmpty()) {
            item { HomeSectionTitle("اخیراً دیده‌شده", "دسترسی سریع") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(recent, key = { "recent-${it.id}" }) { league ->
                        LeagueLogoShortcut(league) { openLeague(league.id) }
                    }
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(countries) { country ->
                    FilterChip(
                        selected = countryFilter == country,
                        onClick = { countryFilter = country },
                        label = { Text(country) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.onBackground, selectedLabelColor = MaterialTheme.colorScheme.background)
                    )
                }
            }
        }
        if (filtered.isEmpty()) {
            item { EmptyState("لیگی با این نام یا کشور پیدا نشد.") }
        } else grouped.forEach { (region, regionLeagues) ->
            item(key = "region-$region") {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable {
                        collapsedGroups = if (region in collapsedGroups) collapsedGroups - region else collapsedGroups + region
                    }.padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(region, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text("${regionLeagues.size} لیگ", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    Text(if (region in collapsedGroups) "+" else "−", modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.titleLarge)
                }
            }
            if (region !in collapsedGroups) items(regionLeagues, key = { it.id }) { league ->
                LeagueHubCard(
                    league = league,
                    liveCount = liveCounts[league.id] ?: 0,
                    onOpen = { openLeague(league.id) },
                    onOpenTab = { tab ->
                        recentIds = (listOf(league.id) + recentIds.filterNot { it == league.id }).take(5)
                        onNavigateToLeagueTab(league.id, tab)
                    },
                    onFavorite = { viewModel.toggleLeagueFavorite(league.id, league.isFavorite) }
                )
            }
        }
    }
}

private fun leagueRegion(country: String): String = when {
    country.contains("ایران") -> "ایران"
    listOf("انگل", "اسپان", "ایتال", "آلمان", "فرانس", "پرتغال", "هلند", "اروپا").any { country.contains(it, true) } -> "اروپا"
    listOf("آسیا", "عرب", "قطر", "امارات", "ژاپن", "کره", "چین").any { country.contains(it, true) } -> "آسیا"
    country.contains("ملی") || country.contains("جهان") -> "ملی و بین‌المللی"
    else -> "سایر رقابت‌ها"
}

@Composable
private fun FeaturedLeagueCard(league: LeagueEntity, liveCount: Int, onOpen: () -> Unit, onFavorite: () -> Unit) {
    val standings = remember(league.standingsJson) { runCatching { dashboardStandingsAdapter.fromJson(league.standingsJson).orEmpty() }.getOrDefault(emptyList()) }
    val leader = standings.minByOrNull { it.rank }
    Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(26.dp)).clickable(onClick = onOpen)) {
        Column(modifier = Modifier.background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.13f), Color.Transparent))).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background, modifier = Modifier.size(68.dp)) { AsyncImage(league.logo, league.name, modifier = Modifier.padding(10.dp)) }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                    Text("لیگ محبوب", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    Text(league.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(league.country.ifBlank { "رقابت بین‌المللی" }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onFavorite) { Icon(Icons.Default.Favorite, "حذف از محبوب‌ها") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                leader?.let { LeagueInfoPill("صدرنشین", "${it.teamName} · ${it.points}", Modifier.weight(1f)) }
                if (liveCount > 0) LeagueInfoPill("اکنون", "$liveCount بازی زنده", Modifier.weight(1f), LiveRed)
            }
        }
    }
}

@Composable
private fun LeagueInfoPill(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Surface(shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.background, modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            Text(value, color = valueColor, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun HotLeagueCard(league: LeagueEntity, liveCount: Int, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.width(150.dp).clickable(onClick = onClick)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AsyncImage(league.logo, league.name, modifier = Modifier.size(42.dp))
            Text(league.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { PulseDot(size = 7.dp); Text("$liveCount بازی زنده", color = LiveRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun LeagueLogoShortcut(league: LeagueEntity, onClick: () -> Unit) {
    Column(modifier = Modifier.width(84.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(58.dp)) { AsyncImage(league.logo, league.name, modifier = Modifier.padding(9.dp)) }
        Text(league.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LeagueHubCard(league: LeagueEntity, liveCount: Int, onOpen: () -> Unit, onOpenTab: (String) -> Unit, onFavorite: () -> Unit) {
    val standings = remember(league.standingsJson) { runCatching { dashboardStandingsAdapter.fromJson(league.standingsJson).orEmpty() }.getOrDefault(emptyList()) }
    val scorers = remember(league.scorersJson) { runCatching { dashboardScorersAdapter.fromJson(league.scorersJson).orEmpty() }.getOrDefault(emptyList()) }
    val fixtures = remember(league.fixturesJson) { runCatching { dashboardFixturesAdapter.fromJson(league.fixturesJson).orEmpty() }.getOrDefault(emptyList()) }
    val leader = standings.minByOrNull { it.rank }
    val scorer = scorers.maxByOrNull { it.goals }
    val next = fixtures.firstOrNull()?.matches?.firstOrNull()
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)).clickable(onClick = onOpen)) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background, modifier = Modifier.size(52.dp)) { AsyncImage(league.logo, league.name, modifier = Modifier.padding(8.dp)) }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(league.name, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    Text(league.country.ifBlank { "سایر" }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
                if (liveCount > 0) Text("$liveCount زنده", color = LiveRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                IconButton(onClick = onFavorite) { Icon(if (league.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "محبوب", tint = if (league.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (leader != null || scorer != null || next != null) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    leader?.let { Text("صدرنشین  ${it.teamName} · ${it.points} امتیاز", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium) }
                    scorer?.let { Text("بهترین گلزن  ${it.name} · ${it.goals} گل", maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium) }
                    next?.let { Text("بازی بعدی  ${it.homeTeamName} - ${it.awayTeamName} · ${it.time}", maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("table" to "جدول", "scorers" to "گلزنان", "week" to "برنامه", "info" to "معرفی").forEach { (tab, label) ->
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background, modifier = Modifier.clickable { onOpenTab(tab) }) { Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
    }
}

@Composable
fun NewsTabContent(viewModel: SportsViewModel) {
    val news by viewModel.newsFeed.collectAsStateWithLifecycle()
    val favoriteTeams by viewModel.favoriteTeams.collectAsStateWithLifecycle()
    val favoritePlayers by viewModel.favoritePlayers.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshingNews.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val newsPrefs = remember { context.getSharedPreferences("natijeh_news", 0) }
    var selectedCategory by remember { mutableStateOf("همه") }
    var selectedArticle by remember { mutableStateOf<NewsEntity?>(null) }
    var savedArticles by remember { mutableStateOf(newsPrefs.getStringSet("saved_ids", emptySet()).orEmpty().toSet()) }
    var readerScale by remember { mutableStateOf(1f) }
    val filteredNews = remember(news, selectedCategory, savedArticles, favoriteTeams, favoritePlayers) {
        val uniqueNews = news.distinctBy { it.title.trim().lowercase() }
        val favoriteNames = (favoriteTeams.map { it.name } + favoritePlayers.map { it.name }).filter { it.length > 2 }
        when (selectedCategory) {
            "داخلی" -> uniqueNews.filter { it.source.contains("داخلی") }
            "خارجی" -> uniqueNews.filter { it.source.contains("خارجی") }
            "نقل‌وانتقالات" -> uniqueNews.filter { it.title.contains("انتقال") || it.summary.contains("انتقال") }
            "ذخیره‌شده" -> uniqueNews.filter { it.id in savedArticles }
            "برای من" -> uniqueNews.filter { article -> favoriteNames.any { name -> article.title.contains(name, true) || article.summary.contains(name, true) } }
            else -> uniqueNews
        }
    }

    selectedArticle?.let { article ->
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedArticle = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                    Text("متن خبر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            if (article.imageUrl.isNotBlank()) {
                item {
                    AsyncImage(
                        model = article.imageUrl,
                        contentDescription = article.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(230.dp)
                    )
                }
            }
            item {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(article.source, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(article.date, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                    Text(article.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = article.id in savedArticles,
                            onClick = {
                                savedArticles = if (article.id in savedArticles) savedArticles - article.id else savedArticles + article.id
                                newsPrefs.edit().putStringSet("saved_ids", savedArticles).apply()
                            },
                            label = { Text(if (article.id in savedArticles) "ذخیره شد" else "ذخیره خبر") }
                        )
                        FilterChip(selected = readerScale > 1f, onClick = { readerScale = if (readerScale >= 1.25f) 1f else readerScale + 0.125f }, label = { Text("اندازه متن A+") })
                    }
                    Text(
                        article.content.ifBlank { article.summary },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize * readerScale)
                    )
                    if (article.articleUrl.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().clickable {
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.articleUrl))) }
                            }
                        ) {
                            Text("مشاهده منبع اصلی", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().clickable {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.articleUrl}")
                            }
                            context.startActivity(Intent.createChooser(share, "اشتراک‌گذاری خبر"))
                        }
                    ) { Text("اشتراک‌گذاری خبر", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold) }
                    val related = news.filter { it.id != article.id }.take(3)
                    if (related.isNotEmpty()) {
                        Text("خبرهای مرتبط", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        related.forEach { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth().clickable { selectedArticle = item }
                            ) {
                                Text(item.title, modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("برای من", "همه", "داخلی", "خارجی", "نقل‌وانتقالات", "ذخیره‌شده")) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = when (category) {
                                    "داخلی" -> "فوتبال داخلی"
                                    "خارجی" -> "فوتبال خارجی"
                                    "نقل‌وانتقالات" -> "نقل‌وانتقالات"
                                    "ذخیره‌شده" -> "ذخیره‌شده"
                                    "برای من" -> "برای من"
                                    else -> "همه اخبار"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
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
            IconButton(onClick = { viewModel.refreshNews() }, modifier = Modifier.size(36.dp)) {
                if (isRefreshing) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "بروزرسانی اخبار", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
        }

        if (filteredNews.isEmpty()) {
            EmptyState(message = "هیچ خبری در این دسته یافت نشد.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val featured = filteredNews.first()
                item(key = "featured-${featured.id}") {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().clickable { selectedArticle = featured }
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                            AsyncImage(featured.imageUrl, featured.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f)))))
                            Column(Modifier.align(Alignment.BottomStart).padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Surface(shape = CircleShape, color = LiveRed) { Text("خبر ویژه", color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black) }
                                Text(featured.title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                Text("${featured.source} · ${featured.date}", color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = CircleShape, color = LiveRed.copy(alpha = 0.14f)) { Text("فوری", color = LiveRed, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) }
                        Text("تازه‌ترین خبرهای فوتبال", fontWeight = FontWeight.Black)
                    }
                }
                items(filteredNews.drop(1), key = { it.id }) { article ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedArticle = article }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (article.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = article.imageUrl,
                                    contentDescription = article.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(104.dp).clip(RoundedCornerShape(14.dp))
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = article.source, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text(text = article.date, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = article.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = article.summary, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesTabContent(
    viewModel: SportsViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit = {}
) {
    val matches by viewModel.favoriteMatches.collectAsStateWithLifecycle()
    val teams by viewModel.favoriteTeams.collectAsStateWithLifecycle()
    val leagues by viewModel.favoriteLeagues.collectAsStateWithLifecycle()
    val players by viewModel.favoritePlayers.collectAsStateWithLifecycle()
    val allMatches by viewModel.allMatches.collectAsStateWithLifecycle()
    var category by remember { mutableStateOf("all") }
    val teamIds = remember(teams) { teams.map { it.id }.toSet() }
    val leagueIds = remember(leagues) { leagues.map { it.id }.toSet() }
    val relatedMatches = remember(allMatches, matches, teamIds, leagueIds) {
        (matches + allMatches.filter { it.homeTeamId in teamIds || it.awayTeamId in teamIds || it.leagueId in leagueIds }).distinctBy { it.id }
    }
    val shownMatches = when (category) {
        "live" -> relatedMatches.filter { it.status == "LIVE" }
        "today" -> relatedMatches.filter { it.dayOffset == 0 }
        else -> relatedMatches
    }

    if (matches.isEmpty() && teams.isEmpty() && leagues.isEmpty() && players.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            EmptyState(message = "هنوز دنیای فوتبال خودت را نساخته‌ای.")
            Text("از جستجو، تیم‌ها، بازیکنان و لیگ‌های دلخواهت را انتخاب کن.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), Color.Transparent))).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("دنیای فوتبال من", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            FavoriteMetric(teams.size, "تیم")
                            FavoriteMetric(players.size, "بازیکن")
                            FavoriteMetric(leagues.size, "لیگ")
                            FavoriteMetric(matches.size, "بازی")
                        }
                    }
                }
            }
            val liveRelated = relatedMatches.filter { it.status == "LIVE" }
            if (liveRelated.isNotEmpty() && category == "all") {
                item { HomeSectionTitle("اکنون زنده", "مسابقات مرتبط با محبوب‌های شما") }
                items(liveRelated, key = { "fav-live-${it.id}" }) { match ->
                    MatchCard(match, { onNavigateToMatch(match.id) }, { viewModel.toggleMatchFavorite(match.id, match.isFavorite) }, { onNavigateToTeam(match.homeTeamId) }, { onNavigateToTeam(match.awayTeamId) }, { onNavigateToLeague(match.leagueId) })
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("all" to "همه", "live" to "زنده", "today" to "امروز", "matches" to "بازی‌ها", "teams" to "تیم‌ها", "players" to "بازیکنان", "leagues" to "لیگ‌ها")) { (key, label) ->
                        FilterChip(selected = category == key, onClick = { category = key }, label = { Text(label) }, shape = CircleShape)
                    }
                }
            }
            if (shownMatches.isNotEmpty() && category in listOf("all", "matches", "live", "today")) {
                item { Text("مسابقات محبوب", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(shownMatches, key = { "m${it.id}" }) { match ->
                    MatchCard(
                        match = match,
                        onClick = { onNavigateToMatch(match.id) },
                        onFavoriteToggle = { viewModel.toggleMatchFavorite(match.id, match.isFavorite) },
                        onHomeTeamClick = { onNavigateToTeam(match.homeTeamId) },
                        onAwayTeamClick = { onNavigateToTeam(match.awayTeamId) },
                        onLeagueClick = { onNavigateToLeague(match.leagueId) }
                    )
                }
            }
            if (teams.isNotEmpty() && category in listOf("all", "teams")) {
                item { Text("تیم‌های محبوب", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(teams, key = { "t${it.id}" }) { team ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTeam(team.id) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AsyncImage(model = team.logo, contentDescription = team.name, modifier = Modifier.size(32.dp))
                                Text(text = team.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { viewModel.toggleTeamFavorite(team.id, team.isFavorite) }) {
                                Icon(imageVector = Icons.Default.Favorite, contentDescription = "حذف", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
            if (leagues.isNotEmpty() && category in listOf("all", "leagues")) {
                item { Text("جام‌ها و لیگ‌های محبوب", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(leagues, key = { "l${it.id}" }) { league ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToLeague(league.id) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = league.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.toggleLeagueFavorite(league.id, league.isFavorite) }) {
                                Icon(imageVector = Icons.Default.Favorite, contentDescription = "حذف", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
            if (players.isNotEmpty() && category in listOf("all", "players")) {
                item { Text("بازیکنان محبوب", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(players, key = { "p${it.id}" }) { player ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToPlayer(player.id) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(48.dp)) {
                                AsyncImage(model = player.portrait, contentDescription = player.name, contentScale = ContentScale.Crop)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(player.name, fontWeight = FontWeight.Bold, maxLines = 1)
                                if (player.name.contains("مسی", true) || player.name.contains("Messi", true)) {
                                    Text("GOAT · LEGEND", color = com.natijeh.ui.theme.RankGold, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                                }
                                Text(
                                    listOf(player.position, player.teamName).filter { it.isNotBlank() }.joinToString(" · "),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { viewModel.togglePlayerFavorite(player.id, player.isFavorite) }) {
                                Icon(Icons.Default.Favorite, contentDescription = "حذف از محبوب‌ها", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteMetric(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun SearchResultsContent(
    query: String,
    sportsViewModel: SportsViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit
) {
    val allMatches by sportsViewModel.allMatches.collectAsStateWithLifecycle()
    val leagues by sportsViewModel.allLeagues.collectAsStateWithLifecycle()
    val teams by sportsViewModel.allTeams.collectAsStateWithLifecycle()
    val players by sportsViewModel.allPlayers.collectAsStateWithLifecycle()
    val favoriteTeams by sportsViewModel.favoriteTeams.collectAsStateWithLifecycle()
    val favoritePlayers by sportsViewModel.favoritePlayers.collectAsStateWithLifecycle()
    var category by remember(query) { mutableStateOf("all") }
    val needle = query.trim()

    if (needle.isBlank()) {
        val today = allMatches.filter { it.dayOffset == 0 }.sortedBy { it.time }.take(4)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("کشف فوتبال", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        Text("نام فارسی یا انگلیسی تیم، بازیکن، لیگ و مسابقه را وارد کنید.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (favoriteTeams.isNotEmpty()) {
                item { HomeSectionTitle("تیم‌های شما", "دسترسی سریع") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(favoriteTeams, key = { "discover-${it.id}" }) { team ->
                            Card(modifier = Modifier.width(132.dp).clickable { onNavigateToTeam(team.id) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
                                Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AsyncImage(team.logo, team.name, modifier = Modifier.size(48.dp))
                                    Text(team.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            if (favoritePlayers.isNotEmpty()) {
                item { HomeSectionTitle("بازیکنان محبوب", "پروفایل و آمار") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(favoritePlayers, key = { "discover-player-${it.id}" }) { player ->
                            Card(modifier = Modifier.width(150.dp).clickable { onNavigateToPlayer(player.id) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
                                Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    AsyncImage(player.portrait, player.name, contentScale = ContentScale.Crop, modifier = Modifier.size(54.dp).clip(CircleShape))
                                    Text(player.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                                    Text(player.teamName, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
            if (today.isNotEmpty()) {
                item { HomeSectionTitle("مسابقات مهم امروز", "برنامه امروز") }
                items(today, key = { "discover-match-${it.id}" }) { match ->
                    MatchCard(match, { onNavigateToMatch(match.id) }, { sportsViewModel.toggleMatchFavorite(match.id, match.isFavorite) }, { onNavigateToTeam(match.homeTeamId) }, { onNavigateToTeam(match.awayTeamId) }, { onNavigateToLeague(match.leagueId) })
                }
            }
        }
        return
    }

    val filteredMatches = allMatches.filter {
        it.homeTeamName.contains(needle, ignoreCase = true) ||
            it.awayTeamName.contains(needle, ignoreCase = true) ||
            it.leagueName.contains(needle, ignoreCase = true) ||
            it.referee.contains(needle, ignoreCase = true)
    }
    val filteredLeagues = leagues.filter { it.name.contains(needle, ignoreCase = true) }
    val filteredTeams = teams.filter {
        it.name.contains(needle, ignoreCase = true) || it.coach.contains(needle, ignoreCase = true)
    }
    val filteredPlayers = players.filter {
        it.name.contains(needle, ignoreCase = true) ||
            it.teamName.contains(needle, ignoreCase = true) ||
            it.position.contains(needle, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("all" to "همه", "matches" to "بازی", "teams" to "تیم", "leagues" to "لیگ", "players" to "بازیکن")) { (key, label) ->
                    FilterChip(selected = category == key, onClick = { category = key }, label = { Text(label) })
                }
            }
        }
        if (filteredMatches.isNotEmpty() && category in listOf("all", "matches")) {
            item { Text("مسابقات یافت شده", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(filteredMatches, key = { it.id }) { match ->
                MatchCard(
                    match = match,
                    onClick = { onNavigateToMatch(match.id) },
                    onFavoriteToggle = { sportsViewModel.toggleMatchFavorite(match.id, match.isFavorite) },
                    onHomeTeamClick = { onNavigateToTeam(match.homeTeamId) },
                    onAwayTeamClick = { onNavigateToTeam(match.awayTeamId) },
                    onLeagueClick = { onNavigateToLeague(match.leagueId) }
                )
            }
        }
        if (filteredTeams.isNotEmpty() && category in listOf("all", "teams")) {
            item { Text("تیم‌های یافت شده", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(filteredTeams, key = { it.id }) { team ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToTeam(team.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AsyncImage(model = team.logo, contentDescription = team.name, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(team.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                            if (team.coach.isNotBlank()) {
                                Text(team.coach, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        IconButton(onClick = { sportsViewModel.toggleTeamFavorite(team.id, team.isFavorite) }) {
                            Icon(if (team.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "محبوب")
                        }
                    }
                }
            }
        }
        if (filteredLeagues.isNotEmpty() && category in listOf("all", "leagues")) {
            item { Text("لیگ‌های یافت شده", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(filteredLeagues, key = { it.id }) { league ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToLeague(league.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = league.logo, contentDescription = league.name, modifier = Modifier.size(28.dp))
                        Text(league.name, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f).padding(horizontal = 12.dp), style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { sportsViewModel.toggleLeagueFavorite(league.id, league.isFavorite) }) {
                            Icon(if (league.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "محبوب")
                        }
                    }
                }
            }
        }
        if (filteredPlayers.isNotEmpty() && category in listOf("all", "players")) {
            item { Text("بازیکنان یافت شده", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(filteredPlayers, key = { "search-player-${it.id}" }) { player ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToPlayer(player.id) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(42.dp)) {
                            AsyncImage(model = player.portrait, contentDescription = player.name, contentScale = ContentScale.Crop)
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(player.name, fontWeight = FontWeight.Bold)
                            Text(listOf(player.position, player.teamName).filter { it.isNotBlank() }.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { sportsViewModel.togglePlayerFavorite(player.id, player.isFavorite) }) {
                            Icon(if (player.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "محبوب")
                        }
                    }
                }
            }
        }
        if (filteredMatches.isEmpty() && filteredLeagues.isEmpty() && filteredTeams.isEmpty() && filteredPlayers.isEmpty()) {
            item { EmptyState(message = "موردی با جستجوی شما یافت نشد.") }
        }
    }
}

@Composable
private fun SlimTabIcon(icon: ImageVector, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(imageVector = icon, contentDescription = null)
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
        )
    }
}

@Composable
private fun MoreTabContent(
    moreSection: String?,
    onSelectSection: (String?) -> Unit,
    sportsViewModel: SportsViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    when (moreSection) {
        "leagues" -> MoreSectionScaffold("لیگ‌ها", onBack = { onSelectSection(null) }) {
            LeaguesTabContent(sportsViewModel, onNavigateToLeague)
        }
        "news" -> MoreSectionScaffold("اخبار", onBack = { onSelectSection(null) }) {
            NewsTabContent(sportsViewModel)
        }
        "favorites" -> MoreSectionScaffold("علاقه‌مندی‌ها", onBack = { onSelectSection(null) }) {
            FavoritesTabContent(sportsViewModel, onNavigateToMatch, onNavigateToTeam, onNavigateToLeague, onNavigateToPlayer)
        }
        "notifications" -> MoreSectionScaffold("مرکز اعلان", onBack = { onSelectSection(null) }) {
            NotificationCenterContent(sportsViewModel, onNavigateToMatch)
        }
        "calendar" -> MoreSectionScaffold("تقویم مسابقات", onBack = { onSelectSection(null) }) {
            MatchCalendarContent(sportsViewModel, onNavigateToMatch, onNavigateToTeam, onNavigateToLeague)
        }
        "rankings" -> MoreSectionScaffold("جدول‌ها و رتبه‌بندی", onBack = { onSelectSection(null) }) {
            RankingsContent(sportsViewModel, onNavigateToTeam, onNavigateToPlayer, onNavigateToLeague)
        }
        "comparison" -> MoreSectionScaffold("مرکز مقایسه", onBack = { onSelectSection(null) }) {
            ComparisonCenterContent(sportsViewModel, onNavigateToTeam, onNavigateToPlayer)
        }
        "form" -> MoreSectionScaffold("فرم و آمار فصل", onBack = { onSelectSection(null) }) {
            FormAndSeasonContent(sportsViewModel, onNavigateToTeam)
        }
        "reminders" -> MoreSectionScaffold("مدیریت یادآورها", onBack = { onSelectSection(null) }) {
            ReminderManagerContent(sportsViewModel, onNavigateToSettings, onNavigateToMatch)
        }
        "offline" -> MoreSectionScaffold("مرکز آفلاین", onBack = { onSelectSection(null) }) {
            OfflineCenterContent(sportsViewModel)
        }
        "recent" -> MoreSectionScaffold("اخیراً دیده‌شده", onBack = { onSelectSection(null) }) {
            RecentlyViewedContent { entry ->
                when (entry.type) {
                    "team" -> onNavigateToTeam(entry.id)
                    "player" -> onNavigateToPlayer(entry.id)
                    "league" -> onNavigateToLeague(entry.id)
                    else -> onNavigateToMatch(entry.id)
                }
            }
        }
        "venues" -> MoreSectionScaffold("ورزشگاه و مربی", onBack = { onSelectSection(null) }) {
            StadiumCoachContent(sportsViewModel, onNavigateToTeam)
        }
        "share" -> MoreSectionScaffold("کارت اشتراک", onBack = { onSelectSection(null) }) {
            ShareMatchCardContent(sportsViewModel, onNavigateToMatch)
        }
        "health" -> MoreSectionScaffold("وضعیت داده‌ها", onBack = { onSelectSection(null) }) {
            DataHealthContent(sportsViewModel)
        }
        "collections" -> MoreSectionScaffold("مجموعه‌های من", onBack = { onSelectSection(null) }) {
            CollectionsContent(sportsViewModel, onNavigateToTeam, onNavigateToPlayer)
        }
        "changes" -> MoreSectionScaffold("تغییرات نسخه", onBack = { onSelectSection(null) }) {
            ReleaseChangesContent(sportsViewModel)
        }
        "multi_match" -> MoreSectionScaffold("چندمسابقه‌ای", onBack = { onSelectSection(null) }) {
            MultiMatchCenterContent(sportsViewModel, onNavigateToMatch)
        }
        "live_table" -> MoreSectionScaffold("جدول زنده", onBack = { onSelectSection(null) }) {
            LiveStandingsContent(sportsViewModel, onNavigateToTeam)
        }
        else -> MoreHub(onSelectSection, onNavigateToSettings, sportsViewModel)
    }
}

@Composable
private fun MoreSectionScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
            }
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
        Box(modifier = Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun MatchCalendarContent(
    viewModel: SportsViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit
) {
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    val leagues by viewModel.allLeagues.collectAsStateWithLifecycle()
    var day by rememberSaveable { mutableStateOf(0) }
    var leagueId by rememberSaveable { mutableStateOf("all") }
    val days = listOf(-2 to "۲ روز قبل", -1 to "دیروز", 0 to "امروز", 1 to "فردا", 2 to "۲ روز بعد")
    val shown = matches.filter { it.dayOffset == day && (leagueId == "all" || it.leagueId == leagueId) }

    Column(Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days) { (offset, title) ->
                FilterChip(
                    selected = day == offset,
                    onClick = { day = offset; viewModel.selectDate(offset) },
                    label = { Text(title) },
                    shape = CircleShape
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { FilterChip(selected = leagueId == "all", onClick = { leagueId = "all" }, label = { Text("همه لیگ‌ها") }) }
            items(leagues, key = { it.id }) { league ->
                FilterChip(selected = leagueId == league.id, onClick = { leagueId = league.id }, label = { Text(league.name) })
            }
        }
        if (shown.isEmpty()) {
            EmptyState("برای این تاریخ مسابقه‌ای پیدا نشد.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(days.first { it.first == day }.second, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                        Text("${shown.size} مسابقه", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(shown, key = { "calendar-${it.id}" }) { match ->
                    MatchCard(
                        match,
                        { onNavigateToMatch(match.id) },
                        { viewModel.toggleMatchFavorite(match.id, match.isFavorite) },
                        { onNavigateToTeam(match.homeTeamId) },
                        { onNavigateToTeam(match.awayTeamId) },
                        { onNavigateToLeague(match.leagueId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RankingsContent(
    viewModel: SportsViewModel,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit
) {
    val leagues by viewModel.allLeagues.collectAsStateWithLifecycle()
    var mode by rememberSaveable { mutableStateOf("teams") }
    val standings = remember(leagues) {
        leagues.flatMap { league ->
            runCatching { dashboardStandingsAdapter.fromJson(league.standingsJson).orEmpty() }.getOrDefault(emptyList())
                .map { league to it }
        }.sortedByDescending { it.second.points }
    }
    val scorers = remember(leagues) {
        leagues.flatMap { league ->
            runCatching { dashboardScorersAdapter.fromJson(league.scorersJson).orEmpty() }.getOrDefault(emptyList())
                .map { league to it }
        }.sortedByDescending { it.second.goals }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("بهترین‌های فوتبال", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("مقایسه تیم‌ها و گلزنان تمام لیگ‌های دریافت‌شده", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = mode == "teams", onClick = { mode = "teams" }, label = { Text("تیم‌ها") })
                        FilterChip(selected = mode == "players", onClick = { mode = "players" }, label = { Text("گلزنان") })
                    }
                }
            }
        }
        if (mode == "teams") {
            if (standings.isEmpty()) item { EmptyState("جدول رتبه‌بندی هنوز دریافت نشده است.") }
            items(standings.take(30), key = { "rank-team-${it.first.id}-${it.second.teamId}" }) { (league, row) ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToTeam(row.teamId) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(row.rank.toString(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                        AsyncImage(row.teamLogo, row.teamName, modifier = Modifier.size(36.dp))
                        Column(Modifier.weight(1f)) {
                            Text(row.teamName, fontWeight = FontWeight.Bold)
                            Text(league.name, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { onNavigateToLeague(league.id) })
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(row.points.toString(), fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                            Text("امتیاز", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        } else {
            if (scorers.isEmpty()) item { EmptyState("آمار گلزنان هنوز دریافت نشده است.") }
            items(scorers.take(30), key = { "rank-player-${it.first.id}-${it.second.playerId}" }) { (league, scorer) ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToPlayer(scorer.playerId) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AsyncImage(scorer.portrait, scorer.name, contentScale = ContentScale.Crop, modifier = Modifier.size(46.dp).clip(CircleShape))
                        Column(Modifier.weight(1f)) {
                            Text(scorer.name, fontWeight = FontWeight.Bold)
                            Text("${scorer.teamName} · ${league.name}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                        Text("${scorer.goals} گل", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreHub(onSelect: (String) -> Unit, onSettings: () -> Unit, viewModel: SportsViewModel) {
    val history by viewModel.notificationHistory.collectAsStateWithLifecycle()
    val allMatches by viewModel.allMatches.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val options = listOf(
        Triple("leagues", "لیگ‌ها", "جدول، گلزنان و برنامه هفته"),
        Triple("news", "اخبار", "فوتبال داخلی و خارجی"),
        Triple("calendar", "تقویم مسابقات", "انتخاب تاریخ و برنامه بازی‌ها"),
        Triple("rankings", "رتبه‌بندی", "جدول تیم‌ها و گلزنان"),
        Triple("favorites", "مدیریت محبوب‌ها", "تیم‌ها، لیگ‌ها و بازیکنان"),
        Triple("notifications", "مرکز اعلان", "گل‌ها و اتفاقات مهم قبلی"),
        Triple("multi_match", "مرکز چندمسابقه‌ای", "دنبال‌کردن هم‌زمان چهار بازی"),
        Triple("live_table", "جدول زنده لیگ", "رتبه لحظه‌ای با نتایج در جریان"),
        Triple("comparison", "مرکز مقایسه", "مقایسه تیم‌ها و بازیکنان"),
        Triple("form", "فرم و آمار فصل", "عملکرد و قدرت تیم‌ها"),
        Triple("reminders", "مدیریت یادآورها", "بازی‌ها و نوع اعلان"),
        Triple("offline", "مرکز آفلاین", "دانلود، پیشرفت و فضای مصرفی"),
        Triple("recent", "اخیراً دیده‌شده", "تاریخچه صفحات بازشده"),
        Triple("venues", "ورزشگاه و مربی", "اطلاعات تکمیلی باشگاه‌ها"),
        Triple("share", "کارت اشتراک مسابقه", "نتیجه مینیمال برای اشتراک"),
        Triple("health", "وضعیت داده‌ها", "اتصال، کش و آخرین دریافت"),
        Triple("collections", "مجموعه‌های من", "پوشه‌های شخصی فوتبال"),
        Triple("changes", "تغییرات نسخه", "امکانات نسخه نصب‌شده"),
        Triple("settings", "تنظیمات", "ظاهر، اعلان و دانلود آفلاین"),
    ).filter { query.isBlank() || it.second.contains(query, true) || it.third.contains(query, true) }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("بیشتر", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LeagueInfoPill("آخرین داده", "${allMatches.size} مسابقه", Modifier.weight(1f))
                    LeagueInfoPill("اعلان‌ها", history.size.toString(), Modifier.weight(1f))
                }
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(18.dp), leadingIcon = { Icon(Icons.Default.Search, null) }, placeholder = { Text("جستجوی امکانات…") })
            }
        }
        items(options, key = { it.first }) { (key, title, subtitle) ->
            MoreTile(
                title = title,
                subtitle = subtitle,
                icon = when (key) { "news" -> Icons.AutoMirrored.Filled.List; "favorites" -> Icons.Default.Favorite; "notifications" -> Icons.Outlined.Notifications; "settings" -> Icons.Default.Settings; "calendar" -> Icons.Default.SportsSoccer; else -> Icons.Default.Star },
                onClick = { if (key == "settings") onSettings() else onSelect(key) }
            )
        }
        if (options.isEmpty()) item { EmptyState("امکانی با این عبارت پیدا نشد.") }
    }
}

@Composable
private fun NotificationCenterContent(viewModel: SportsViewModel, onNavigateToMatch: (String) -> Unit) {
    val history by viewModel.notificationHistory.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf("all") }
    if (history.isEmpty()) {
        EmptyState("هنوز اعلانی ثبت نشده است.")
        return
    }
    val filtered = history.filter {
        filter == "all" || when (filter) {
            "goal" -> it.kind.contains("GOAL", true)
            "card" -> it.kind.contains("CARD", true)
            "match" -> it.kind.contains("KICKOFF", true) || it.kind.contains("FULL", true)
            "unread" -> !it.isRead
            else -> true
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            val unreadCount = history.count { !it.isRead }
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("اعلان‌های مسابقات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (unreadCount > 0) "$unreadCount اعلان خوانده‌نشده" else "همه اعلان‌ها خوانده شده‌اند",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (unreadCount > 0) {
                    Text(
                        "خواندن همه",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.markAllNotificationsRead() }.padding(8.dp)
                    )
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("all" to "همه", "unread" to "خوانده‌نشده", "goal" to "گل‌ها", "card" to "کارت‌ها", "match" to "شروع و پایان")) { (key, label) ->
                    FilterChip(selected = filter == key, onClick = { filter = key }, label = { Text(label) }, shape = CircleShape)
                }
            }
        }
        if (filtered.isEmpty()) item { EmptyState("اعلانی در این دسته وجود ندارد.") }
        items(filtered, key = { it.id }) { alert ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (alert.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
                ),
                modifier = Modifier.fillMaxWidth().clickable {
                    viewModel.markNotificationRead(alert.id)
                    onNavigateToMatch(alert.matchId)
                }
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            when {
                                alert.kind.contains("GOAL", true) -> "⚽"
                                alert.kind.contains("RED", true) -> "▰"
                                alert.kind.contains("KICKOFF", true) -> "▶"
                                else -> "✓"
                            },
                            color = if (alert.kind.contains("RED", true)) LiveRed else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!alert.isRead) {
                                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            }
                            Text(alert.title, fontWeight = FontWeight.Bold)
                        }
                        Text(alert.body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Text(
                            SimpleDateFormat("HH:mm · yyyy/MM/dd", Locale("fa")).format(Date(alert.createdAt)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreTile(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = natijehCardElevation(),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.SportsSoccer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(34.dp))
            }
            Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun LoadingState() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(5) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().height(116.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(Modifier.size(44.dp).clip(CircleShape).shimmer())
                    Box(Modifier.width(96.dp).height(20.dp).clip(RoundedCornerShape(8.dp)).shimmer())
                    Box(Modifier.size(44.dp).clip(CircleShape).shimmer())
                }
            }
        }
    }
}
