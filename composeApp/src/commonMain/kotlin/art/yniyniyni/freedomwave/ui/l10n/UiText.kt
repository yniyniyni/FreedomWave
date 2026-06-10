package art.yniyniyni.freedomwave.ui.l10n

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** A user-facing string that is either raw text (e.g. from the backend) or a localizable resource. */
sealed interface UiText {
    data class Raw(val value: String) : UiText
    data class Res(val res: StringResource, val args: List<Any> = emptyList()) : UiText {
        constructor(res: StringResource, vararg args: Any) : this(res, args.toList())
    }
}

@Composable
fun UiText.resolve(): String = when (this) {
    is UiText.Raw -> value
    is UiText.Res ->
        if (args.isEmpty()) stringResource(res)
        else stringResource(res, *args.toTypedArray())
}
