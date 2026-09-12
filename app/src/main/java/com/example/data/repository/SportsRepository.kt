package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.SportsDao
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import okhttp3.OkHttpClient
import okhttp3.Request
import android.util.Xml
import org.xmlpull.v1.XmlPullParser

class SportsRepository(
    private val dao: SportsDao,
    private val context: Context
) {
    private val tag = "SportsRepository"
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val httpClient = OkHttpClient()

    // Type Adapters
    private val eventAdapter = moshi.adapter<List<MatchEvent>>(
        Types.newParameterizedType(List::class.java, MatchEvent::class.java)
    )
    private val statsAdapter = moshi.adapter(MatchStats::class.java)
    private val lineupsAdapter = moshi.adapter(MatchLineups::class.java)
    private val momentumAdapter = moshi.adapter<List<Int>>(
        Types.newParameterizedType(List::class.java, Int::class.javaObjectType)
    )
    private val h2hAdapter = moshi.adapter(HeadToHeadData::class.java)
    private val standingsAdapter = moshi.adapter<List<StandingRow>>(
        Types.newParameterizedType(List::class.java, StandingRow::class.java)
    )
    private val squadAdapter = moshi.adapter<List<SquadPlayer>>(
        Types.newParameterizedType(List::class.java, SquadPlayer::class.java)
    )
    private val honoursAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    // Flow Exposures
    val allMatches: Flow<List<MatchEntity>> = dao.getAllMatches()
    val liveMatches: Flow<List<MatchEntity>> = dao.getLiveMatches()
    val favoriteMatches: Flow<List<MatchEntity>> = dao.getFavoriteMatches()
    val favoriteTeams: Flow<List<TeamEntity>> = dao.getFavoriteTeams()
    val favoriteLeagues: Flow<List<LeagueEntity>> = dao.getFavoriteLeagues()
    val allLeagues: Flow<List<LeagueEntity>> = dao.getAllLeagues()
    val allTeams: Flow<List<TeamEntity>> = dao.getAllTeams()
    val newsFeed: Flow<List<NewsEntity>> = dao.getAllNews()

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var simulationJob: Job? = null

    init {
        repositoryScope.launch {
            // Seed initial data if DB is empty
            val existing = dao.getAllLeagues().first()
            if (existing.isEmpty()) {
                Log.d(tag, "Seeding initial sports database...")
                seedDatabase()
            }
            // Fetch real news from Varzesh3
            try {
                fetchRssNews()
            } catch (e: Exception) {
                Log.e(tag, "Failed to fetch RSS news on startup", e)
            }
            // Start the Realtime Live Simulation Engine
            startLiveSimulation()
        }
    }

    fun getMatchesByDate(date: String): Flow<List<MatchEntity>> {
        return dao.getMatchesByDate(date)
    }

    fun getMatchByIdFlow(id: String): Flow<MatchEntity?> {
        return dao.getMatchByIdFlow(id)
    }

    fun getTeamByIdFlow(id: String): Flow<TeamEntity?> {
        return dao.getTeamByIdFlow(id)
    }

    fun getLeagueByIdFlow(id: String): Flow<LeagueEntity?> {
        return dao.getLeagueByIdFlow(id)
    }

    suspend fun setMatchFavorite(id: String, isFav: Boolean) {
        dao.setMatchFavorite(id, isFav)
    }

    suspend fun setTeamFavorite(id: String, isFav: Boolean) {
        dao.setTeamFavorite(id, isFav)
    }

    suspend fun setLeagueFavorite(id: String, isFav: Boolean) {
        dao.setLeagueFavorite(id, isFav)
    }

    // --- REALTIME LIVE SIMULATION ENGINE ---

    private fun startLiveSimulation() {
        simulationJob = repositoryScope.launch {
            while (isActive) {
                delay(30000) // 30 seconds interval to save battery
                val currentMatches = dao.getLiveMatches().first()
                if (currentMatches.isEmpty()) {
                    Log.d(tag, "No live matches, simulation idle.")
                    continue
                }
                val updatedList = currentMatches.map { match ->
                    var min = match.minute + 1
                    var status = match.status
                    if (min > 90) {
                        min = 90
                        status = "FINISHED"
                    }

                    // Parse existing events and stats
                    val events = eventAdapter.fromJson(match.eventsJson)?.toMutableList() ?: mutableListOf()
                    val stats = statsAdapter.fromJson(match.statsJson) ?: createDefaultStats()
                    
                    var homeScore = match.homeScore
                    var awayScore = match.awayScore

                    // Chance of key event (goal, card, etc.)
                    val dice = Random.nextInt(100)
                    if (status == "LIVE" && dice < 15) { // 15% chance per tick
                        val isHome = Random.nextBoolean()
                        val eventTypeDice = Random.nextInt(10)
                        
                        val teamName = if (isHome) match.homeTeamName else match.awayTeamName
                        val lineups = lineupsAdapter.fromJson(match.lineupsJson)
                        val playersList = if (isHome) lineups?.homeStarting else lineups?.awayStarting
                        val playerName = playersList?.random()?.name ?: "بازیکن"

                        when {
                            eventTypeDice < 4 -> { // Goal!
                                if (isHome) homeScore++ else awayScore++
                                events.add(
                                    MatchEvent(
                                        minute = min,
                                        type = "GOAL",
                                        isHome = isHome,
                                        playerName = playerName,
                                        detail = "گل زیبا برای $teamName"
                                    )
                                )
                            }
                            eventTypeDice < 6 -> { // Yellow Card
                                events.add(
                                    MatchEvent(
                                        minute = min,
                                        type = "CARD_YELLOW",
                                        isHome = isHome,
                                        playerName = playerName,
                                        detail = "کارت زرد به دلیل خطای شدید"
                                    )
                                )
                            }
                            eventTypeDice < 7 -> { // Red Card
                                events.add(
                                    MatchEvent(
                                        minute = min,
                                        type = "CARD_RED",
                                        isHome = isHome,
                                        playerName = playerName,
                                        detail = "اخراج مستقیم بازیکن از زمین بازی"
                                    )
                                )
                            }
                            eventTypeDice < 8 -> { // Penalty
                                val penaltyScored = Random.nextInt(10) < 8 // 80% score chance
                                if (penaltyScored) {
                                    if (isHome) homeScore++ else awayScore++
                                }
                                events.add(
                                    MatchEvent(
                                        minute = min,
                                        type = "PENALTY",
                                        isHome = isHome,
                                        playerName = playerName,
                                        detail = if (penaltyScored) "گل از روی نقطه پنالتی!" else "ضربه پنالتی از دست رفت!"
                                    )
                                )
                            }
                            else -> { // VAR Review
                                events.add(
                                    MatchEvent(
                                        minute = min,
                                        type = "VAR_REVIEW",
                                        isHome = isHome,
                                        playerName = "داور ویدیویی",
                                        detail = "بررسی خطای پنالتی یا آفساید توسط VAR"
                                    )
                                )
                            }
                        }
                    }

                    // Generate some small updates to stats to make them dynamic
                    val updatedStats = stats.copy(
                        possessionHome = if (Random.nextBoolean()) (stats.possessionHome + Random.nextInt(-2, 3)).coerceIn(30, 70) else stats.possessionHome,
                        possessionAway = 100 - stats.possessionHome,
                        shotsHome = stats.shotsHome + if (Random.nextInt(10) < 3) 1 else 0,
                        shotsAway = stats.shotsAway + if (Random.nextInt(10) < 3) 1 else 0,
                        expectedGoalsHome = stats.expectedGoalsHome + if (Random.nextInt(100) < 5) 0.15 else 0.0,
                        expectedGoalsAway = stats.expectedGoalsAway + if (Random.nextInt(100) < 5) 0.15 else 0.0
                    )

                    match.copy(
                        minute = min,
                        status = status,
                        homeScore = homeScore,
                        awayScore = awayScore,
                        eventsJson = eventAdapter.toJson(events),
                        statsJson = statsAdapter.toJson(updatedStats)
                    )
                }
                dao.insertMatches(updatedList)
                Log.d(tag, "Live simulation updated ${updatedList.size} matches.")
            }
        }
    }

    private fun createDefaultStats(): MatchStats {
        return MatchStats(
            possessionHome = 50, possessionAway = 50,
            shotsHome = 8, shotsAway = 7,
            shotsOnTargetHome = 3, shotsOnTargetAway = 2,
            expectedGoalsHome = 0.85, expectedGoalsAway = 0.72,
            passAccuracyHome = 82, passAccuracyAway = 80,
            cornersHome = 4, cornersAway = 3,
            foulsHome = 11, foulsAway = 12,
            offsidesHome = 1, offsidesAway = 2
        )
    }

    // --- SEED DATABASE ENGINE ---

    private suspend fun seedDatabase() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowStr = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -2)
        val yesterdayStr = sdf.format(cal.time)

        // Seed Leagues
        val leagues = listOf(
            LeagueEntity(
                id = "L1", name = "لیگ برتر خلیج فارس", logo = "https://cdn-icons-png.flaticon.com/128/3002/3002651.png", country = "ایران",
                standingsJson = standingsAdapter.toJson(listOf(
                    StandingRow(1, "T1", "پرسپولیس", "", 12, 8, 3, 1, 18, 6, 27),
                    StandingRow(2, "T2", "استقلال", "", 12, 7, 4, 1, 15, 8, 25),
                    StandingRow(3, "T3", "سپاهان", "", 12, 7, 2, 3, 21, 10, 23),
                    StandingRow(4, "T4", "تراکتور", "", 12, 6, 3, 3, 16, 9, 21)
                ))
            ),
            LeagueEntity(
                id = "L2", name = "لیگ برتر انگلیس (EPL)", logo = "https://cdn-icons-png.flaticon.com/128/5310/5310675.png", country = "انگلستان",
                standingsJson = standingsAdapter.toJson(listOf(
                    StandingRow(1, "T5", "منچستر سیتی", "", 15, 11, 2, 2, 35, 14, 35),
                    StandingRow(2, "T6", "آرسنال", "", 15, 10, 3, 2, 30, 11, 33),
                    StandingRow(3, "T7", "لیورپول", "", 15, 9, 4, 2, 28, 15, 31)
                ))
            ),
            LeagueEntity(
                id = "L3", name = "لیگ قهرمانان اروپا", logo = "https://cdn-icons-png.flaticon.com/128/1164/1164620.png", country = "اروپا",
                standingsJson = standingsAdapter.toJson(listOf(
                    StandingRow(1, "T8", "رئال مادرید", "", 6, 5, 1, 0, 14, 4, 16),
                    StandingRow(2, "T5", "منچستر سیتی", "", 6, 4, 2, 0, 16, 6, 14),
                    StandingRow(3, "T9", "بایرن مونیخ", "", 6, 4, 1, 1, 12, 5, 13)
                ))
            )
        )
        dao.insertLeagues(leagues)

        // Seed Teams
        val teams = listOf(
            TeamEntity("T1", "پرسپولیس", "خوان کارلوس گاریدو", "ورزشگاه آزادی", "۱۳۴۲", "$۱۵.۵ میلیون", squadAdapter.toJson(listOf(
                SquadPlayer("علیرضا بیرانوند", "ایرانی", 31, "194cm", "85kg", "دروازه‌بان", "راست", "$۱.۲ میلیون", 0, 0, 12),
                SquadPlayer("مرتضی پورعلی‌گنجی", "ایرانی", 32, "188cm", "80kg", "مدافع", "راست", "$۷۰۰ هزار", 1, 0, 10),
                SquadPlayer("وحید امیری", "ایرانی", 36, "179cm", "74kg", "هافبک", "چپ", "$۴۰۰ هزار", 2, 3, 11),
                SquadPlayer("عیسی آل‌کثیر", "ایرانی", 34, "182cm", "78kg", "مهاجم", "راست", "$۵۰۰ هزار", 5, 1, 12)
            )), "4-4-2", honoursAdapter.toJson(listOf("۱۶ قهرمانی لیگ برتر", "۷ قهرمانی جام حذفی"))),
            
            TeamEntity("T2", "استقلال", "پیتسو موسیمانه", "ورزشگاه آزادی", "۱۳۲۴", "$۱۴.۲ میلیون", squadAdapter.toJson(listOf(
                SquadPlayer("سید حسین حسینی", "ایرانی", 31, "189cm", "82kg", "دروازه‌بان", "راست", "$۹۰۰ هزار", 0, 0, 12),
                SquadPlayer("روزبه چشمی", "ایرانی", 30, "192cm", "84kg", "مدافع", "راست", "$۷۵۰ هزار", 1, 1, 11),
                SquadPlayer("آرش رضاوند", "ایرانی", 30, "183cm", "76kg", "هافبک", "راست", "$۶۰۰ هزار", 2, 2, 12),
                SquadPlayer("گوستاوو بلانکو", "آرژانتینی", 32, "190cm", "85kg", "مهاجم", "راست", "$۸۰۰ هزار", 4, 1, 10)
            )), "4-2-3-1", honoursAdapter.toJson(listOf("۹ قهرمانی لیگ برتر", "۷ قهرمانی جام حذفی", "۲ قهرمانی آسیا"))),

            TeamEntity("T5", "منچستر سیتی", "پپ گواردیولا", "ورزشگاه اتحاد", "۱۸۸۰", "$۱.۲ میلیارد", squadAdapter.toJson(listOf(
                SquadPlayer("ادرسیون", "برزیلی", 30, "188cm", "86kg", "دروازه‌بان", "چپ", "$۴۰ میلیون", 0, 0, 15),
                SquadPlayer("روبن دیاز", "پرتغالی", 27, "187cm", "85kg", "مدافع", "راست", "$۸۰ میلیون", 1, 0, 14),
                SquadPlayer("کوین دی بروینه", "بلژیکی", 32, "181cm", "70kg", "هافبک", "راست", "$۷۰ میلیون", 5, 12, 13),
                SquadPlayer("ارلینگ هالند", "نروژی", 23, "195cm", "88kg", "مهاجم", "راست", "$۱۸۰ میلیون", 18, 3, 15)
            )), "4-3-3", honoursAdapter.toJson(listOf("۱۰ قهرمانی لیگ انگلیس", "۱ قهرمانی چمپیونزلیگ"))),

            TeamEntity("T8", "رئال مادرید", "کارلو آنچلوتی", "ورزشگاه سانتیاگو برنابئو", "۱۹۰۲", "$۱.۰۴ میلیارد", squadAdapter.toJson(listOf(
                SquadPlayer("تیبو کورتوا", "بلژیکی", 32, "200cm", "96kg", "دروازه‌بان", "چپ", "$۳۵ میلیون", 0, 0, 6),
                SquadPlayer("آنتونیو رودیگر", "آلمانی", 31, "190cm", "85kg", "مدافع", "راست", "$۲۵ میلیون", 1, 0, 6),
                SquadPlayer("جود بلینگهام", "انگلیسی", 20, "186cm", "75kg", "هافبک", "راست", "$۱۸۰ میلیون", 4, 5, 6),
                SquadPlayer("وینیسیوس جونیور", "برزیلی", 23, "176cm", "73kg", "مهاجم", "راست", "$۱۸۰ میلیون", 5, 4, 6)
            )), "4-3-1-2", honoursAdapter.toJson(listOf("۳۶ قهرمانی لالیگا", "۱۵ قهرمانی چمپیونزلیگ")))
        )
        dao.insertTeams(teams)

        // Seed Matches
        val sampleLineups = MatchLineups(
            homeFormation = "4-4-2", awayFormation = "4-2-3-1",
            homeStarting = listOf(
                PlayerLineup(1, "علیرضا بیرانوند", "GK", 8.2),
                PlayerLineup(4, "شجاع خلیل‌زاده", "DF", 7.1),
                PlayerLineup(8, "مرتضی پورعلی‌گنجی", "DF", 7.5),
                PlayerLineup(11, "دانیال اسماعیلی‌فر", "DF", 6.8),
                PlayerLineup(5, "میلاد محمدی", "DF", 7.0),
                PlayerLineup(6, "مسعود ریگی", "MF", 6.9),
                PlayerLineup(19, "وحید امیری", "MF", 7.8),
                PlayerLineup(9, "مهدی ترابی", "MF", 7.4),
                PlayerLineup(10, "اوستون اورونوف", "MF", 8.5),
                PlayerLineup(18, "علی علیپور", "FW", 7.2),
                PlayerLineup(72, "عیسی آل‌کثیر", "FW", 7.6)
            ),
            homeBench = listOf(PlayerLineup(22, "مهرشاد اسدی", "GK", 6.0), PlayerLineup(3, "فرشاد فرجی", "DF", 6.5)),
            awayStarting = listOf(
                PlayerLineup(1, "سید حسین حسینی", "GK", 7.9),
                PlayerLineup(5, "آرمین سهرابیان", "DF", 7.0),
                PlayerLineup(14, "روزبه چشمی", "DF", 7.4),
                PlayerLineup(2, "صالح حردانی", "DF", 6.9),
                PlayerLineup(3, "ابوالفضل جلالی", "DF", 7.2),
                PlayerLineup(88, "آرش رضاوند", "MF", 6.8),
                PlayerLineup(8, "زبیر نیک‌نفس", "MF", 6.7),
                PlayerLineup(10, "مهرداد محمدی", "MF", 7.1),
                PlayerLineup(77, "جلال‌الدین ماشاریپوف", "MF", 8.0),
                PlayerLineup(21, "کوتی حسن", "FW", 6.5),
                PlayerLineup(19, "گوستاوو بلانکو", "FW", 7.5)
            ),
            awayBench = listOf(PlayerLineup(12, "محمدرضا خالدآبادی", "GK", 6.0), PlayerLineup(18, "پیمان بابایی", "FW", 6.6)),
            homeCoach = "خوان کارلوس گاریدو", awayCoach = "پیتسو موسیمانه"
        )

        val eplLineups = MatchLineups(
            homeFormation = "4-3-3", awayFormation = "4-2-3-1",
            homeStarting = listOf(
                PlayerLineup(31, "ادرسیون", "GK", 7.5),
                PlayerLineup(3, "روبن دیاز", "DF", 8.0),
                PlayerLineup(5, "جان استونز", "DF", 7.8),
                PlayerLineup(2, "کایل واکر", "DF", 7.4),
                PlayerLineup(24, "یوشکو گواردیول", "DF", 7.9),
                PlayerLineup(16, "رودری", "MF", 8.5),
                PlayerLineup(17, "کوین دی بروینه", "MF", 9.1),
                PlayerLineup(20, "برناردو سیلوا", "MF", 8.2),
                PlayerLineup(47, "فیل فودن", "FW", 8.4),
                PlayerLineup(11, "جرمی دوکو", "FW", 7.6),
                PlayerLineup(9, "ارلینگ هالند", "FW", 9.5)
            ),
            homeBench = listOf(),
            awayStarting = listOf(
                PlayerLineup(1, "داوید رایا", "GK", 8.1),
                PlayerLineup(2, "ویلیام سالیبا", "DF", 8.2),
                PlayerLineup(6, "گابریل ماگالاش", "DF", 8.0),
                PlayerLineup(4, "بن وایت", "DF", 7.5),
                PlayerLineup(12, "یوریان تیمبر", "DF", 7.3),
                PlayerLineup(41, "دکلان رایس", "MF", 8.4),
                PlayerLineup(5, "توماس پارتی", "MF", 7.6),
                PlayerLineup(8, "مارتین اودگارد", "MF", 8.6),
                PlayerLineup(7, "بوکایو ساکا", "FW", 8.8),
                PlayerLineup(11, "گابریل مارتینلی", "FW", 7.5),
                PlayerLineup(29, "کای هاورتز", "FW", 7.9)
            ),
            awayBench = listOf(),
            homeCoach = "پپ گواردیولا", awayCoach = "میکل آرتتا"
        )

        val matches = listOf(
            // Live Match: Persepolis vs Esteghlal (Derby of Tehran)
            MatchEntity(
                id = "M1", homeTeamId = "T1", homeTeamName = "پرسپولیس",
                awayTeamId = "T2", awayTeamName = "استقلال",
                homeScore = 1, awayScore = 1, status = "LIVE", minute = 64,
                date = todayStr, time = "18:00", leagueId = "L1", leagueName = "لیگ برتر خلیج فارس",
                venue = "ورزشگاه بزرگ آزادی، تهران", referee = "بیژن حیدری", attendance = "۷۵,۰۰۰ نفر",                eventsJson = eventAdapter.toJson(listOf(
                    MatchEvent(12, "GOAL", true, "عیسی آل‌کثیر", "گل با ضربه سر روی کرنر ارسالی وحید امیری"),
                    MatchEvent(35, "CARD_YELLOW", false, "روزبه چشمی", "خطا روی اوستون اورونوف"),
                    MatchEvent(55, "GOAL", false, "گوستاوو بلانکو", "شوت پای راست محکم گوشه دروازه")
                )),
                statsJson = statsAdapter.toJson(MatchStats(55, 45, 12, 9, 5, 4, 1.45, 1.12, 85, 79, 6, 4, 12, 14, 2, 1)),
                lineupsJson = lineupsAdapter.toJson(sampleLineups),
                momentumJson = momentumAdapter.toJson(listOf(20, 30, -10, 15, -40, 50, 60, -20, 10, 20, 35, 40)),
                h2hJson = h2hAdapter.toJson(HeadToHeadData(27, 26, 49, listOf(
                    HeadToHeadMatch("۱۴۰۲/۱۲/۲۴", "استقلال", "پرسپولیس", "۰ - ۰"),
                    HeadToHeadMatch("۱۴۰۲/۰۹/۲۳", "پرسپولیس", "استقلال", "۱ - ۱"),
                    HeadToHeadMatch("۱۴۰۲/۰۳/۱۰", "استقلال", "پرسپولیس", "۱ - ۲")
                )))
            ),
            // Scheduled Match EPL: Manchester City vs Arsenal
            MatchEntity(
                id = "M2", homeTeamId = "T5", homeTeamName = "منچستر سیتی",
                awayTeamId = "T6", awayTeamName = "آرسنال",
                homeScore = 0, awayScore = 0, status = "SCHEDULED", minute = 0,
                date = todayStr, time = "21:30", leagueId = "L2", leagueName = "لیگ برتر انگلیس (EPL)",
                venue = "ورزشگاه اتحاد، منچستر", referee = "مایکل اولیور", attendance = "۵۳,۴۰۰ نفر",
                eventsJson = eventAdapter.toJson(emptyList()),
                statsJson = statsAdapter.toJson(createDefaultStats()),
                lineupsJson = lineupsAdapter.toJson(eplLineups),
                momentumJson = momentumAdapter.toJson(emptyList()),
                h2hJson = h2hAdapter.toJson(HeadToHeadData(14, 10, 8, listOf(
                    HeadToHeadMatch("2024-03-31", "منچستر سیتی", "آرسنال", "۰ - ۰"),
                    HeadToHeadMatch("2023-10-08", "آرسنال", "منچستر سیتی", "۱ - ۰")
                )))
            ),
            // Yesterday's Finished Match CL: Real Madrid vs Bayern Munich
            MatchEntity(
                id = "M3", homeTeamId = "T8", homeTeamName = "رئال مادرید",
                awayTeamId = "T9", awayTeamName = "بایرن مونیخ",
                homeScore = 2, awayScore = 1, status = "FINISHED", minute = 90,
                date = yesterdayStr, time = "22:30", leagueId = "L3", leagueName = "لیگ قهرمانان اروپا",
                venue = "ورزشگاه سانتیاگو برنابئو، مادرید", referee = "سیمون مارسینیاک", attendance = "۸۱,۰۰۰ نفر",
                eventsJson = eventAdapter.toJson(listOf(
                    MatchEvent(68, "GOAL", false, "آلفونسو دیویس", "شوت کات‌دار تماشایی"),
                    MatchEvent(88, "GOAL", true, "خوسلو", "اشتباه نویر و ریباند گل"),
                    MatchEvent(91, "GOAL", true, "خوسلو", "پاس تماشایی رودیگر و ضربه نهایی")
                )),
                statsJson = statsAdapter.toJson(MatchStats(58, 42, 19, 10, 7, 4, 2.10, 1.05, 89, 82, 8, 4, 10, 11, 1, 3)),
                lineupsJson = lineupsAdapter.toJson(eplLineups), // use same for mock simplicity
                momentumJson = momentumAdapter.toJson(listOf(-10, -20, -5, 10, 20, -30, -50, 40, 55, 60, 75, 90)),
                h2hJson = h2hAdapter.toJson(HeadToHeadData(12, 11, 4, listOf(
                    HeadToHeadMatch("2024-05-08", "رئال مادرید", "بایرن مونیخ", "۲ - ۱"),
                    HeadToHeadMatch("2024-04-30", "بایرن مونیخ", "رئال مادرید", "۲ - ۲")
                )))
            ),
            // Tomorrow's Scheduled Match EPL: Liverpool vs Chelsea
            MatchEntity(
                id = "M4", homeTeamId = "T7", homeTeamName = "لیورپول",
                awayTeamId = "T10", awayTeamName = "چلسی",
                homeScore = 0, awayScore = 0, status = "SCHEDULED", minute = 0,
                date = tomorrowStr, time = "19:00", leagueId = "L2", leagueName = "لیگ برتر انگلیس (EPL)",
                venue = "ورزشگاه آنفیلد، لیورپول", referee = "آنتونی تیلور", attendance = "۶۰,۷۰۰ نفر",
                eventsJson = eventAdapter.toJson(emptyList()),
                statsJson = statsAdapter.toJson(createDefaultStats()),
                lineupsJson = lineupsAdapter.toJson(eplLineups),
                momentumJson = momentumAdapter.toJson(emptyList()),
                h2hJson = h2hAdapter.toJson(HeadToHeadData(10, 8, 12, listOf(
                    HeadToHeadMatch("2024-02-25", "چلسی", "لیورپول", "۰ - ۱")
                )))
            )
        )
        dao.insertMatches(matches)

        // Seed News
        val news = listOf(
            NewsEntity(
                id = "N1", title = "اختصاصی نتیجه: رونمایی از کادر فنی جدید استقلال تهران",
                summary = "مربی استقلال دستیاران جدید خود را به کادر فنی معرفی کرد.",
                content = "امروز باشگاه استقلال تهران به صورت رسمی اعلام کرد که کادر فنی تیم برای حضور مقتدرانه در ادامه رقابت‌های لیگ برتر و لیگ قهرمانان نخبگان آسیا تکمیل شده است. دستیاران جدید دارای کارنامه قوی در فوتبال اروپا و آفریقا هستند و از فردا در تمرینات حضور خواهند یافت.",
                imageUrl = "https://cdn.pixabay.com/photo/2016/11/29/02/05/audience-1866738_1285.jpg",
                date = todayStr, source = "خبرگزاری نتیجه"
            ),
            NewsEntity(
                id = "N2", title = "برنده توپ طلای سال ۲۰۲۶ با تحلیل آماری پیشرفته مشخص شد",
                summary = "بر اساس آمار شبیه‌سازی شده نتیجه، ارلینگ هالند شانس نخست کسب توپ طلاست.",
                content = "با بررسی داده‌های آماری پیشرفته در فصل فوتبالی جاری شامل گل‌ها، پاس‌گل‌ها، شاخص xG (امید گل) و پاس‌های کلیدی در لیگ‌های معتبر اروپایی، نامزدهای نهایی توپ طلا تحلیل شده است که ارلینگ هالند با نمره میانگین ۸.۸ در صدر قرار دارد.",
                imageUrl = "https://cdn.pixabay.com/photo/2016/11/18/12/53/balloon-1834279_1280.jpg",
                date = yesterdayStr, source = "تحلیل آماری نتیجه"
            ),
            NewsEntity(
                id = "N3", title = "آخرین بمب نقل و انتقالاتی اروپا در مسیر لیگ روشن عربستان",
                summary = "یکی دیگر از ستاره‌های مطرح لالیگا در آستانه امضای قرارداد با الهلال عربستان است.",
                content = "منابع نزدیک به بازار نقل و انتقالات فاش کرده‌اند که هافبک طراح و بازیساز لالیگا با دریافت یک پیشنهاد سرسام‌آور نجومی به ارزش سالانه ۴۵ میلیون یورو موافقت کرده و طی ۴۸ ساعت آینده برای انجام تست‌های پزشکی به ریاض پرواز خواهد کرد.",
                imageUrl = "https://cdn.pixabay.com/photo/2016/05/20/21/57/football-1406106_1280.jpg",
                date = yesterdayStr, source = "نقل و انتقالات اروپا"
            )
        )
        news.forEach { item ->
            // Check if item already exists to avoid overwriting real RSS articles with seed ones
            val exists = dao.getNewsById(item.id) != null
            if (!exists) {
                dao.insertNews(listOf(item))
            }
        }
    }

    // --- REALTIME RSS NEWS FETCHING & PARSING ENGINE ---

    suspend fun fetchRssNews(): Unit = withContext(Dispatchers.IO) {
        val feeds = listOf(
            "https://www.varzesh3.com/rss/domesticfootball" to "ورزش ۳ (فوتبال داخلی)",
            "https://www.varzesh3.com/rss/foreignfootball" to "ورزش ۳ (فوتبال خارجی)"
        )
        
        val parsedNewsList = mutableListOf<NewsEntity>()
        
        for ((url, sourceName) in feeds) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .build()
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val xmlData = response.body?.string()
                    if (xmlData != null) {
                        val newsFromFeed = parseRssXml(xmlData, sourceName)
                        parsedNewsList.addAll(newsFromFeed)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to fetch RSS from $url", e)
            }
        }
        
        if (parsedNewsList.isNotEmpty()) {
            dao.insertNews(parsedNewsList)
            Log.d(tag, "Inserted ${parsedNewsList.size} articles from RSS feeds.")
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
                                tagName.equals("title", ignoreCase = true) -> {
                                    currentTitle = parser.nextText().trim()
                                }
                                tagName.equals("link", ignoreCase = true) -> {
                                    currentLink = parser.nextText().trim()
                                }
                                tagName.equals("description", ignoreCase = true) -> {
                                    currentDescription = parser.nextText().trim()
                                }
                                tagName.equals("pubDate", ignoreCase = true) -> {
                                    currentPubDate = parser.nextText().trim()
                                }
                                tagName.equals("enclosure", ignoreCase = true) -> {
                                    val urlAttr = parser.getAttributeValue(null, "url")
                                    if (!urlAttr.isNullOrBlank()) {
                                        currentImage = urlAttr
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            insideItem = false
                            if (currentTitle.isNotEmpty()) {
                                // Deterministic unique ID
                                val rawString = currentLink.ifEmpty { currentTitle }
                                val id = try {
                                    val digest = java.security.MessageDigest.getInstance("MD5")
                                    val hash = digest.digest(rawString.toByteArray())
                                    hash.joinToString("") { "%02x".format(it) }
                                } catch (e: Exception) {
                                    UUID.randomUUID().toString()
                                }
                                
                                val cleanSummary = stripHtml(currentDescription)
                                
                                var finalImageUrl = currentImage
                                if (finalImageUrl.isEmpty()) {
                                    finalImageUrl = extractImageUrlFromHtml(currentDescription) ?: ""
                                }
                                
                                if (finalImageUrl.isEmpty()) {
                                    finalImageUrl = if (sourceName.contains("داخلی")) {
                                        "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?q=80&w=600&auto=format&fit=crop"
                                    } else {
                                        "https://images.unsplash.com/photo-1518063319789-7217e6706b04?q=80&w=600&auto=format&fit=crop"
                                    }
                                }
                                
                                items.add(
                                    NewsEntity(
                                        id = id,
                                        title = currentTitle,
                                        summary = if (cleanSummary.length > 200) cleanSummary.take(200) + "..." else cleanSummary,
                                        content = cleanSummary,
                                        imageUrl = finalImageUrl,
                                        date = currentPubDate,
                                        source = sourceName
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("SportsRepository", "XML parse error for $sourceName", e)
        }
        return items
    }

    private fun stripHtml(html: String): String {
        return html.replace(Regex("<[^>]*>"), "").replace("&nbsp;", " ").trim()
    }

    private fun extractImageUrlFromHtml(html: String): String? {
        val match = Regex("""<img[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(html)
        return match?.groupValues?.get(1)
    }

    fun close() {
        simulationJob?.cancel()
        repositoryScope.cancel()
    }
}

