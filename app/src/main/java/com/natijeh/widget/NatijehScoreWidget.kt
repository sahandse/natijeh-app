package com.natijeh.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.natijeh.MainActivity
import com.natijeh.NatijehApp
import com.natijeh.R
import com.natijeh.data.repository.SportsRepository
import com.natijeh.data.notify.GoalNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NatijehScoreWidget : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val app = context.applicationContext as? NatijehApp ?: return
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { app.repository.refreshLiveScore(0) }
                updateAll(context, app.repository)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val app = context.applicationContext as? NatijehApp ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val snapshot = runCatching { app.repository.snapshotForWidget() }.getOrNull()
            appWidgetIds.forEach { id ->
                appWidgetManager.updateAppWidget(id, bind(context, snapshot))
            }
        }
    }

    companion object {
        private const val ACTION_REFRESH = "com.natijeh.widget.REFRESH"

        fun updateAll(context: Context, repository: SportsRepository) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, NatijehScoreWidget::class.java))
            if (ids.isEmpty()) return
            CoroutineScope(Dispatchers.IO).launch {
                val snapshot = runCatching { repository.snapshotForWidget() }.getOrNull()
                ids.forEach { id -> manager.updateAppWidget(id, bind(context, snapshot)) }
            }
        }

        private fun bind(context: Context, snapshot: SportsRepository.WidgetSnapshot?): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.natijeh_widget)
            val match = snapshot?.match
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                match?.id?.let { putExtra(GoalNotifier.EXTRA_MATCH_ID, it) }
            }
            val launch = PendingIntent.getActivity(
                context,
                match?.id?.hashCode() ?: 0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, launch)
            val refreshIntent = Intent(context, NatijehScoreWidget::class.java).apply { action = ACTION_REFRESH }
            val refresh = PendingIntent.getBroadcast(
                context,
                91,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_refresh, refresh)
            if (match == null) {
                views.setTextViewText(R.id.widget_status, context.getString(R.string.widget_empty))
                views.setTextViewText(R.id.widget_league, context.getString(R.string.app_name))
                views.setTextViewText(R.id.widget_date, "")
                views.setViewVisibility(R.id.widget_score_row, View.GONE)
                views.setViewVisibility(R.id.widget_more_matches, View.GONE)
                return views
            }
            views.setViewVisibility(R.id.widget_score_row, View.VISIBLE)
            views.setTextViewText(R.id.widget_league, match.leagueName)
            views.setTextViewText(R.id.widget_date, match.date)
            views.setTextViewText(R.id.widget_home, match.homeTeamName)
            views.setTextViewText(R.id.widget_away, match.awayTeamName)
            val score = if (match.status == "SCHEDULED") "- : -" else "${match.homeScore} : ${match.awayScore}"
            views.setTextViewText(R.id.widget_score, score)
            val status = when {
                snapshot.live || match.status == "LIVE" -> "زنده ${match.liveTime}"
                match.status == "FINISHED" -> match.statusTitle.ifBlank { "پایان" }
                else -> listOf(match.date, match.time).filter { it.isNotBlank() }.joinToString(" · ")
            }
            views.setTextViewText(R.id.widget_status, status)
            views.setTextViewText(R.id.widget_refresh, if (snapshot.live) "●" else "↻")
            val extras = snapshot.matches.drop(1).take(2)
            views.setViewVisibility(R.id.widget_more_matches, if (extras.isEmpty()) View.GONE else View.VISIBLE)
            views.setTextViewText(R.id.widget_match_two, extras.getOrNull(0)?.let(::compactLine).orEmpty())
            views.setTextViewText(R.id.widget_match_three, extras.getOrNull(1)?.let(::compactLine).orEmpty())
            return views
        }

        private fun compactLine(match: com.natijeh.data.model.MatchEntity): String {
            val state = if (match.status == "SCHEDULED") match.time else "${match.homeScore}-${match.awayScore}"
            return "${match.homeTeamName}  $state  ${match.awayTeamName}"
        }
    }
}
