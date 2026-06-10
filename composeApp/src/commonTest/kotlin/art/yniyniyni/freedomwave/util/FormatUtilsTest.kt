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
        assertEquals(RelativePast.Ago(5, DurationUnit.MINUTES), relativePast("2026-06-01T23:55:00Z", now))
    @Test fun relative_hours() =
        assertEquals(RelativePast.Ago(2, DurationUnit.HOURS), relativePast("2026-06-01T22:00:00Z", now))
    @Test fun relative_days() =
        assertEquals(RelativePast.Ago(3, DurationUnit.DAYS), relativePast("2026-05-30T00:00:00Z", now))
    @Test fun relative_seconds() =
        assertEquals(RelativePast.Ago(15, DurationUnit.SECONDS), relativePast("2026-06-01T23:59:45Z", now))
    @Test fun relative_null_is_never() = assertEquals(RelativePast.Never, relativePast(null, now))
    @Test fun relative_future_is_now() =
        assertEquals(RelativePast.Now, relativePast("2026-06-02T00:00:01Z", now))

    @Test fun expiry_days_left() =
        assertEquals(ExpiryRemaining.Left(12, DurationUnit.DAYS), expiryRemaining("2026-06-14T00:00:00.000Z", now))
    @Test fun expiry_hours_left() =
        assertEquals(ExpiryRemaining.Left(4, DurationUnit.HOURS), expiryRemaining("2026-06-02T04:00:00.000Z", now))
    @Test fun expiry_seconds_left() =
        assertEquals(ExpiryRemaining.Left(30, DurationUnit.SECONDS), expiryRemaining("2026-06-02T00:00:30.000Z", now))
    @Test fun expiry_expired() =
        assertEquals(ExpiryRemaining.Expired, expiryRemaining("2026-05-01T00:00:00.000Z", now))
    @Test fun expiry_forever_sentinel() = assertEquals(ExpiryRemaining.Infinite, expiryRemaining(FOREVER_DATE, now))
    @Test fun expiry_null_is_infinite() = assertEquals(ExpiryRemaining.Infinite, expiryRemaining(null, now))

    @Test fun bytes_gb() = assertEquals(ByteValue(2.0, ByteUnit.GB), byteValue(2_147_483_648L))
    @Test fun bytes_small() = assertEquals(ByteValue(512.0, ByteUnit.B), byteValue(512L))
    @Test fun uptime_days() = assertEquals(UptimeParts(3, 4, 5), uptimeParts(3 * 86_400L + 4 * 3_600L + 5 * 60L))
    @Test fun double_format2() = assertEquals("31.29", 31.2891.format2())
    @Test fun double_format2_pads_zero() = assertEquals("2.00", 2.0.format2())

    @Test fun parse_instant_invalid_returns_null() {
        assertNull(parseInstant("not-a-date"))
        assertNull(parseInstant(null))
        assertNull(parseInstant(""))
    }

    @Test fun parse_instant_valid_round_trip() =
        assertEquals(Instant.parse("2026-06-02T00:00:00Z"), parseInstant("2026-06-02T00:00:00Z"))
}
