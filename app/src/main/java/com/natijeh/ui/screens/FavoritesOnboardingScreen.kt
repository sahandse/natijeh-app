package com.natijeh.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.natijeh.ui.viewmodel.SettingsViewModel
import com.natijeh.ui.viewmodel.SportsViewModel

private data class FavoriteChoice(val id: String, val name: String, val logo: String, val favorite: Boolean)

@Composable
fun FavoritesOnboardingScreen(
    sportsViewModel: SportsViewModel,
    settingsViewModel: SettingsViewModel,
    onFinished: () -> Unit
) {
    val teams by sportsViewModel.allTeams.collectAsStateWithLifecycle()
    val leagues by sportsViewModel.allLeagues.collectAsStateWithLifecycle()
    var section by remember { mutableStateOf("teams") }
    val shown = if (section == "teams") {
        teams.take(30).map { FavoriteChoice(it.id, it.name, it.logo, it.isFavorite) }
    } else {
        leagues.take(30).map { FavoriteChoice(it.id, it.name, it.logo, it.isFavorite) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 36.dp)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("فوتبال خودت را بساز", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("تیم‌ها و لیگ‌های موردعلاقه را انتخاب کن؛ بعداً بازیکنان را هم از صفحه هر تیم دنبال می‌کنی.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = section == "teams", onClick = { section = "teams" }, label = { Text("تیم‌ها") })
                FilterChip(selected = section == "leagues", onClick = { section = "leagues" }, label = { Text("لیگ‌ها") })
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(shown, key = { it.id }) { item ->
                val favorite = item.favorite
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (section == "teams") sportsViewModel.toggleTeamFavorite(item.id, favorite)
                        else sportsViewModel.toggleLeagueFavorite(item.id, favorite)
                    }
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) { AsyncImage(model = item.logo, contentDescription = item.name, modifier = Modifier.size(30.dp)) }
                        }
                        Text(item.name, modifier = Modifier.weight(1f).padding(horizontal = 12.dp), fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = {
                            if (section == "teams") sportsViewModel.toggleTeamFavorite(item.id, favorite)
                            else sportsViewModel.toggleLeagueFavorite(item.id, favorite)
                        }) {
                            Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "انتخاب", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
        Button(
            onClick = { settingsViewModel.completeOnboarding(); onFinished() },
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            shape = RoundedCornerShape(16.dp)
        ) { Text("شروع استفاده از نتیجه", modifier = Modifier.padding(vertical = 6.dp), fontWeight = FontWeight.Bold) }
    }
}
