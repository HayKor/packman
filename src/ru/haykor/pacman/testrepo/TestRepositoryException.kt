package ru.haykor.pacman.testrepo

import ru.haykor.pacman.PacmanException

/**
 * Raised when a test repository description is invalid.
 *
 * @param lineNumber number of the offending line, `1`-based.
 * @param message description of the problem.
 */
class TestRepositoryException(lineNumber: Int, message: String) :
    PacmanException("test repository line $lineNumber: $message")
