package com.natijeh.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.natijeh.data.model.PlayerEntity
import com.natijeh.ui.theme.RankGold
import com.natijeh.ui.theme.natijehCardElevation
import com.natijeh.ui.viewmodel.SportsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerDetailScreen(
    playerId: String,
    viewModel: SportsViewModel,
    onBack: () -> Unit,
    onNavigateToTeam: (String) -> Unit
) {
    val player by viewModel.getPlayerFlow(playerId).collectAsStateWithLifecycle(initialValue = null)
    val knowledge by viewModel.getProfileKnowledgeFlow("player", playerId).collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(playerId) {
        viewModel.loadPlayer(playerId)
    }
    LaunchedEffect(playerId, player?.name) {
        player?.name?.let { viewModel.loadProfileKnowledge("player", playerId, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(player?.name ?: "پروفایل بازیکن", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
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
        player?.let { p ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                PlayerHeader(
                    player = p,
                    onLongPressFavorite = { viewModel.togglePlayerFavorite(p.id, p.isFavorite) },
                    onTeamClick = {
                        if (p.teamId.isNotBlank() && p.teamId != "0") onNavigateToTeam(p.teamId)
                    }
                )
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (p.isLionelMessi()) {
                        MessiLegendCard()
                    }
                    Text("عملکرد فصل", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PlayerMetric("گل", p.goals.toString(), Modifier.weight(1f))
                        PlayerMetric("سن", if (p.age > 0) p.age.toString() else "—", Modifier.weight(1f))
                        PlayerMetric("شماره", if (p.shirtNumber > 0) p.shirtNumber.toString() else "—", Modifier.weight(1f))
                    }
                    PlayerFact("پست", p.position.ifBlank { "—" })
                    if (p.country.isNotBlank()) {
                        PlayerFact("ملیت", p.country)
                    }
                    if (p.teamName.isNotBlank()) {
                        PlayerFact("باشگاه", p.teamName)
                    }
                    Text(
                        "آمار براساس آخرین داده منتشرشده رقابت‌ها نمایش داده می‌شود.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    knowledge?.let { ProfileKnowledgeCard(it, "زندگینامه و افتخارات") }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun PlayerEntity.isLionelMessi(): Boolean {
    val normalized = name.lowercase().replace('‌', ' ').trim()
    return normalized in setOf("لیونل مسی", "لئو مسی", "مسی", "lionel messi", "leo messi")
}

@Composable
private fun MessiLegendCard() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RankGold.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("LIONEL MESSI", fontWeight = FontWeight.Black, color = RankGold)
                Text("اسطوره فوتبال", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlayerHeaderBadge("LEGEND")
                PlayerHeaderBadge("GOAT")
            }
        }
    }
}

@Composable
private fun PlayerMetric(label: String, value: String, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlayerHeader(
    player: PlayerEntity,
    onLongPressFavorite: () -> Unit,
    onTeamClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(colors.primary.copy(alpha = 0.22f), colors.surface)
                )
            )
            .combinedClickable(onClick = {}, onLongClick = onLongPressFavorite)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(shape = CircleShape, color = colors.surfaceVariant, modifier = Modifier.size(112.dp)) {
                if (player.portrait.isNotBlank()) {
                    AsyncImage(model = player.portrait, contentDescription = player.name, modifier = Modifier.size(112.dp).clip(CircleShape))
                }
            }
            if (player.shirtNumber > 0) {
                Surface(
                    shape = CircleShape,
                    color = colors.primary,
                    modifier = Modifier.align(Alignment.BottomEnd).size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(player.shirtNumber.toString(), color = colors.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            IconButton(
                onClick = onLongPressFavorite,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (player.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (player.isFavorite) "حذف بازیکن از محبوب‌ها" else "افزودن بازیکن به محبوب‌ها",
                    tint = if (player.isFavorite) colors.primary else colors.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(player.name, color = colors.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (player.position.isNotBlank()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PlayerHeaderBadge(player.position)
                if (player.country.isNotBlank()) PlayerHeaderBadge(player.country)
            }
        }
        if (player.teamName.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.clickable(onClick = onTeamClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (player.teamLogo.isNotBlank()) {
                    AsyncImage(model = player.teamLogo, contentDescription = player.teamName, modifier = Modifier.size(28.dp))
                }
                Text(player.teamName, color = colors.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlayerHeaderBadge(text: String) {
    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.background.copy(alpha = 0.72f)) {
        Text(text, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PlayerFact(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = natijehCardElevation(),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
    }
}
