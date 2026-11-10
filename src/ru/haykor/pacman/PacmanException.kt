package ru.haykor.pacman

/**
 * Base class for all expected failures that are reported to the user without a stack trace.
 *
 * @param message human readable description of the failure.
 */
open class PacmanException(message: String) : Exception(message)
