package ru.haykor.pacman.config

/**
 * Validates the name of the generated graph image.
 */
object ImageFileValidator {
    private const val MAX_LENGTH: Int = 255
    private const val PNG_EXTENSION: String = ".png"

    /**
     * Checks the image file name and returns it unchanged.
     *
     * @param value raw value from the configuration file.
     * @return the validated file name.
     */
    fun validate(value: String): String {
        if (value.isEmpty()) {
            throw parameterError(ConfigKeys.IMAGE_FILE, "value must not be empty")
        }
        if (value.length > MAX_LENGTH) {
            throw parameterError(
                ConfigKeys.IMAGE_FILE,
                "value must not be longer than $MAX_LENGTH characters",
            )
        }
        if (!value.endsWith(PNG_EXTENSION, ignoreCase = true)) {
            throw parameterError(
                ConfigKeys.IMAGE_FILE,
                "'$value' must have the '$PNG_EXTENSION' extension",
            )
        }
        if (value.contains('/') || value.contains('\\')) {
            throw parameterError(
                ConfigKeys.IMAGE_FILE,
                "'$value' must be a file name without directories",
            )
        }
        if (value == "." || value == "..") {
            throw parameterError(ConfigKeys.IMAGE_FILE, "'$value' is not a file name")
        }
        return value
    }
}
