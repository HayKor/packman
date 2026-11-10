package ru.haykor.pacman.config

/**
 * Names of the configuration parameters as they appear in the YAML file.
 */
object ConfigKeys {
    /** Name of the analysed package. */
    const val PACKAGE: String = "package"

    /** Registry URL or path to a test repository description. */
    const val REPOSITORY: String = "repository"

    /** Mode of interaction with the repository. */
    const val REPOSITORY_MODE: String = "repository_mode"

    /** Name of the generated graph image. */
    const val IMAGE_FILE: String = "image_file"

    /** Whether an ASCII tree must be printed. */
    const val ASCII_TREE: String = "ascii_tree"

    /** Substring used to filter packages out. */
    const val FILTER: String = "filter"

    /** Every key supported by the loader. */
    val ALL: Set<String> = setOf(
        PACKAGE,
        REPOSITORY,
        REPOSITORY_MODE,
        IMAGE_FILE,
        ASCII_TREE,
        FILTER,
    )
}
