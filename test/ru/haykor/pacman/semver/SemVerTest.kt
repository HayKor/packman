package ru.haykor.pacman.semver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SemVerTest {
    @Test
    fun parsesPlainVersion() {
        assertEquals(SemVer(1, 2, 3), SemVerParser.parseOrNull("1.2.3"))
    }

    @Test
    fun parsesPrereleaseAndBuildMetadata() {
        assertEquals(
            SemVer(1, 2, 3, listOf("beta", "1")),
            SemVerParser.parseOrNull("v1.2.3-beta.1+build.7"),
        )
    }

    @Test
    fun rendersVersionBackToString() {
        assertEquals("1.2.3-beta.1", SemVer(1, 2, 3, listOf("beta", "1")).toString())
    }

    @Test
    fun rejectsMalformedVersions() {
        listOf("1.0", "abc", "1.0.0.0", "").forEach { text ->
            assertNull(SemVerParser.parseOrNull(text), "expected '$text' to be rejected")
        }
    }

    @Test
    fun ordersReleaseVersions() {
        assertTrue(SemVer(1, 0, 0) < SemVer(1, 0, 1))
        assertTrue(SemVer(1, 0, 1) < SemVer(1, 1, 0))
        assertTrue(SemVer(1, 9, 9) < SemVer(2, 0, 0))
    }

    @Test
    fun ordersPrereleaseVersions() {
        assertTrue(SemVer(1, 0, 0, listOf("alpha")) < SemVer(1, 0, 0, listOf("alpha", "1")))
        assertTrue(SemVer(1, 0, 0, listOf("alpha", "1")) < SemVer(1, 0, 0, listOf("beta")))
        assertTrue(SemVer(1, 0, 0, listOf("beta")) < SemVer(1, 0, 0))
    }
}
