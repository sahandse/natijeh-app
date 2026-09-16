package com.natijeh.data.remote

import com.natijeh.data.remote.dto.ApiGlanceResponse
import com.natijeh.data.remote.dto.ApiLeagueMatchesResponse
import com.natijeh.data.remote.dto.ApiLiveLeague
import com.natijeh.data.remote.dto.ApiMatchDetail
import com.natijeh.data.remote.dto.ApiPlayerDetail
import com.natijeh.data.remote.dto.ApiPlayerStatGroup
import com.natijeh.data.remote.dto.ApiSquadGroup
import com.natijeh.data.remote.dto.ApiStandingResponse
import com.natijeh.data.remote.dto.ApiTeamDetail
import com.natijeh.data.remote.dto.ApiTeamMatchesResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class Varzesh3Service(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val liveLeagueListAdapter = moshi.adapter<List<ApiLiveLeague>>(
        Types.newParameterizedType(List::class.java, ApiLiveLeague::class.java)
    )
    private val matchAdapter = moshi.adapter(ApiMatchDetail::class.java)
    private val leagueAdapter = moshi.adapter(ApiLiveLeague::class.java)
    private val standingAdapter = moshi.adapter(ApiStandingResponse::class.java)
    private val teamAdapter = moshi.adapter(ApiTeamDetail::class.java)
    private val squadAdapter = moshi.adapter<List<ApiSquadGroup>>(
        Types.newParameterizedType(List::class.java, ApiSquadGroup::class.java)
    )
    private val teamMatchesAdapter = moshi.adapter(ApiTeamMatchesResponse::class.java)
    private val glanceAdapter = moshi.adapter(ApiGlanceResponse::class.java)
    private val fixtureAdapter = moshi.adapter(ApiLeagueMatchesResponse::class.java)
    private val playerStatsAdapter = moshi.adapter<List<ApiPlayerStatGroup>>(
        Types.newParameterizedType(List::class.java, ApiPlayerStatGroup::class.java)
    )
    private val playerAdapter = moshi.adapter(ApiPlayerDetail::class.java)

    fun fetchLiveScore(offset: Int): List<ApiLiveLeague> {
        val path = if (offset == 0) "today" else offset.toString()
        val json = get("$BASE/livescore/$path")
        return liveLeagueListAdapter.fromJson(json).orEmpty()
    }

    fun fetchMatch(matchId: String): ApiMatchDetail? {
        val json = get("$BASE/football/matches/$matchId")
        return matchAdapter.fromJson(json)
    }

    fun fetchLeague(leagueId: String): ApiLiveLeague? {
        val json = get("$BASE/football/leagues/$leagueId")
        return leagueAdapter.fromJson(json)
    }

    fun fetchStandings(url: String): ApiStandingResponse? {
        val json = get(url)
        return standingAdapter.fromJson(json)
    }

    fun fetchTeam(teamId: String): ApiTeamDetail? {
        val json = get("$BASE/football/teams/$teamId")
        return teamAdapter.fromJson(json)
    }

    fun fetchSquad(teamId: String): List<ApiSquadGroup> {
        val json = get("$BASE/football/teams/$teamId/squad")
        return squadAdapter.fromJson(json).orEmpty()
    }

    fun fetchTeamResults(teamId: String): ApiTeamMatchesResponse? {
        val json = get("$BASE/football/teams/$teamId/results")
        return teamMatchesAdapter.fromJson(json)
    }

    fun fetchTeamGlance(teamId: String): ApiGlanceResponse? {
        val json = get("$BASE/football/teams/$teamId/at-a-glance")
        return glanceAdapter.fromJson(json)
    }

    fun fetchRss(url: String): String = get(url)

    fun fetchLeagueFixtures(url: String): ApiLeagueMatchesResponse? {
        val json = get(url)
        return fixtureAdapter.fromJson(json)
    }

    fun fetchPlayerStats(url: String): List<ApiPlayerStatGroup> {
        val json = get(url)
        return playerStatsAdapter.fromJson(json).orEmpty()
    }

    fun fetchPlayer(playerId: String): ApiPlayerDetail? {
        val json = get("$BASE/football/players/$playerId")
        return playerAdapter.fromJson(json)
    }

    private fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json, text/xml;q=0.9, */*;q=0.8")
            .header("Accept-Language", "fa-IR,fa;q=0.9,en;q=0.8")
            .build()
        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code} for $url")
            }
            return body
        }
    }

    companion object {
        const val BASE = "https://web-api.varzesh3.com/v2.0"
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
    }
}
