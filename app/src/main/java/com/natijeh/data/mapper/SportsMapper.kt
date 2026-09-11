package com.natijeh.data.mapper

import com.natijeh.data.model.FixtureMatch
import com.natijeh.data.model.FixtureRound
import com.natijeh.data.model.HeadToHeadData
import com.natijeh.data.model.HeadToHeadMatch
import com.natijeh.data.model.LeagueEntity
import com.natijeh.data.model.MatchEntity
import com.natijeh.data.model.MatchEvent
import com.natijeh.data.model.MatchLineups
import com.natijeh.data.model.PlayerLineup
import com.natijeh.data.model.ScorerRow
import com.natijeh.data.model.SquadPlayer
import com.natijeh.data.model.TeamResultMatch
import com.natijeh.data.model.StatItem
import com.natijeh.data.remote.dto.ApiEvent
import com.natijeh.data.remote.dto.ApiFixtureRound
import com.natijeh.data.remote.dto.ApiLineupSide
import com.natijeh.data.remote.dto.ApiLiveMatch
import com.natijeh.data.remote.dto.ApiPlayerStatGroup
import com.natijeh.data.remote.dto.ApiSide
import com.natijeh.data.remote.dto.ApiSquadGroup
import com.natijeh.data.remote.dto.ApiStandingTeam
import com.natijeh.data.remote.dto.ApiStatPeriod
import com.natijeh.data.remote.dto.ApiTeamMatchItem

object SportsMapper {

    fun cleanLeagueTitle(title: String): String {
        return title.substringBefore(" - ").trim().ifBlank { title.trim() }
    }

    fun mapStatus(isLive: Boolean, status: Int?): String {
        return when {
            isLive || status == 2 -> "LIVE"
            status == 7 || status == 8 -> "FINISHED"
            else -> "SCHEDULED"
        }
    }

    fun parseMinute(liveTime: String?, status: Int?): Int {
        if (status == 7 || status == 8) return 90
        val digits = liveTime.orEmpty().takeWhile { it.isDigit() }
        return digits.toIntOrNull() ?: 0
    }

    fun sideId(side: ApiSide?): String {
        val id = side?.id
        if (id != null && id != 0) return id.toString()
        parseIdFromLink(side?.link, "team")?.let { return it }
        return side?.name.orEmpty().ifBlank { "unknown" }
    }

    fun parseIdFromLink(link: String?, type: String): String? {
        if (link.isNullOrBlank()) return null
        val regex = "/$type/(\\d+)".toRegex()
        return regex.find(link)?.groupValues?.getOrNull(1)
    }

    fun mapEvents(events: List<ApiEvent>?): List<MatchEvent> {
        return events.orEmpty().map { event ->
            val type = when (event.eventType) {
                1 -> if (event.goalType == 1 || event.goalType == 2) "PENALTY" else "GOAL"
                2 -> if (event.cardType == 2 || event.cardType == 3) "CARD_RED" else "CARD_YELLOW"
                3 -> "PENALTY"
                4 -> "SUBSTITUTION"
                5 -> "VAR_REVIEW"
                else -> "EVENT"
            }
            val playerName = when (type) {
                "GOAL", "PENALTY" -> event.strickerName ?: event.kickerName ?: event.offendingPlayerName.orEmpty()
                "CARD_YELLOW", "CARD_RED" -> event.offendingPlayerName.orEmpty()
                "SUBSTITUTION" -> event.incomingPlayerName.orEmpty().ifBlank { "بازیکن" }
                else -> event.offendingPlayerName ?: event.strickerName.orEmpty()
            }
            val playerId = when (type) {
                "GOAL", "PENALTY" -> event.strikerId ?: event.kickerId
                "CARD_YELLOW", "CARD_RED" -> event.offendingPlayerId
                "SUBSTITUTION" -> event.incomingPlayerId
                else -> event.strikerId ?: event.offendingPlayerId
            }
            MatchEvent(
                minute = event.rawTime ?: event.time?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0,
                type = type,
                isHome = event.side == 0,
                playerName = playerName.ifBlank { "بازیکن" },
                detail = event.description.orEmpty(),
                playerId = playerId?.takeIf { it != 0 }?.toString().orEmpty(),
                extraPlayerName = event.outgoingPlayerName.orEmpty(),
                extraPlayerId = event.outgoingPlayerId?.takeIf { it != 0 }?.toString().orEmpty()
            )
        }.sortedBy { it.minute }
    }

    fun mapStats(periods: List<ApiStatPeriod>?): List<StatItem> {
        val period = periods?.firstOrNull { it.title == "مجموع" } ?: periods?.lastOrNull()
        val inner = period?.stats ?: return emptyList()
        val items = mutableListOf<StatItem>()
        inner.possession?.let { possession ->
            items += StatItem(
                title = possession.title ?: "مالکیت توپ",
                home = possession.host?.value ?: possession.hostValue?.toString().orEmpty(),
                away = possession.guest?.value ?: possession.guestValue?.toString().orEmpty(),
                homePercent = possession.host?.percent ?: possession.hostValue ?: 50,
                awayPercent = possession.guest?.percent ?: possession.guestValue ?: 50
            )
        }
        inner.items.orEmpty().forEach { row ->
            items += StatItem(
                title = row.title.orEmpty(),
                home = row.host?.value ?: row.hostValue?.toString().orEmpty(),
                away = row.guest?.value ?: row.guestValue?.toString().orEmpty(),
                homePercent = row.host?.percent ?: 50,
                awayPercent = row.guest?.percent ?: 50
            )
        }
        return items
    }

    fun mapLineups(host: ApiLineupSide?, guest: ApiLineupSide?): MatchLineups {
        return MatchLineups(
            homeFormation = host?.formation.orEmpty().ifBlank { "-" },
            awayFormation = guest?.formation.orEmpty().ifBlank { "-" },
            homeStarting = flattenLineup(host),
            homeBench = mapPlayers(host?.benchedPlayers),
            awayStarting = flattenLineup(guest),
            awayBench = mapPlayers(guest?.benchedPlayers),
            homeCoach = host?.coach?.name.orEmpty(),
            awayCoach = guest?.coach?.name.orEmpty()
        )
    }

    private fun flattenLineup(side: ApiLineupSide?): List<PlayerLineup> {
        val lines = side?.formationLines.orEmpty()
        return lines.flatMapIndexed { index, line ->
            val position = when (index) {
                0 -> "GK"
                1 -> "DF"
                lines.lastIndex -> "FW"
                else -> "MF"
            }
            (line.players.orEmpty()).map { player ->
                PlayerLineup(
                    number = player.shirtNumber ?: 0,
                    name = player.name.orEmpty(),
                    position = position,
                    rating = 0.0,
                    playerId = player.id?.takeIf { it != 0 }?.toString().orEmpty(),
                    line = index,
                    portrait = player.portrait.orEmpty()
                )
            }
        }
    }

    private fun mapPlayers(players: List<com.natijeh.data.remote.dto.ApiLineupPlayer>?): List<PlayerLineup> {
        return players.orEmpty().map {
            PlayerLineup(
                number = it.shirtNumber ?: 0,
                name = it.name.orEmpty(),
                position = "",
                rating = 0.0,
                playerId = it.id?.takeIf { id -> id != 0 }?.toString().orEmpty(),
                line = -1,
                portrait = it.portrait.orEmpty()
            )
        }
    }

    fun mapStandings(teams: List<ApiStandingTeam>?): List<StandingRow> {
        return teams.orEmpty().map { row ->
            StandingRow(
                rank = row.rank ?: 0,
                teamId = (row.id ?: 0).toString(),
                teamName = row.name.orEmpty(),
                teamLogo = row.logo.orEmpty(),
                played = row.played ?: 0,
                won = row.wins ?: 0,
                drawn = row.draws ?: 0,
                lost = row.losses ?: 0,
                goalsFor = row.goalFor ?: 0,
                goalsAgainst = row.goalAgainst ?: 0,
                points = row.points ?: 0
            )
        }
    }

    fun mapSquad(groups: List<ApiSquadGroup>?): List<SquadPlayer> {
        return groups.orEmpty().flatMap { group ->
            val position = group.role.orEmpty()
            group.players.orEmpty().map { player ->
                SquadPlayer(
                    id = (player.id ?: 0).takeIf { it != 0 }?.toString()
                        ?: parseIdFromLink(player.link, "person")
                        ?: parseIdFromLink(player.link, "players")
                        ?: parseIdFromLink(player.link, "player")
                        ?: "",
                    name = player.name.orEmpty(),
                    nationality = "",
                    age = player.age ?: 0,
                    height = "",
                    weight = "",
                    position = position,
                    preferredFoot = "",
                    marketValue = "",
                    goals = 0,
                    assists = 0,
                    appearances = 0,
                    portrait = player.portrait.orEmpty(),
                    shirtNumber = player.shirtNumber ?: 0,
                    countryFlag = player.countryFlag.orEmpty()
                )
            }
        }
    }

    fun involvesTeam(item: ApiTeamMatchItem, name: String, id: String): Boolean {
        val hostId = sideId(item.host)
        val guestId = sideId(item.guest)
        if (id.isNotBlank() && id != "unknown" && (hostId == id || guestId == id)) return true
        val host = item.host?.name.orEmpty()
        val guest = item.guest?.name.orEmpty()
        return namesMatch(host, name) || namesMatch(guest, name)
    }

    fun mapHeadToHead(
        homeName: String,
        awayName: String,
        homeId: String,
        awayId: String,
        results: List<ApiTeamMatchItem>?
    ): HeadToHeadData {
        val past = results.orEmpty()
            .filter { involvesTeam(it, homeName, homeId) && involvesTeam(it, awayName, awayId) }
            .distinctBy { it.id ?: "${it.date}${it.host?.name}${it.guest?.name}" }
            .map { item ->
                val host = item.host?.name.orEmpty()
                val guest = item.guest?.name.orEmpty()
                val hostGoals = item.goals?.host
                val guestGoals = item.goals?.guest
                val score = if (hostGoals != null && guestGoals != null) {
                    "$hostGoals - $guestGoals"
                } else {
                    item.statusTitle.orEmpty().ifBlank { item.time.orEmpty() }
                }
                HeadToHeadMatch(
                    date = item.date.orEmpty(),
                    homeTeam = host,
                    awayTeam = guest,
                    score = score
                )
            }
            .take(8)

        var homeWins = 0
        var awayWins = 0
        var draws = 0
        past.forEach { match ->
            val parts = match.score.split("-").map { it.trim().toIntOrNull() }
            if (parts.size != 2 || parts[0] == null || parts[1] == null) return@forEach
            val hostScore = parts[0]!!
            val guestScore = parts[1]!!
            when {
                hostScore == guestScore -> draws++
                namesMatch(match.homeTeam, homeName) && hostScore > guestScore -> homeWins++
                namesMatch(match.awayTeam, homeName) && guestScore > hostScore -> homeWins++
                namesMatch(match.homeTeam, awayName) && hostScore > guestScore -> awayWins++
                namesMatch(match.awayTeam, awayName) && guestScore > hostScore -> awayWins++
            }
        }
        return HeadToHeadData(homeWins, awayWins, draws, past)
    }

    fun liveMatchScore(match: ApiLiveMatch): Pair<Int, Int> {
        return (match.goals?.host ?: 0) to (match.goals?.guest ?: 0)
    }

    fun leaguePriority(id: String, name: String): Int {
        val n = normalizeFa(name)
        return when {
            id == IRAN_PREMIER_LEAGUE_ID || n.contains("لیگ برتر ایران") || n.contains("خلیج فارس") -> 0
            n.contains("جام حذفی") && n.contains("ایران") -> 1
            id == "24" || n.contains("آزادگان") -> 2
            n.contains("ایران") -> 3
            n.contains("لیگ قهرمانان اروپا") || n.contains("لیگ نخبگان") -> 10
            else -> 20
        }
    }

    fun groupMatchesByLeague(matches: List<MatchEntity>): List<Pair<LeagueEntity, List<MatchEntity>>> {
        return matches
            .groupBy { Triple(it.leagueId, it.leagueName, it.leagueLogo) }
            .entries
            .sortedWith(
                compareBy<Map.Entry<Triple<String, String, String>, List<MatchEntity>>>(
                    { leaguePriority(it.key.first, it.key.second) },
                    { it.key.second }
                )
            )
            .map { (key, list) ->
                LeagueEntity(
                    id = key.first,
                    name = key.second,
                    logo = key.third,
                    standingsJson = "[]"
                ) to list.sortedWith(compareBy({ it.utcStart }, { it.time }))
            }
    }

    fun mapScorers(groups: List<ApiPlayerStatGroup>?): List<ScorerRow> {
        val group = groups.orEmpty().firstOrNull { it.title?.contains("گلزن") == true }
            ?: groups.orEmpty().firstOrNull { it.type == 1 }
            ?: return emptyList()
        return group.items.orEmpty().map { row ->
            ScorerRow(
                playerId = (row.id ?: 0).toString(),
                name = row.name.orEmpty(),
                portrait = row.portrait.orEmpty(),
                teamId = (row.teamId ?: 0).toString(),
                teamName = row.teamName.orEmpty(),
                teamLogo = row.teamLogo.orEmpty(),
                goals = row.stat?.filter { it.isDigit() }?.toIntOrNull() ?: 0,
                shirtNumber = row.shirtNumber ?: 0
            )
        }
    }

    fun mapFixtureRounds(rounds: List<ApiFixtureRound>?): List<FixtureRound> {
        return rounds.orEmpty().map { round ->
            val matches = round.dates.orEmpty().flatMap { day ->
                day.matches.orEmpty().mapNotNull { match ->
                    val id = match.id?.toString() ?: return@mapNotNull null
                    val host = match.host ?: return@mapNotNull null
                    val guest = match.guest ?: return@mapNotNull null
                    FixtureMatch(
                        id = id,
                        homeTeamId = sideId(host),
                        homeTeamName = host.name.orEmpty(),
                        homeTeamLogo = host.logo.orEmpty(),
                        awayTeamId = sideId(guest),
                        awayTeamName = guest.name.orEmpty(),
                        awayTeamLogo = guest.logo.orEmpty(),
                        homeScore = match.goals?.host,
                        awayScore = match.goals?.guest,
                        time = match.time.orEmpty(),
                        date = day.date.orEmpty(),
                        status = mapStatus(match.isLive == true, match.status),
                        round = round.round.orEmpty()
                    )
                }
            }
            FixtureRound(
                round = round.round.orEmpty().ifBlank { "هفته" },
                selected = round.selected == true,
                matches = matches
            )
        }.filter { it.matches.isNotEmpty() }
    }

    fun squadBucket(role: String): String {
        val compact = role.replace("\u200c", "").replace(" ", "")
        return when {
            compact.contains("دروازه") -> "GK"
            compact.contains("مدافع") || compact.contains("دفاع") -> "DF"
            compact.contains("هافبک") || compact.contains("میانه") -> "MF"
            compact.contains("مهاجم") || compact.contains("حمله") -> "FW"
            else -> "OT"
        }
    }

    fun mapTeamResults(items: List<ApiTeamMatchItem>?, teamId: String): List<TeamResultMatch> {
        return items.orEmpty().map { item ->
            TeamResultMatch(
                id = (item.id ?: 0).takeIf { it != 0 }?.toString()
                    ?: parseIdFromLink(item.link, "match").orEmpty(),
                date = item.date.orEmpty(),
                time = item.time.orEmpty(),
                homeTeam = item.host?.name.orEmpty(),
                awayTeam = item.guest?.name.orEmpty(),
                homeTeamId = sideId(item.host),
                awayTeamId = sideId(item.guest),
                homeScore = item.goals?.host,
                awayScore = item.goals?.guest,
                leagueName = item.league?.name ?: item.league?.title.orEmpty(),
                status = mapStatus(item.isLive == true, item.status)
            )
        }.filter { it.id.isNotBlank() }
    }

    fun resultVersus(match: TeamResultMatch, teamId: String): String {
        if (match.status != "FINISHED" || match.homeScore == null || match.awayScore == null) return "SCHEDULED"
        val isHome = match.homeTeamId == teamId
        val isAway = match.awayTeamId == teamId
        if (!isHome && !isAway) return "SCHEDULED"
        val thisScore = if (isHome) match.homeScore else match.awayScore
        val otherScore = if (isHome) match.awayScore else match.homeScore
        return when {
            thisScore > otherScore -> "WIN"
            thisScore < otherScore -> "LOSS"
            else -> "DRAW"
        }
    }

    fun tabHref(leagueTabs: List<com.natijeh.data.remote.dto.ApiTab>?, type: Int, relContains: String? = null): String? {
        val tab = leagueTabs?.firstOrNull { it.type == type } ?: return null
        val links = tab.links.orEmpty()
        if (!relContains.isNullOrBlank()) {
            links.firstOrNull { it.rel?.contains(relContains) == true || it.href?.contains(relContains) == true }?.href?.let { return it }
        }
        return links.firstOrNull { it.rel == "get" || it.method?.equals("GET", ignoreCase = true) == true }?.href
            ?: links.firstOrNull()?.href
    }

    private fun namesMatch(left: String, right: String): Boolean {
        val a = normalizeFa(left)
        val b = normalizeFa(right)
        if (a.isBlank() || b.isBlank()) return false
        return a == b || a.contains(b) || b.contains(a)
    }

    private fun normalizeFa(value: String): String {
        return value.replace("ي", "ی").replace("ك", "ک").trim()
    }

    const val IRAN_PREMIER_LEAGUE_ID = "6"
}
