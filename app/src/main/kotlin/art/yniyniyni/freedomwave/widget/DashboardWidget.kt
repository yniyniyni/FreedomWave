package art.yniyniyni.freedomwave.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import art.yniyniyni.freedomwave.MainActivity
import art.yniyniyni.freedomwave.R
import kotlinx.serialization.json.Json

class DashboardWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL, MEDIUM, LARGE)
    )

    override suspend fun provideGlance(context: android.content.Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetBody(readSnapshot(currentState()))
            }
        }
    }

    companion object {
        val SMALL = DpSize(120.dp, 80.dp)
        val MEDIUM = DpSize(260.dp, 80.dp)
        val LARGE = DpSize(260.dp, 160.dp)
    }
}

private fun readSnapshot(prefs: Preferences): WidgetSnapshot {
    val json = prefs[SnapshotKeys.snapshot] ?: return WidgetSnapshot.loading()
    return runCatching { Json.decodeFromString<WidgetSnapshot>(json) }.getOrDefault(WidgetSnapshot.loading())
}

@Composable
private fun WidgetBody(snapshot: WidgetSnapshot) {
    val context = LocalContext.current
    val size = LocalSize.current
    val openApp = GlanceModifier.clickable(actionStartActivity<MainActivity>())

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(12.dp)
            .then(openApp)
    ) {
        if (snapshot.status == Status.NotConnected) {
            Text(context.getString(R.string.widget_not_connected))
            return@Column
        }

        // Priority stats: always present.
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Stat(context.getString(R.string.widget_online), snapshot.onlineNow.toString())
            Spacer(GlanceModifier.width(16.dp))
            Stat(context.getString(R.string.widget_traffic), snapshot.trafficLabel)
            if (size.width >= DashboardWidget.MEDIUM.width) {
                Spacer(GlanceModifier.width(16.dp))
                Stat(context.getString(R.string.widget_nodes), "${snapshot.nodesOnline}/${snapshot.nodesTotal}")
            }
        }

        // Fills in only at the large breakpoint.
        if (size.height >= DashboardWidget.LARGE.height) {
            Spacer(GlanceModifier.width(8.dp))
            Stat(context.getString(R.string.widget_users), "${snapshot.activeUsers}/${snapshot.totalUsers}")
        }

        Spacer(GlanceModifier.width(8.dp))
        Footer(snapshot)
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(value, style = TextStyle(color = GlanceTheme.colors.onSurface))
        Text(label, style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant))
    }
}

@Composable
private fun Footer(snapshot: WidgetSnapshot) {
    val context = LocalContext.current
    val nowMs = System.currentTimeMillis()
    val label = when (snapshot.status) {
        Status.Error -> context.getString(R.string.widget_refresh_failed)
        Status.Loading -> context.getString(R.string.widget_loading)
        else -> context.getString(R.string.widget_updated, relativeTimeLabel(snapshot.updatedAtEpochMs, nowMs))
    }
    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant))
        Spacer(GlanceModifier.width(8.dp))
        Text("↻", modifier = GlanceModifier.clickable(actionRunCallback<RefreshCallback>()))
    }
}
