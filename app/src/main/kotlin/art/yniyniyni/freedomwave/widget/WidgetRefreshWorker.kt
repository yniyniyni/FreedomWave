package art.yniyniyni.freedomwave.widget

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import art.yniyniyni.freedomwave.data.repository.DashboardRepository
import art.yniyniyni.freedomwave.data.store.AppPreferences
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext

const val SNAPSHOT_PREF_KEY = "widget_snapshot_json"

/**
 * Fetches dashboard stats (reusing the app's DashboardRepository via Koin),
 * resolves the next snapshot, writes it to every placed widget's Glance state,
 * and triggers a redraw. Koin is guaranteed started by FreedomWaveApplication,
 * which always runs before any worker in this process.
 */
class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val koin = GlobalContext.getOrNull() ?: return Result.retry()
        val prefs = koin.get<AppPreferences>()
        val repo = koin.get<DashboardRepository>()

        val apiKeyPresent = prefs.getApiKey() != null
        val fetch = if (apiKeyPresent) repo.getStats(clearCredentialsOnUnauthorized = false) else null
        val nowMs = System.currentTimeMillis()

        val manager = GlanceAppWidgetManager(applicationContext)
        val glanceIds = manager.getGlanceIds(DashboardWidget::class.java)

        for (glanceId in glanceIds) {
            updateAppWidgetState(applicationContext, glanceId) { mutablePrefs ->
                val previous = mutablePrefs[SnapshotKeys.snapshot]
                    ?.let { runCatching { Json.decodeFromString<WidgetSnapshot>(it) }.getOrNull() }
                val next = decideSnapshot(apiKeyPresent, fetch, nowMs, previous)
                mutablePrefs[SnapshotKeys.snapshot] = Json.encodeToString(next)
            }
        }
        DashboardWidget().updateAll(applicationContext)
        return Result.success()
    }
}

/** Glance state keys for the dashboard widget. */
object SnapshotKeys {
    val snapshot = stringPreferencesKey(SNAPSHOT_PREF_KEY)
}
