package ru.haykor.pacman.npm

import kotlinx.coroutines.runBlocking
import ru.haykor.pacman.source.DependencySource
import ru.haykor.pacman.source.PackageRequest
import ru.haykor.pacman.semver.VersionRangeException
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NpmDependencySourceTest {
    private val registry = FakeRegistry(
        mapOf(
            "sample" to PackageInfo(
                name = "sample",
                latestVersion = "2.0.0",
                manifests = mapOf(
                    "1.5.0" to PackageManifest("sample", "1.5.0", mapOf("old" to "^1.0.0")),
                    "2.0.0" to PackageManifest("sample", "2.0.0", mapOf("new" to "^2.0.0")),
                ),
            ),
        ),
    )

    @Test
    fun resolvesLatestDistributionTag() = runBlocking {
        val source = NpmDependencySource(registry)

        val resolved = source.resolve(request("sample", DependencySource.LATEST_TAG))

        assertEquals("2.0.0", resolved.version)
        assertEquals("sample@2.0.0", resolved.id)
        assertEquals(mapOf("new" to "^2.0.0"), resolved.dependencies)
    }

    @Test
    fun resolvesRangeAndReusesDocument() = runBlocking {
        val source = NpmDependencySource(registry)

        source.resolve(request("sample", DependencySource.LATEST_TAG))
        val resolved = source.resolve(request("sample", "^1.0.0"))

        assertEquals("1.5.0", resolved.version)
        assertEquals(mapOf("old" to "^1.0.0"), resolved.dependencies)
        assertEquals(1, registry.fetchCount)
    }

    @Test
    fun reportsRangeWithoutMatchingVersion() {
        runBlocking {
            val source = NpmDependencySource(registry)

            assertFailsWith<VersionRangeException> {
                source.resolve(request("sample", "^9.0.0"))
            }
        }
    }

    @Test
    fun reportsUnknownPackage() = runBlocking {
        val source = NpmDependencySource(registry)

        val error = assertFailsWith<RegistryException> {
            source.resolve(request("unknown", DependencySource.LATEST_TAG))
        }

        assertContains(error.message.orEmpty(), "package 'unknown' is not known")
    }

    private fun request(name: String, range: String): PackageRequest = PackageRequest(name, range)

    private class FakeRegistry(
        private val documents: Map<String, PackageInfo>,
    ) : NpmRegistryClient {
        var fetchCount: Int = 0
            private set

        override suspend fun fetchPackage(name: String): PackageInfo {
            fetchCount++
            return documents[name] ?: throw RegistryException("package '$name' is not known")
        }
    }
}
