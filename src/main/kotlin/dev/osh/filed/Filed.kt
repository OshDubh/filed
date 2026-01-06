/*
 * Filed.kt
 * main
 * created by osh
 *
 * created at 18:37 on Sunday, the 27th of December, 2025
 * last modified at 19:59 on Tuesday, the 06th of January, 2026
 */

package dev.osh.filed

import io.javalin.Javalin
import org.bukkit.plugin.java.JavaPlugin

class Filed : JavaPlugin() {
    private var app: Javalin? = null
    private lateinit var tokenAuth: TokenAuth

    override fun onEnable() {
        // create/load our config
        saveDefaultConfig()
        val port = config.getInt("port", 9847)

        // for performing file operations
        val fileService =
            FileService(
                allowedFiles = config.getStringList("allowed.files"),
                allowedDirectories = config.getStringList("allowed.directories"),
                serverRoot = server.worldContainer.toPath(),
                maximumAllowedFileSize = config.getInt("maximum_allowed_file_size", 1_000_000),
            )

        val tokenFile = config.getString("auth_token_file", "tokens.json")
        tokenAuth = TokenAuth(dataFolder.toPath().resolve(tokenFile))

        // serve the API and respond to requests
        app = Javalin.create { config -> config.showJavalinBanner = false }
            .apply {
                // precheck auth
                before("/files") { req ->
                    val authHeader = req.header("Authorization")
                    val token = authHeader?.removePrefix("Bearer ")?.trim()

                    if (token == null || !tokenAuth.verify(token)) {
                        ctx.status(401)
                        ctx.json(mapOf("error" to "unauthorized", "message" to "${if (token == null) "missing" else "invalid"} Authorization token"))
                        ctx.skipRemainingHandlers()
                    }
                }

                get("/health") { req ->
                    req.json(mapOf("status" to "ok"))
                    req.status(200)
                }

                // retreiving files
                get("/files") { req ->
                    val path = req.queryParam("path") ?: ""
                    val includeContent = req.queryParam("content") == "true"

                    when (val result = fileService.read(path, includeContent = includeContent)) {
                        is Result.Success -> req.json(result.data)

                        is Result.Error -> {
                            req.status(result.code)
                            req.json(mapOf("error" to result.message))
                        }
                    }
                }

                // creating / modifying files
                put("/files") { req ->
                    val path = req.queryParam("path") ?: ""
                    when (val result = fileService.write(path, req.body())) {
                        is Result.Success -> req.json(result.data)

                        is Result.Error -> {
                            req.status(result.code)
                            req.json(mapOf("error" to result.message))
                        }
                    }
                }

                // you'll never guess what this method does
                delete("/files") { req ->
                    val path = req.queryParam("path") ?: ""

                    when (val result = fileService.delete(path)) {
                        is Result.Success -> req.json(result.data)

                        is Result.Error -> {
                            req.status(result.code)
                            req.json(mapOf("error" to result.message))
                        }
                    }
                }

                start(port)
            }

        logger.info("Filed available on port $port")
    }

    override fun onDisable() {
        app?.stop()
        logger.info("Filed stopped")
    }
}
