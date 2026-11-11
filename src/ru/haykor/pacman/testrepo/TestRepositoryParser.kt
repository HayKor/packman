package ru.haykor.pacman.testrepo

/**
 * Parser for the text description of a test repository.
 *
 * Every non-empty line has the form `NAME: DEPENDENCY DEPENDENCY`.
 * Dependencies are separated by whitespace or commas, `#` starts a comment,
 * package names consist of uppercase Latin letters, digits, `_` and `-`.
 */
object TestRepositoryParser {
    private const val COMMENT_PREFIX: String = "#"
    private const val DECLARATION_SEPARATOR: String = ":"
    private val NAME_PATTERN = Regex("^[A-Z][A-Z0-9_-]*$")
    private val DEPENDENCY_SEPARATORS = Regex("[\\s,]+")

    /**
     * Parses a test repository description.
     *
     * @param text content of the description file.
     * @return parsed repository.
     * @throws TestRepositoryException when a line violates the format.
     */
    fun parse(text: String): TestRepository {
        val declarations = LinkedHashMap<String, List<String>>()
        text.lines().forEachIndexed { index, line ->
            parseLine(index + 1, line, declarations)
        }
        return TestRepository(declarations)
    }

    private fun parseLine(
        lineNumber: Int,
        rawLine: String,
        declarations: MutableMap<String, List<String>>,
    ) {
        val line = rawLine.substringBefore(COMMENT_PREFIX).trim()
        if (line.isEmpty()) {
            return
        }
        val separatorIndex = line.indexOf(DECLARATION_SEPARATOR)
        if (separatorIndex == NOT_FOUND) {
            throw TestRepositoryException(lineNumber, "expected '<NAME>: <DEPENDENCIES>'")
        }
        val name = line.substring(0, separatorIndex).trim()
        validateName(lineNumber, name, ROLE_PACKAGE)
        if (declarations.containsKey(name)) {
            throw TestRepositoryException(lineNumber, "package '$name' is declared twice")
        }
        val dependencies = line.substring(separatorIndex + 1)
        declarations[name] = parseDependencies(lineNumber, dependencies)
    }

    private fun parseDependencies(lineNumber: Int, text: String): List<String> {
        val names = text.split(DEPENDENCY_SEPARATORS).filter { name -> name.isNotEmpty() }
        names.forEach { name -> validateName(lineNumber, name, ROLE_DEPENDENCY) }
        return names.distinct()
    }

    private fun validateName(lineNumber: Int, name: String, role: String) {
        if (!NAME_PATTERN.matches(name)) {
            throw TestRepositoryException(
                lineNumber,
                "invalid $role '$name'; use uppercase Latin letters, digits, '_' or '-' " +
                    "and start with a letter",
            )
        }
    }

    private const val NOT_FOUND: Int = -1
    private const val ROLE_PACKAGE: String = "package name"
    private const val ROLE_DEPENDENCY: String = "dependency name"
}
