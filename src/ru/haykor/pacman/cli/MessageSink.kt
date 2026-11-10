package ru.haykor.pacman.cli

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fputs
import platform.posix.stderr

/**
 * Destination for one line of textual output.
 */
interface MessageSink {
    /**
     * Writes a single line to the destination.
     *
     * @param message text without the line separator.
     */
    fun line(message: String)
}

/**
 * Sink that writes to the standard output stream.
 */
object StandardOutput : MessageSink {
    override fun line(message: String) {
        println(message)
    }
}

/**
 * Sink that writes to the standard error stream.
 *
 * Kotlin/Native does not provide `System.err`, so the C standard library is used directly.
 */
object DiagnosticOutput : MessageSink {
    @OptIn(ExperimentalForeignApi::class)
    override fun line(message: String) {
        fputs(message, stderr)
        fputs("\n", stderr)
    }
}
