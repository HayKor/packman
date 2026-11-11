package ru.haykor.pacman.semver

import ru.haykor.pacman.PacmanException

/**
 * Raised when a version range cannot be understood or satisfied.
 *
 * @param message description of the problem addressed to the user.
 */
class VersionRangeException(message: String) : PacmanException(message)
