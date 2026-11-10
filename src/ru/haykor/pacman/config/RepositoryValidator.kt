package ru.haykor.pacman.config

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * Validates the repository parameter for the selected repository mode.
 */
object RepositoryValidator {
    private val REGISTRY_URL_PATTERN = Regex("^https?://[^\\s/?#]+(/\\S*)?$")

    /**
     * Checks the repository value and returns it unchanged.
     *
     * @param value raw value from the configuration file.
     * @param mode repository mode that defines the accepted syntax.
     * @return the validated repository value.
     */
    fun validate(value: String, mode: RepositoryMode): String = when (mode) {
        RepositoryMode.REGISTRY -> validateRegistryUrl(value)
        RepositoryMode.TEST -> validateTestRepositoryFile(value)
    }

    private fun validateRegistryUrl(value: String): String {
        if (value.isEmpty()) {
            throw parameterError(ConfigKeys.REPOSITORY, "value must not be empty")
        }
        if (!REGISTRY_URL_PATTERN.matches(value)) {
            throw parameterError(
                ConfigKeys.REPOSITORY,
                "'$value' is not a valid HTTP(S) URL, for example https://registry.npmjs.org",
            )
        }
        return value
    }

    private fun validateTestRepositoryFile(value: String): String {
        if (value.isEmpty()) {
            throw parameterError(
                ConfigKeys.REPOSITORY,
                "a path to a test repository file is required",
            )
        }
        val metadata = SystemFileSystem.metadataOrNull(Path(value))
            ?: throw parameterError(ConfigKeys.REPOSITORY, "file '$value' does not exist")
        if (!metadata.isRegularFile) {
            throw parameterError(ConfigKeys.REPOSITORY, "'$value' is not a regular file")
        }
        return value
    }
}
