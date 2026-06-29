package org.freedomwave.widget

import org.freedomwave.domain.model.DashboardStats

/**
 * Pure resolution of the snapshot the widget should display next.
 * Networking and persistence happen in the worker; this only picks the result.
 */
fun decideSnapshot(
    apiKeyPresent: Boolean,
    result: Result<DashboardStats>?,
    nowMs: Long,
    previous: WidgetSnapshot?
): WidgetSnapshot {
    if (!apiKeyPresent) return WidgetSnapshot.notConnected(nowMs)
    val r = result ?: return (previous ?: WidgetSnapshot()).copy(status = Status.Error)
    return r.fold(
        onSuccess = { WidgetSnapshot.from(it, nowMs) },
        onFailure = { (previous ?: WidgetSnapshot(updatedAtEpochMs = nowMs)).copy(status = Status.Error) }
    )
}
