package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
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
import com.example.ui.screens.LeagueDetailScreen
import com.example.ui.screens.MainDashboard
import com.example.ui.screens.MatchDetailScreen
import com.example.ui.screens.TeamDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SportsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val repository = SportsRepository(database.sportsDao())

        setContent {
            MyApplicationTheme(darkTheme = true) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        val sportsViewModel: SportsViewModel = viewModel(
                            factory = SportsViewModel.Factory(repository)
                        )

                        NavHost(
                            navController = navController,
                            startDestination = "dashboard"
                        ) {
                            composable("dashboard") {
                                MainDashboard(
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
                            composable(
                                route = "match_detail/{matchId}",
                                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val matchId = backStackEntry.arguments?.getString("matchId").orEmpty()
                                MatchDetailScreen(
                                    matchId = matchId,
                                    sportsViewModel = sportsViewModel,
                                    onBack = { navController.popBackStack() }
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
                                    onBack = { navController.popBackStack() }
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
