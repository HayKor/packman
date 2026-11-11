package ru.haykor.pacman.semver

/**
 * Chooses the highest published version that satisfies a range.
 */
object VersionResolver {
    /**
     * Resolves a version range against the published versions of a package.
     *
     * @param available version strings taken from the registry document.
     * @param rangeText range declared by the dependent package.
     * @return the highest matching version string.
     * @throws VersionRangeException when the range is unsupported or no version matches.
     */
    fun resolve(available: Collection<String>, rangeText: String): String {
        val range = VersionRange.parse(rangeText)
        val candidates = available.mapNotNull { text ->
            SemVerParser.parseOrNull(text)?.let { version -> version to text }
        }.filter { (version, _) -> range.matches(version) }
        return candidates.maxByOrNull { (version, _) -> version }?.second
            ?: throw VersionRangeException(
                "no published version satisfies range '$rangeText' " +
                    "(${available.size} versions available)",
            )
    }
}
