package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.LeagueEntity
import com.example.data.model.MatchEntity
import com.example.data.model.NewsEntity
import com.example.data.model.TeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SportsDao {
    @Query("SELECT * FROM matches ORDER BY utcStart ASC, time ASC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE status = 'LIVE' ORDER BY utcStart ASC")
    fun getLiveMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE dayOffset = :offset ORDER BY utcStart ASC, time ASC")
    fun getMatchesByOffset(offset: Int): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :id")
    fun getMatchByIdFlow(id: String): Flow<MatchEntity?>

    @Query("SELECT * FROM matches WHERE id = :id")
    suspend fun getMatchById(id: String): MatchEntity?

    @Query("SELECT * FROM teams WHERE id = :id")
    suspend fun getTeamById(id: String): TeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Query("DELETE FROM matches WHERE dayOffset = :offset AND id NOT IN (:keepIds)")
    suspend fun deleteStaleMatches(offset: Int, keepIds: List<String>)

    @Query("DELETE FROM matches WHERE dayOffset = :offset")
    suspend fun deleteMatchesByOffset(offset: Int)

    @Query("UPDATE matches SET isFavorite = :isFav WHERE id = :id")
    suspend fun setMatchFavorite(id: String, isFav: Boolean)

    @Query("SELECT id FROM matches WHERE isFavorite = 1")
    suspend fun getFavoriteMatchIds(): List<String>

    @Query("SELECT * FROM matches WHERE isFavorite = 1 ORDER BY utcStart DESC")
    fun getFavoriteMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE id = :id")
    fun getTeamByIdFlow(id: String): Flow<TeamEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Query("UPDATE teams SET isFavorite = :isFav WHERE id = :id")
    suspend fun setTeamFavorite(id: String, isFav: Boolean)

    @Query("SELECT id FROM teams WHERE isFavorite = 1")
    suspend fun getFavoriteTeamIds(): List<String>

    @Query("SELECT * FROM teams WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM leagues ORDER BY name ASC")
    fun getAllLeagues(): Flow<List<LeagueEntity>>

    @Query("SELECT * FROM leagues WHERE id = :id")
    fun getLeagueByIdFlow(id: String): Flow<LeagueEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeagues(leagues: List<LeagueEntity>)

    @Query("UPDATE leagues SET isFavorite = :isFav WHERE id = :id")
    suspend fun setLeagueFavorite(id: String, isFav: Boolean)

    @Query("SELECT id FROM leagues WHERE isFavorite = 1")
    suspend fun getFavoriteLeagueIds(): List<String>

    @Query("SELECT * FROM leagues WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteLeagues(): Flow<List<LeagueEntity>>

    @Query("SELECT * FROM news ORDER BY date DESC")
    fun getAllNews(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE id = :id")
    suspend fun getNewsById(id: String): NewsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: List<NewsEntity>)
}
