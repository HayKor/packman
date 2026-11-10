package ru.haykor.pacman.npm

import io.ktor.client.HttpClient
import io.ktor.client.engine.curl.Curl
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

/**
 * npm registry client backed by libcurl through the Ktor Curl engine.
 *
 * @param baseUrl registry base URL, for example `https://registry.npmjs.org`.
 */
class KtorNpmRegistryClient(private val baseUrl: String) : NpmRegistryClient {
    private val client = HttpClient(Curl) {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
            connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
            socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
        }
    }

    override suspend fun fetchPackage(name: String): PackageInfo {
        val response = try {
            client.get(buildPackageUrl(name)) {
                header(HttpHeaders.UserAgent, USER_AGENT)
            }
        } catch (error: Exception) {
            throw RegistryException(
                "cannot reach npm registry at '$baseUrl': ${error.message?.trimEnd()}",
            )
        }
        return when (response.status) {
            HttpStatusCode.OK -> NpmPackumentParser.parse(response.bodyAsText())
            HttpStatusCode.NotFound -> throw RegistryException(
                "package '$name' was not found in registry '$baseUrl'",
            )

            else -> throw RegistryException(
                "registry '$baseUrl' responded with HTTP ${response.status.value} for '$name'",
            )
        }
    }

    /**
     * Releases network resources held by the client.
     */
    fun close() {
        client.close()
    }

    private fun buildPackageUrl(name: String): String {
        val encodedName = name.replace(SCOPE_SEPARATOR, ENCODED_SCOPE_SEPARATOR)
        return "${baseUrl.trimEnd('/')}/$encodedName"
    }

    private companion object {
        const val USER_AGENT: String = "ru.haykor.pacman/0.1"
        const val REQUEST_TIMEOUT_MILLIS: Long = 30_000
        const val CONNECT_TIMEOUT_MILLIS: Long = 10_000
        const val SOCKET_TIMEOUT_MILLIS: Long = 30_000
        const val SCOPE_SEPARATOR: String = "/"
        const val ENCODED_SCOPE_SEPARATOR: String = "%2F"
    }
}
