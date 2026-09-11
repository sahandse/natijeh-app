package com.example.data.mapper

import com.example.data.model.HeadToHeadData
import com.example.data.model.HeadToHeadMatch
import com.example.data.model.MatchEvent
import com.example.data.model.MatchLineups
import com.example.data.model.PlayerLineup
import com.example.data.model.SquadPlayer
import com.example.data.model.StandingRow
import com.example.data.model.StatItem
import com.example.data.remote.dto.ApiEvent
import com.example.data.remote.dto.ApiLineupSide
import com.example.data.remote.dto.ApiLiveMatch
import com.example.data.remote.dto.ApiSide
import com.example.data.remote.dto.ApiSquadGroup
import com.example.data.remote.dto.ApiStandingTeam
import com.example.data.remote.dto.ApiStatPeriod
import com.example.data.remote.dto.ApiTeamMatchItem

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
                "GOAL", "PENALTY" -> event.strickerName ?: event.offendingPlayerName.orEmpty()
                "CARD_YELLOW", "CARD_RED" -> event.offendingPlayerName.orEmpty()
                "SUBSTITUTION" -> listOfNotNull(event.incomingPlayerName, event.outgoingPlayerName)
                    .joinToString(" ← ")
                else -> event.offendingPlayerName ?: event.strickerName.orEmpty()
            }
            MatchEvent(
                minute = event.rawTime ?: event.time?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0,
                type = type,
                isHome = event.side == 0,
                playerName = playerName.ifBlank { "بازیکن" },
                detail = event.description.orEmpty()
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
                    rating = 0.0
                )
            }
        }
    }

    private fun mapPlayers(players: List<com.example.data.remote.dto.ApiLineupPlayer>?): List<PlayerLineup> {
        return players.orEmpty().map {
            PlayerLineup(
                number = it.shirtNumber ?: 0,
                name = it.name.orEmpty(),
                position = "",
                rating = 0.0
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
                    appearances = player.shirtNumber ?: 0,
                    portrait = player.portrait.orEmpty(),
                    shirtNumber = player.shirtNumber ?: 0
                )
            }
        }
    }

    fun mapHeadToHead(
        homeName: String,
        awayName: String,
        results: List<ApiTeamMatchItem>?
    ): HeadToHeadData {
        val past = results.orEmpty().mapNotNull { item ->
            val host = item.host?.name.orEmpty()
            val guest = item.guest?.name.orEmpty()
            val involvesAway = host.contains(awayName) || guest.contains(awayName)
            val involvesHome = host.contains(homeName) || guest.contains(homeName)
            if (!involvesAway || !involvesHome) return@mapNotNull null
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
        }.take(8)

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
                match.homeTeam.contains(homeName) && hostScore > guestScore -> homeWins++
                match.awayTeam.contains(homeName) && guestScore > hostScore -> homeWins++
                match.homeTeam.contains(awayName) && hostScore > guestScore -> awayWins++
                match.awayTeam.contains(awayName) && guestScore > hostScore -> awayWins++
            }
        }
        return HeadToHeadData(homeWins, awayWins, draws, past)
    }

    fun liveMatchScore(match: ApiLiveMatch): Pair<Int, Int> {
        return (match.goals?.host ?: 0) to (match.goals?.guest ?: 0)
    }
}
