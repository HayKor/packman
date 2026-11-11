package ru.haykor.pacman.semver

/**
 * Comparison operator of a single version range comparator.
 */
internal enum class RangeOperator {
    /** Exact version match. */
    EQUAL,

    /** Candidate must be greater than the bound. */
    GREATER,

    /** Candidate must be greater than or equal to the bound. */
    GREATER_OR_EQUAL,

    /** Candidate must be less than the bound. */
    LESS,

    /** Candidate must be less than or equal to the bound. */
    LESS_OR_EQUAL,
}

/**
 * Single bound of a version range.
 *
 * @property operator comparison operator.
 * @property version version used as the bound.
 */
internal data class VersionComparator(val operator: RangeOperator, val version: SemVer) {
    /**
     * Checks a candidate version against this bound.
     *
     * @param candidate version to check.
     * @return `true` when the candidate satisfies the bound.
     */
    fun matches(candidate: SemVer): Boolean = when (operator) {
        RangeOperator.EQUAL -> candidate == version
        RangeOperator.GREATER -> candidate > version
        RangeOperator.GREATER_OR_EQUAL -> candidate >= version
        RangeOperator.LESS -> candidate < version
        RangeOperator.LESS_OR_EQUAL -> candidate <= version
    }
}

/**
 * npm-style version range such as `^1.2.3`, `~1.2`, `>=1.0.0 <2.0.0`
 * or `1.2.3 - 2.0.0`.
 */
class VersionRange private constructor(
    private val alternatives: List<List<VersionComparator>>,
    private val allowsPrerelease: Boolean,
) {
    /**
     * Checks whether a version satisfies the range.
     *
     * Pre-release versions match only ranges that mention a pre-release,
     * which mirrors npm behaviour.
     *
     * @param candidate version to check.
     * @return `true` when at least one alternative accepts the version.
     */
    fun matches(candidate: SemVer): Boolean {
        if (candidate.isPrerelease && !allowsPrerelease) {
            return false
        }
        return alternatives.any { group ->
            group.all { comparator -> comparator.matches(candidate) }
        }
    }

    companion object {
        private const val ALTERNATIVE_SEPARATOR: String = "||"
        private const val HYPHEN_SEPARATOR: String = " - "
        private const val CARET: String = "^"
        private const val TILDE: String = "~"
        private const val OPERATOR_CHARS: String = "><="
        private const val MAX_HYPHEN_PARTS: Int = 2
        private val SPACES = Regex("\\s+")
        private val PRERELEASE_IN_RANGE = Regex("\\d+(\\.\\d+){0,2}-[0-9A-Za-z]")
        private val ANY_TOKENS = setOf("*", "x", "X", "latest")
        private val PREFIX_OPERATORS = setOf(">=", "<=", ">", "<", "=")
        private val UNSUPPORTED_MARKERS = listOf(":", "git+", "file:", "workspace:")
        private const val STABLE_MAJOR_VERSION: Int = 1
        private const val STABLE_MINOR_VERSION: Int = 1

        /**
         * Parses a version range.
         *
         * @param text range text from a dependency declaration.
         * @return parsed range.
         * @throws VersionRangeException when the range uses unsupported syntax.
         */
        fun parse(text: String): VersionRange {
            val trimmed = text.trim()
            if (trimmed.isEmpty() || trimmed in ANY_TOKENS) {
                return VersionRange(listOf(emptyList()), allowsPrerelease = false)
            }
            rejectUnsupported(trimmed)
            val alternatives = trimmed.split(ALTERNATIVE_SEPARATOR)
                .map { alternative -> parseAlternative(alternative.trim()) }
            return VersionRange(
                alternatives = alternatives,
                allowsPrerelease = PRERELEASE_IN_RANGE.containsMatchIn(trimmed),
            )
        }

        private fun rejectUnsupported(text: String) {
            if (UNSUPPORTED_MARKERS.any { marker -> text.contains(marker) }) {
                throw VersionRangeException(
                    "unsupported version range '$text'; aliases, URLs and local paths " +
                        "cannot be resolved",
                )
            }
        }

        private fun parseAlternative(text: String): List<VersionComparator> {
            if (text.isEmpty()) {
                throw VersionRangeException("empty alternative in a version range")
            }
            val hyphenParts = text.split(HYPHEN_SEPARATOR)
            if (hyphenParts.size > 1) {
                return parseHyphenRange(text, hyphenParts)
            }
            return parseTokens(text, text.split(SPACES).filter { token -> token.isNotEmpty() })
        }

        private fun parseHyphenRange(
            source: String,
            parts: List<String>,
        ): List<VersionComparator> {
            if (parts.size != MAX_HYPHEN_PARTS) {
                throw VersionRangeException("invalid hyphen range '$source'")
            }
            val lower = parsePartial(parts[0].trim(), source)
            val upper = parsePartial(parts[1].trim(), source)
            return listOf(
                VersionComparator(RangeOperator.GREATER_OR_EQUAL, lower.lowest()),
                upperComparator(upper, inclusive = true),
            )
        }

        private fun parseTokens(
            source: String,
            tokens: List<String>,
        ): List<VersionComparator> {
            val comparators = mutableListOf<VersionComparator>()
            var index = 0
            while (index < tokens.size) {
                val token = tokens[index]
                if (token in PREFIX_OPERATORS) {
                    val value = tokens.getOrNull(index + 1)
                        ?: throw VersionRangeException(
                            "operator '$token' has no version in range '$source'",
                        )
                    comparators += expandOperator(token, value, source)
                    index += 2
                } else {
                    comparators += expandToken(token, source)
                    index++
                }
            }
            return comparators
        }

        private fun expandToken(token: String, source: String): List<VersionComparator> = when {
            token.startsWith(CARET) -> expandCaret(parsePartial(token.drop(1), source))
            token.startsWith(TILDE) -> expandTilde(parsePartial(token.drop(1), source))
            token.first() in OPERATOR_CHARS -> expandOperator(
                token.takeWhile { char -> char in OPERATOR_CHARS },
                token.dropWhile { char -> char in OPERATOR_CHARS },
                source,
            )

            else -> expandPlain(parsePartial(token, source))
        }

        private fun expandOperator(
            operator: String,
            value: String,
            source: String,
        ): List<VersionComparator> {
            val partial = parsePartial(value, source)
            val lower = partial.lowest()
            return when (operator) {
                ">=" -> listOf(VersionComparator(RangeOperator.GREATER_OR_EQUAL, lower))
                "<" -> listOf(VersionComparator(RangeOperator.LESS, lower))
                ">" -> listOf(
                    if (partial.isComplete) {
                        VersionComparator(RangeOperator.GREATER, lower)
                    } else {
                        VersionComparator(RangeOperator.GREATER_OR_EQUAL, upperBound(partial))
                    },
                )

                "<=" -> listOf(
                    if (partial.isComplete) {
                        VersionComparator(RangeOperator.LESS_OR_EQUAL, lower)
                    } else {
                        VersionComparator(RangeOperator.LESS, upperBound(partial))
                    },
                )

                "=" -> expandPlain(partial)
                else -> throw VersionRangeException(
                    "unsupported operator '$operator' in range '$source'",
                )
            }
        }

        private fun expandPlain(partial: PartialVersion): List<VersionComparator> {
            if (partial.isComplete) {
                return listOf(VersionComparator(RangeOperator.EQUAL, partial.lowest()))
            }
            return listOf(
                VersionComparator(RangeOperator.GREATER_OR_EQUAL, partial.lowest()),
                VersionComparator(RangeOperator.LESS, upperBound(partial)),
            )
        }

        private fun expandCaret(partial: PartialVersion): List<VersionComparator> {
            val lower = partial.lowest()
            val major = partial.major ?: 0
            val upper = when {
                major >= STABLE_MAJOR_VERSION -> SemVer(major + 1, 0, 0)
                partial.minor == null -> SemVer(1, 0, 0)
                partial.minor >= STABLE_MINOR_VERSION -> SemVer(0, partial.minor + 1, 0)
                partial.patch == null -> SemVer(0, 1, 0)
                else -> SemVer(0, 0, partial.patch + 1)
            }
            return listOf(
                VersionComparator(RangeOperator.GREATER_OR_EQUAL, lower),
                VersionComparator(RangeOperator.LESS, upper),
            )
        }

        private fun expandTilde(partial: PartialVersion): List<VersionComparator> {
            val lower = partial.lowest()
            val upper = if (partial.minor == null) {
                SemVer(lower.major + 1, 0, 0)
            } else {
                SemVer(lower.major, lower.minor + 1, 0)
            }
            return listOf(
                VersionComparator(RangeOperator.GREATER_OR_EQUAL, lower),
                VersionComparator(RangeOperator.LESS, upper),
            )
        }

        private fun upperComparator(
            partial: PartialVersion,
            inclusive: Boolean,
        ): VersionComparator {
            val lower = partial.lowest()
            return if (partial.isComplete) {
                val operator = if (inclusive) RangeOperator.LESS_OR_EQUAL else RangeOperator.LESS
                VersionComparator(operator, lower)
            } else {
                VersionComparator(RangeOperator.LESS, upperBound(partial))
            }
        }

        private fun upperBound(partial: PartialVersion): SemVer {
            val lower = partial.lowest()
            return when {
                partial.minor == null -> SemVer(lower.major + 1, 0, 0)
                partial.patch == null -> SemVer(lower.major, lower.minor + 1, 0)
                else -> lower
            }
        }

        private fun parsePartial(text: String, source: String): PartialVersion =
            PartialVersionParser.parseOrNull(text)
                ?: throw VersionRangeException("unsupported version '$text' in range '$source'")
    }
}
