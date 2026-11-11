package ru.haykor.pacman.semver

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VersionResolverTest {
    private val available = listOf("1.0.0", "1.2.0", "1.3.0", "2.0.0")

    @Test
    fun picksHighestMatchingVersion() {
        assertEquals("1.3.0", VersionResolver.resolve(available, "^1.0.0"))
        assertEquals("1.0.0", VersionResolver.resolve(available, "~1.0.0"))
        assertEquals("2.0.0", VersionResolver.resolve(available, ">=1.5.0"))
    }

    @Test
    fun reportsUnsatisfiedRange() {
        val error = assertFailsWith<VersionRangeException> {
            VersionResolver.resolve(available, "^3.0.0")
        }

        assertContains(error.message.orEmpty(), "no published version satisfies range '^3.0.0'")
    }

    @Test
    fun ignoresUnparsableVersions() {
        val mixed = listOf("1.0.0", "not-a-version", "1.1.0")

        assertEquals("1.1.0", VersionResolver.resolve(mixed, "^1.0.0"))
    }
}
