package ru.haykor.pacman.semver

/**
 * Version with possibly missing minor and patch parts, as written in a range.
 *
 * @property major major version number, `null` for a wildcard.
 * @property minor minor version number, `null` for a wildcard.
 * @property patch patch version number, `null` for a wildcard.
 * @property prerelease pre-release identifiers of the lowest version.
 */
internal data class PartialVersion(
    val major: Int?,
    val minor: Int?,
    val patch: Int?,
    val prerelease: List<String> = emptyList(),
) {
    /** Whether all three numeric parts are present. */
    val isComplete: Boolean
        get() = major != null && minor != null && patch != null

    /**
     * Lowest version covered by this partial version.
     *
     * @return version with missing parts replaced by zero.
     */
    fun lowest(): SemVer = SemVer(major ?: 0, minor ?: 0, patch ?: 0, prerelease)
}

/**
 * Parses the numeric part of a version range token.
 */
internal object PartialVersionParser {
    private const val DOT: Char = '.'
    private const val BUILD_SEPARATOR: Char = '+'
    private const val PRERELEASE_SEPARATOR: Char = '-'
    private const val VERSION_PREFIX: String = "v"
    private const val EQUALS_PREFIX: String = "="
    private val WILDCARDS = setOf("x", "X", "*")

    /**
     * Parses a partial version token.
     *
     * @param text token such as `1`, `1.2`, `1.2.x` or `1.2.3-beta`.
     * @return parsed version or `null` when the token is not a version.
     */
    fun parseOrNull(text: String): PartialVersion? {
        val cleaned = text.removePrefix(VERSION_PREFIX).removePrefix(EQUALS_PREFIX)
        val withoutBuild = cleaned.substringBefore(BUILD_SEPARATOR)
        val core = withoutBuild.substringBefore(PRERELEASE_SEPARATOR)
        val prerelease = if (withoutBuild.contains(PRERELEASE_SEPARATOR)) {
            withoutBuild.substringAfter(PRERELEASE_SEPARATOR).split(DOT).filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
        val parts = core.split(DOT)
        if (parts.isEmpty() || parts.size > MAX_PARTS) {
            return null
        }
        val numbers = readNumbers(parts) ?: return null
        if (numbers.first() == null) {
            return null
        }
        return PartialVersion(
            major = numbers[0],
            minor = numbers.getOrNull(1),
            patch = numbers.getOrNull(2),
            prerelease = prerelease,
        )
    }

    private fun readNumbers(parts: List<String>): List<Int?>? {
        val numbers = mutableListOf<Int?>()
        var wildcardSeen = false
        for (part in parts) {
            val wildcard = wildcardSeen || part in WILDCARDS
            if (wildcard) {
                wildcardSeen = true
                numbers += null
            } else {
                numbers += part.toIntOrNull() ?: return null
            }
        }
        return numbers
    }

    private const val MAX_PARTS: Int = 3
}
