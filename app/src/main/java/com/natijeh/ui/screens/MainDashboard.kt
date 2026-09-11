package com.natijeh.ui.screens

import android.content.Intent
import android.net.Uri

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.natijeh.ui.viewmodel.SportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    sportsViewModel: SportsViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf("today") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val isRefreshing by sportsViewModel.isRefreshing.collectAsStateWithLifecycle()
    val errorMessage by sportsViewModel.errorMessage.collectAsStateWithLifecycle()
    val liveMatches by sportsViewModel.liveMatches.collectAsStateWithLifecycle()
    var openedLiveTab by remember { mutableStateOf(false) }
    LaunchedEffect(liveMatches) {
        if (!openedLiveTab && liveMatches.isNotEmpty()) {
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF161B22))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(R.drawable.ic_natijeh_logo)
                                    .build(),
                                contentDescription = "لوگو نتیجه",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column {
                            Text(
                                text = "نتیجه",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF18C964),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "لایواسکور زنده",
                                color = Color(0xFF94A3B8),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { sportsViewModel.refresh() }) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                color = Color(0xFF18C964),
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "به‌روزرسانی زنده",
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "جستجو",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0E14),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF111827),
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF18C964),
                    selectedTextColor = Color(0xFF18C964),
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B),
                    indicatorColor = Color(0xFF21262D)
                )
                NavigationBarItem(
                    selected = activeTab == "today",
                    onClick = { activeTab = "today"; showSearch = false },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "بازی‌ها") },
                    label = { Text("بازی‌ها", style = MaterialTheme.typography.labelSmall) },
                    colors = itemColors,
                    modifier = Modifier.testTag("today_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "live",
                    onClick = { activeTab = "live"; showSearch = false },
                    icon = { Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "زنده") },
                    label = { Text("زنده", style = MaterialTheme.typography.labelSmall) },
                    colors = itemColors,
                    modifier = Modifier.testTag("live_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "leagues",
                    onClick = { activeTab = "leagues"; showSearch = false },
                    icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "لیگ‌ها") },
                    label = { Text("لیگ‌ها", style = MaterialTheme.typography.labelSmall) },
                    colors = itemColors,
                    modifier = Modifier.testTag("leagues_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "news",
                    onClick = { activeTab = "news"; showSearch = false },
                    icon = { Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "اخبار") },
                    label = { Text("اخبار", style = MaterialTheme.typography.labelSmall) },
                    colors = itemColors,
                    modifier = Modifier.testTag("news_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "favorites",
                    onClick = { activeTab = "favorites"; showSearch = false },
                    icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "محبوب") },
                    label = { Text("محبوب", style = MaterialTheme.typography.labelSmall) },
                    colors = itemColors,
                    modifier = Modifier.testTag("favorites_tab")
                )
            }
        },
        containerColor = Color(0xFF0B0E14)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = Color(0xFFFBBF24),
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
                    placeholder = { Text("نام تیم، لیگ یا مربی را جستجو کنید...", color = Color(0xFF64748B)) },
                    singleLine = true,
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "جستجو", tint = Color(0xFF18C964)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "پاک کردن", tint = Color.White)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF18C964),
                        unfocusedBorderColor = Color(0xFF21262D),
                        focusedContainerColor = Color(0xFF161B22),
                        unfocusedContainerColor = Color(0xFF161B22)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            if (showSearch && searchQuery.isNotBlank()) {
                SearchResultsContent(
                    query = searchQuery,
                    sportsViewModel = sportsViewModel,
                    onNavigateToMatch = onNavigateToMatch,
                    onNavigateToTeam = onNavigateToTeam,
                    onNavigateToLeague = onNavigateToLeague
                )
            } else {
                when (activeTab) {
                    "today" -> TodayTabContent(sportsViewModel, onNavigateToMatch, onNavigateToTeam, onNavigateToLeague, isRefreshing)
                    "live" -> LiveTabContent(sportsViewModel, onNavigateToMatch, onNavigateToTeam, onNavigateToLeague, isRefreshing)
                    "leagues" -> LeaguesTabContent(sportsViewModel, onNavigateToLeague)
                    "news" -> NewsTabContent(sportsViewModel)
                    "favorites" -> FavoritesTabContent(sportsViewModel, onNavigateToMatch, onNavigateToTeam, onNavigateToLeague)
                }
            }
        }
    }
}

@Composable
fun TodayTabContent(
    viewModel: SportsViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
    isRefreshing: Boolean
) {
    val selectedOffset by viewModel.selectedOffset.collectAsStateWithLifecycle()
    val matches by viewModel.matchesForSelectedDate.collectAsStateWithLifecycle()
    val favoriteTeams by viewModel.favoriteTeams.collectAsStateWithLifecycle()
    val favoriteLeagues by viewModel.favoriteLeagues.collectAsStateWithLifecycle()
    var onlyFavorites by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.datesList) { (label, offset) ->
                val isSelected = selectedOffset == offset
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectDate(offset) },
                    label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF18C964),
                        selectedLabelColor = Color(0xFF0B0E14),
                        containerColor = Color(0xFF161B22),
                        labelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        GroupedMatchList(
            matches = matches,
            isRefreshing = isRefreshing,
            emptyMessage = if (onlyFavorites) "مسابقه‌ای از علاقه‌مندی‌ها در این روز نیست." else "هیچ مسابقه‌ای برای این تاریخ برنامه‌ریزی نشده است.",
            onlyFavorites = onlyFavorites,
            favoriteTeamIds = favoriteTeams.map { it.id }.toSet(),
            favoriteLeagueIds = favoriteLeagues.map { it.id }.toSet(),
            onRefresh = { viewModel.refresh() },
            onMatchClick = onNavigateToMatch,
            onTeamClick = onNavigateToTeam,
            onLeagueClick = onNavigateToLeague,
            onFavoriteToggle = { match -> viewModel.toggleMatchFavorite(match.id, match.isFavorite) },
            onOnlyFavoritesChange = { onlyFavorites = it }
        )
    }
}

@Composable
fun LiveTabContent(
    viewModel: SportsViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
    isRefreshing: Boolean
) {
    val liveMatches by viewModel.liveMatches.collectAsStateWithLifecycle()
    val favoriteTeams by viewModel.favoriteTeams.collectAsStateWithLifecycle()
    val favoriteLeagues by viewModel.favoriteLeagues.collectAsStateWithLifecycle()
    var onlyFavorites by remember { mutableStateOf(false) }
    GroupedMatchList(
        matches = liveMatches,
        isRefreshing = isRefreshing,
        emptyMessage = if (onlyFavorites) "بازی زنده‌ای از علاقه‌مندی‌ها در جریان نیست." else "در حال حاضر هیچ مسابقه‌ای به صورت زنده برگزار نمی‌شود.",
        onlyFavorites = onlyFavorites,
        favoriteTeamIds = favoriteTeams.map { it.id }.toSet(),
        favoriteLeagueIds = favoriteLeagues.map { it.id }.toSet(),
        onRefresh = { viewModel.refresh() },
        onMatchClick = onNavigateToMatch,
        onTeamClick = onNavigateToTeam,
        onLeagueClick = onNavigateToLeague,
        onFavoriteToggle = { match -> viewModel.toggleMatchFavorite(match.id, match.isFavorite) },
        onOnlyFavoritesChange = { onlyFavorites = it }
    )
}

@Composable
fun LeaguesTabContent(viewModel: SportsViewModel, onNavigateToLeague: (String) -> Unit) {
    val leagues by viewModel.allLeagues.collectAsStateWithLifecycle()
    if (leagues.isEmpty()) {
        EmptyState(message = "لیگ‌ها در حال بارگذاری هستند.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(leagues, key = { it.id }) { league ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLeague(league.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(shape = CircleShape, color = Color(0xFF0B0E14), modifier = Modifier.size(48.dp)) {
                            AsyncImage(
                                model = league.logo,
                                contentDescription = league.name,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize()
                            )
                        }
                        Text(
                            text = league.name,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { viewModel.toggleLeagueFavorite(league.id, league.isFavorite) }) {
                        Icon(
                            imageVector = if (league.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "علاقه‌مندی",
                            tint = if (league.isFavorite) Color(0xFF18C964) else Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewsTabContent(viewModel: SportsViewModel) {
    val news by viewModel.newsFeed.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshingNews.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("همه") }
    val filteredNews = remember(news, selectedCategory) {
        when (selectedCategory) {
            "داخلی" -> news.filter { it.source.contains("داخلی") }
            "خارجی" -> news.filter { it.source.contains("خارجی") }
            else -> news
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("همه", "داخلی", "خارجی").forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = when (category) {
                                "داخلی" -> "فوتبال داخلی"
                                "خارجی" -> "فوتبال خارجی"
                                else -> "همه اخبار"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF18C964),
                        selectedLabelColor = Color(0xFF0B0E14),
                        containerColor = Color(0xFF161B22),
                        labelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.refreshNews() }, modifier = Modifier.size(36.dp)) {
                if (isRefreshing) {
                    CircularProgressIndicator(color = Color(0xFF18C964), modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "بروزرسانی اخبار", tint = Color(0xFF18C964), modifier = Modifier.size(20.dp))
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
                items(filteredNews, key = { it.id }) { article ->
                    var isExpanded by remember { mutableStateOf(false) }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (article.articleUrl.isNotBlank()) {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.articleUrl)))
                                    }
                                } else {
                                    isExpanded = !isExpanded
                                }
                            }
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (article.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = article.imageUrl,
                                    contentDescription = article.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                )
                            }
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = article.source, color = Color(0xFF18C964), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text(text = article.date, color = Color(0xFF64748B), style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = article.title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isExpanded) article.content else article.summary,
                                    color = Color(0xFF94A3B8),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (article.articleUrl.isNotBlank()) "ادامه مطلب در ورزش ۳" else if (isExpanded) "بستن متن خبر" else "ادامه مطلب...",
                                    color = Color(0xFF18C964).copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelSmall,
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

@Composable
fun FavoritesTabContent(
    viewModel: SportsViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit
) {
    val matches by viewModel.favoriteMatches.collectAsStateWithLifecycle()
    val teams by viewModel.favoriteTeams.collectAsStateWithLifecycle()
    val leagues by viewModel.favoriteLeagues.collectAsStateWithLifecycle()

    if (matches.isEmpty() && teams.isEmpty() && leagues.isEmpty()) {
        EmptyState(message = "آیتمی در لیست علاقه‌مندی‌ها موجود نیست.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (matches.isNotEmpty()) {
                item { Text("مسابقات محبوب", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(matches, key = { "m${it.id}" }) { match ->
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
            if (teams.isNotEmpty()) {
                item { Text("تیم‌های محبوب", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(teams, key = { "t${it.id}" }) { team ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
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
                                Text(text = team.name, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { viewModel.toggleTeamFavorite(team.id, team.isFavorite) }) {
                                Icon(imageVector = Icons.Default.Favorite, contentDescription = "حذف", tint = Color(0xFF18C964))
                            }
                        }
                    }
                }
            }
            if (leagues.isNotEmpty()) {
                item { Text("جام‌ها و لیگ‌های محبوب", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(leagues, key = { "l${it.id}" }) { league ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToLeague(league.id) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = league.name, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.toggleLeagueFavorite(league.id, league.isFavorite) }) {
                                Icon(imageVector = Icons.Default.Favorite, contentDescription = "حذف", tint = Color(0xFF18C964))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultsContent(
    query: String,
    sportsViewModel: SportsViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit
) {
    val allMatches by sportsViewModel.allMatches.collectAsStateWithLifecycle()
    val leagues by sportsViewModel.allLeagues.collectAsStateWithLifecycle()
    val teams by sportsViewModel.allTeams.collectAsStateWithLifecycle()
    val needle = query.trim()

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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (filteredMatches.isNotEmpty()) {
            item { Text("مسابقات یافت شده", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
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
        if (filteredTeams.isNotEmpty()) {
            item { Text("تیم‌های یافت شده", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(filteredTeams, key = { it.id }) { team ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToTeam(team.id) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AsyncImage(model = team.logo, contentDescription = team.name, modifier = Modifier.size(28.dp))
                        Column {
                            Text(team.name, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                            if (team.coach.isNotBlank()) {
                                Text(team.coach, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
        if (filteredLeagues.isNotEmpty()) {
            item { Text("لیگ‌های یافت شده", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(filteredLeagues, key = { it.id }) { league ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToLeague(league.id) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
                ) {
                    Text(league.name, color = Color.White, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        if (filteredMatches.isEmpty() && filteredLeagues.isEmpty() && filteredTeams.isEmpty()) {
            item { EmptyState(message = "موردی با جستجوی شما یافت نشد.") }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(64.dp))
            Text(text = message, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(color = Color(0xFF18C964))
            Text("در حال دریافت نتایج زنده...", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
