package com.natijeh.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.natijeh.ui.viewmodel.SettingsViewModel
import com.natijeh.ui.viewmodel.SportsViewModel

private data class FavoriteChoice(val id: String, val name: String, val logo: String, val favorite: Boolean)
private data class OnboardingStep(val key: String, val title: String, val subtitle: String)

private val onboardingSteps = listOf(
    OnboardingStep("leagues", "لیگ‌های محبوب", "مسابقات مهم را بالاتر از بقیه ببین"),
    OnboardingStep("teams", "تیم‌های محبوب", "نتیجه و برنامه تیم‌ها را سریع دنبال کن"),
    OnboardingStep("players", "بازیکنان محبوب", "برای اتفاق‌های مهم بازیکن‌ها اعلان بگیر")
)

@Composable
fun FavoritesOnboardingScreen(
    sportsViewModel: SportsViewModel,
    settingsViewModel: SettingsViewModel,
    onFinished: () -> Unit
) {
    val teams by sportsViewModel.allTeams.collectAsStateWithLifecycle()
    val leagues by sportsViewModel.allLeagues.collectAsStateWithLifecycle()
    val players by sportsViewModel.allPlayers.collectAsStateWithLifecycle()
    var stepIndex by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    val step = onboardingSteps[stepIndex]
    val section = step.key
    val choices = when (section) {
        "teams" -> teams.map { FavoriteChoice(it.id, it.name, it.logo, it.isFavorite) }
        "leagues" -> leagues.map { FavoriteChoice(it.id, it.name, it.logo, it.isFavorite) }
        else -> players.map { FavoriteChoice(it.id, it.name, it.portrait.ifBlank { it.teamLogo }, it.isFavorite) }
    }.sortedWith(compareByDescending<FavoriteChoice> { it.name.isIranianFootball() }.thenBy { it.name })
    val shown = choices.filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }.take(50)
    val selectedCount = choices.count { it.favorite }

    fun finish() {
        settingsViewModel.completeOnboarding()
        onFinished()
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 24.dp)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("شخصی‌سازی نتیجه", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = ::finish) { Text("فعلاً رد کن") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                onboardingSteps.indices.forEach { index ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(if (index <= stepIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
            Text(step.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(step.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (selectedCount > 0) {
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)) {
                    Text("$selectedCount انتخاب شده", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = {
                    Text(when (section) {
                        "teams" -> "جست‌وجوی تیم"
                        "leagues" -> "جست‌وجوی لیگ"
                        else -> "جست‌وجوی بازیکن"
                    })
                }
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (shown.isEmpty()) {
                item {
                    Text(
                        text = if (section == "players" && players.isEmpty())
                            "بازیکن‌ها پس از دریافت اطلاعات مسابقات و ترکیب تیم‌ها اینجا نمایش داده می‌شوند."
                        else "نتیجه‌ای برای «$query» پیدا نشد.",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(shown, key = { "${section}_${it.id}" }) { item ->
                val favorite = item.favorite
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().clickable {
                        when (section) {
                            "teams" -> sportsViewModel.toggleTeamFavorite(item.id, favorite)
                            "leagues" -> sportsViewModel.toggleLeagueFavorite(item.id, favorite)
                            else -> sportsViewModel.togglePlayerFavorite(item.id, favorite)
                        }
                    }
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) { AsyncImage(model = item.logo, contentDescription = item.name, modifier = Modifier.size(30.dp)) }
                        }
                        Text(item.name, modifier = Modifier.weight(1f).padding(horizontal = 12.dp), fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = {
                            when (section) {
                                "teams" -> sportsViewModel.toggleTeamFavorite(item.id, favorite)
                                "leagues" -> sportsViewModel.toggleLeagueFavorite(item.id, favorite)
                                else -> sportsViewModel.togglePlayerFavorite(item.id, favorite)
                            }
                        }) {
                            Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "انتخاب", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (stepIndex > 0) {
                TextButton(onClick = { stepIndex--; query = "" }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    Text("قبلی")
                }
            }
            Button(
                onClick = {
                    if (stepIndex == onboardingSteps.lastIndex) finish()
                    else { stepIndex++; query = "" }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (stepIndex == onboardingSteps.lastIndex) "شروع استفاده" else "مرحله بعد", modifier = Modifier.padding(vertical = 6.dp), fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        }
    }
}

private fun String.isIranianFootball(): Boolean = listOf(
    "ایران", "استقلال", "پرسپولیس", "سپاهان", "تراکتور", "فولاد", "ملوان", "گل‌گهر", "ذوب آهن"
).any(::contains)
