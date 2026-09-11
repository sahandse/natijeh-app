package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val homeTeamId: String,
    val homeTeamName: String,
    val homeTeamLogo: String = "",
    val awayTeamId: String,
    val awayTeamName: String,
    val awayTeamLogo: String = "",
    val homeScore: Int,
    val awayScore: Int,
    val status: String,
    val statusTitle: String = "",
    val liveTime: String = "",
    val minute: Int,
    val date: String,
    val time: String,
    val utcStart: String = "",
    val dayOffset: Int = 0,
    val leagueId: String,
    val leagueName: String,
    val leagueLogo: String = "",
    val venue: String,
    val referee: String,
    val attendance: String = "",
    val eventsJson: String,
    val statsJson: String,
    val lineupsJson: String,
    val momentumJson: String = "[]",
    val h2hJson: String,
    val isFavorite: Boolean = false
)

data class MatchEvent(
    val minute: Int,
    val type: String,
    val isHome: Boolean,
    val playerName: String,
    val detail: String
)

data class StatItem(
    val title: String,
    val home: String,
    val away: String,
    val homePercent: Int = 50,
    val awayPercent: Int = 50
)

data class PlayerLineup(
    val number: Int,
    val name: String,
    val position: String,
    val rating: Double,
    val hasYellowCard: Boolean = false,
    val hasRedCard: Boolean = false,
    val substitutedIn: Int? = null,
    val substitutedOut: Int? = null
)

data class MatchLineups(
    val homeFormation: String,
    val awayFormation: String,
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
    val logo: String = "",
    val coach: String = "",
    val stadium: String = "",
    val founded: String = "",
    val marketValue: String = "",
    val squadJson: String,
    val formation: String = "",
    val honoursJson: String = "[]",
    val recentJson: String = "[]",
    val rankLabel: String = "",
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
    val appearances: Int,
    val portrait: String = "",
    val shirtNumber: Int = 0
)

@Entity(tableName = "leagues")
data class LeagueEntity(
    @PrimaryKey val id: String,
    val name: String,
    val logo: String,
    val country: String = "",
    val standingsJson: String,
    val standingUrl: String = "",
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
