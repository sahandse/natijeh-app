package com.natijeh.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfficialStreamResolverTest {
    @Test
    fun scheduledMatchHasNoStreamOptions() {
        assertTrue(OfficialStreamResolver.forMatch(match(status = "SCHEDULED")).isEmpty())
    }

    @Test
    fun iranianLiveMatchIncludesOfficialTv3Route() {
        val streams = OfficialStreamResolver.forMatch(match(status = "LIVE", league = "لیگ برتر ایران"))
        assertTrue(streams.any { it.id == "tv3" })
        assertFalse(streams.any { !it.url.startsWith("https://") })
    }

    @Test
    fun finishedMatchOffersHighlightsInsteadOfLiveTv() {
        val streams = OfficialStreamResolver.forMatch(match(status = "FINISHED"))
        assertTrue(streams.any { it.id == "highlight-v3" })
        assertTrue(streams.any { it.id == "highlight-360" })
        assertFalse(streams.any { it.id == "tv3" })
    }

    private fun match(status: String, league: String = "لالیگا") = MatchEntity(
        id = "1", homeTeamId = "1", homeTeamName = "استقلال", awayTeamId = "2", awayTeamName = "پرسپولیس",
        homeScore = 0, awayScore = 0, status = status, minute = 0, date = "", time = "", leagueId = "1",
        leagueName = league, venue = "", referee = "", eventsJson = "[]", statsJson = "[]", lineupsJson = "[]", h2hJson = "[]"
    )
}
