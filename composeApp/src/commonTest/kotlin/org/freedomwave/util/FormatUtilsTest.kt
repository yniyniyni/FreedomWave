package org.freedomwave.util

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FormatUtilsTest {
    private val now = Instant.parse("2026-06-02T00:00:00Z")

    @Test fun flag_for_DE() = assertEquals("🇩🇪", countryFlag("DE"))
    @Test fun flag_lowercase_us() = assertEquals("🇺🇸", countryFlag("us"))
    @Test fun flag_xx_returns_empty() = assertEquals("", countryFlag("XX"))
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

    // --- relativePast boundary tests ---
    @Test fun relative_59s_is_seconds() =
        assertEquals(RelativePast.Ago(59, DurationUnit.SECONDS), relativePast("2026-06-01T23:59:01Z", now))
    @Test fun relative_60s_is_minutes() =
        assertEquals(RelativePast.Ago(1, DurationUnit.MINUTES), relativePast("2026-06-01T23:59:00Z", now))
    @Test fun relative_3599s_is_minutes() =
        assertEquals(RelativePast.Ago(59, DurationUnit.MINUTES), relativePast("2026-06-01T23:00:01Z", now))
    @Test fun relative_3600s_is_hours() =
        assertEquals(RelativePast.Ago(1, DurationUnit.HOURS), relativePast("2026-06-01T23:00:00Z", now))
    @Test fun relative_86399s_is_hours() =
        assertEquals(RelativePast.Ago(23, DurationUnit.HOURS), relativePast("2026-06-01T00:00:01Z", now))
    @Test fun relative_86400s_is_days() =
        assertEquals(RelativePast.Ago(1, DurationUnit.DAYS), relativePast("2026-06-01T00:00:00Z", now))

    // --- byteValue boundary tests ---
    @Test fun bytes_exactly_1024_is_kb() = assertEquals(ByteValue(1.0, ByteUnit.KB), byteValue(1024L))
    @Test fun bytes_1023_is_b() = assertEquals(ByteValue(1023.0, ByteUnit.B), byteValue(1023L))
    @Test fun bytes_tb_range() = assertEquals(ByteValue(2.0, ByteUnit.TB), byteValue(2_199_023_255_552L))

    // --- format2 edge cases ---
    @Test fun format2_rounding_carry() = assertEquals("1.00", 0.999.format2())
    @Test fun format2_single_digit_remainder() = assertEquals("2.05", 2.05.format2())
    @Test fun format2_negative() = assertEquals("-1.50", (-1.5).format2())
    @Test fun format2_negative_below_one() = assertEquals("-0.50", (-0.5).format2())

    // --- uptimeParts boundary ---
    @Test fun uptime_under_minute_is_zero_minutes() = assertEquals(UptimeParts(0, 0, 0), uptimeParts(59L))
}
