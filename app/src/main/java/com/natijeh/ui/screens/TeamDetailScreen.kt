package com.natijeh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.natijeh.data.model.HeadToHeadMatch
import com.natijeh.data.model.SquadPlayer
import com.natijeh.ui.viewmodel.SportsViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
private val squadAdapter = moshi.adapter<List<SquadPlayer>>(
    Types.newParameterizedType(List::class.java, SquadPlayer::class.java)
)
private val recentAdapter = moshi.adapter<List<HeadToHeadMatch>>(
    Types.newParameterizedType(List::class.java, HeadToHeadMatch::class.java)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    teamId: String,
    viewModel: SportsViewModel,
    onBack: () -> Unit
) {
    val team by viewModel.getTeamFlow(teamId).collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(teamId) {
        viewModel.loadTeamDetails(teamId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(team?.name ?: "اطلاعات تیم", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = Color.White)
                    }
                },
                actions = {
                    team?.let { t ->
                        IconButton(onClick = { viewModel.toggleTeamFavorite(t.id, t.isFavorite) }) {
                            Icon(
                                imageVector = if (t.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "محبوب",
                                tint = if (t.isFavorite) Color(0xFF18C964) else Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0E14))
            )
        },
        containerColor = Color(0xFF0B0E14)
    ) { innerPadding ->
        team?.let { t ->
            val squad = squadAdapter.fromJson(t.squadJson).orEmpty()
            val recent = recentAdapter.fromJson(t.recentJson).orEmpty()
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(model = t.logo, contentDescription = t.name, modifier = Modifier.size(72.dp))
                            Text(t.name, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (t.rankLabel.isNotBlank()) {
                                Text(t.rankLabel, color = Color(0xFF18C964), style = MaterialTheme.typography.bodyMedium)
                            }
                            if (t.coach.isNotBlank()) {
                                Text("مربی: ${t.coach}", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
                            }
                            if (t.stadium.isNotBlank()) {
                                Text("ورزشگاه: ${t.stadium}", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                if (recent.isNotEmpty()) {
                    item { Text("بازی‌های اخیر", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(recent) { match ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF21262D)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(match.homeTeam, color = Color.White, modifier = Modifier.weight(1f), maxLines = 1)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(match.score, color = Color(0xFF18C964), fontWeight = FontWeight.Bold)
                                    Text(match.date, color = Color(0xFF64748B), style = MaterialTheme.typography.labelSmall)
                                }
                                Text(match.awayTeam, color = Color.White, modifier = Modifier.weight(1f), maxLines = 1)
                            }
                        }
                    }
                }
                if (squad.isNotEmpty()) {
                    item { Text("لیست بازیکنان", color = Color(0xFF18C964), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(squad) { player ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (player.portrait.isNotBlank()) {
                                    AsyncImage(model = player.portrait, contentDescription = player.name, modifier = Modifier.size(44.dp))
                                } else {
                                    Surface(shape = CircleShape, color = Color(0xFF21262D), modifier = Modifier.size(44.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(player.shirtNumber.toString(), color = Color(0xFF18C964), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(player.name, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF18C964).copy(alpha = 0.2f)) {
                                            Text(player.position, color = Color(0xFF18C964), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    Text(
                                        text = buildString {
                                            if (player.shirtNumber > 0) append("شماره ${player.shirtNumber}")
                                            if (player.age > 0) {
                                                if (isNotEmpty()) append("  |  ")
                                                append("${player.age} سال")
                                            }
                                        },
                                        color = Color(0xFF94A3B8),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF18C964))
            }
        }
    }
}
