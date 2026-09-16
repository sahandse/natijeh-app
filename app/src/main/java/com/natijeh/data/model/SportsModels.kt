package com.natijeh.data.model

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
    val lastUpdatedMillis: Long = 0,
    val isFavorite: Boolean = false
)

data class MatchEvent(
    val minute: Int,
    val type: String,
    val isHome: Boolean,
    val playerName: String,
    val detail: String,
    val playerId: String = "",
    val extraPlayerName: String = "",
    val extraPlayerId: String = ""
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
    val substitutedOut: Int? = null,
    val playerId: String = "",
    val line: Int = 0,
    val portrait: String = ""
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
    val rank: Int = 0,
    val points: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val played: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val leagueName: String = "",
    val isFavorite: Boolean = false
)

data class SquadPlayer(
    val id: String = "",
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
    val shirtNumber: Int = 0,
    val countryFlag: String = ""
)

@Entity(tableName = "leagues")
data class LeagueEntity(
    @PrimaryKey val id: String,
    val name: String,
    val logo: String,
    val country: String = "",
    val standingsJson: String,
    val standingUrl: String = "",
    val scorersJson: String = "[]",
    val fixturesJson: String = "[]",
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

data class TeamResultMatch(
    val id: String,
    val date: String,
    val time: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamId: String = "",
    val awayTeamId: String = "",
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val leagueName: String = "",
    val status: String = "FINISHED"
)

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val portrait: String = "",
    val teamId: String = "",
    val teamName: String = "",
    val teamLogo: String = "",
    val shirtNumber: Int = 0,
    val position: String = "",
    val age: Int = 0,
    val country: String = "",
    val goals: Int = 0,
    val isFavorite: Boolean = false
)

data class ScorerRow(
    val playerId: String,
    val name: String,
    val portrait: String,
    val teamId: String,
    val teamName: String,
    val teamLogo: String,
    val goals: Int,
    val shirtNumber: Int
)

data class FixtureMatch(
    val id: String,
    val homeTeamId: String,
    val homeTeamName: String,
    val homeTeamLogo: String,
    val awayTeamId: String,
    val awayTeamName: String,
    val awayTeamLogo: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val time: String,
    val date: String,
    val status: String,
    val round: String
)

data class FixtureRound(
    val round: String,
    val selected: Boolean,
    val matches: List<FixtureMatch>
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val imageUrl: String,
    val date: String,
    val articleUrl: String = "",
    val publishedAtMillis: Long = 0,
    val source: String
)

data class MatchAlert(
    val matchId: String,
    val title: String,
    val body: String,
    val kind: Kind
) {
    enum class Kind { GOAL, RED_CARD, KICKOFF, FULL_TIME, LINEUP, SECOND_HALF }
}

@Entity(tableName = "notification_history")
data class NotificationHistoryEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val title: String,
    val body: String,
    val kind: String,
    val createdAt: Long,
    val isRead: Boolean = false
)

@Entity(tableName = "profile_knowledge")
data class ProfileKnowledgeEntity(
    @PrimaryKey val key: String,
    val entityType: String,
    val entityId: String,
    val title: String,
    val description: String,
    val imageUrl: String = "",
    val articleUrl: String = "",
    val source: String = "ویکی‌پدیا",
    val updatedAt: Long = 0
)
