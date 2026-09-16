package com.natijeh.data.util

import com.natijeh.data.model.MatchEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchAlertFormatterTest {
    private fun match(
        homeScore: Int = 0,
        awayScore: Int = 0,
        status: String = "LIVE",
        eventsJson: String = "[]",
        isFavorite: Boolean = false,
        homeTeamId: String = "4",
        leagueId: String = "6",
        minute: Int = 20,
        lineupsJson: String = "{}"
    ) = MatchEntity(
        id = "1",
        homeTeamId = homeTeamId,
        homeTeamName = "استقلال",
        awayTeamId = "6",
        awayTeamName = "پرسپولیس",
        homeScore = homeScore,
        awayScore = awayScore,
        status = status,
        minute = minute,
        date = "",
        time = "19:00",
        leagueId = leagueId,
        leagueName = "لیگ برتر ایران",
        venue = "",
        referee = "",
        eventsJson = eventsJson,
        statsJson = "[]",
        lineupsJson = lineupsJson,
        h2hJson = "{}",
        isFavorite = isFavorite
    )

    @Test
    fun `favorite team match is watched`() {
        assertTrue(MatchAlertFormatter.isWatched(match(), setOf("4"), emptySet()))
    }

    @Test
    fun `score increase creates goal alert`() {
        val alerts = MatchAlertFormatter.alerts(match(homeScore = 0), match(homeScore = 1))
        assertEquals(1, alerts.size)
        assertEquals(com.natijeh.data.model.MatchAlert.Kind.GOAL, alerts.first().kind)
    }

    @Test
    fun `kickoff alert when match goes live`() {
        val alerts = MatchAlertFormatter.alerts(match(status = "SCHEDULED"), match(status = "LIVE"))
        assertTrue(alerts.any { it.kind == com.natijeh.data.model.MatchAlert.Kind.KICKOFF })
    }

    @Test
    fun `confirmed lineup creates lineup alert`() {
        val alerts = MatchAlertFormatter.alerts(match(lineupsJson = "{}"), match(lineupsJson = "{\"homeStarting\":[{\"name\":\"A\"}]}"))
        assertTrue(alerts.any { it.kind == com.natijeh.data.model.MatchAlert.Kind.LINEUP })
    }

    @Test
    fun `minute crossing halftime creates second half alert`() {
        val alerts = MatchAlertFormatter.alerts(match(minute = 45), match(minute = 46))
        assertTrue(alerts.any { it.kind == com.natijeh.data.model.MatchAlert.Kind.SECOND_HALF })
    }
}
