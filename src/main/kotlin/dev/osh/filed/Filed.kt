package dev.osh.filed

import io.javalin.Javalin
import org.bukkit.plugin.java.JavaPlugin

class Filed : JavaPlugin() {

    private var app: Javalin? = null

    override fun onEnable() {
        saveDefaultConfig()

        val port = config.getInt("port", 9847)
        val allowedFiles = config.getStringList("allowed.files")
        val allowedDirs = config.getStringList("allowed.directories")

        val Files = Files(
            allowedFiles = config.getStringList("allowed.files"),
            allowedDirs = config.getStringList("allowed.directories"),
            serverRoot = server.worldContainer.toPath()
        )

        app = Javalin.create { config ->
            config.showJavalinBanner = false
        }.apply {
            get("/health") { ctx ->
                ctx.json(mapOf("status" to "ok"))
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
