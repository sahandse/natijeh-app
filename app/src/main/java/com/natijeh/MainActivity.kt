package com.natijeh

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.natijeh.data.notify.GoalNotifier
import com.natijeh.data.settings.AppSettings
import com.natijeh.ui.screens.LeagueDetailScreen
import com.natijeh.ui.screens.FavoritesOnboardingScreen
import com.natijeh.ui.screens.MainDashboard
import com.natijeh.ui.screens.MatchDetailScreen
import com.natijeh.ui.screens.PlayerDetailScreen
import com.natijeh.ui.screens.OfficialStreamScreen
import com.natijeh.ui.screens.SettingsScreen
import com.natijeh.ui.screens.TeamDetailScreen
import com.natijeh.ui.theme.NatijehTheme
import com.natijeh.ui.viewmodel.SettingsViewModel
import com.natijeh.ui.viewmodel.SportsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()

        val app = application as NatijehApp
        val repository = app.repository

        setContent {
            val settings by app.settingsStore.settings.collectAsStateWithLifecycle(AppSettings())
            NatijehTheme(themeMode = settings.themeMode) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        val sportsViewModel: SportsViewModel = viewModel(
                            factory = SportsViewModel.Factory(repository)
                        )
                        val settingsViewModel: SettingsViewModel = viewModel(
                            factory = SettingsViewModel.Factory(app.settingsStore)
                        )

                        LaunchedEffect(intent) {
                            val matchId = intent?.getStringExtra(GoalNotifier.EXTRA_MATCH_ID)
                            if (!matchId.isNullOrBlank()) {
                                navController.navigate("match_detail/$matchId")
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = if (settings.onboardingCompleted) "dashboard" else "onboarding"
                        ) {
                            composable("onboarding") {
                                FavoritesOnboardingScreen(
                                    sportsViewModel = sportsViewModel,
                                    settingsViewModel = settingsViewModel,
                                    onFinished = { navController.navigate("dashboard") { popUpTo("onboarding") { inclusive = true } } }
                                )
                            }
                            composable("dashboard") {
                                MainDashboard(
                                    sportsViewModel = sportsViewModel,
                                    openLiveTab = settings.openLiveTab,
                                    compactCards = settings.cardDensity == com.natijeh.data.settings.CardDensity.COMPACT,
                                    onNavigateToMatch = { matchId ->
                                        navController.navigate("match_detail/$matchId")
                                    },
                                    onNavigateToTeam = { teamId ->
                                        navController.navigate("team_detail/$teamId")
                                    },
                                    onNavigateToLeague = { leagueId ->
                                        navController.navigate("league_detail/$leagueId")
                                    },
                                    onNavigateToPlayer = { playerId ->
                                        navController.navigate("player_detail/$playerId")
                                    },
                                    onNavigateToStream = { matchId, providerId ->
                                        navController.navigate("official_stream/$matchId/$providerId")
                                    },
                                    onNavigateToSettings = {
                                        navController.navigate("settings")
                                    }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    viewModel = settingsViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(
                                route = "official_stream/{matchId}/{providerId}",
                                arguments = listOf(
                                    navArgument("matchId") { type = NavType.StringType },
                                    navArgument("providerId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                OfficialStreamScreen(
                                    matchId = backStackEntry.arguments?.getString("matchId").orEmpty(),
                                    providerId = backStackEntry.arguments?.getString("providerId").orEmpty(),
                                    viewModel = sportsViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(
                                route = "match_detail/{matchId}",
                                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val matchId = backStackEntry.arguments?.getString("matchId").orEmpty()
                                MatchDetailScreen(
                                    matchId = matchId,
                                    sportsViewModel = sportsViewModel,
                                    keepScreenOnLive = settings.keepScreenOnLive,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToTeam = { teamId ->
                                        navController.navigate("team_detail/$teamId")
                                    },
                                    onNavigateToLeague = { leagueId ->
                                        navController.navigate("league_detail/$leagueId")
                                    },
                                    onNavigateToPlayer = { playerId ->
                                        navController.navigate("player_detail/$playerId")
                                    },
                                    onNavigateToStream = { streamMatchId, providerId ->
                                        navController.navigate("official_stream/$streamMatchId/$providerId")
                                    }
                                )
                            }
                            composable(
                                route = "team_detail/{teamId}",
                                arguments = listOf(navArgument("teamId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val teamId = backStackEntry.arguments?.getString("teamId").orEmpty()
                                TeamDetailScreen(
                                    teamId = teamId,
                                    viewModel = sportsViewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToMatch = { matchId ->
                                        navController.navigate("match_detail/$matchId")
                                    },
                                    onNavigateToPlayer = { playerId ->
                                        navController.navigate("player_detail/$playerId")
                                    }
                                )
                            }
                            composable(
                                route = "player_detail/{playerId}",
                                arguments = listOf(navArgument("playerId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val playerId = backStackEntry.arguments?.getString("playerId").orEmpty()
                                PlayerDetailScreen(
                                    playerId = playerId,
                                    viewModel = sportsViewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToTeam = { teamId ->
                                        navController.navigate("team_detail/$teamId")
                                    }
                                )
                            }
                            composable(
                                route = "league_detail/{leagueId}",
                                arguments = listOf(navArgument("leagueId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val leagueId = backStackEntry.arguments?.getString("leagueId").orEmpty()
                                LeagueDetailScreen(
                                    leagueId = leagueId,
                                    viewModel = sportsViewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToTeam = { teamId ->
                                        navController.navigate("team_detail/$teamId")
                                    },
                                    onNavigateToMatch = { matchId ->
                                        navController.navigate("match_detail/$matchId")
                                    },
                                    onNavigateToPlayer = { playerId ->
                                        navController.navigate("player_detail/$playerId")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1101)
        }
    }
}
