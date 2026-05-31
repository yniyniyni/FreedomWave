package art.yniyniyni.freedomwave.util

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
