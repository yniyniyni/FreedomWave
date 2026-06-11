package art.yniyniyni.freedomwave.ui.l10n

import androidx.compose.runtime.Composable
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.time_ago_days
import freedomwave.composeapp.generated.resources.time_ago_hours
import freedomwave.composeapp.generated.resources.time_ago_minutes
import freedomwave.composeapp.generated.resources.time_ago_seconds
import freedomwave.composeapp.generated.resources.time_expired
import freedomwave.composeapp.generated.resources.symbol_infinity
import freedomwave.composeapp.generated.resources.time_left_days
import freedomwave.composeapp.generated.resources.time_left_hours
import freedomwave.composeapp.generated.resources.time_left_minutes
import freedomwave.composeapp.generated.resources.time_left_seconds
import freedomwave.composeapp.generated.resources.time_never
import freedomwave.composeapp.generated.resources.time_now
import freedomwave.composeapp.generated.resources.unit_b
import freedomwave.composeapp.generated.resources.unit_gb
import freedomwave.composeapp.generated.resources.unit_kb
import freedomwave.composeapp.generated.resources.unit_mb
import freedomwave.composeapp.generated.resources.unit_tb
import freedomwave.composeapp.generated.resources.uptime_days_hours
import freedomwave.composeapp.generated.resources.uptime_hours_minutes
import freedomwave.composeapp.generated.resources.uptime_minutes
import art.yniyniyni.freedomwave.util.ByteUnit
import art.yniyniyni.freedomwave.util.DurationUnit
import art.yniyniyni.freedomwave.util.ExpiryRemaining
import art.yniyniyni.freedomwave.util.RelativePast
import art.yniyniyni.freedomwave.util.UptimeParts
import art.yniyniyni.freedomwave.util.byteValue
import art.yniyniyni.freedomwave.util.format2
import org.jetbrains.compose.resources.stringResource

@Composable
fun RelativePast.localized(): String = when (this) {
    RelativePast.Never -> stringResource(Res.string.time_never)
    RelativePast.Now   -> stringResource(Res.string.time_now)
    is RelativePast.Ago -> stringResource(
        when (unit) {
            DurationUnit.SECONDS -> Res.string.time_ago_seconds
            DurationUnit.MINUTES -> Res.string.time_ago_minutes
            DurationUnit.HOURS   -> Res.string.time_ago_hours
            DurationUnit.DAYS    -> Res.string.time_ago_days
        }, value
    )
}

@Composable
fun ExpiryRemaining.localized(): String = when (this) {
    ExpiryRemaining.Infinite -> stringResource(Res.string.symbol_infinity)
    ExpiryRemaining.Expired  -> stringResource(Res.string.time_expired)
    is ExpiryRemaining.Left -> stringResource(
        when (unit) {
            DurationUnit.SECONDS -> Res.string.time_left_seconds
            DurationUnit.MINUTES -> Res.string.time_left_minutes
            DurationUnit.HOURS   -> Res.string.time_left_hours
            DurationUnit.DAYS    -> Res.string.time_left_days
        }, value
    )
}

@Composable
fun localizedBytes(bytes: Long): String {
    val (value, unit) = byteValue(bytes)
    val unitLabel = stringResource(
        when (unit) {
            ByteUnit.B  -> Res.string.unit_b
            ByteUnit.KB -> Res.string.unit_kb
            ByteUnit.MB -> Res.string.unit_mb
            ByteUnit.GB -> Res.string.unit_gb
            ByteUnit.TB -> Res.string.unit_tb
        }
    )
    return if (unit == ByteUnit.B) "${value.toLong()} $unitLabel" else "${value.format2()} $unitLabel"
}

@Composable
fun localizedBytes(bytesStr: String): String = localizedBytes(bytesStr.toLongOrNull() ?: 0L)

@Composable
fun UptimeParts.localized(): String = when {
    days > 0  -> stringResource(Res.string.uptime_days_hours, days, hours)
    hours > 0 -> stringResource(Res.string.uptime_hours_minutes, hours, minutes)
    else      -> stringResource(Res.string.uptime_minutes, minutes)
}
