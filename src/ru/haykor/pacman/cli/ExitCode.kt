package ru.haykor.pacman.cli

/**
 * Process exit codes used by the command line interface.
 *
 * @property value numeric value passed to the operating system.
 */
enum class ExitCode(val value: Int) {
    /** The command finished successfully. */
    SUCCESS(0),

    /** A configuration or runtime problem interrupted the command. */
    FAILURE(1),

    /** The command line arguments are not valid. */
    USAGE(2),
}
