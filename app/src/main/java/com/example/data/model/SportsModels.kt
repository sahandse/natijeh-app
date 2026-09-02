package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val homeTeamId: String,
    val homeTeamName: String,
    val awayTeamId: String,
    val awayTeamName: String,
    val homeScore: Int,
    val awayScore: Int,
    val status: String, // "LIVE", "SCHEDULED", "FINISHED"
    val minute: Int,
    val date: String, // "YYYY-MM-DD"
    val time: String, // "HH:MM"
    val leagueId: String,
    val leagueName: String,
    val venue: String,
    val referee: String,
    val attendance: String,
    val eventsJson: String, // JSON representing List<MatchEvent>
    val statsJson: String,  // JSON representing MatchStats
    val lineupsJson: String, // JSON representing MatchLineups
    val momentumJson: String, // JSON representing List<Int> (momentum points)
    val h2hJson: String,     // JSON representing HeadToHeadData
    val isFavorite: Boolean = false
)

data class MatchEvent(
    val minute: Int,
    val type: String, // "GOAL", "CARD_YELLOW", "CARD_RED", "SUBSTITUTION", "VAR_REVIEW", "PENALTY"
    val isHome: Boolean,
    val playerName: String,
    val detail: String // e.g., "Assisted by Player X" or "Red Card for foul"
)

data class MatchStats(
    val possessionHome: Int,
    val possessionAway: Int,
    val shotsHome: Int,
    val shotsAway: Int,
    val shotsOnTargetHome: Int,
    val shotsOnTargetAway: Int,
    val expectedGoalsHome: Double,
    val expectedGoalsAway: Double,
    val passAccuracyHome: Int,
    val passAccuracyAway: Int,
    val cornersHome: Int,
    val cornersAway: Int,
    val foulsHome: Int,
    val foulsAway: Int,
    val offsidesHome: Int,
    val offsidesAway: Int
)

data class PlayerLineup(
    val number: Int,
    val name: String,
    val position: String, // "GK", "DF", "MF", "FW"
    val rating: Double,
    val hasYellowCard: Boolean = false,
    val hasRedCard: Boolean = false,
    val substitutedIn: Int? = null,
    val substitutedOut: Int? = null
)

data class MatchLineups(
    val homeFormation: String, // e.g., "4-3-3"
    val awayFormation: String, // e.g., "4-2-3-1"
    val homeStarting: List<PlayerLineup>,
    val homeBench: List<PlayerLineup>,
    val awayStarting: List<PlayerLineup>,
    val awayBench: List<PlayerLineup>,
    val homeCoach: String,
    val awayCoach: String
)

data class HeadToHeadMatch(
    val date: String,
    val homeTeam: String,
    val awayTeam: String,
    val score: String
)

data class HeadToHeadData(
    val homeWins: Int,
    val awayWins: Int,
    val draws: Int,
    val pastMatches: List<HeadToHeadMatch>
)

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val coach: String,
    val stadium: String,
    val founded: String,
    val marketValue: String,
    val squadJson: String, // List<SquadPlayer>
    val formation: String,
    val honoursJson: String, // List<String>
    val isFavorite: Boolean = false
)

data class SquadPlayer(
    val name: String,
    val nationality: String,
    val age: Int,
    val height: String,
    val weight: String,
    val position: String,
    val preferredFoot: String,
    val marketValue: String,
    val goals: Int,
    val assists: Int,
    val appearances: Int
)

@Entity(tableName = "leagues")
data class LeagueEntity(
    @PrimaryKey val id: String,
    val name: String,
    val logo: String,
    val country: String,
    val standingsJson: String, // List<StandingRow>
    val isFavorite: Boolean = false
)

data class StandingRow(
    val rank: Int,
    val teamId: String,
    val teamName: String,
    val teamLogo: String,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val points: Int
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val imageUrl: String,
    val date: String,
    val source: String
)
