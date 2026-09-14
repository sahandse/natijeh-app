package com.natijeh.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Xml
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import com.natijeh.data.local.SportsDao
import com.natijeh.data.mapper.SportsMapper
import com.natijeh.data.model.FixtureRound
import com.natijeh.data.model.HeadToHeadData
import com.natijeh.data.model.LeagueEntity
import com.natijeh.data.model.MatchEntity
import com.natijeh.data.model.MatchLineups
import com.natijeh.data.model.NewsEntity
import com.natijeh.data.model.PlayerEntity
import com.natijeh.data.model.ProfileKnowledgeEntity
import com.natijeh.data.model.NotificationHistoryEntity
import com.natijeh.data.model.MatchAlert
import com.natijeh.data.model.TeamEntity
import com.natijeh.data.model.TeamResultMatch
import com.natijeh.data.notify.GoalNotifier
import com.natijeh.data.notify.LiveScoreService
import com.natijeh.data.remote.Varzesh3Service
import com.natijeh.data.remote.dto.ApiLiveMatch
import com.natijeh.data.settings.SettingsStore
import com.natijeh.data.util.JalaliDate
import com.natijeh.data.util.MatchAlertFormatter
import com.natijeh.widget.NatijehScoreWidget
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
import java.io.File
import java.io.FileOutputStream
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject

class SportsRepository(
    private val dao: SportsDao,
    private val appContext: Context,
    private val settingsStore: SettingsStore,
    private val api: Varzesh3Service = Varzesh3Service()
) {
    private val tag = "SportsRepository"
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val eventAdapter = moshi.adapter<List<com.natijeh.data.model.MatchEvent>>(
        Types.newParameterizedType(List::class.java, com.natijeh.data.model.MatchEvent::class.java)
    )
    private val statsAdapter = moshi.adapter<List<com.natijeh.data.model.StatItem>>(
        Types.newParameterizedType(List::class.java, com.natijeh.data.model.StatItem::class.java)
    )
    private val lineupsAdapter = moshi.adapter(MatchLineups::class.java)
    private val h2hAdapter = moshi.adapter(HeadToHeadData::class.java)
    private val standingsAdapter = moshi.adapter<List<com.natijeh.data.model.StandingRow>>(
        Types.newParameterizedType(List::class.java, com.natijeh.data.model.StandingRow::class.java)
    )
    private val squadAdapter = moshi.adapter<List<com.natijeh.data.model.SquadPlayer>>(
        Types.newParameterizedType(List::class.java, com.natijeh.data.model.SquadPlayer::class.java)
    )
    private val recentAdapter = moshi.adapter<List<TeamResultMatch>>(
        Types.newParameterizedType(List::class.java, TeamResultMatch::class.java)
    )
    private val scorersAdapter = moshi.adapter<List<com.natijeh.data.model.ScorerRow>>(
        Types.newParameterizedType(List::class.java, com.natijeh.data.model.ScorerRow::class.java)
    )
    private val fixturesAdapter = moshi.adapter<List<FixtureRound>>(
        Types.newParameterizedType(List::class.java, FixtureRound::class.java)
    )
    private val notifier = GoalNotifier(appContext, settingsStore)
    private val imageLoader = ImageLoader(appContext)
    private val httpClient = OkHttpClient()

    val allMatches: Flow<List<MatchEntity>> = dao.getAllMatches()
    val liveMatches: Flow<List<MatchEntity>> = dao.getLiveMatches()
    val favoriteMatches: Flow<List<MatchEntity>> = dao.getFavoriteMatches()
    val favoriteTeams: Flow<List<TeamEntity>> = dao.getFavoriteTeams()
    val favoriteLeagues: Flow<List<LeagueEntity>> = dao.getFavoriteLeagues()
    val allLeagues: Flow<List<LeagueEntity>> = dao.getAllLeagues()
    val allTeams: Flow<List<TeamEntity>> = dao.getAllTeams()
    val allPlayers: Flow<List<PlayerEntity>> = dao.getAllPlayers()
    val favoritePlayers: Flow<List<PlayerEntity>> = dao.getFavoritePlayers()
    val newsFeed: Flow<List<NewsEntity>> = dao.getAllNews()
    val notificationHistory: Flow<List<NotificationHistoryEntity>> = dao.getNotificationHistory()

    suspend fun markNotificationRead(id: String) = dao.markNotificationRead(id)
    suspend fun markAllNotificationsRead() = dao.markAllNotificationsRead()

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollJob: Job? = null

    fun getMatchesByOffset(offset: Int): Flow<List<MatchEntity>> = dao.getMatchesByOffset(offset)
    fun getMatchByIdFlow(id: String): Flow<MatchEntity?> = dao.getMatchByIdFlow(id)
    fun getTeamByIdFlow(id: String): Flow<TeamEntity?> = dao.getTeamByIdFlow(id)
    fun getLeagueByIdFlow(id: String): Flow<LeagueEntity?> = dao.getLeagueByIdFlow(id)
    fun getPlayerByIdFlow(id: String): Flow<PlayerEntity?> = dao.getPlayerByIdFlow(id)
    fun getProfileKnowledgeFlow(type: String, id: String): Flow<ProfileKnowledgeEntity?> = dao.getProfileKnowledge("$type:$id")

    suspend fun loadProfileKnowledge(type: String, id: String, displayName: String) = withContext(Dispatchers.IO) {
        if (displayName.isBlank()) return@withContext
        val key = "$type:$id"
        val cached = dao.getProfileKnowledgeOnce(key)
        if (cached != null && System.currentTimeMillis() - cached.updatedAt < 7 * 24 * 60 * 60 * 1000L) return@withContext
        val suffix = when (type) { "player" -> "فوتبالیست"; "team" -> "باشگاه فوتبال"; else -> "لیگ فوتبال" }
        fetchWikipedia(displayName, suffix)?.let { info ->
            dao.insertProfileKnowledge(info.copy(key = key, entityType = type, entityId = id))
        }
    }

    private fun fetchWikipedia(displayName: String, suffix: String): ProfileKnowledgeEntity? {
        val url = "https://fa.wikipedia.org/w/api.php".toHttpUrl().newBuilder()
            .addQueryParameter("action", "query")
            .addQueryParameter("format", "json")
            .addQueryParameter("formatversion", "2")
            .addQueryParameter("generator", "search")
            .addQueryParameter("gsrsearch", "$displayName $suffix")
            .addQueryParameter("gsrlimit", "1")
            .addQueryParameter("prop", "extracts|pageimages|info")
            .addQueryParameter("explaintext", "1")
            .addQueryParameter("exsectionformat", "plain")
            .addQueryParameter("piprop", "thumbnail")
            .addQueryParameter("pithumbsize", "800")
            .addQueryParameter("inprop", "url")
            .addQueryParameter("origin", "*")
            .build()
        val request = Request.Builder().url(url).header("User-Agent", "Natijeh/1.12 (https://github.com/sahandse/natijeh-app)").build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val page = JSONObject(response.body?.string().orEmpty()).optJSONObject("query")
                    ?.optJSONArray("pages")?.optJSONObject(0) ?: return@use null
                val extract = page.optString("extract").trim()
                if (extract.isBlank()) return@use null
                ProfileKnowledgeEntity(
                    key = "",
                    entityType = "",
                    entityId = "",
                    title = page.optString("title", displayName),
                    description = extract.take(12_000),
                    imageUrl = page.optJSONObject("thumbnail")?.optString("source").orEmpty(),
                    articleUrl = page.optString("fullurl"),
                    updatedAt = System.currentTimeMillis()
                )
            }
        }.getOrNull()
    }

    suspend fun setMatchFavorite(id: String, isFav: Boolean) = dao.setMatchFavorite(id, isFav)
    suspend fun setTeamFavorite(id: String, isFav: Boolean) = dao.setTeamFavorite(id, isFav)
    suspend fun setLeagueFavorite(id: String, isFav: Boolean) = dao.setLeagueFavorite(id, isFav)

    suspend fun setPlayerFavorite(id: String, isFav: Boolean) {
        if (dao.getPlayerById(id) == null) {
            loadPlayer(id)
        }
        dao.setPlayerFavorite(id, isFav)
    }

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
        try {
            loadLeagueDetails(SportsMapper.IRAN_PREMIER_LEAGUE_ID)
        } catch (e: Exception) {
            Log.e(tag, "Failed premier league index", e)
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

    suspend fun downloadOfflineData(onProgress: (Int) -> Unit = {}): Int = withContext(Dispatchers.IO) {
        onProgress(5)
        refreshAll(includeNews = true)
        onProgress(20)
        val imageUrls = buildSet {
            dao.getAllTeams().first().forEach { add(it.logo) }
            dao.getAllLeagues().first().forEach { add(it.logo) }
            dao.getAllPlayers().first().forEach { add(it.portrait); add(it.teamLogo) }
            dao.getAllMatches().first().forEach {
                add(it.homeTeamLogo); add(it.awayTeamLogo); add(it.leagueLogo)
            }
        }.filter { it.startsWith("https://") }
        imageUrls.forEachIndexed { index, url ->
            runCatching { imageLoader.execute(ImageRequest.Builder(appContext).data(url).build()) }
            onProgress(20 + ((index + 1) * 80 / imageUrls.size.coerceAtLeast(1)))
        }
        onProgress(100)
        imageUrls.size
    }

    suspend fun clearOfflineImages() = withContext(Dispatchers.IO) {
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }

    data class ReleaseInfo(val version: String, val pageUrl: String, val apkUrl: String)

    suspend fun latestRelease(): ReleaseInfo = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://api.github.com/repos/sahandse/natijeh-app/releases/latest")
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "natijeh-android")
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("GitHub ${response.code}")
            val json = response.body?.string().orEmpty()
            val tag = Regex("\\\"tag_name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(json)?.groupValues?.get(1)
                ?: error("release tag missing")
            val url = Regex("\\\"html_url\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(json)?.groupValues?.get(1)
                ?: error("release url missing")
            val apkUrl = Regex("\\\"browser_download_url\\\"\\s*:\\s*\\\"([^\\\"]+\\.apk)\\\"")
                .find(json)?.groupValues?.get(1)
                ?: error("APK asset missing")
            ReleaseInfo(tag.removePrefix("v"), url, apkUrl)
        }
    }

    suspend fun downloadUpdateApk(version: String, url: String, onProgress: (Int) -> Unit): Uri = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).header("User-Agent", "natijeh-android").build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Download ${response.code}")
            val body = response.body ?: error("Empty APK")
            val total = body.contentLength()
            val targetDir = File(appContext.cacheDir, "updates").apply { mkdirs() }
            val target = File(targetDir, "natijeh-$version.apk")
            body.byteStream().use { input ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var downloaded = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (total > 0) onProgress(((downloaded * 100) / total).toInt().coerceIn(0, 100))
                    }
                }
            }
            onProgress(100)
            FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", target)
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
        val favoritePlayers = watchedPlayerIds()
        val now = System.currentTimeMillis()

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
                        isFavorite = match.id?.toString() in favoriteMatches,
                        now = now
                    ) ?: continue
                    matches += entity
                    teams += teamStub(entity.homeTeamId, entity.homeTeamName, entity.homeTeamLogo, favoriteTeams)
                    teams += teamStub(entity.awayTeamId, entity.awayTeamName, entity.awayTeamLogo, favoriteTeams)
                }
            }
        }

        val existing = dao.getAllMatches().first().associateBy { it.id }
        val mergedMatches = matches.map { incoming -> mergeMatch(incoming, existing[incoming.id]) }
        emitWatchAlerts(existing, mergedMatches, favoriteTeams, favoriteLeagues, favoritePlayers)
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
        syncLiveTracking()
        NatijehScoreWidget.updateAll(appContext, this@SportsRepository)
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
        val homeId = SportsMapper.sideId(host)
        val awayId = SportsMapper.sideId(guest)
        val h2h = try {
            val homeResults = api.fetchTeamResults(homeId)?.items.orEmpty()
            val awayResults = api.fetchTeamResults(awayId)?.items.orEmpty()
            val combined = (homeResults + awayResults).distinctBy { it.id ?: "${it.date}${it.host?.name}${it.guest?.name}" }
            SportsMapper.mapHeadToHead(host?.name.orEmpty(), guest?.name.orEmpty(), homeId, awayId, combined)
        } catch (e: Exception) {
            Log.w(tag, "H2H unavailable", e)
            existing?.h2hJson?.let { h2hAdapter.fromJson(it) } ?: HeadToHeadData(0, 0, 0, emptyList())
        }
        val updated = MatchEntity(
            id = matchId,
            homeTeamId = homeId,
            homeTeamName = host?.name ?: existing?.homeTeamName.orEmpty(),
            homeTeamLogo = host?.logo ?: existing?.homeTeamLogo.orEmpty(),
            awayTeamId = awayId,
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
            lastUpdatedMillis = System.currentTimeMillis(),
            isFavorite = existing?.isFavorite == true
        )
        val favoriteTeams = dao.getFavoriteTeamIds().toSet()
        val favoriteLeagues = dao.getFavoriteLeagueIds().toSet()
        val favoritePlayers = watchedPlayerIds()
        if (existing != null && MatchAlertFormatter.isWatched(updated, favoriteTeams, favoriteLeagues, favoritePlayers)) {
            notifyAndStore(MatchAlertFormatter.alerts(existing, updated))
        }
        dao.insertMatches(listOf(updated))

        mergeTeams(
            listOf(
                teamStub(updated.homeTeamId, updated.homeTeamName, updated.homeTeamLogo, favoriteTeams)
                    .copy(coach = lineups.homeCoach, stadium = updated.venue, formation = lineups.homeFormation),
                teamStub(updated.awayTeamId, updated.awayTeamName, updated.awayTeamLogo, favoriteTeams)
                    .copy(coach = lineups.awayCoach, formation = lineups.awayFormation)
            )
        )
    }

    suspend fun loadLeagueDetails(leagueId: String) = withContext(Dispatchers.IO) {
        val league = api.fetchLeague(leagueId) ?: return@withContext
        val standingHref = SportsMapper.tabHref(league.tabs, 1, "standing")
        val fixturesHref = SportsMapper.tabHref(league.tabs, 2)
        val scorersHref = SportsMapper.tabHref(league.tabs, 3, "players")
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
        val scorers = if (!scorersHref.isNullOrBlank()) {
            try {
                SportsMapper.mapScorers(api.fetchPlayerStats(scorersHref))
            } catch (e: Exception) {
                Log.e(tag, "Scorers failed for $leagueId", e)
                emptyList()
            }
        } else {
            emptyList()
        }
        val fixtures = if (!fixturesHref.isNullOrBlank()) {
            try {
                fetchFixturePages(fixturesHref)
            } catch (e: Exception) {
                Log.e(tag, "Fixtures failed for $leagueId", e)
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
                    scorersJson = scorersAdapter.toJson(scorers),
                    fixturesJson = fixturesAdapter.toJson(fixtures),
                    isFavorite = fav
                )
            )
        )
        val favTeams = dao.getFavoriteTeamIds().toSet()
        val fromStandings = standings.map { row -> teamStub(row.teamId, row.teamName, row.teamLogo, favTeams) }
        val fromScorers = scorers.map { row -> teamStub(row.teamId, row.teamName, row.teamLogo, favTeams) }
        val mergedIndex = (fromStandings + fromScorers).distinctBy { it.id }.filter { it.id.isNotBlank() && it.id != "0" }
        if (mergedIndex.isNotEmpty()) mergeTeams(mergedIndex)
    }

    suspend fun loadTeamDetails(teamId: String) = withContext(Dispatchers.IO) {
        val team = api.fetchTeam(teamId)
        val squad = try {
            SportsMapper.mapSquad(api.fetchSquad(teamId))
        } catch (e: Exception) {
            Log.e(tag, "Squad failed", e)
            emptyList()
        }
        val recent = try {
            SportsMapper.mapTeamResults(api.fetchTeamResults(teamId)?.items, teamId)
        } catch (e: Exception) {
            Log.e(tag, "Team results failed", e)
            emptyList()
        }
        val prev = dao.getTeamById(teamId)
        var rank = prev?.rank ?: 0
        var points = prev?.points ?: 0
        var won = prev?.won ?: 0
        var drawn = prev?.drawn ?: 0
        var lost = prev?.lost ?: 0
        var played = prev?.played ?: 0
        var goalsFor = prev?.goalsFor ?: 0
        var goalsAgainst = prev?.goalsAgainst ?: 0
        var leagueName = prev?.leagueName.orEmpty()
        var standingLabel = prev?.rankLabel.orEmpty()
        try {
            val standing = api.fetchStandings("${Varzesh3Service.BASE}/football/teams/$teamId/standing")
            val row = standing?.teams?.firstOrNull { it.id?.toString() == teamId }
                ?: standing?.teams?.firstOrNull { it.name == team?.name }
            val title = SportsMapper.cleanLeagueTitle(standing?.title.orEmpty())
            if (title.isNotBlank()) leagueName = title
            if (row != null) {
                rank = row.rank ?: 0
                points = row.points ?: 0
                won = row.wins ?: 0
                drawn = row.draws ?: 0
                lost = row.losses ?: 0
                played = row.played ?: 0
                goalsFor = row.goalFor ?: 0
                goalsAgainst = row.goalAgainst ?: 0
            }
            standingLabel = buildString {
                if (rank > 0) append("رتبه $rank")
                if (leagueName.isNotBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(leagueName)
                }
            }.ifBlank { standingLabel }
        } catch (e: Exception) {
            Log.w(tag, "Team standing failed", e)
        }
        var coach = prev?.coach.orEmpty()
        var formation = prev?.formation.orEmpty()
        val latestFinished = recent.firstOrNull { it.status == "FINISHED" && it.id.isNotBlank() }
        if (latestFinished != null) {
            try {
                val detail = api.fetchMatch(latestFinished.id)
                if (detail != null) {
                    val lineups = SportsMapper.mapLineups(detail.lineup?.host, detail.lineup?.guest)
                    val isHome = SportsMapper.sideId(detail.host) == teamId
                    val fromMatch = if (isHome) lineups.homeCoach else lineups.awayCoach
                    if (fromMatch.isNotBlank()) coach = fromMatch
                    val form = if (isHome) lineups.homeFormation else lineups.awayFormation
                    if (form.isNotBlank() && form != "-") formation = form
                }
            } catch (e: Exception) {
                Log.w(tag, "Coach from lineup failed", e)
            }
        }
        dao.insertTeams(
            listOf(
                TeamEntity(
                    id = teamId,
                    name = team?.name ?: prev?.name ?: teamId,
                    logo = team?.logo?.ifBlank { prev?.logo.orEmpty() } ?: prev?.logo.orEmpty(),
                    coach = coach,
                    stadium = "",
                    formation = formation,
                    squadJson = squadAdapter.toJson(squad),
                    recentJson = recentAdapter.toJson(recent),
                    rankLabel = standingLabel,
                    rank = rank,
                    points = points,
                    won = won,
                    drawn = drawn,
                    lost = lost,
                    played = played,
                    goalsFor = goalsFor,
                    goalsAgainst = goalsAgainst,
                    leagueName = leagueName,
                    isFavorite = prev?.isFavorite == true
                )
            )
        )
    }

    suspend fun loadPlayer(playerId: String) = withContext(Dispatchers.IO) {
        if (playerId.isBlank() || playerId == "0") return@withContext
        val prev = dao.getPlayerById(playerId)
        val detail = try {
            api.fetchPlayer(playerId)
        } catch (e: Exception) {
            Log.e(tag, "Player $playerId failed", e)
            null
        }
        if (detail == null && prev == null) {
            dao.insertPlayers(listOf(PlayerEntity(id = playerId, name = "بازیکن")))
            return@withContext
        }
        val goals = dao.getAllLeagues().first()
            .flatMap { scorersAdapter.fromJson(it.scorersJson).orEmpty() }
            .firstOrNull { it.playerId == playerId }
            ?.goals
            ?: prev?.goals
            ?: 0
        val teamId = SportsMapper.sideId(detail?.team).takeIf { it.isNotBlank() && it != "unknown" }
            ?: prev?.teamId.orEmpty()
        dao.insertPlayers(
            listOf(
                PlayerEntity(
                    id = playerId,
                    name = detail?.name ?: prev?.name ?: playerId,
                    portrait = detail?.portrait?.ifBlank { prev?.portrait.orEmpty() } ?: prev?.portrait.orEmpty(),
                    teamId = teamId,
                    teamName = detail?.team?.name ?: prev?.teamName.orEmpty(),
                    teamLogo = detail?.team?.logo ?: prev?.teamLogo.orEmpty(),
                    shirtNumber = detail?.shirtNumber ?: prev?.shirtNumber ?: 0,
                    position = detail?.role ?: prev?.position.orEmpty(),
                    age = detail?.age ?: prev?.age ?: 0,
                    country = detail?.country ?: prev?.country.orEmpty(),
                    goals = goals,
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

    suspend fun snapshotForWidget(): WidgetSnapshot {
        val favoriteTeams = dao.getFavoriteTeams().first()
        val live = dao.getLiveMatches().first()
        val all = dao.getAllMatches().first()
        val favoriteIds = favoriteTeams.map { it.id }.toSet()
        val liveFav = live.firstOrNull { it.homeTeamId in favoriteIds || it.awayTeamId in favoriteIds }
        if (liveFav != null) {
            return WidgetSnapshot(liveFav, true)
        }
        val upcoming = all
            .filter { it.status == "SCHEDULED" && (it.homeTeamId in favoriteIds || it.awayTeamId in favoriteIds) }
            .minByOrNull { it.utcStart.ifBlank { it.time } }
        val fallback = live.firstOrNull() ?: all
            .filter { it.status == "SCHEDULED" }
            .minByOrNull { it.utcStart.ifBlank { it.time } }
        return WidgetSnapshot(upcoming ?: fallback, live = upcoming == null && fallback?.status == "LIVE")
    }

    suspend fun hasWatchedLiveMatches(): Boolean {
        val live = dao.getLiveMatches().first()
        val teams = dao.getFavoriteTeamIds().toSet()
        val leagues = dao.getFavoriteLeagueIds().toSet()
        val players = watchedPlayerIds()
        return live.any { MatchAlertFormatter.isWatched(it, teams, leagues, players) }
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

    private fun fetchFixturePages(startUrl: String): List<FixtureRound> {
        val collected = mutableListOf<FixtureRound>()
        var url: String? = startUrl
        repeat(4) {
            val page = api.fetchLeagueFixtures(url ?: return@repeat) ?: return@repeat
            collected += SportsMapper.mapFixtureRounds(page.items)
            url = page.links?.firstOrNull { it.rel == "next" }?.href
            if (url.isNullOrBlank() || page.hasMore != true) return@repeat
        }
        return collected.distinctBy { it.round + it.matches.joinToString { match -> match.id } }
    }

    private fun mapLiveMatch(
        match: ApiLiveMatch,
        leagueId: String,
        leagueName: String,
        leagueLogo: String,
        offset: Int,
        isFavorite: Boolean,
        now: Long
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
            lastUpdatedMillis = now,
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
            lastUpdatedMillis = incoming.lastUpdatedMillis.takeIf { it > 0 } ?: existing.lastUpdatedMillis,
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
                rank = stub.rank.takeIf { it > 0 } ?: prev?.rank ?: 0,
                points = stub.points.takeIf { it > 0 } ?: prev?.points ?: 0,
                won = stub.won.takeIf { it > 0 } ?: prev?.won ?: 0,
                drawn = stub.drawn.takeIf { it > 0 } ?: prev?.drawn ?: 0,
                lost = stub.lost.takeIf { it > 0 } ?: prev?.lost ?: 0,
                played = stub.played.takeIf { it > 0 } ?: prev?.played ?: 0,
                goalsFor = stub.goalsFor.takeIf { it > 0 } ?: prev?.goalsFor ?: 0,
                goalsAgainst = stub.goalsAgainst.takeIf { it > 0 } ?: prev?.goalsAgainst ?: 0,
                leagueName = stub.leagueName.ifBlank { prev?.leagueName.orEmpty() },
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
                scorersJson = if (stub.scorersJson != "[]") stub.scorersJson else prev?.scorersJson ?: "[]",
                fixturesJson = if (stub.fixturesJson != "[]") stub.fixturesJson else prev?.fixturesJson ?: "[]",
                logo = stub.logo.ifBlank { prev?.logo.orEmpty() },
                isFavorite = stub.isFavorite || prev?.isFavorite == true
            )
        }
        dao.insertLeagues(merged)
    }

    private suspend fun emitWatchAlerts(
        existing: Map<String, MatchEntity>,
        merged: List<MatchEntity>,
        favoriteTeams: Set<String>,
        favoriteLeagues: Set<String>,
        favoritePlayers: Set<String>
    ) {
        merged.forEach { current ->
            if (!MatchAlertFormatter.isWatched(current, favoriteTeams, favoriteLeagues, favoritePlayers)) return@forEach
            val previous = existing[current.id] ?: return@forEach
            notifyAndStore(MatchAlertFormatter.alerts(previous, current))
        }
    }

    private suspend fun watchedPlayerIds(): Set<String> {
        return if (settingsStore.settings.first().playerNotifications) {
            dao.getFavoritePlayerIds().toSet()
        } else {
            emptySet()
        }
    }

    private suspend fun notifyAndStore(alerts: List<MatchAlert>) {
        if (alerts.isEmpty()) return
        val now = System.currentTimeMillis()
        dao.insertNotificationHistory(alerts.mapIndexed { index, alert ->
            NotificationHistoryEntity(
                id = "${alert.matchId}-${alert.kind}-$now-$index",
                matchId = alert.matchId,
                title = alert.title,
                body = alert.body,
                kind = alert.kind.name,
                createdAt = now
            )
        })
        notifier.notify(alerts)
    }

    private suspend fun syncLiveTracking() {
        if (hasWatchedLiveMatches()) {
            LiveScoreService.start(appContext)
        } else {
            LiveScoreService.stop(appContext)
        }
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
                                val image = currentImage.ifBlank {
                                    Regex("""<img[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                                        .find(currentDescription)?.groupValues?.get(1).orEmpty()
                                }
                                val published = JalaliDate.parseRss(currentPubDate) ?: 0L
                                items += NewsEntity(
                                    id = id,
                                    title = currentTitle,
                                    summary = if (cleanSummary.length > 200) cleanSummary.take(200) + "..." else cleanSummary,
                                    content = cleanSummary,
                                    imageUrl = image,
                                    date = if (published > 0) JalaliDate.format(published) else currentPubDate,
                                    articleUrl = currentLink,
                                    publishedAtMillis = published,
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

    data class WidgetSnapshot(val match: MatchEntity?, val live: Boolean)
}
