package com.natijeh.data.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.natijeh.NatijehApp

class LiveScoreWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as? NatijehApp ?: return Result.retry()
        return try {
            app.repository.refreshLiveScore(0)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_NAME = "natijeh-live-score"
    }
}
