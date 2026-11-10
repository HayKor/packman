package ru.haykor.pacman.config

import net.mamoe.yamlkt.YamlElement
import net.mamoe.yamlkt.YamlLiteral

/**
 * Typed access to the parameters of a parsed YAML document.
 *
 * @property values raw scalar or compound values indexed by parameter name.
 */
class ConfigParameters(private val values: Map<String, YamlElement>) {
    /**
     * Verifies that the document contains no keys unknown to the application.
     *
     * @param allowed names supported by the current version of the application.
     */
    fun requireOnly(allowed: Set<String>) {
        val unknown = values.keys.filterNot { key -> key in allowed }.sorted()
        if (unknown.isEmpty()) {
            return
        }
        val unknownText = unknown.joinToString { key -> "'$key'" }
        val allowedText = allowed.sorted().joinToString { key -> "'$key'" }
        throw ConfigException(
            "unknown parameter(s) $unknownText; supported parameters: $allowedText",
        )
    }

    /**
     * Reads a mandatory scalar parameter as text.
     *
     * @param name parameter name.
     * @return raw text value.
     */
    fun string(name: String): String {
        if (!values.containsKey(name)) {
            throw ConfigException("parameter '$name' is required")
        }
        val element = values[name]
        if (element !is YamlLiteral) {
            throw ConfigException("parameter '$name' must be a scalar value")
        }
        return element.content
    }

    /**
     * Reads a mandatory boolean parameter.
     *
     * @param name parameter name.
     * @return parsed boolean value.
     */
    fun boolean(name: String): Boolean {
        val text = string(name).trim()
        return when (text.lowercase()) {
            "true" -> true
            "false" -> false
            else -> throw ConfigException(
                "parameter '$name' must be a boolean ('true' or 'false'), got '$text'",
            )
        }
    }
}
