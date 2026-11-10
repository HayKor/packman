package ru.haykor.pacman

import kotlin.system.exitProcess
import ru.haykor.pacman.cli.MainApplication

/**
 * Entry point of the application.
 *
 * @param args command line arguments passed to the process.
 */
fun main(args: Array<String>) {
    exitProcess(MainApplication().run(args.toList()).value)
}
