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
                ApiEvent(eventType = 1, rawTime = 54, side = 0, strickerName = "عارف رستمی", strikerId = 901, description = "گل"),
                ApiEvent(eventType = 2, rawTime = 43, side = 1, cardType = 1, offendingPlayerName = "شجاع", offendingPlayerId = 12, description = "زرد"),
                ApiEvent(
                    eventType = 4,
                    rawTime = 61,
                    side = 0,
                    incomingPlayerName = "موسوی",
                    incomingPlayerId = 33,
                    outgoingPlayerName = "بازگیر",
                    outgoingPlayerId = 44
                )
            )
        )
        assertEquals("GOAL", events[1].type)
        assertEquals("901", events[1].playerId)
        assertEquals("CARD_YELLOW", events[0].type)
        assertEquals("12", events[0].playerId)
        assertEquals("SUBSTITUTION", events[2].type)
        assertEquals("موسوی", events[2].playerName)
        assertEquals("33", events[2].playerId)
        assertEquals("بازگیر", events[2].extraPlayerName)
        assertEquals("44", events[2].extraPlayerId)
    }

    @Test
    fun `squadBucket maps persian roles`() {
        assertEquals("GK", SportsMapper.squadBucket("دروازه بان ها"))
        assertEquals("DF", SportsMapper.squadBucket("مدافعان"))
        assertEquals("MF", SportsMapper.squadBucket("هافبک ها"))
        assertEquals("FW", SportsMapper.squadBucket("مهاجمان"))
        assertEquals("OT", SportsMapper.squadBucket("سایر"))
    }

    @Test
    fun `mapSquad keeps player id from numeric or person link`() {
        val fromId = com.natijeh.data.remote.dto.ApiSquadGroup(
            role = "مهاجمان",
            players = listOf(
                com.natijeh.data.remote.dto.ApiSquadPlayer(id = 77, name = "علی", shirtNumber = 9)
            )
        )
        val fromLink = com.natijeh.data.remote.dto.ApiSquadGroup(
            role = "دروازه بان ها",
            players = listOf(
                com.natijeh.data.remote.dto.ApiSquadPlayer(id = 0, name = "حسین", link = "/person/55/حسین")
            )
        )
        val mapped = SportsMapper.mapSquad(listOf(fromId, fromLink))
        assertEquals("77", mapped[0].id)
        assertEquals("FW", SportsMapper.squadBucket(mapped[0].position))
        assertEquals("55", mapped[1].id)
        assertEquals("GK", SportsMapper.squadBucket(mapped[1].position))
    }

    @Test
    fun `resultVersus colors win loss and draw for this team`() {
        val win = com.natijeh.data.model.TeamResultMatch(
            id = "1",
            date = "1404/01/01",
            time = "19:00",
            homeTeam = "استقلال",
            awayTeam = "پرسپولیس",
            homeTeamId = "4",
            awayTeamId = "6",
            homeScore = 2,
            awayScore = 1,
            status = "FINISHED"
        )
        assertEquals("WIN", SportsMapper.resultVersus(win, "4"))
        assertEquals("LOSS", SportsMapper.resultVersus(win, "6"))
        val draw = win.copy(homeScore = 1, awayScore = 1)
        assertEquals("DRAW", SportsMapper.resultVersus(draw, "4"))
        val upcoming = win.copy(homeScore = null, awayScore = null, status = "SCHEDULED")
        assertEquals("SCHEDULED", SportsMapper.resultVersus(upcoming, "4"))
    }

    @Test
    fun `mapTeamResults reads match id from link when numeric id is zero`() {
        val item = ApiTeamMatchItem(
            id = 0,
            date = "1404/01/02",
            time = "17:30",
            status = 7,
            host = ApiSide(id = 0, name = "استقلال", link = "/football/team/4/استقلال"),
            guest = ApiSide(id = 0, name = "پرسپولیس", link = "/football/team/6/پرسپولیس"),
            goals = ApiGoals(host = 1, guest = 0),
            link = "/football/match/888/derby"
        )
        val mapped = SportsMapper.mapTeamResults(listOf(item), "4")
        assertEquals(1, mapped.size)
        assertEquals("888", mapped[0].id)
        assertEquals("4", mapped[0].homeTeamId)
        assertEquals("6", mapped[0].awayTeamId)
        assertEquals("WIN", SportsMapper.resultVersus(mapped[0], "4"))
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
