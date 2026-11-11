package ru.haykor.pacman.config

/**
 * Renders configuration parameters as key-value lines.
 */
object ConfigPrinter {
    private const val EMPTY_VALUE: String = "\"\""

    /**
     * Renders every user adjustable parameter.
     *
     * @param config validated configuration.
     * @return lines in a stable order.
     */
    fun render(config: PacmanConfig): List<String> = listOf(
        "${ConfigKeys.PACKAGE}: ${renderText(config.packageName)}",
        "${ConfigKeys.REPOSITORY}: ${config.repository}",
        "${ConfigKeys.REPOSITORY_MODE}: ${config.repositoryMode.configValue}",
        "${ConfigKeys.IMAGE_FILE}: ${config.imageFile}",
        "${ConfigKeys.ASCII_TREE}: ${config.asciiTree}",
        "${ConfigKeys.FILTER}: ${renderText(config.filter)}",
        "${ConfigKeys.LOAD_ORDER}: ${config.loadOrder}",
    )

    private fun renderText(value: String): String = if (value.isEmpty()) EMPTY_VALUE else value
}
