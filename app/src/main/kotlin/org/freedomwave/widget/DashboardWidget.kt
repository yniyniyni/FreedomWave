package org.freedomwave.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import org.freedomwave.MainActivity
import org.freedomwave.R
import kotlinx.serialization.json.Json

class DashboardWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL, MEDIUM, LARGE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetBody(readSnapshot(currentState()))
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
    val isLarge = size.height >= DashboardWidget.LARGE.height
    val isWide = size.width >= DashboardWidget.MEDIUM.width

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.surface)
            .cornerRadius(18.dp)
            .padding(if (isLarge) 16.dp else 12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        if (snapshot.status == Status.NotConnected) {
            NotConnected(context)
            return@Column
        }

        if (isLarge) {
            Header(context)
            Spacer(GlanceModifier.height(12.dp))
        }

        // Priority stats — always present.
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Stat(context.getString(R.string.widget_online), snapshot.onlineNow.toString(), WidgetColors.aqua, hero = true)
            Spacer(GlanceModifier.width(16.dp))
            Stat(context.getString(R.string.widget_traffic), snapshot.trafficLabel, WidgetColors.onSurface)
            if (isWide) {
                Spacer(GlanceModifier.width(16.dp))
                val nodes = nodesColor(snapshot)
                Stat(
                    context.getString(R.string.widget_nodes),
                    "${snapshot.nodesOnline}/${snapshot.nodesTotal}",
                    nodes,
                    dot = nodes
                )
            }
        }

        // Fills in only at the large breakpoint.
        if (isLarge) {
            Spacer(GlanceModifier.height(12.dp))
            Stat(
                context.getString(R.string.widget_users),
                "${snapshot.activeUsers}/${snapshot.totalUsers}",
                WidgetColors.onSurface
            )
        }

        Spacer(GlanceModifier.defaultWeight())
        Footer(context, snapshot)
    }
}

/** Health color for the nodes figure: all up = green, none up = red, partial = amber. */
private fun nodesColor(s: WidgetSnapshot): ColorProvider = when {
    s.nodesTotal == 0 -> WidgetColors.onSurfaceVariant
    s.nodesOnline >= s.nodesTotal -> WidgetColors.online
    s.nodesOnline == 0 -> WidgetColors.down
    else -> WidgetColors.warn
}

@Composable
private fun Header(context: Context) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            provider = ImageProvider(R.drawable.ic_fw_wave),
            contentDescription = null,
            modifier = GlanceModifier.size(22.dp)
        )
        Spacer(GlanceModifier.width(8.dp))
        Text(
            context.getString(R.string.widget_dashboard),
            style = TextStyle(color = WidgetColors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun Stat(
    label: String,
    value: String,
    valueColor: ColorProvider,
    hero: Boolean = false,
    dot: ColorProvider? = null
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (dot != null) {
                Box(GlanceModifier.size(8.dp).cornerRadius(4.dp).background(dot)) {}
                Spacer(GlanceModifier.width(6.dp))
            }
            Text(
                value,
                style = TextStyle(
                    color = valueColor,
                    fontSize = if (hero) 22.sp else 18.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
        // Overline label: uppercase, muted (per the brand's badge/overline treatment).
        Text(
            label.uppercase(),
            style = TextStyle(
                color = WidgetColors.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun Footer(context: Context, snapshot: WidgetSnapshot) {
    val nowMs = System.currentTimeMillis()
    val isError = snapshot.status == Status.Error
    val label = when (snapshot.status) {
        Status.Error -> context.getString(R.string.widget_refresh_failed)
        Status.Loading -> context.getString(R.string.widget_loading)
        else -> context.getString(R.string.widget_updated, relativeTimeLabel(snapshot.updatedAtEpochMs, nowMs))
    }
    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = if (isError) WidgetColors.down else WidgetColors.onSurfaceVariant,
                fontSize = 11.sp
            )
        )
        Image(
            provider = ImageProvider(R.drawable.ic_fw_refresh),
            contentDescription = context.getString(R.string.widget_refresh_cd),
            modifier = GlanceModifier.size(18.dp).clickable(actionRunCallback<RefreshCallback>()),
            colorFilter = ColorFilter.tint(WidgetColors.aqua)
        )
    }
}

@Composable
private fun NotConnected(context: Context) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_fw_wave),
            contentDescription = null,
            modifier = GlanceModifier.size(28.dp)
        )
        Spacer(GlanceModifier.height(8.dp))
        Text(
            context.getString(R.string.widget_not_connected),
            style = TextStyle(
                color = WidgetColors.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}
