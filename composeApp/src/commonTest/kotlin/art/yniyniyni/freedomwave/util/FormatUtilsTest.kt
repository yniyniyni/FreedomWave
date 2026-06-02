package art.yniyniyni.freedomwave.util

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FormatUtilsTest {
    private val now = Instant.parse("2026-06-02T00:00:00Z")

    @Test fun flag_for_DE() = assertEquals("🇩🇪", countryFlag("DE"))
    @Test fun flag_lowercase_us() = assertEquals("🇺🇸", countryFlag("us"))
    @Test fun flag_empty_for_bad_input() {
        assertEquals("", countryFlag(""))
        assertEquals("", countryFlag("X"))
        assertEquals("", countryFlag("1A"))
    }

    @Test fun relative_minutes() =
        assertEquals("5m ago", formatRelativePast("2026-06-01T23:55:00Z", now))
    @Test fun relative_hours() =
        assertEquals("2h ago", formatRelativePast("2026-06-01T22:00:00Z", now))
    @Test fun relative_days() =
        assertEquals("3d ago", formatRelativePast("2026-05-30T00:00:00Z", now))
    @Test fun relative_null_is_never() =
        assertEquals("Never", formatRelativePast(null, now))

    @Test fun expiry_days_left() =
        assertEquals("12d left", formatExpiryRemaining("2026-06-14T00:00:00.000Z", now))
    @Test fun expiry_expired() =
        assertEquals("Expired", formatExpiryRemaining("2026-05-01T00:00:00.000Z", now))
    @Test fun expiry_forever_sentinel() =
        assertEquals("∞", formatExpiryRemaining(FOREVER_DATE, now))
    @Test fun expiry_null_is_infinite() =
        assertEquals("∞", formatExpiryRemaining(null, now))

    @Test fun relative_seconds() =
        assertEquals("15s ago", formatRelativePast("2026-06-01T23:59:45Z", now))

    @Test fun relative_future_is_now() =
        assertEquals("now", formatRelativePast("2026-06-02T00:00:01Z", now))

    @Test fun expiry_hours_left() =
        assertEquals("4h left", formatExpiryRemaining("2026-06-02T04:00:00.000Z", now))

    @Test fun expiry_seconds_left() =
        assertEquals("30s left", formatExpiryRemaining("2026-06-02T00:00:30.000Z", now))

    @Test fun parse_instant_invalid_returns_null() {
        assertNull(parseInstant("not-a-date"))
        assertNull(parseInstant(null))
        assertNull(parseInstant(""))
    }

    @Test fun parse_instant_valid_round_trip() =
        assertEquals(Instant.parse("2026-06-02T00:00:00Z"), parseInstant("2026-06-02T00:00:00Z"))
}
