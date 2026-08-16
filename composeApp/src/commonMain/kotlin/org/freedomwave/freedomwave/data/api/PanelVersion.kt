package org.freedomwave.data.api

/**
 * Version of the Remnawave panel the app is talking to.
 *
 * Read from `response.version` of `GET /api/system/stats/recap` — a field that exists with the
 * same shape on both 2.8.x and 3.x, on an endpoint the app already calls while verifying the
 * API key at login. No extra request and no extra token scope are needed to learn it.
 *
 * Only [major] drives behaviour. Panel 3.0.0 deleted the user `uuid` field outright and moved
 * every user route from `{uuid}` to `{userId}`; anything below major 3 keeps the 2.x contract.
 * See `plan.md` Phase 15 for the full route table.
 */
data class PanelVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    /** The original string as reported by the panel, kept so the value survives a save/load. */
    val raw: String
) {
    /** True when user routes are keyed by numeric `id` rather than `uuid`. */
    val isV3: Boolean get() = major >= MAJOR_V3

    companion object {
        private const val MAJOR_V3 = 3

        /**
         * Fallback when the panel version is unknown or unreadable.
         *
         * Deliberately reports as v3: guessing "new" against an old panel produces obvious 404s
         * on user routes, whereas guessing "old" against a new panel would send `uuid` values
         * that no longer exist and quietly break every user operation.
         */
        val UNKNOWN = PanelVersion(major = MAJOR_V3, minor = 0, patch = 0, raw = "")

        /**
         * Parse a panel version string. Tolerates a leading `v`, surrounding whitespace, a
         * missing minor/patch, and semver prerelease/build suffixes (`3.0.0-beta.1`, `3.1.0+b42`).
         * Anything it cannot read becomes [UNKNOWN].
         */
        fun parse(raw: String): PanelVersion {
            val trimmed = raw.trim()
            val cleaned = trimmed.removePrefix("v").removePrefix("V")
            // Cut any prerelease/build suffix before splitting on '.', so "3.0.0-beta.1" does
            // not turn its "-beta" segment into a bogus patch number.
            val core = cleaned.substringBefore('-').substringBefore('+')
            val parts = core.split('.')

            val major = parts.getOrNull(0)?.toIntOrNull() ?: return UNKNOWN
            return PanelVersion(
                major = major,
                minor = parts.getOrNull(1)?.toIntOrNull() ?: 0,
                patch = parts.getOrNull(2)?.toIntOrNull() ?: 0,
                raw   = trimmed
            )
        }
    }
}
