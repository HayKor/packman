package ru.haykor.pacman.semver

/**
 * Parser for semantic versions.
 */
object SemVerParser {
    private val VERSION_PATTERN = Regex(
        "^v?(\\d+)\\.(\\d+)\\.(\\d+)(?:-([0-9A-Za-z.-]+))?(?:\\+[0-9A-Za-z.-]+)?$",
    )
    private const val DOT: String = "."

    /**
     * Parses a version, tolerating a leading `v` and build metadata.
     *
     * @param text version text, for example `1.2.3-beta.1+build.7`.
     * @return parsed version or `null` when the text is not a semantic version.
     */
    fun parseOrNull(text: String): SemVer? {
        val match = VERSION_PATTERN.matchEntire(text.trim()) ?: return null
        val groups = match.groupValues
        val major = groups[1].toIntOrNull() ?: return null
        val minor = groups[2].toIntOrNull() ?: return null
        val patch = groups[3].toIntOrNull() ?: return null
        val prereleaseText = groups[4]
        val prerelease = if (prereleaseText.isEmpty()) {
            emptyList()
        } else {
            prereleaseText.split(DOT)
        }
        return SemVer(major, minor, patch, prerelease)
    }
}
