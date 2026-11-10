package ru.haykor.pacman.config

/**
 * Source of dependency data selected by the user.
 *
 * @property configValue value used in the YAML configuration file.
 */
enum class RepositoryMode(val configValue: String) {
    /** Dependencies are requested from a live npm registry. */
    REGISTRY("registry"),

    /** Dependencies are read from a local description of a test repository. */
    TEST("test"),
    ;

    companion object {
        /**
         * Converts a configuration value to a mode.
         *
         * @param value raw value from the configuration file.
         * @return matching mode or `null` when the value is unknown.
         */
        fun fromConfigValue(value: String): RepositoryMode? =
            entries.firstOrNull { mode -> mode.configValue == value }

        /**
         * Renders all accepted values for diagnostics.
         *
         * @return values separated by a vertical bar.
         */
        fun supportedValues(): String =
            entries.joinToString(separator = "|") { mode -> mode.configValue }
    }
}

/**
 * User adjustable parameters of the application.
 *
 * @property packageName name of the package to analyse.
 * @property repository npm registry URL or path to a test repository description.
 * @property repositoryMode source of dependency data.
 * @property imageFile name of the generated graph image.
 * @property asciiTree whether dependencies must be printed as an ASCII tree.
 * @property filter substring used to exclude packages from the analysis.
 */
data class PacmanConfig(
    val packageName: String,
    val repository: String,
    val repositoryMode: RepositoryMode,
    val imageFile: String,
    val asciiTree: Boolean,
    val filter: String,
)
