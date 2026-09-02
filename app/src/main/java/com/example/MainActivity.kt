package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.AppDatabase
import com.example.data.repository.SportsRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SportsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Core Room Database & Repository
        val database = AppDatabase.getDatabase(this)
        val repository = SportsRepository(database.sportsDao(), this)

        setContent {
            MyApplicationTheme {
                // Force Right-to-Left (RTL) layout direction globally for Persian language support
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()

                        // Create ViewModels with simple Constructor Factories
                        val sportsViewModel: SportsViewModel = viewModel(
                            factory = SportsViewModel.Factory(repository)
                        )

                        NavHost(
                            navController = navController,
                            startDestination = "onboarding"
                        ) {
                            // Onboarding / Welcome Splash screen
                            composable("onboarding") {
                                OnboardingScreen(onFinished = { username ->
                                    navController.navigate("dashboard/$username") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                })
                            }

                            // Main Dashboard containing all sub-tabs (Today, Live, Leagues, News, Favorites)
                            composable(
                                route = "dashboard/{username}",
                                arguments = listOf(navArgument("username") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val username = backStackEntry.arguments?.getString("username") ?: "کاربر"
                                MainDashboard(
                                    username = username,
                                    sportsViewModel = sportsViewModel,
                                    onNavigateToMatch = { matchId ->
                                        navController.navigate("match_detail/$matchId")
                                    },
                                    onNavigateToTeam = { teamId ->
                                        navController.navigate("team_detail/$teamId")
                                    },
                                    onNavigateToLeague = { leagueId ->
                                        navController.navigate("league_detail/$leagueId")
                                    }
                                )
                            }

                            // Match Details Screen (Timeline, Statistics, Lineups)
                            composable(
                                route = "match_detail/{matchId}",
                                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                                MatchDetailScreen(
                                    matchId = matchId,
                                    sportsViewModel = sportsViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Team Details Screen (Coach, Stadium, Rosters, Squad, Honours)
                            composable(
                                route = "team_detail/{teamId}",
                                arguments = listOf(navArgument("teamId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                                TeamDetailScreen(
                                    teamId = teamId,
                                    viewModel = sportsViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // League Details Screen (Standings table, Rank, GF/GA, Points)
                            composable(
                                route = "league_detail/{leagueId}",
                                arguments = listOf(navArgument("leagueId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val leagueId = backStackEntry.arguments?.getString("leagueId") ?: ""
                                LeagueDetailScreen(
                                    leagueId = leagueId,
                                    viewModel = sportsViewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToTeam = { teamId ->
                                        navController.navigate("team_detail/$teamId")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
