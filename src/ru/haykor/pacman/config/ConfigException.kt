package ru.haykor.pacman.config

import ru.haykor.pacman.PacmanException

/**
 * Raised when the configuration file cannot be read, parsed or validated.
 *
 * @param message description of the problem addressed to the user.
 */
class ConfigException(message: String) : PacmanException(message)

/**
 * Builds an error for a single configuration parameter.
 *
 * @param name parameter name as written in the YAML file.
 * @param reason explanation of the violated rule.
 * @return exception ready to be thrown by a validator.
 */
internal fun parameterError(name: String, reason: String): ConfigException =
    ConfigException("parameter '$name': $reason")
