package art.yniyniyni.freedomwave.ui.l10n

import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.language_english
import freedomwave.composeapp.generated.resources.language_russian
import freedomwave.composeapp.generated.resources.settings_language_system
import org.jetbrains.compose.resources.StringResource

// When adding a locale, also update app/src/main/res/xml/locales_config.xml and add composeResources/values-<tag>/strings.xml
enum class AppLanguage(val tag: String?, val labelRes: StringResource) {
    SYSTEM(null, Res.string.settings_language_system),
    ENGLISH("en", Res.string.language_english),
    RUSSIAN("ru", Res.string.language_russian);

    companion object {
        fun fromTag(tag: String?): AppLanguage {
            val first = tag?.substringBefore(',')
            return entries.firstOrNull { e ->
                e.tag != null && first != null && (first == e.tag || first.startsWith("${e.tag}-"))
            } ?: SYSTEM
        }
    }
}

/** Set the app language override; null = follow system. Persisted by the platform. */
expect fun applyAppLanguage(tag: String?)

/** Current override tag (e.g. "ru"), or null when following the system locale. */
expect fun currentAppLanguageTag(): String?
