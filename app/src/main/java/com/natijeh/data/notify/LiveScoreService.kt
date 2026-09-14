package com.natijeh.data.notify

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.natijeh.NatijehApp
import com.natijeh.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LiveScoreService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val notifier = GoalNotifier(this)
        val notification = notifier.ongoingLive(getString(R.string.foreground_live))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(GoalNotifier.FOREGROUND_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(GoalNotifier.FOREGROUND_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (job?.isActive == true) return START_STICKY
        val app = application as NatijehApp
        job = scope.launch {
            while (isActive) {
                try {
                    if (!app.repository.hasWatchedLiveMatches()) {
                        stopSelf()
                        break
                    }
                    app.repository.refreshLiveScore(0)
                } catch (_: Exception) {
                }
                delay(20_000)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        job?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val ACTION_STOP = "com.natijeh.STOP_LIVE"

        fun start(context: Context) {
            val intent = Intent(context, LiveScoreService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LiveScoreService::class.java))
        }
    }
}
