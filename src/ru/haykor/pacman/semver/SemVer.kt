package ru.haykor.pacman.semver

/**
 * Semantic version with optional pre-release identifiers.
 *
 * @property major major version number.
 * @property minor minor version number.
 * @property patch patch version number.
 * @property prerelease pre-release identifiers, empty for stable releases.
 */
data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val prerelease: List<String> = emptyList(),
) : Comparable<SemVer> {
    /** Whether this version is a pre-release. */
    val isPrerelease: Boolean
        get() = prerelease.isNotEmpty()

    override fun compareTo(other: SemVer): Int {
        val majorResult = major.compareTo(other.major)
        if (majorResult != EQUAL_RESULT) {
            return majorResult
        }
        val minorResult = minor.compareTo(other.minor)
        if (minorResult != EQUAL_RESULT) {
            return minorResult
        }
        val patchResult = patch.compareTo(other.patch)
        if (patchResult != EQUAL_RESULT) {
            return patchResult
        }
        return comparePrerelease(other.prerelease)
    }

    override fun toString(): String {
        val base = "$major.$minor.$patch"
        return if (prerelease.isEmpty()) base else "$base-${prerelease.joinToString(".")}"
    }

    private fun comparePrerelease(other: List<String>): Int {
        if (prerelease.isEmpty() && other.isEmpty()) {
            return 0
        }
        if (prerelease.isEmpty()) {
            return 1
        }
        if (other.isEmpty()) {
            return -1
        }
        val sharedSize = minOf(prerelease.size, other.size)
        for (index in 0 until sharedSize) {
            val result = compareIdentifier(prerelease[index], other[index])
            if (result != EQUAL_RESULT) {
                return result
            }
        }
        return prerelease.size.compareTo(other.size)
    }

    private fun compareIdentifier(left: String, right: String): Int {
        val leftNumber = left.toIntOrNull()
        val rightNumber = right.toIntOrNull()
        return when {
            leftNumber != null && rightNumber != null -> leftNumber.compareTo(rightNumber)
            leftNumber != null -> -1
            rightNumber != null -> 1
            else -> left.compareTo(right)
        }
    }

    private companion object {
        const val EQUAL_RESULT: Int = 0
    }
}
