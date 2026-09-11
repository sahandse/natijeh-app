package com.natijeh.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.natijeh.data.model.LeagueEntity
import com.natijeh.data.model.MatchEntity
import com.natijeh.data.model.NewsEntity
import com.natijeh.data.model.PlayerEntity
import com.natijeh.data.model.TeamEntity

@Database(
    entities = [
        MatchEntity::class,
        TeamEntity::class,
        LeagueEntity::class,
        NewsEntity::class,
        PlayerEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sportsDao(): SportsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "natijeh_sports_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
