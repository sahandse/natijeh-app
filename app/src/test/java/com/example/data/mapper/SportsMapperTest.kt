package com.example.data.mapper

import com.example.data.remote.dto.ApiEvent
import com.example.data.remote.dto.ApiSide
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
}
