package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.*
import com.example.ui.viewmodel.SportsViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

private val dashboardMoshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
private val dashboardSquadAdapter = dashboardMoshi.adapter<List<SquadPlayer>>(
    Types.newParameterizedType(List::class.java, SquadPlayer::class.java)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    username: String,
    sportsViewModel: SportsViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToLeague: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var activeTab by remember { mutableStateOf("today") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

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
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = "نتیجه",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF18C964),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "جستجو",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "تنظیمات",
                            tint = Color.White
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF21262D),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "پروفایل",
                                tint = Color(0xFF18C964),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = username,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 80.dp)
                            )
                        }
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
                NavigationBarItem(
                    selected = activeTab == "today",
                    onClick = { activeTab = "today"; showSearch = false },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "امروز") },
                    label = { Text("بازی‌ها", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF18C964),
                        selectedTextColor = Color(0xFF18C964),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFF21262D)
                    ),
                    modifier = Modifier.testTag("today_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "live",
                    onClick = { activeTab = "live"; showSearch = false },
                    icon = { Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "زنده") },
                    label = { Text("زنده", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF18C964),
                        selectedTextColor = Color(0xFF18C964),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFF21262D)
                    ),
                    modifier = Modifier.testTag("live_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "leagues",
                    onClick = { activeTab = "leagues"; showSearch = false },
                    icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "لیگ‌ها") },
                    label = { Text("لیگ‌ها", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF18C964),
                        selectedTextColor = Color(0xFF18C964),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFF21262D)
                    ),
                    modifier = Modifier.testTag("leagues_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "news",
                    onClick = { activeTab = "news"; showSearch = false },
                    icon = { Icon(imageVector = Icons.Default.List, contentDescription = "اخبار") },
                    label = { Text("اخبار", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF18C964),
                        selectedTextColor = Color(0xFF18C964),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFF21262D)
                    ),
                    modifier = Modifier.testTag("news_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "favorites",
                    onClick = { activeTab = "favorites"; showSearch = false },
                    icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "محبوب") },
                    label = { Text("محبوب", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF18C964),
                        selectedTextColor = Color(0xFF18C964),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFF21262D)
                    ),
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
            // Dropdown / Slide-out Search Panel
            AnimatedVisibility(
                visible = showSearch,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("نام تیم، بازیکن، لیگ یا مربی را جستجو کنید...", color = Color(0xFF64748B)) },
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

            // Render active tab content
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
                    "today" -> TodayTabContent(sportsViewModel, onNavigateToMatch)
                    "live" -> LiveTabContent(sportsViewModel, onNavigateToMatch)
                    "leagues" -> LeaguesTabContent(sportsViewModel, onNavigateToLeague)
                    "news" -> NewsTabContent(sportsViewModel)
                    "favorites" -> FavoritesTabContent(sportsViewModel, onNavigateToMatch, onNavigateToTeam, onNavigateToLeague)
                }
            }
        }
    }
}

// --- TAB CONTENT COMPOSABLES ---

@Composable
fun TodayTabContent(viewModel: SportsViewModel, onNavigateToMatch: (String) -> Unit) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val matches by viewModel.matchesForSelectedDate.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Date Selector Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.datesList) { (label, dateVal) ->
                val isSelected = selectedDate == dateVal
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectDate(dateVal) },
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

        // Matches List
        if (matches.isEmpty()) {
            EmptyState(message = "هیچ مسابقه‌ای برای این تاریخ برنامه‌ریزی نشده است.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(matches) { match ->
                    MatchCard(match = match, onClick = { onNavigateToMatch(match.id) }, onFavoriteToggle = {
                        viewModel.toggleMatchFavorite(match.id, match.isFavorite)
                    })
                }
            }
        }
    }
}

@Composable
fun LiveTabContent(viewModel: SportsViewModel, onNavigateToMatch: (String) -> Unit) {
    val liveMatches by viewModel.liveMatches.collectAsStateWithLifecycle()

    if (liveMatches.isEmpty()) {
        EmptyState(message = "در حال حاضر هیچ مسابقه‌ای به صورت زنده برگزار نمی‌شود.")
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            LiveHeaderBanner(count = liveMatches.size)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(liveMatches, key = { it.id }) { match ->
                    LiveMatchCard(
                        match = match,
                        onClick = { onNavigateToMatch(match.id) },
                        onFavoriteToggle = { viewModel.toggleMatchFavorite(match.id, match.isFavorite) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveHeaderBanner(count: Int) {
    val infinite = rememberInfiniteTransition(label = "live-pulse")
    val alpha by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
        label = "pulse-alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFEF4444).copy(alpha = 0.22f), Color(0xFF0B0E14))
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = alpha))
            )
            Text(
                "$count مسابقه به صورت زنده در جریان است",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
fun LeaguesTabContent(viewModel: SportsViewModel, onNavigateToLeague: (String) -> Unit) {
    val leagues by viewModel.allLeagues.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(leagues) { league ->
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
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0B0E14),
                            modifier = Modifier.size(48.dp)
                        ) {
                            AsyncImage(
                                model = league.logo,
                                contentDescription = league.name,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize()
                            )
                        }
                        Column {
                            Text(text = league.name, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = league.country, color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
                        }
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

    var selectedCategory by remember { mutableStateOf("همه") }

    val filteredNews = remember(news, selectedCategory) {
        when (selectedCategory) {
            "داخلی" -> news.filter { it.source.contains("داخلی") }
            "خارجی" -> news.filter { it.source.contains("خارجی") }
            else -> news
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Category Selector and Refresh Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val categories = listOf("همه", "داخلی", "خارجی")
            categories.forEach { category ->
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

            // Refresh Button
            IconButton(
                onClick = { viewModel.refreshNews() },
                modifier = Modifier.size(36.dp)
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        color = Color(0xFF18C964),
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "بروزرسانی اخبار",
                        tint = Color(0xFF18C964),
                        modifier = Modifier.size(20.dp)
                    )
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
                items(filteredNews) { article ->
                    var isExpanded by remember { mutableStateOf(false) }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = article.imageUrl,
                                contentDescription = article.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            )
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = article.source,
                                        color = Color(0xFF18C964),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = article.date,
                                        color = Color(0xFF64748B),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = article.title,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isExpanded) article.content else article.summary,
                                    color = Color(0xFF94A3B8),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isExpanded) "بستن متن خبر" else "ادامه مطلب...",
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
                item { Text("مسابقات محبوب", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp)) }
                items(matches) { match ->
                    MatchCard(match = match, onClick = { onNavigateToMatch(match.id) }, onFavoriteToggle = {
                        viewModel.toggleMatchFavorite(match.id, match.isFavorite)
                    })
                }
            }

            if (teams.isNotEmpty()) {
                item { Text("تیم‌های محبوب", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                items(teams) { team ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTeam(team.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = team.name, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.toggleTeamFavorite(team.id, team.isFavorite) }) {
                                Icon(imageVector = Icons.Default.Favorite, contentDescription = "حذف", tint = Color(0xFF18C964))
                            }
                        }
                    }
                }
            }

            if (leagues.isNotEmpty()) {
                item { Text("جام‌ها و لیگ‌های محبوب", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                items(leagues) { league ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToLeague(league.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
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
    // Search across ALL matches, not just selected date
    val allMatches by sportsViewModel.allMatches.collectAsStateWithLifecycle()
    val leagues by sportsViewModel.allLeagues.collectAsStateWithLifecycle()
    val teams by sportsViewModel.allTeams.collectAsStateWithLifecycle()

    val filteredMatches = allMatches.filter { it.homeTeamName.contains(query) || it.awayTeamName.contains(query) || it.leagueName.contains(query) }
    val filteredLeagues = leagues.filter { it.name.contains(query) || it.country.contains(query) }

    // Search players inside each team's squad JSON
    val filteredPlayers = remember(teams, query) {
        teams.flatMap { team ->
            val squad = try {
                dashboardSquadAdapter.fromJson(team.squadJson) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            squad.filter { it.name.contains(query) }.map { player -> team to player }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (filteredMatches.isNotEmpty()) {
            item { Text("مسابقات یافت شده", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(filteredMatches) { match ->
                MatchCard(match = match, onClick = { onNavigateToMatch(match.id) }, onFavoriteToggle = {
                    sportsViewModel.toggleMatchFavorite(match.id, match.isFavorite)
                })
            }
        }
        if (filteredLeagues.isNotEmpty()) {
            item { Text("لیگ‌های یافت شده", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(filteredLeagues) { league ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToLeague(league.id) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
                ) {
                    Text(league.name, color = Color.White, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        if (filteredPlayers.isNotEmpty()) {
            item { Text("بازیکنان یافت شده", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(filteredPlayers) { (team, player) ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTeam(team.id) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(player.name, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text("${team.name} · ${player.position}", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF18C964).copy(alpha = 0.2f)) {
                            Text(player.position, color = Color(0xFF18C964), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        if (filteredMatches.isEmpty() && filteredLeagues.isEmpty() && filteredPlayers.isEmpty()) {
            item { EmptyState(message = "موردی با جستجوی شما یافت نشد.") }
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun MatchCard(match: MatchEntity, onClick: () -> Unit, onFavoriteToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: League & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = match.leagueName, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (match.status == "LIVE") Color(0xFFEF4444) else Color(0xFF21262D)
                ) {
                    val statusText = when (match.status) {
                        "LIVE" -> "زنده ${match.minute}'"
                        "FINISHED" -> "پایان"
                        else -> match.time
                    }
                    val statusColor = if (match.status == "LIVE") Color.White else Color(0xFF94A3B8)
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Teams and Scoreline
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home Team Name
                Text(
                    text = match.homeTeamName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )

                // Scoreline Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    val scoreColor = if (match.status == "LIVE") Color(0xFF18C964) else Color.White
                    Text(
                        text = if (match.status == "SCHEDULED") "-" else match.homeScore.toString(),
                        color = scoreColor,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = ":",
                        color = Color(0xFF475569),
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = if (match.status == "SCHEDULED") "-" else match.awayScore.toString(),
                        color = scoreColor,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Away Team Name
                Text(
                    text = match.awayTeamName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer actions
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = match.venue, color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
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

@Composable
fun LiveMatchCard(
    match: MatchEntity,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "live-dot")
    val dotAlpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
        label = "live-dot-alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFEF4444).copy(alpha = 0.35f), Color(0xFF161B22))
                )
            )
            .padding(1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .background(Color(0xFF12161D))
                .clickable { onClick() }
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(match.leagueName, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = dotAlpha))
                    )
                    Text("زنده ${match.minute}'", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TeamBadge(name = match.homeTeamName, modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(match.homeScore.toString(), color = Color(0xFF18C964), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Text(":", color = Color(0xFF475569), style = MaterialTheme.typography.displaySmall)
                    Text(match.awayScore.toString(), color = Color(0xFF18C964), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                }

                TeamBadge(name = match.awayTeamName, modifier = Modifier.weight(1f), alignEnd = true)
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = match.venue, color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
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

@Composable
private fun TeamBadge(name: String, modifier: Modifier = Modifier, alignEnd: Boolean = false) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF18C964).copy(alpha = 0.25f), Color(0xFF21262D)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                name.take(1),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "No data",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = message,
                color = Color(0xFF94A3B8),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}
