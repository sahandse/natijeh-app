package com.natijeh

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.natijeh.data.local.AppDatabase
import com.natijeh.data.notify.GoalNotifier
import com.natijeh.data.notify.LiveScoreWorker
import com.natijeh.data.repository.SportsRepository
import java.util.concurrent.TimeUnit

class NatijehApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: SportsRepository by lazy { SportsRepository(database.sportsDao(), this) }

    override fun onCreate() {
        super.onCreate()
        GoalNotifier(this).ensureChannels()
        val request = PeriodicWorkRequestBuilder<LiveScoreWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            LiveScoreWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
