package art.yniyniyni.freedomwave.util

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
