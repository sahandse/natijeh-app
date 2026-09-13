package com.natijeh.ui.screens

import android.content.Intent
import android.net.Uri

import androidx.compose.animation.AnimatedVisibility
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
import com.natijeh.data.model.NewsEntity
import com.natijeh.ui.theme.LiveRed
import com.natijeh.ui.theme.PulseDot
import com.natijeh.ui.theme.natijehCardElevation
import com.natijeh.ui.theme.shimmer
import com.natijeh.ui.viewmodel.SportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    sportsViewModel: SportsViewModel,
    openLiveTab: Boolean = true,
    compactCards: Boolean = true,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
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

            if (showSearch && searchQuery.isNotBlank()) {
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
                    "today" -> TodayTabContent(sportsViewModel, onNavigateToMatch, onNavigateToTeam, onNavigateToLeague, isRefreshing, compactCards)
                    "live" -> LiveTabContent(sportsViewModel, onNavigateToMatch, onNavigateToTeam, onNavigateToLeague, isRefreshing, compactCards)
                    "leagues" -> LeaguesTabContent(sportsViewModel, onNavigateToLeague)
                    "favorites" -> FavoritesTabContent(sportsViewModel, onNavigateToMatch, onNavigateToTeam, onNavigateToLeague, onNavigateToPlayer)
                    else -> MoreTabContent(
                        moreSection = moreSection,
                        onSelectSection = { moreSection = it },
                        sportsViewModel = sportsViewModel,
                        onNavigateToMatch = onNavigateToMatch,
                        onNavigateToTeam = onNavigateToTeam,
                        onNavigateToLeague = onNavigateToLeague,
                        onNavigateToPlayer = onNavigateToPlayer
                    )
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
    isRefreshing: Boolean,
    compactCards: Boolean = true
) {
    val selectedOffset by viewModel.selectedOffset.collectAsStateWithLifecycle()
    val matches by viewModel.matchesForSelectedDate.collectAsStateWithLifecycle()
    val favoriteTeams by viewModel.favoriteTeams.collectAsStateWithLifecycle()
    val favoriteLeagues by viewModel.favoriteLeagues.collectAsStateWithLifecycle()
    var onlyFavorites by remember { mutableStateOf(false) }
    val liveCount = matches.count { it.status == "LIVE" }
    val finishedCount = matches.count { it.status == "FINISHED" }

    Column(modifier = Modifier.fillMaxSize()) {
        if (matches.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(if (selectedOffset == 0) "فوتبال امروز" else "مسابقات این روز", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${matches.size} مسابقه", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (liveCount > 0) LiveMetric(liveCount.toString(), "زنده")
                        LiveMetric(finishedCount.toString(), "تمام‌شده")
                    }
                }
            }
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
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
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
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
            onOnlyFavoritesChange = { onlyFavorites = it },
            compactCards = compactCards
        )
    }
}

@Composable
fun LiveTabContent(
    viewModel: SportsViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
    isRefreshing: Boolean,
    compactCards: Boolean = true
) {
    val liveMatches by viewModel.liveMatches.collectAsStateWithLifecycle()
    val favoriteTeams by viewModel.favoriteTeams.collectAsStateWithLifecycle()
    val favoriteLeagues by viewModel.favoriteLeagues.collectAsStateWithLifecycle()
    var onlyFavorites by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        if (liveMatches.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            PulseDot(size = 9.dp)
                            Text("همین حالا زنده", color = LiveRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Text("نتیجه لحظه‌ای مسابقات", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("${liveMatches.size} بازی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
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
            onOnlyFavoritesChange = { onlyFavorites = it },
            compactCards = compactCards
        )
    }
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = natijehCardElevation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
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
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background, modifier = Modifier.size(48.dp)) {
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
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { viewModel.toggleLeagueFavorite(league.id, league.isFavorite) }) {
                        Icon(
                            imageVector = if (league.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "علاقه‌مندی",
                            tint = if (league.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
    var selectedArticle by remember { mutableStateOf<NewsEntity?>(null) }
    val filteredNews = remember(news, selectedCategory) {
        val uniqueNews = news.distinctBy { it.title.trim().lowercase() }
        when (selectedCategory) {
            "داخلی" -> uniqueNews.filter { it.source.contains("داخلی") }
            "خارجی" -> uniqueNews.filter { it.source.contains("خارجی") }
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
                    Text(
                        article.content.ifBlank { article.summary },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
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
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
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
                items(filteredNews, key = { it.id }) { article ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedArticle = article }
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
                                    Text(text = article.source, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text(text = article.date, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = article.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = article.summary,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "مطالعه خبر در برنامه",
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
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
    onNavigateToLeague: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit = {}
) {
    val matches by viewModel.favoriteMatches.collectAsStateWithLifecycle()
    val teams by viewModel.favoriteTeams.collectAsStateWithLifecycle()
    val leagues by viewModel.favoriteLeagues.collectAsStateWithLifecycle()
    val players by viewModel.favoritePlayers.collectAsStateWithLifecycle()
    var category by remember { mutableStateOf("all") }

    if (matches.isEmpty() && teams.isEmpty() && leagues.isEmpty() && players.isEmpty()) {
        EmptyState(message = "آیتمی در لیست علاقه‌مندی‌ها موجود نیست.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("all" to "همه", "matches" to "بازی‌ها", "teams" to "تیم‌ها", "leagues" to "لیگ‌ها", "players" to "بازیکنان")) { (key, label) ->
                        FilterChip(selected = category == key, onClick = { category = key }, label = { Text(label) })
                    }
                }
            }
            if (matches.isNotEmpty() && category in listOf("all", "matches")) {
                item { Text("مسابقات محبوب", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
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
    var category by remember(query) { mutableStateOf("all") }
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
    onNavigateToPlayer: (String) -> Unit
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
        else -> MoreHub(onSelectSection)
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
private fun MoreHub(onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MoreTile(
            title = "لیگ‌ها",
            subtitle = "جدول، گلزنان و برنامه هفته",
            icon = Icons.Default.Star,
            onClick = { onSelect("leagues") }
        )
        MoreTile(
            title = "اخبار",
            subtitle = "فوتبال داخلی و خارجی از ورزش ۳",
            icon = Icons.AutoMirrored.Filled.List,
            onClick = { onSelect("news") }
        )
        MoreTile(
            title = "علاقه‌مندی‌ها",
            subtitle = "تیم‌ها، لیگ‌ها و بازی‌های محبوب",
            icon = Icons.Default.Favorite,
            onClick = { onSelect("favorites") }
        )
        MoreTile(
            title = "مرکز اعلان",
            subtitle = "گل‌ها و اتفاقات مهم قبلی",
            icon = Icons.Outlined.Notifications,
            onClick = { onSelect("notifications") }
        )
    }
}

@Composable
private fun NotificationCenterContent(viewModel: SportsViewModel, onNavigateToMatch: (String) -> Unit) {
    val history by viewModel.notificationHistory.collectAsStateWithLifecycle()
    if (history.isEmpty()) {
        EmptyState("هنوز اعلانی ثبت نشده است.")
        return
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
        items(history, key = { it.id }) { alert ->
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
                    ) { Icon(Icons.Outlined.Notifications, contentDescription = null) }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!alert.isRead) {
                                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            }
                            Text(alert.title, fontWeight = FontWeight.Bold)
                        }
                        Text(alert.body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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
