package ru.haykor.pacman.graph

/**
 * Filter that excludes packages whose name contains the configured substring.
 *
 * @property value configured substring; an empty value disables filtering.
 */
class PackageNameFilter(val value: String) {
    /** Whether filtering is enabled. */
    val isEnabled: Boolean
        get() = value.isNotEmpty()

    /**
     * Checks a package name.
     *
     * @param name package name to check.
     * @return `true` when the name contains the substring, ignoring case.
     */
    fun matches(name: String): Boolean = isEnabled && name.contains(value, ignoreCase = true)
}
