package ru.haykor.pacman.config

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlElement
import net.mamoe.yamlkt.YamlLiteral

/**
 * Loads and validates the YAML configuration of the application.
 */
class ConfigLoader {
    /**
     * Reads the configuration from a file.
     *
     * @param path path to the YAML file.
     * @return validated configuration.
     */
    fun load(path: String): PacmanConfig = loadFromText(readText(path))

    /**
     * Validates a configuration already read into memory.
     *
     * @param text content of the YAML file.
     * @return validated configuration.
     */
    fun loadFromText(text: String): PacmanConfig {
        val parameters = ConfigParameters(parse(text))
        parameters.requireOnly(ConfigKeys.ALL)
        val mode = readMode(parameters)
        val packageName = PackageNameValidator.validate(
            parameters.string(ConfigKeys.PACKAGE),
            mode,
        )
        val repository = RepositoryValidator.validate(
            parameters.string(ConfigKeys.REPOSITORY),
            mode,
        )
        return PacmanConfig(
            packageName = packageName,
            repository = repository,
            repositoryMode = mode,
            imageFile = ImageFileValidator.validate(parameters.string(ConfigKeys.IMAGE_FILE)),
            asciiTree = parameters.boolean(ConfigKeys.ASCII_TREE),
            filter = FilterValidator.validate(parameters.string(ConfigKeys.FILTER)),
        )
    }

    private fun readMode(parameters: ConfigParameters): RepositoryMode {
        val text = parameters.string(ConfigKeys.REPOSITORY_MODE)
        return RepositoryMode.fromConfigValue(text)
            ?: throw ConfigException(
                "parameter '${ConfigKeys.REPOSITORY_MODE}': unknown value '$text', " +
                    "expected one of ${RepositoryMode.supportedValues()}",
            )
    }

    private fun parse(text: String): Map<String, YamlElement> {
        if (text.isBlank()) {
            throw ConfigException("configuration file is empty")
        }
        val document = try {
            Yaml.decodeYamlMapFromString(text)
        } catch (error: Exception) {
            throw ConfigException(
                "configuration file is not valid YAML: ${error.message?.trimEnd()}; " +
                    "note that values containing '://' must be quoted",
            )
        }
        val parameters = LinkedHashMap<String, YamlElement>()
        for ((key, value) in document) {
            val name = key as? YamlLiteral
                ?: throw ConfigException("configuration parameter names must be plain strings")
            parameters[name.content] = value
        }
        return parameters
    }

    private fun readText(path: String): String {
        val filePath = Path(path)
        val metadata = SystemFileSystem.metadataOrNull(filePath)
            ?: throw ConfigException("configuration file '$path' does not exist")
        if (!metadata.isRegularFile) {
            throw ConfigException("configuration file '$path' is not a regular file")
        }
        return try {
            SystemFileSystem.source(filePath).buffered().use { source -> source.readString() }
        } catch (error: Exception) {
            throw ConfigException("cannot read configuration file '$path': ${error.message}")
        }
    }
}
