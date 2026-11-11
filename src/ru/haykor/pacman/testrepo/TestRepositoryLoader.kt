package ru.haykor.pacman.testrepo

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import ru.haykor.pacman.ExecutionException

/**
 * Loads a test repository description from the file system.
 */
class TestRepositoryLoader {
    /**
     * Reads and parses a test repository description.
     *
     * @param path path to the description file.
     * @return parsed repository.
     * @throws ExecutionException when the file cannot be read.
     */
    fun load(path: String): TestRepository {
        val text = try {
            SystemFileSystem.source(Path(path)).buffered().use { source -> source.readString() }
        } catch (error: Exception) {
            throw ExecutionException("cannot read test repository '$path': ${error.message}")
        }
        return TestRepositoryParser.parse(text)
    }
}
