package ru.haykor.pacman.config

/**
 * Validates the name of the analysed package for the selected repository mode.
 */
object PackageNameValidator {
    private const val MAX_LENGTH: Int = 214
    private val REGISTRY_PATTERN = Regex("^(@[a-z0-9._~-]+/)?[a-z0-9._~-]+$")
    private val TEST_PATTERN = Regex("^[A-Z][A-Z0-9_-]*$")

    /**
     * Checks the package name and returns it unchanged.
     *
     * @param value raw value from the configuration file.
     * @param mode repository mode that defines the accepted syntax.
     * @return the validated package name.
     */
    fun validate(value: String, mode: RepositoryMode): String = when (mode) {
        RepositoryMode.REGISTRY -> validateRegistryName(value)
        RepositoryMode.TEST -> validateTestName(value)
    }

    private fun validateRegistryName(value: String): String {
        if (value.isEmpty()) {
            throw parameterError(ConfigKeys.PACKAGE, "value must not be empty")
        }
        if (value.length > MAX_LENGTH) {
            throw parameterError(
                ConfigKeys.PACKAGE,
                "value must not be longer than $MAX_LENGTH characters",
            )
        }
        if (!REGISTRY_PATTERN.matches(value)) {
            throw parameterError(
                ConfigKeys.PACKAGE,
                "'$value' is not a valid npm package name; use lowercase letters, digits, " +
                    "'.', '_', '~', '-' and an optional @scope/ prefix",
            )
        }
        return value
    }

    private fun validateTestName(value: String): String {
        if (!TEST_PATTERN.matches(value)) {
            throw parameterError(
                ConfigKeys.PACKAGE,
                "'$value' is not a test repository package name; use uppercase Latin letters, " +
                    "digits, '_' or '-' and start with a letter",
            )
        }
        return value
    }
}
