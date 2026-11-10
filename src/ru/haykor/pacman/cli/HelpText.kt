package ru.haykor.pacman.cli

/**
 * Built-in help page of the command line interface.
 */
object HelpText {
    private const val USAGE_LINE: String =
        "Usage: pacman [options]"

    /**
     * Renders the help page.
     *
     * @return help text ready to be printed.
     */
    fun render(): String = listOf(
        "pacman - dependency graph visualizer for npm packages",
        "",
        USAGE_LINE,
        "",
        "Options:",
        "  --config <path>  path to the YAML configuration file (default: config.yaml)",
        "  -h, --help       show this help page and exit",
    ).joinToString(separator = "\n")
}
