package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SportsDao {
    // --- MATCHES ---
    @Query("SELECT * FROM matches ORDER BY time ASC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE status = 'LIVE' ORDER BY id ASC")
    fun getLiveMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE date = :date ORDER BY time ASC")
    fun getMatchesByDate(date: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :id")
    fun getMatchByIdFlow(id: String): Flow<MatchEntity?>

    @Query("SELECT * FROM matches WHERE id = :id")
    suspend fun getMatchById(id: String): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("UPDATE matches SET isFavorite = :isFav WHERE id = :id")
    suspend fun setMatchFavorite(id: String, isFav: Boolean)

    @Query("SELECT * FROM matches WHERE isFavorite = 1 ORDER BY date DESC, time DESC")
    fun getFavoriteMatches(): Flow<List<MatchEntity>>

    // --- TEAMS ---
    @Query("SELECT * FROM teams WHERE id = :id")
    suspend fun getTeamById(id: String): TeamEntity?

    @Query("SELECT * FROM teams WHERE id = :id")
    fun getTeamByIdFlow(id: String): Flow<TeamEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Query("UPDATE teams SET isFavorite = :isFav WHERE id = :id")
    suspend fun setTeamFavorite(id: String, isFav: Boolean)

    @Query("SELECT * FROM teams WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteTeams(): Flow<List<TeamEntity>>

    // --- LEAGUES ---
    @Query("SELECT * FROM leagues ORDER BY id ASC")
    fun getAllLeagues(): Flow<List<LeagueEntity>>

    @Query("SELECT * FROM leagues WHERE id = :id")
    suspend fun getLeagueById(id: String): LeagueEntity?

    @Query("SELECT * FROM leagues WHERE id = :id")
    fun getLeagueByIdFlow(id: String): Flow<LeagueEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeagues(leagues: List<LeagueEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeague(league: LeagueEntity)

    @Query("UPDATE leagues SET isFavorite = :isFav WHERE id = :id")
    suspend fun setLeagueFavorite(id: String, isFav: Boolean)

    @Query("SELECT * FROM leagues WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteLeagues(): Flow<List<LeagueEntity>>

    // --- NEWS ---
    @Query("SELECT * FROM news ORDER BY date DESC")
    fun getAllNews(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE id = :id")
    suspend fun getNewsById(id: String): NewsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: List<NewsEntity>)
}
