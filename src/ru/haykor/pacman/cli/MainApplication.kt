package ru.haykor.pacman.cli

import kotlinx.coroutines.runBlocking
import ru.haykor.pacman.PacmanException
import ru.haykor.pacman.config.ConfigLoader

/**
 * Application use case independent from the process environment.
 *
 * @property output sink for regular output.
 * @property errorOutput sink for diagnostics.
 */
class MainApplication(
    private val output: MessageSink = StandardOutput,
    private val errorOutput: MessageSink = DiagnosticOutput,
) {
    /**
     * Executes the command.
     *
     * @param arguments process arguments without the executable name.
     * @return exit code describing the outcome.
     */
    fun run(arguments: List<String>): ExitCode =
        when (val parsed = ArgumentParser.parse(arguments)) {
            ArgumentsResult.HelpRequested -> showHelp()
            is ArgumentsResult.Invalid -> failUsage(parsed.message)
            is ArgumentsResult.Parsed -> execute(parsed.configPath)
        }

    private fun showHelp(): ExitCode {
        HelpText.render().lines().forEach(output::line)
        return ExitCode.SUCCESS
    }

    private fun execute(configPath: String): ExitCode = try {
        val config = ConfigLoader().load(configPath)
        runBlocking { DirectDependenciesCommand(output).execute(config) }
        ExitCode.SUCCESS
    } catch (error: PacmanException) {
        fail(error.message ?: UNKNOWN_FAILURE)
    }

    private fun fail(message: String): ExitCode {
        errorOutput.line("error: $message")
        return ExitCode.FAILURE
    }

    private fun failUsage(message: String): ExitCode {
        errorOutput.line("error: $message")
        errorOutput.line(USAGE_HINT)
        return ExitCode.USAGE
    }

    private companion object {
        const val UNKNOWN_FAILURE: String = "unknown failure"
        const val USAGE_HINT: String = "use '--help' to see available options"
    }
}
