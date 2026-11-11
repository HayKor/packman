package ru.haykor.pacman.semver

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VersionRangeTest {
    @Test
    fun matchesExactVersion() {
        val range = VersionRange.parse("1.2.3")

        assertMatches(range, "1.2.3")
        assertDoesNotMatch(range, "1.2.4")
    }

    @Test
    fun matchesCaretRange() {
        val range = VersionRange.parse("^1.2.3")

        assertMatches(range, "1.2.3")
        assertMatches(range, "1.9.9")
        assertDoesNotMatch(range, "1.2.2")
        assertDoesNotMatch(range, "2.0.0")
    }

    @Test
    fun matchesCaretRangeOfZeroMajor() {
        assertDoesNotMatch(VersionRange.parse("^0.2.3"), "0.3.0")
        assertMatches(VersionRange.parse("^0.2.3"), "0.2.9")
        assertDoesNotMatch(VersionRange.parse("^0.0.3"), "0.0.4")
    }

    @Test
    fun matchesTildeRange() {
        val range = VersionRange.parse("~1.2.3")

        assertMatches(range, "1.2.9")
        assertDoesNotMatch(range, "1.3.0")
        assertMatches(VersionRange.parse("~1"), "1.9.9")
        assertDoesNotMatch(VersionRange.parse("~1"), "2.0.0")
    }

    @Test
    fun matchesComparatorRanges() {
        val range = VersionRange.parse(">=1.0.0 <2.0.0")

        assertMatches(range, "1.0.0")
        assertMatches(range, "1.9.9")
        assertDoesNotMatch(range, "2.0.0")
    }

    @Test
    fun expandsPartialVersions() {
        assertMatches(VersionRange.parse("1.2"), "1.2.5")
        assertDoesNotMatch(VersionRange.parse("1.2"), "1.3.0")
        assertMatches(VersionRange.parse("1"), "1.9.9")
        assertDoesNotMatch(VersionRange.parse("1"), "2.0.0")
        assertMatches(VersionRange.parse("1.2.x"), "1.2.7")
    }

    @Test
    fun expandsPartialComparatorVersions() {
        assertMatches(VersionRange.parse("<=1.2"), "1.2.9")
        assertDoesNotMatch(VersionRange.parse("<=1.2"), "1.3.0")
        assertMatches(VersionRange.parse(">1.2"), "1.3.0")
        assertDoesNotMatch(VersionRange.parse(">1.2"), "1.2.9")
    }

    @Test
    fun matchesHyphenRange() {
        val range = VersionRange.parse("1.2.3 - 2.0.0")

        assertMatches(range, "1.2.3")
        assertMatches(range, "2.0.0")
        assertDoesNotMatch(range, "2.0.1")
    }

    @Test
    fun matchesAlternatives() {
        val range = VersionRange.parse("1.x || >=3.0.0")

        assertMatches(range, "1.4.0")
        assertMatches(range, "3.1.0")
        assertDoesNotMatch(range, "2.0.0")
    }

    @Test
    fun matchesWildcardAndLatest() {
        assertMatches(VersionRange.parse("*"), "1.0.0")
        assertMatches(VersionRange.parse("latest"), "1.0.0")
    }

    @Test
    fun excludesPrereleasesUnlessRequested() {
        assertDoesNotMatch(VersionRange.parse("*"), "1.0.0-beta")
        assertMatches(VersionRange.parse(">=1.0.0-alpha"), "1.0.0-beta")
        assertMatches(VersionRange.parse("1.0.0-beta"), "1.0.0-beta")
    }

    @Test
    fun rejectsUnsupportedSyntax() {
        listOf("npm:other@1.0.0", "https://example.com/pkg.tgz", "file:../local").forEach { text ->
            assertFailsWith<VersionRangeException>("expected '$text' to be rejected") {
                VersionRange.parse(text)
            }
        }
    }

    @Test
    fun rejectsOperatorWithoutVersion() {
        assertFailsWith<VersionRangeException> { VersionRange.parse(">=") }
    }

    private fun assertMatches(range: VersionRange, version: String) {
        assertTrue(range.matches(versionOf(version)), "'$version' must match")
    }

    private fun assertDoesNotMatch(range: VersionRange, version: String) {
        assertFalse(range.matches(versionOf(version)), "'$version' must not match")
    }

    private fun versionOf(text: String): SemVer =
        requireNotNull(SemVerParser.parseOrNull(text)) { "invalid test version $text" }
}
