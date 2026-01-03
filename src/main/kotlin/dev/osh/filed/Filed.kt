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
        val files = Files(
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
            }
            get("/files") { req ->
                // read the path specified and check if it's allowed
                val path = req.queryParam("path") ?: ""
                req.result("Allowed: ${files.isAllowed(path)}")
            }
            start(port)
        }

        logger.info("Filed API running on port $port")
    }

    override fun onDisable() {
        app?.stop()
        logger.info("Filed stopped")
    }
}
