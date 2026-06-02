package art.yniyniyni.freedomwave.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

enum class ExpiryPreset(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    MONTHS_3("3M"),
    MONTHS_6("6M"),
    YEAR("Year"),
    FOREVER("Forever"),
}

/** Epoch-millis expiry for a preset, relative to [now]. FOREVER returns the 2099 sentinel. */
fun presetExpiryMillis(
    preset: ExpiryPreset,
    now: Instant = Clock.System.now(),
    tz: TimeZone = TimeZone.currentSystemDefault(),
): Long = when (preset) {
    ExpiryPreset.DAY      -> now.plus(1, DateTimeUnit.DAY, tz)
    ExpiryPreset.WEEK     -> now.plus(7, DateTimeUnit.DAY, tz)
    ExpiryPreset.MONTH    -> now.plus(1, DateTimeUnit.MONTH, tz)
    ExpiryPreset.MONTHS_3 -> now.plus(3, DateTimeUnit.MONTH, tz)
    ExpiryPreset.MONTHS_6 -> now.plus(6, DateTimeUnit.MONTH, tz)
    ExpiryPreset.YEAR     -> now.plus(1, DateTimeUnit.YEAR, tz)
    ExpiryPreset.FOREVER  -> Instant.parse(FOREVER_DATE)
}.toEpochMilliseconds()
