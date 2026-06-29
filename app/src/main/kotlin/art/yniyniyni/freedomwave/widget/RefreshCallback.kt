package art.yniyniyni.freedomwave.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

/** Invoked when the user taps the refresh glyph — enqueues an immediate fetch. */
class RefreshCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WidgetRefreshScheduler.refreshNow(context)
    }
}
