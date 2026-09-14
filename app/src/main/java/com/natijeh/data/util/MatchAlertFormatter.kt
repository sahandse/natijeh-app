package com.natijeh.data.util

import com.natijeh.data.model.MatchAlert
import com.natijeh.data.model.MatchEntity
import com.natijeh.data.model.MatchEvent
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MatchAlertFormatter {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val eventAdapter = moshi.adapter<List<MatchEvent>>(
        Types.newParameterizedType(List::class.java, MatchEvent::class.java)
    )

    fun isWatched(
        match: MatchEntity,
        favoriteTeamIds: Set<String>,
        favoriteLeagueIds: Set<String>,
        favoritePlayerIds: Set<String> = emptySet()
    ): Boolean {
        return match.isFavorite ||
            match.homeTeamId in favoriteTeamIds ||
            match.awayTeamId in favoriteTeamIds ||
            match.leagueId in favoriteLeagueIds ||
            containsFavoritePlayer(match, favoritePlayerIds)
    }

    private fun containsFavoritePlayer(match: MatchEntity, favoritePlayerIds: Set<String>): Boolean {
        if (favoritePlayerIds.isEmpty()) return false
        return eventAdapter.fromJson(match.eventsJson).orEmpty().any {
            it.playerId in favoritePlayerIds || it.extraPlayerId in favoritePlayerIds
        }
    }

    fun alerts(previous: MatchEntity?, current: MatchEntity): List<MatchAlert> {
        if (previous == null) return emptyList()
        val scoreLine = "${current.homeTeamName} ${current.homeScore}-${current.awayScore} ${current.awayTeamName}"
        val out = mutableListOf<MatchAlert>()
        if (previous.status != "LIVE" && current.status == "LIVE") {
            out += MatchAlert(current.id, "شروع بازی", "${current.homeTeamName} - ${current.awayTeamName}", MatchAlert.Kind.KICKOFF)
        }
        if (previous.status == "LIVE" && current.status == "FINISHED") {
            out += MatchAlert(current.id, "پایان بازی", scoreLine, MatchAlert.Kind.FULL_TIME)
        }
        val homeDelta = current.homeScore - previous.homeScore
        val awayDelta = current.awayScore - previous.awayScore
        if (homeDelta > 0 || awayDelta > 0) {
            val scorer = newestGoal(previous, current)
            val title = if (scorer != null) "گل ${scorer.playerName}" else "گل"
            out += MatchAlert(current.id, title, scoreLine, MatchAlert.Kind.GOAL)
        }
        val newReds = newestEvents(previous, current).filter { it.type == "CARD_RED" }
        newReds.forEach { red ->
            out += MatchAlert(
                current.id,
                "کارت قرمز ${red.playerName}",
                scoreLine,
                MatchAlert.Kind.RED_CARD
            )
        }
        return out
    }

    private fun newestGoal(previous: MatchEntity, current: MatchEntity): MatchEvent? {
        return newestEvents(previous, current).lastOrNull { it.type == "GOAL" || it.type == "PENALTY" }
    }

    private fun newestEvents(previous: MatchEntity, current: MatchEntity): List<MatchEvent> {
        val old = eventAdapter.fromJson(previous.eventsJson).orEmpty()
        val now = eventAdapter.fromJson(current.eventsJson).orEmpty()
        val oldKeys = old.map { eventKey(it) }.toSet()
        return now.filter { eventKey(it) !in oldKeys }
    }

    private fun eventKey(event: MatchEvent): String {
        return "${event.minute}|${event.type}|${event.playerName}|${event.isHome}"
    }
}
