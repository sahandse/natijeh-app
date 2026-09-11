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
import androidx.compose.material.icons.filled.Star
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

    LaunchedEffect(playerId) {
        viewModel.loadPlayer(playerId)
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
                    if (p.goals > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = natijehCardElevation(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("گل در لیگ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${p.goals}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    PlayerFact("پست", p.position.ifBlank { "—" })
                    PlayerFact("شماره پیراهن", if (p.shirtNumber > 0) p.shirtNumber.toString() else "—")
                    PlayerFact("سن", if (p.age > 0) "${p.age} سال" else "—")
                    if (p.country.isNotBlank()) {
                        PlayerFact("ملیت", p.country)
                    }
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
        Box {
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
            if (player.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "محبوب",
                    tint = colors.primary,
                    modifier = Modifier.align(Alignment.TopEnd).size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(player.name, color = colors.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (player.position.isNotBlank()) {
            Text(player.position, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
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
