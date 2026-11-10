package ru.haykor.pacman.config

/**
 * Validates the substring used to exclude packages from the analysis.
 */
object FilterValidator {
    private const val MAX_LENGTH: Int = 64

    /**
     * Checks the filter value and returns it unchanged.
     *
     * An empty value disables filtering.
     *
     * @param value raw value from the configuration file.
     * @return the validated filter value.
     */
    fun validate(value: String): String {
        if (value.length > MAX_LENGTH) {
            throw parameterError(
                ConfigKeys.FILTER,
                "value must not be longer than $MAX_LENGTH characters",
            )
        }
        if (value.contains('\n') || value.contains('\r')) {
            throw parameterError(ConfigKeys.FILTER, "value must be a single line substring")
        }
        return value
    }
}
