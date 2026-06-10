package art.yniyniyni.freedomwave.ui.l10n

import art.yniyniyni.freedomwave.resources.Res
import art.yniyniyni.freedomwave.resources.language_english
import art.yniyniyni.freedomwave.resources.language_russian
import art.yniyniyni.freedomwave.resources.settings_language_system
import org.jetbrains.compose.resources.StringResource

enum class AppLanguage(val tag: String?, val labelRes: StringResource) {
    SYSTEM(null, Res.string.settings_language_system),
    ENGLISH("en", Res.string.language_english),
    RUSSIAN("ru", Res.string.language_russian);

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { e -> e.tag?.let { tag?.startsWith(it) } == true } ?: SYSTEM
    }
}

/** Set the app language override; null = follow system. Persisted by the platform. */
expect fun applyAppLanguage(tag: String?)

/** Current override tag (e.g. "ru"), or null when following the system locale. */
expect fun currentAppLanguageTag(): String?
