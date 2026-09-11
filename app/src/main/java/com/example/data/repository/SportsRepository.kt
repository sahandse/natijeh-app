package com.example.data.repository

import android.util.Log
import android.util.Xml
import com.example.data.local.SportsDao
import com.example.data.mapper.SportsMapper
import com.example.data.model.HeadToHeadData
import com.example.data.model.LeagueEntity
import com.example.data.model.MatchEntity
import com.example.data.model.MatchLineups
import com.example.data.model.NewsEntity
import com.example.data.model.TeamEntity
import com.example.data.remote.Varzesh3Service
import com.example.data.remote.dto.ApiLiveMatch
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.util.UUID

class SportsRepository(
    private val dao: SportsDao,
    private val api: Varzesh3Service = Varzesh3Service()
) {
    private val tag = "SportsRepository"
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val eventAdapter = moshi.adapter<List<com.example.data.model.MatchEvent>>(
        Types.newParameterizedType(List::class.java, com.example.data.model.MatchEvent::class.java)
    )
    private val statsAdapter = moshi.adapter<List<com.example.data.model.StatItem>>(
        Types.newParameterizedType(List::class.java, com.example.data.model.StatItem::class.java)
    )
    private val lineupsAdapter = moshi.adapter(MatchLineups::class.java)
    private val h2hAdapter = moshi.adapter(HeadToHeadData::class.java)
    private val standingsAdapter = moshi.adapter<List<com.example.data.model.StandingRow>>(
        Types.newParameterizedType(List::class.java, com.example.data.model.StandingRow::class.java)
    )
    private val squadAdapter = moshi.adapter<List<com.example.data.model.SquadPlayer>>(
        Types.newParameterizedType(List::class.java, com.example.data.model.SquadPlayer::class.java)
    )
    private val recentAdapter = moshi.adapter<List<com.example.data.model.HeadToHeadMatch>>(
        Types.newParameterizedType(List::class.java, com.example.data.model.HeadToHeadMatch::class.java)
    )

    val allMatches: Flow<List<MatchEntity>> = dao.getAllMatches()
    val liveMatches: Flow<List<MatchEntity>> = dao.getLiveMatches()
    val favoriteMatches: Flow<List<MatchEntity>> = dao.getFavoriteMatches()
    val favoriteTeams: Flow<List<TeamEntity>> = dao.getFavoriteTeams()
    val favoriteLeagues: Flow<List<LeagueEntity>> = dao.getFavoriteLeagues()
    val allLeagues: Flow<List<LeagueEntity>> = dao.getAllLeagues()
    val allTeams: Flow<List<TeamEntity>> = dao.getAllTeams()
    val newsFeed: Flow<List<NewsEntity>> = dao.getAllNews()

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollJob: Job? = null

    fun getMatchesByOffset(offset: Int): Flow<List<MatchEntity>> = dao.getMatchesByOffset(offset)
    fun getMatchByIdFlow(id: String): Flow<MatchEntity?> = dao.getMatchByIdFlow(id)
    fun getTeamByIdFlow(id: String): Flow<TeamEntity?> = dao.getTeamByIdFlow(id)
    fun getLeagueByIdFlow(id: String): Flow<LeagueEntity?> = dao.getLeagueByIdFlow(id)

    suspend fun setMatchFavorite(id: String, isFav: Boolean) = dao.setMatchFavorite(id, isFav)
    suspend fun setTeamFavorite(id: String, isFav: Boolean) = dao.setTeamFavorite(id, isFav)
    suspend fun setLeagueFavorite(id: String, isFav: Boolean) = dao.setLeagueFavorite(id, isFav)

    fun startLiveUpdates() {
        if (pollJob?.isActive == true) return
        pollJob = repositoryScope.launch {
            while (isActive) {
                val hasLive = try {
                    dao.getLiveMatches().first().isNotEmpty()
                } catch (_: Exception) {
                    false
                }
                delay(if (hasLive) 20_000 else 45_000)
                try {
                    refreshLiveScore(0)
                } catch (e: Exception) {
                    Log.e(tag, "Live poll failed", e)
                }
            }
        }
    }

    fun stopLiveUpdates() {
        pollJob?.cancel()
        pollJob = null
    }

    suspend fun refreshAll(includeNews: Boolean = false) {
        val errors = mutableListOf<String>()
        listOf(-2, -1, 0, 1, 2).forEach { offset ->
            try {
                refreshLiveScore(offset)
            } catch (e: Exception) {
                Log.e(tag, "Failed livescore offset=$offset", e)
                errors += "offset $offset"
            }
        }
        try {
            refreshDefaultLeagues()
        } catch (e: Exception) {
            Log.e(tag, "Failed default leagues", e)
        }
        if (includeNews) {
            try {
                fetchRssNews()
            } catch (e: Exception) {
                Log.e(tag, "Failed RSS", e)
            }
        }
        if (errors.size == 5) {
            throw IllegalStateException("اتصال به داده زنده برقرار نشد")
        }
    }

    suspend fun refreshLiveScore(offset: Int) = withContext(Dispatchers.IO) {
        val leagues = api.fetchLiveScore(offset).filter { it.sport == 1 }
        val matches = mutableListOf<MatchEntity>()
        val teams = mutableListOf<TeamEntity>()
        val leagueEntities = mutableListOf<LeagueEntity>()
        val favoriteMatches = dao.getFavoriteMatchIds().toSet()
        val favoriteTeams = dao.getFavoriteTeamIds().toSet()
        val favoriteLeagues = dao.getFavoriteLeagueIds().toSet()

        for (league in leagues) {
            val leagueId = (league.id ?: continue).toString()
            val leagueName = SportsMapper.cleanLeagueTitle(league.title.orEmpty().ifBlank { "لیگ" })
            val leagueLogo = league.logo.orEmpty()
            leagueEntities += LeagueEntity(
                id = leagueId,
                name = leagueName,
                logo = leagueLogo,
                country = "",
                standingsJson = "[]",
                isFavorite = leagueId in favoriteLeagues
            )
            for (day in league.dates.orEmpty()) {
                for (match in day.matches.orEmpty()) {
                    val entity = mapLiveMatch(
                        match = match,
                        leagueId = leagueId,
                        leagueName = leagueName,
                        leagueLogo = leagueLogo,
                        offset = offset,
                        isFavorite = match.id?.toString() in favoriteMatches
                    ) ?: continue
                    matches += entity
                    teams += teamStub(entity.homeTeamId, entity.homeTeamName, entity.homeTeamLogo, favoriteTeams)
                    teams += teamStub(entity.awayTeamId, entity.awayTeamName, entity.awayTeamLogo, favoriteTeams)
                }
            }
        }

        val existing = dao.getAllMatches().first().associateBy { it.id }
        val mergedMatches = matches.map { incoming -> mergeMatch(incoming, existing[incoming.id]) }
        if (mergedMatches.isNotEmpty()) {
            dao.insertMatches(mergedMatches)
            dao.deleteStaleMatches(offset, mergedMatches.map { it.id })
        } else {
            dao.deleteMatchesByOffset(offset)
        }
        mergedMatches.filter { it.status == "LIVE" }.forEach { liveMatch ->
            try {
                loadMatchDetails(liveMatch.id)
            } catch (e: Exception) {
                Log.w(tag, "Live detail refresh failed for ${liveMatch.id}", e)
            }
        }
        if (teams.isNotEmpty()) mergeTeams(teams)
        if (leagueEntities.isNotEmpty()) mergeLeagues(leagueEntities)
        Log.d(tag, "Stored ${mergedMatches.size} live matches for offset $offset")
    }

    suspend fun loadMatchDetails(matchId: String) = withContext(Dispatchers.IO) {
        val detail = api.fetchMatch(matchId) ?: return@withContext
        val existing = dao.getMatchById(matchId)
        val host = detail.host
        val guest = detail.guest
        val leagueLinkId = SportsMapper.parseIdFromLink(detail.league?.link, "league")
        val leagueId = leagueLinkId ?: existing?.leagueId.orEmpty()
        val (homeScore, awayScore) = (detail.goals?.host ?: existing?.homeScore ?: 0) to
            (detail.goals?.guest ?: existing?.awayScore ?: 0)
        val events = SportsMapper.mapEvents(detail.events)
        val stats = SportsMapper.mapStats(detail.stats)
        val lineups = SportsMapper.mapLineups(detail.lineup?.host, detail.lineup?.guest)
        val h2h = try {
            val results = api.fetchTeamResults(SportsMapper.sideId(host))?.items
            SportsMapper.mapHeadToHead(host?.name.orEmpty(), guest?.name.orEmpty(), results)
        } catch (e: Exception) {
            Log.w(tag, "H2H unavailable", e)
            existing?.h2hJson?.let { h2hAdapter.fromJson(it) } ?: HeadToHeadData(0, 0, 0, emptyList())
        }
        val updated = MatchEntity(
            id = matchId,
            homeTeamId = SportsMapper.sideId(host),
            homeTeamName = host?.name ?: existing?.homeTeamName.orEmpty(),
            homeTeamLogo = host?.logo ?: existing?.homeTeamLogo.orEmpty(),
            awayTeamId = SportsMapper.sideId(guest),
            awayTeamName = guest?.name ?: existing?.awayTeamName.orEmpty(),
            awayTeamLogo = guest?.logo ?: existing?.awayTeamLogo.orEmpty(),
            homeScore = homeScore,
            awayScore = awayScore,
            status = SportsMapper.mapStatus(detail.isLive == true, detail.status),
            statusTitle = detail.statusTitle?.ifBlank { existing?.statusTitle }.orEmpty(),
            liveTime = existing?.liveTime.orEmpty(),
            minute = SportsMapper.parseMinute(existing?.liveTime, detail.status),
            date = detail.date ?: existing?.date.orEmpty(),
            time = detail.time ?: existing?.time.orEmpty(),
            utcStart = detail.scheduledStartOnUtc ?: existing?.utcStart.orEmpty(),
            dayOffset = existing?.dayOffset ?: 0,
            leagueId = leagueId,
            leagueName = detail.league?.title ?: existing?.leagueName.orEmpty(),
            leagueLogo = detail.league?.logo ?: existing?.leagueLogo.orEmpty(),
            venue = detail.stadium.orEmpty(),
            referee = detail.referee.orEmpty(),
            attendance = existing?.attendance.orEmpty(),
            eventsJson = eventAdapter.toJson(events),
            statsJson = statsAdapter.toJson(stats),
            lineupsJson = lineupsAdapter.toJson(lineups),
            h2hJson = h2hAdapter.toJson(h2h),
            isFavorite = existing?.isFavorite == true
        )
        dao.insertMatches(listOf(updated))

        val favTeams = dao.getFavoriteTeamIds().toSet()
        mergeTeams(
            listOf(
                teamStub(updated.homeTeamId, updated.homeTeamName, updated.homeTeamLogo, favTeams)
                    .copy(coach = lineups.homeCoach, stadium = updated.venue, formation = lineups.homeFormation),
                teamStub(updated.awayTeamId, updated.awayTeamName, updated.awayTeamLogo, favTeams)
                    .copy(coach = lineups.awayCoach, formation = lineups.awayFormation)
            )
        )
    }

    suspend fun loadLeagueDetails(leagueId: String) = withContext(Dispatchers.IO) {
        val league = api.fetchLeague(leagueId) ?: return@withContext
        val standingHref = league.tabs
            ?.firstOrNull { it.type == 1 || it.title?.contains("جدول") == true }
            ?.links
            ?.firstOrNull { it.rel == "get" || it.href?.contains("standing") == true }
            ?.href
        val standings = if (!standingHref.isNullOrBlank()) {
            try {
                SportsMapper.mapStandings(api.fetchStandings(standingHref)?.teams)
            } catch (e: Exception) {
                Log.e(tag, "Standings failed for $leagueId", e)
                emptyList()
            }
        } else {
            emptyList()
        }
        val fav = dao.getFavoriteLeagueIds().contains(leagueId)
        dao.insertLeagues(
            listOf(
                LeagueEntity(
                    id = leagueId,
                    name = league.name ?: league.title ?: "لیگ",
                    logo = league.logo.orEmpty(),
                    country = "",
                    standingsJson = standingsAdapter.toJson(standings),
                    standingUrl = standingHref.orEmpty(),
                    isFavorite = fav
                )
            )
        )
        val favTeams = dao.getFavoriteTeamIds().toSet()
        if (standings.isNotEmpty()) {
            mergeTeams(
                standings.map { row ->
                    teamStub(row.teamId, row.teamName, row.teamLogo, favTeams)
                }
            )
        }
    }

    suspend fun loadTeamDetails(teamId: String) = withContext(Dispatchers.IO) {
        val team = api.fetchTeam(teamId)
        val squad = try {
            SportsMapper.mapSquad(api.fetchSquad(teamId))
        } catch (e: Exception) {
            Log.e(tag, "Squad failed", e)
            emptyList()
        }
        val glance = try {
            api.fetchTeamGlance(teamId)?.carousel?.matches.orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
        val recent = glance.map {
            com.example.data.model.HeadToHeadMatch(
                date = it.date.orEmpty(),
                homeTeam = it.host?.name.orEmpty(),
                awayTeam = it.guest?.name.orEmpty(),
                score = if (it.goals?.host != null && it.goals.guest != null) {
                    "${it.goals.host} - ${it.goals.guest}"
                } else {
                    it.time.orEmpty()
                }
            )
        }
        val standingLabel = try {
            val standing = api.fetchStandings("https://web-api.varzesh3.com/v2.0/football/teams/$teamId/standing")
            val row = standing?.teams?.firstOrNull { it.id?.toString() == teamId }
                ?: standing?.teams?.firstOrNull { it.name == team?.name }
            if (row != null) {
                "رتبه ${row.rank} | ${row.points} امتیاز | ${standing?.title.orEmpty()}"
            } else {
                standing?.title.orEmpty()
            }
        } catch (_: Exception) {
            ""
        }
        val prev = dao.getTeamById(teamId)
        dao.insertTeams(
            listOf(
                TeamEntity(
                    id = teamId,
                    name = team?.name ?: prev?.name ?: teamId,
                    logo = team?.logo?.ifBlank { prev?.logo.orEmpty() } ?: prev?.logo.orEmpty(),
                    coach = prev?.coach.orEmpty(),
                    stadium = prev?.stadium.orEmpty(),
                    formation = prev?.formation.orEmpty(),
                    squadJson = squadAdapter.toJson(squad),
                    recentJson = recentAdapter.toJson(recent),
                    rankLabel = standingLabel,
                    isFavorite = prev?.isFavorite == true
                )
            )
        )
    }

    suspend fun fetchRssNews(): Unit = withContext(Dispatchers.IO) {
        val feeds = listOf(
            "https://www.varzesh3.com/rss/domesticfootball" to "ورزش ۳ (فوتبال داخلی)",
            "https://www.varzesh3.com/rss/foreignfootball" to "ورزش ۳ (فوتبال خارجی)"
        )
        val parsed = mutableListOf<NewsEntity>()
        for ((url, sourceName) in feeds) {
            try {
                parsed += parseRssXml(api.fetchRss(url), sourceName)
            } catch (e: Exception) {
                Log.e(tag, "RSS failed $url", e)
            }
        }
        if (parsed.isNotEmpty()) dao.insertNews(parsed)
    }

    private suspend fun refreshDefaultLeagues() {
        val iran = api.fetchLeague("6") ?: return
        val related = iran.leagues.orEmpty()
        val fav = dao.getFavoriteLeagueIds().toSet()
        val entities = related.mapNotNull { item ->
            val id = item.id?.toString() ?: return@mapNotNull null
            LeagueEntity(
                id = id,
                name = item.title.orEmpty(),
                logo = item.logo.orEmpty(),
                standingsJson = "[]",
                isFavorite = id in fav
            )
        }
        if (entities.isNotEmpty()) mergeLeagues(entities)
    }

    private fun mapLiveMatch(
        match: ApiLiveMatch,
        leagueId: String,
        leagueName: String,
        leagueLogo: String,
        offset: Int,
        isFavorite: Boolean
    ): MatchEntity? {
        val id = match.id?.toString() ?: return null
        val host = match.host ?: return null
        val guest = match.guest ?: return null
        val (homeScore, awayScore) = SportsMapper.liveMatchScore(match)
        val emptyLineup = MatchLineups("-", "-", emptyList(), emptyList(), emptyList(), emptyList(), "", "")
        return MatchEntity(
            id = id,
            homeTeamId = SportsMapper.sideId(host),
            homeTeamName = host.name.orEmpty(),
            homeTeamLogo = host.logo.orEmpty(),
            awayTeamId = SportsMapper.sideId(guest),
            awayTeamName = guest.name.orEmpty(),
            awayTeamLogo = guest.logo.orEmpty(),
            homeScore = homeScore,
            awayScore = awayScore,
            status = SportsMapper.mapStatus(match.isLive == true, match.status),
            statusTitle = match.statusTitle.orEmpty(),
            liveTime = match.liveTime.orEmpty(),
            minute = SportsMapper.parseMinute(match.liveTime, match.status),
            date = match.date.orEmpty(),
            time = match.time.orEmpty(),
            utcStart = match.startOnUtc.orEmpty(),
            dayOffset = offset,
            leagueId = leagueId,
            leagueName = leagueName,
            leagueLogo = leagueLogo,
            venue = "",
            referee = "",
            eventsJson = eventAdapter.toJson(emptyList()),
            statsJson = statsAdapter.toJson(emptyList()),
            lineupsJson = lineupsAdapter.toJson(emptyLineup),
            h2hJson = h2hAdapter.toJson(HeadToHeadData(0, 0, 0, emptyList())),
            isFavorite = isFavorite
        )
    }

    private fun mergeMatch(incoming: MatchEntity, existing: MatchEntity?): MatchEntity {
        if (existing == null) return incoming
        val emptyEvents = eventAdapter.toJson(emptyList())
        val emptyStats = statsAdapter.toJson(emptyList())
        return incoming.copy(
            eventsJson = if (incoming.eventsJson != emptyEvents) incoming.eventsJson else existing.eventsJson,
            statsJson = if (incoming.statsJson == emptyStats) existing.statsJson else incoming.statsJson,
            lineupsJson = if (incoming.venue.isBlank() && existing.lineupsJson.isNotBlank()) existing.lineupsJson else incoming.lineupsJson,
            h2hJson = if (incoming.venue.isBlank()) existing.h2hJson else incoming.h2hJson,
            venue = incoming.venue.ifBlank { existing.venue },
            referee = incoming.referee.ifBlank { existing.referee },
            isFavorite = existing.isFavorite || incoming.isFavorite
        )
    }

    private fun teamStub(id: String, name: String, logo: String, favorites: Set<String>): TeamEntity {
        return TeamEntity(
            id = id,
            name = name,
            logo = logo,
            squadJson = "[]",
            isFavorite = id in favorites
        )
    }

    private suspend fun mergeTeams(incoming: List<TeamEntity>) {
        val existing = dao.getAllTeams().first().associateBy { it.id }
        val merged = incoming.map { stub ->
            val prev = existing[stub.id]
            stub.copy(
                coach = stub.coach.ifBlank { prev?.coach.orEmpty() },
                stadium = stub.stadium.ifBlank { prev?.stadium.orEmpty() },
                squadJson = if (stub.squadJson != "[]") stub.squadJson else prev?.squadJson ?: "[]",
                formation = stub.formation.ifBlank { prev?.formation.orEmpty() },
                honoursJson = stub.honoursJson.ifBlank { prev?.honoursJson ?: "[]" },
                recentJson = if (stub.recentJson != "[]") stub.recentJson else prev?.recentJson ?: "[]",
                rankLabel = stub.rankLabel.ifBlank { prev?.rankLabel.orEmpty() },
                logo = stub.logo.ifBlank { prev?.logo.orEmpty() },
                isFavorite = stub.isFavorite || prev?.isFavorite == true
            )
        }
        dao.insertTeams(merged)
    }

    private suspend fun mergeLeagues(incoming: List<LeagueEntity>) {
        val existing = dao.getAllLeagues().first().associateBy { it.id }
        val merged = incoming.map { stub ->
            val prev = existing[stub.id]
            stub.copy(
                standingsJson = if (stub.standingsJson != "[]") stub.standingsJson else prev?.standingsJson ?: "[]",
                standingUrl = stub.standingUrl.ifBlank { prev?.standingUrl.orEmpty() },
                logo = stub.logo.ifBlank { prev?.logo.orEmpty() },
                isFavorite = stub.isFavorite || prev?.isFavorite == true
            )
        }
        dao.insertLeagues(merged)
    }

    private fun parseRssXml(xml: String, sourceName: String): List<NewsEntity> {
        val items = mutableListOf<NewsEntity>()
        try {
            val parser = Xml.newPullParser()
            parser.setInput(xml.reader())
            var eventType = parser.eventType
            var currentTitle = ""
            var currentLink = ""
            var currentDescription = ""
            var currentPubDate = ""
            var currentImage = ""
            var insideItem = false
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            insideItem = true
                            currentTitle = ""
                            currentLink = ""
                            currentDescription = ""
                            currentPubDate = ""
                            currentImage = ""
                        } else if (insideItem) {
                            when {
                                tagName.equals("title", ignoreCase = true) -> currentTitle = parser.nextText().trim()
                                tagName.equals("link", ignoreCase = true) -> currentLink = parser.nextText().trim()
                                tagName.equals("description", ignoreCase = true) -> currentDescription = parser.nextText().trim()
                                tagName.equals("pubDate", ignoreCase = true) -> currentPubDate = parser.nextText().trim()
                                tagName.equals("enclosure", ignoreCase = true) -> {
                                    val urlAttr = parser.getAttributeValue(null, "url")
                                    if (!urlAttr.isNullOrBlank()) currentImage = urlAttr
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            insideItem = false
                            if (currentTitle.isNotEmpty()) {
                                val rawString = currentLink.ifEmpty { currentTitle }
                                val id = try {
                                    val digest = java.security.MessageDigest.getInstance("MD5")
                                    digest.digest(rawString.toByteArray()).joinToString("") { "%02x".format(it) }
                                } catch (_: Exception) {
                                    UUID.randomUUID().toString()
                                }
                                val cleanSummary = currentDescription.replace(Regex("<[^>]*>"), "").replace("&nbsp;", " ").trim()
                                var image = currentImage.ifBlank {
                                    Regex("""<img[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                                        .find(currentDescription)?.groupValues?.get(1).orEmpty()
                                }
                                if (image.isEmpty()) {
                                    image = if (sourceName.contains("داخلی")) {
                                        "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?q=80&w=600&auto=format&fit=crop"
                                    } else {
                                        "https://images.unsplash.com/photo-1518063319789-7217e6706b04?q=80&w=600&auto=format&fit=crop"
                                    }
                                }
                                items += NewsEntity(
                                    id = id,
                                    title = currentTitle,
                                    summary = if (cleanSummary.length > 200) cleanSummary.take(200) + "..." else cleanSummary,
                                    content = cleanSummary,
                                    imageUrl = image,
                                    date = currentPubDate,
                                    source = sourceName
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(tag, "XML parse error for $sourceName", e)
        }
        return items
    }

}
