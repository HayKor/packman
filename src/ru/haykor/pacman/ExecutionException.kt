package ru.haykor.pacman

/**
 * Raised when a command cannot be executed because of external data or state.
 *
 * @param message description of the problem addressed to the user.
 */
class ExecutionException(message: String) : PacmanException(message)
