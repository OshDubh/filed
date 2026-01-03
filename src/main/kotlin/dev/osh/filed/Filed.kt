package dev.osh.filed

import io.javalin.Javalin
import org.bukkit.plugin.java.JavaPlugin

class Filed : JavaPlugin() {

    private var app: Javalin? = null

    override fun onEnable() {
        // create/load our config
        saveDefaultConfig()
        val port = config.getInt("port", 9847)
        val allowedFiles = config.getStringList("allowed.files")
        val allowedDirectories = config.getStringList("allowed.directories")

        // for performing file operations
        val fileService = FileService(
            allowedFiles = config.getStringList("allowed.files"),
            allowedDirectories = config.getStringList("allowed.directories"),
            serverRoot = server.worldContainer.toPath()
        )

        // serve the API and respond to requests
        app = Javalin.create { config ->
            config.showJavalinBanner = false
        }.apply {
            get("/health") { req ->
                req.json(mapOf("status" to "ok"))
                req.status(200)
            }
            get("/file") { req ->
                // read the path specified and check if it's allowed
                val path = req.queryParam("path") ?: ""
                val allowed = fileService.isAllowed(path)
                if (allowed) {
                    req.status(200)
                } else {
                    req.status(403)
                }

            }
            get("/directory") { req ->
                val path = req.queryParam("path") ?: ""

                when(val result = fileService.listFiles(path)) {
                    is Result.Success<List<String>> -> req.json(result.data)
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
