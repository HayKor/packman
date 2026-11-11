package ru.haykor.pacman.cli

/**
 * Outcome of command line parsing.
 */
sealed interface ArgumentsResult {
    /**
     * Arguments were parsed successfully.
     *
     * @property configPath path to the YAML configuration file.
     * @property printConfig whether only the effective configuration must be printed.
     */
    data class Parsed(
        val configPath: String,
        val printConfig: Boolean = false,
    ) : ArgumentsResult

    /** The user asked for the built-in help page. */
    data object HelpRequested : ArgumentsResult

    /**
     * Arguments cannot be used.
     *
     * @property message explanation shown to the user.
     */
    data class Invalid(val message: String) : ArgumentsResult
}

/**
 * Parser for the command line options of the application.
 */
object ArgumentParser {
    /** Configuration file used when `--config` is omitted. */
    const val DEFAULT_CONFIG_PATH: String = "config.yaml"

    private const val CONFIG_OPTION: String = "--config"
    private const val CONFIG_OPTION_PREFIX: String = "$CONFIG_OPTION="
    private const val HELP_OPTION: String = "--help"
    private const val PRINT_CONFIG_OPTION: String = "--print-config"
    private const val SHORT_HELP_OPTION: String = "-h"

    /**
     * Parses the raw process arguments.
     *
     * @param arguments arguments without the executable name.
     * @return parsed arguments, help request or a description of the problem.
     */
    fun parse(arguments: List<String>): ArgumentsResult {
        var configPath = DEFAULT_CONFIG_PATH
        var printConfig = false
        var index = 0
        while (index < arguments.size) {
            val argument = arguments[index]
            when {
                argument == HELP_OPTION || argument == SHORT_HELP_OPTION -> {
                    return ArgumentsResult.HelpRequested
                }

                argument == PRINT_CONFIG_OPTION -> {
                    printConfig = true
                }

                argument == CONFIG_OPTION -> {
                    val value = arguments.getOrNull(index + 1)
                        ?: return ArgumentsResult.Invalid(
                            "option '$CONFIG_OPTION' requires a value",
                        )
                    configPath = value
                    index++
                }

                argument.startsWith(CONFIG_OPTION_PREFIX) -> {
                    configPath = argument.removePrefix(CONFIG_OPTION_PREFIX)
                }

                else -> return ArgumentsResult.Invalid("unknown argument '$argument'")
            }
            index++
        }
        if (configPath.isBlank()) {
            return ArgumentsResult.Invalid("option '$CONFIG_OPTION' requires a non-empty value")
        }
        return ArgumentsResult.Parsed(configPath, printConfig)
    }
}
