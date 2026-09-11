package com.natijeh.data.remote.dto

import com.squareup.moshi.Json

data class ApiLiveLeague(
    val id: Int? = null,
    val logo: String? = null,
    val title: String? = null,
    val sport: Int? = null,
    val dates: List<ApiLiveDate>? = null,
    val leagues: List<ApiRelatedLeague>? = null,
    val name: String? = null,
    val slug: String? = null,
    val tabs: List<ApiTab>? = null
)

data class ApiLiveDate(
    val date: String? = null,
    val matches: List<ApiLiveMatch>? = null
)

data class ApiLiveMatch(
    val id: Int? = null,
    val time: String? = null,
    val date: String? = null,
    val status: Int? = null,
    val statusTitle: String? = null,
    val isLive: Boolean? = null,
    val liveTime: String? = null,
    val hasDetails: Boolean? = null,
    val startOnUtc: String? = null,
    val goals: ApiGoals? = null,
    val host: ApiSide? = null,
    val guest: ApiSide? = null
)

data class ApiGoals(
    val host: Int? = null,
    val guest: Int? = null
)

data class ApiSide(
    val id: Int? = null,
    val name: String? = null,
    val logo: String? = null,
    val link: String? = null
)

data class ApiRelatedLeague(
    val id: Int? = null,
    val title: String? = null,
    val logo: String? = null,
    val link: String? = null
)

data class ApiTab(
    val title: String? = null,
    val type: Int? = null,
    @Json(name = "_links") val links: List<ApiLink>? = null
)

data class ApiLink(
    val href: String? = null,
    val rel: String? = null,
    val method: String? = null
)

data class ApiMatchDetail(
    val id: Int? = null,
    val title: String? = null,
    val referee: String? = null,
    val stadium: String? = null,
    val date: String? = null,
    val time: String? = null,
    val status: Int? = null,
    val statusTitle: String? = null,
    val isLive: Boolean? = null,
    val scheduledStartOnUtc: String? = null,
    val goals: ApiGoals? = null,
    val host: ApiSide? = null,
    val guest: ApiSide? = null,
    val league: ApiMatchLeague? = null,
    val events: List<ApiEvent>? = null,
    val stats: List<ApiStatPeriod>? = null,
    val lineup: ApiLineup? = null
)

data class ApiMatchLeague(
    val title: String? = null,
    val logo: String? = null,
    val link: String? = null,
    val season: String? = null
)

data class ApiEvent(
    val eventType: Int? = null,
    val time: String? = null,
    val rawTime: Int? = null,
    val side: Int? = null,
    val description: String? = null,
    val cardType: Int? = null,
    val offendingPlayerName: String? = null,
    val strickerName: String? = null,
    val strikerId: Int? = null,
    val assisterName: String? = null,
    val assisterId: Int? = null,
    val incomingPlayerName: String? = null,
    val incomingPlayerId: Int? = null,
    val outgoingPlayerName: String? = null,
    val outgoingPlayerId: Int? = null,
    val kickerId: Int? = null,
    val kickerName: String? = null,
    val offendingPlayerId: Int? = null,
    val goalType: Int? = null
)

data class ApiStatPeriod(
    val title: String? = null,
    val stats: ApiStatBlock? = null
)

data class ApiStatBlock(
    val possession: ApiStatRow? = null,
    val items: List<ApiStatRow>? = null
)

data class ApiStatRow(
    val title: String? = null,
    val hostValue: Int? = null,
    val guestValue: Int? = null,
    val host: ApiStatValue? = null,
    val guest: ApiStatValue? = null
)

data class ApiStatValue(
    val percent: Int? = null,
    val value: String? = null
)

data class ApiLineup(
    val host: ApiLineupSide? = null,
    val guest: ApiLineupSide? = null
)

data class ApiLineupSide(
    val formation: String? = null,
    val formationLines: List<ApiFormationLine>? = null,
    val benchedPlayers: List<ApiLineupPlayer>? = null,
    val coach: ApiCoach? = null
)

data class ApiFormationLine(
    val line: Int? = null,
    val players: List<ApiLineupPlayer>? = null
)

data class ApiLineupPlayer(
    val id: Int? = null,
    val name: String? = null,
    val shirtNumber: Int? = null,
    val portrait: String? = null
)

data class ApiCoach(
    val id: Int? = null,
    val name: String? = null
)

data class ApiStandingResponse(
    val title: String? = null,
    val teams: List<ApiStandingTeam>? = null
)

data class ApiStandingTeam(
    val rank: Int? = null,
    val id: Int? = null,
    val name: String? = null,
    val logo: String? = null,
    val wins: Int? = null,
    val draws: Int? = null,
    val losses: Int? = null,
    val points: Int? = null,
    val goalFor: Int? = null,
    val goalAgainst: Int? = null,
    val played: Int? = null
)

data class ApiTeamDetail(
    val id: Int? = null,
    val name: String? = null,
    val logo: String? = null,
    val slug: String? = null,
    val followerCount: Int? = null
)

data class ApiPlayerDetail(
    val id: Int? = null,
    val name: String? = null,
    val portrait: String? = null,
    val shirtNumber: Int? = null,
    val role: String? = null,
    val age: Int? = null,
    val country: String? = null,
    val team: ApiSide? = null
)

data class ApiSquadGroup(
    val role: String? = null,
    val players: List<ApiSquadPlayer>? = null
)

data class ApiSquadPlayer(
    val id: Int? = null,
    val name: String? = null,
    val portrait: String? = null,
    val age: Int? = null,
    val shirtNumber: Int? = null,
    val countryFlag: String? = null,
    val link: String? = null
)

data class ApiTeamMatchesResponse(
    val items: List<ApiTeamMatchItem>? = null
)

data class ApiTeamMatchItem(
    val id: Int? = null,
    val time: String? = null,
    val date: String? = null,
    val status: Int? = null,
    val statusTitle: String? = null,
    val isLive: Boolean? = null,
    val host: ApiSide? = null,
    val guest: ApiSide? = null,
    val goals: ApiGoals? = null,
    val league: ApiMatchLeague? = null,
    val link: String? = null
)

data class ApiGlanceResponse(
    val carousel: ApiCarousel? = null
)

data class ApiCarousel(
    val matches: List<ApiTeamMatchItem>? = null
)

data class ApiLeagueMatchesResponse(
    val items: List<ApiFixtureRound>? = null,
    val hasMore: Boolean? = null,
    @Json(name = "_links") val links: List<ApiLink>? = null
)

data class ApiFixtureRound(
    val round: String? = null,
    val selected: Boolean? = null,
    val dates: List<ApiLiveDate>? = null
)

data class ApiPlayerStatGroup(
    val title: String? = null,
    val type: Int? = null,
    val items: List<ApiPlayerStat>? = null
)

data class ApiPlayerStat(
    val id: Int? = null,
    val name: String? = null,
    val portrait: String? = null,
    val shirtNumber: Int? = null,
    val stat: String? = null,
    val teamId: Int? = null,
    val teamName: String? = null,
    val teamLogo: String? = null
)
