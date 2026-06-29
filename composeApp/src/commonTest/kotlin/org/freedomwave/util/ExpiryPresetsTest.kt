package org.freedomwave.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpiryPresetsTest {
    private val now = Instant.parse("2026-01-15T12:00:00Z")
    private val tz = TimeZone.UTC

    @Test fun day_adds_one_day() =
        assertEquals(Instant.parse("2026-01-16T12:00:00Z").toEpochMilliseconds(),
            presetExpiryMillis(ExpiryPreset.DAY, now, tz))

    @Test fun week_adds_seven_days() =
        assertEquals(Instant.parse("2026-01-22T12:00:00Z").toEpochMilliseconds(),
            presetExpiryMillis(ExpiryPreset.WEEK, now, tz))

    @Test fun month_adds_one_calendar_month() =
        assertEquals(Instant.parse("2026-02-15T12:00:00Z").toEpochMilliseconds(),
            presetExpiryMillis(ExpiryPreset.MONTH, now, tz))

    @Test fun three_months() =
        assertEquals(Instant.parse("2026-04-15T12:00:00Z").toEpochMilliseconds(),
            presetExpiryMillis(ExpiryPreset.MONTHS_3, now, tz))

    @Test fun six_months() =
        assertEquals(Instant.parse("2026-07-15T12:00:00Z").toEpochMilliseconds(),
            presetExpiryMillis(ExpiryPreset.MONTHS_6, now, tz))

    @Test fun year() =
        assertEquals(Instant.parse("2027-01-15T12:00:00Z").toEpochMilliseconds(),
            presetExpiryMillis(ExpiryPreset.YEAR, now, tz))

    @Test fun forever_is_sentinel() =
        assertEquals(Instant.parse(FOREVER_DATE).toEpochMilliseconds(),
            presetExpiryMillis(ExpiryPreset.FOREVER, now, tz))
}
