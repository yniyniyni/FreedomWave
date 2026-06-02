package art.yniyniyni.freedomwave.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

const val FOREVER_DATE = "2099-12-31T23:59:59.000Z"

/** Parse an ISO-8601 instant, returning null on blank/invalid input. */
fun parseInstant(iso: String?): Instant? =
    if (iso.isNullOrBlank()) null else runCatching { Instant.parse(iso) }.getOrNull()

/** Regional-indicator flag emoji from a 2-letter ISO country code. KMP-safe (no java.lang.Character). */
fun countryFlag(code: String): String {
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

/** "5m ago" / "2h ago" / "3d ago" for a past instant; "Never" when null/invalid. */
fun formatRelativePast(iso: String?, now: Instant = Clock.System.now()): String {
    val instant = parseInstant(iso) ?: return "Never"
    val seconds = (now - instant).inWholeSeconds
    if (seconds < 0) return "now"
    return when {
        seconds < 60     -> "${seconds}s ago"
        seconds < 3600   -> "${seconds / 60}m ago"
        seconds < 86_400 -> "${seconds / 3600}h ago"
        else             -> "${seconds / 86_400}d ago"
    }
}

/** "12d left" / "3h left" / "Expired"; "∞" for no expiry or the Forever sentinel (year >= 2099). */
fun formatExpiryRemaining(iso: String?, now: Instant = Clock.System.now()): String {
    val instant = parseInstant(iso) ?: return "∞"
    if (instant.toLocalDateTime(TimeZone.UTC).year >= 2099) return "∞"
    val seconds = (instant - now).inWholeSeconds
    if (seconds <= 0) return "Expired"
    return when {
        seconds < 60     -> "${seconds}s left"
        seconds < 3600   -> "${seconds / 60}m left"
        seconds < 86_400 -> "${seconds / 3600}h left"
        else             -> "${seconds / 86_400}d left"
    }
}

fun formatBytes(bytes: Long): String = when {
    bytes >= 1_099_511_627_776L -> "%.2f TB".format(bytes / 1_099_511_627_776.0)
    bytes >= 1_073_741_824L     -> "%.2f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576L         -> "%.2f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024L             -> "%.2f KB".format(bytes / 1_024.0)
    else                        -> "$bytes B"
}

fun formatBytesStr(bytesStr: String): String = formatBytes(bytesStr.toLongOrNull() ?: 0L)

fun formatUptime(seconds: Long): String {
    val days = seconds / 86_400
    val hours = (seconds % 86_400) / 3_600
    val minutes = (seconds % 3_600) / 60
    return when {
        days > 0  -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        else      -> "${minutes}m"
    }
}
