package com.natijeh.data.mapper

import com.natijeh.data.remote.dto.ApiEvent
import com.natijeh.data.remote.dto.ApiGoals
import com.natijeh.data.remote.dto.ApiSide
import com.natijeh.data.remote.dto.ApiTeamMatchItem
import org.junit.Assert.assertEquals
import org.junit.Test

class SportsMapperTest {

    @Test
    fun `cleanLeagueTitle strips week suffix`() {
        assertEquals("لیگ برتر ایران", SportsMapper.cleanLeagueTitle("لیگ برتر ایران - هفته 7"))
        assertEquals("لالیگا اسپانیا", SportsMapper.cleanLeagueTitle("لالیگا اسپانیا"))
    }

    @Test
    fun `mapStatus uses live flag and numeric codes`() {
        assertEquals("LIVE", SportsMapper.mapStatus(true, 1))
        assertEquals("LIVE", SportsMapper.mapStatus(false, 2))
        assertEquals("FINISHED", SportsMapper.mapStatus(false, 7))
        assertEquals("SCHEDULED", SportsMapper.mapStatus(false, 1))
    }

    @Test
    fun `parseMinute reads live clock and finished matches`() {
        assertEquals(64, SportsMapper.parseMinute("64", 2))
        assertEquals(45, SportsMapper.parseMinute("45+2", 2))
        assertEquals(90, SportsMapper.parseMinute("90", 7))
        assertEquals(0, SportsMapper.parseMinute("", 1))
    }

    @Test
    fun `sideId prefers numeric id then link`() {
        val withId = ApiSide(id = 4, name = "استقلال", link = "/football/team/4/استقلال")
        val fromLink = ApiSide(id = 0, name = "پرسپولیس", link = "/football/team/6/پرسپولیس")
        assertEquals("4", SportsMapper.sideId(withId))
        assertEquals("6", SportsMapper.sideId(fromLink))
    }

    @Test
    fun `mapEvents translates goals cards and substitutions`() {
        val events = SportsMapper.mapEvents(
            listOf(
                ApiEvent(eventType = 1, rawTime = 54, side = 0, strickerName = "عارف رستمی", description = "گل"),
                ApiEvent(eventType = 2, rawTime = 43, side = 1, cardType = 1, offendingPlayerName = "شجاع", description = "زرد"),
                ApiEvent(eventType = 4, rawTime = 61, side = 0, incomingPlayerName = "موسوی", outgoingPlayerName = "بازگیر")
            )
        )
        assertEquals("GOAL", events[1].type)
        assertEquals("CARD_YELLOW", events[0].type)
        assertEquals("SUBSTITUTION", events[2].type)
        assertEquals("موسوی ← بازگیر", events[2].playerName)
    }

    @Test
    fun `iranian leagues are pinned first`() {
        assertEquals(0, SportsMapper.leaguePriority("6", "لیگ برتر ایران"))
        assertEquals(2, SportsMapper.leaguePriority("24", "لیگ آزادگان"))
        assertEquals(20, SportsMapper.leaguePriority("3", "لیگ برتر انگلیس"))
    }

    @Test
    fun `head to head keeps only matches between the two teams`() {
        val home = ApiSide(id = 4, name = "استقلال")
        val away = ApiSide(id = 6, name = "پرسپولیس")
        val other = ApiSide(id = 18, name = "تراکتور")
        val derby = ApiTeamMatchItem(
            id = 1,
            host = home,
            guest = away,
            goals = ApiGoals(host = 1, guest = 0)
        )
        val unrelated = ApiTeamMatchItem(
            id = 2,
            host = home,
            guest = other,
            goals = ApiGoals(host = 2, guest = 2)
        )
        val h2h = SportsMapper.mapHeadToHead("استقلال", "پرسپولیس", "4", "6", listOf(derby, unrelated))
        assertEquals(1, h2h.pastMatches.size)
        assertEquals(1, h2h.homeWins)
        assertEquals(0, h2h.awayWins)
    }
}
