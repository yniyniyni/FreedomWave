package art.yniyniyni.freedomwave.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.roundToLong

const val FOREVER_DATE = "2099-12-31T23:59:59.000Z"

/** Parse an ISO-8601 instant, returning null on blank/invalid input. */
fun parseInstant(iso: String?): Instant? =
    if (iso.isNullOrBlank()) null else runCatching { Instant.parse(iso) }.getOrNull()

/** Locale-invariant `yyyy-MM-dd` rendering of an ISO instant in the device time zone; "—" on null/invalid. */
fun formatDate(iso: String?, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val instant = parseInstant(iso) ?: return "—"
    val date = instant.toLocalDateTime(zone).date
    fun p2(n: Int) = n.toString().padStart(2, '0')
    return "${date.year}-${p2(date.monthNumber)}-${p2(date.dayOfMonth)}"
}

/** Regional-indicator flag emoji from a 2-letter ISO country code. KMP-safe (no java.lang.Character). */
fun countryFlag(code: String): String {
    if (code == "XX") return ""
    if (code.length != 2) return ""
    val a = code[0].uppercaseChar()
    val b = code[1].uppercaseChar()
    if (a !in 'A'..'Z' || b !in 'A'..'Z') return ""
    val base = 0x1F1E6
    return buildString {
        appendCodePoint(base + (a - 'A'))
        appendCodePoint(base + (b - 'A'))
    }
}

private fun StringBuilder.appendCodePoint(codePoint: Int) {
    if (codePoint <= 0xFFFF) {
        // BMP fallback; regional indicators are all > 0xFFFF
        append(codePoint.toChar())
    } else {
        val cp = codePoint - 0x10000
        append((0xD800 + (cp shr 10)).toChar())
        append((0xDC00 + (cp and 0x3FF)).toChar())
    }
}

enum class DurationUnit { SECONDS, MINUTES, HOURS, DAYS }

sealed interface RelativePast {
    data object Never : RelativePast
    data object Now : RelativePast
    data class Ago(val value: Long, val unit: DurationUnit) : RelativePast
}

sealed interface ExpiryRemaining {
    data object Infinite : ExpiryRemaining
    data object Expired : ExpiryRemaining
    data class Left(val value: Long, val unit: DurationUnit) : ExpiryRemaining
}

private fun durationOf(seconds: Long): Pair<Long, DurationUnit> = when {
    seconds < 60     -> seconds to DurationUnit.SECONDS
    seconds < 3600   -> seconds / 60 to DurationUnit.MINUTES
    seconds < 86_400 -> seconds / 3600 to DurationUnit.HOURS
    else             -> seconds / 86_400 to DurationUnit.DAYS
}

/** Structured "time since" for a past instant; [RelativePast.Never] when null/invalid. */
fun relativePast(iso: String?, now: Instant = Clock.System.now()): RelativePast {
    val instant = parseInstant(iso) ?: return RelativePast.Never
    val seconds = (now - instant).inWholeSeconds
    if (seconds < 0) return RelativePast.Now
    val (value, unit) = durationOf(seconds)
    return RelativePast.Ago(value, unit)
}

/** Structured "time left"; [ExpiryRemaining.Infinite] for no expiry or the Forever sentinel (year >= 2099). */
fun expiryRemaining(iso: String?, now: Instant = Clock.System.now()): ExpiryRemaining {
    val instant = parseInstant(iso) ?: return ExpiryRemaining.Infinite
    if (instant.toLocalDateTime(TimeZone.UTC).year >= 2099) return ExpiryRemaining.Infinite
    val seconds = (instant - now).inWholeSeconds
    if (seconds <= 0) return ExpiryRemaining.Expired
    val (value, unit) = durationOf(seconds)
    return ExpiryRemaining.Left(value, unit)
}

enum class ByteUnit(val factor: Double) {
    B(1.0), KB(1_024.0), MB(1_048_576.0), GB(1_073_741_824.0), TB(1_099_511_627_776.0)
}

data class ByteValue(val value: Double, val unit: ByteUnit)

/** Scales a raw byte count into the largest fitting unit. */
fun byteValue(bytes: Long): ByteValue {
    val unit = ByteUnit.entries.last { bytes >= it.factor || it == ByteUnit.B }
    return ByteValue(bytes / unit.factor, unit)
}

data class UptimeParts(val days: Long, val hours: Long, val minutes: Long)

fun uptimeParts(seconds: Long): UptimeParts =
    UptimeParts(seconds / 86_400, (seconds % 86_400) / 3_600, (seconds % 3_600) / 60)

/** KMP-safe "%.2f" replacement (String.format is JVM-only). */
fun Double.format2(): String {
    // TODO(fa): digits + decimal separator localization when Farsi lands
    val scaled = (this * 100).roundToLong()
    val abs = abs(scaled)
    return "${if (scaled < 0) "-" else ""}${abs / 100}.${(abs % 100).toString().padStart(2, '0')}"
}

/**
 * Parses a humanized byte string as produced by the Remnawave backend
 * (e.g. "31.29 GiB", "-10.33 GiB", "0") into a raw byte count.
 * Units are binary (1 KiB = 1024 B); KB/MB/… are treated the same as KiB/MiB/….
 */
fun parsePrettyBytes(pretty: String): Long {
    val trimmed = pretty.trim()
    if (trimmed.isEmpty() || trimmed == "0") return 0L
    val parts = trimmed.split(' ')
    val value = parts[0].replace(',', '.').toDoubleOrNull() ?: return 0L
    val multiplier = when (parts.getOrNull(1)?.uppercase()) {
        null, "B"      -> 1.0
        "KIB", "KB"    -> 1_024.0
        "MIB", "MB"    -> 1_048_576.0
        "GIB", "GB"    -> 1_073_741_824.0
        "TIB", "TB"    -> 1_099_511_627_776.0
        "PIB", "PB"    -> 1_125_899_906_842_624.0
        else           -> 1.0
    }
    return (value * multiplier).toLong()
}
