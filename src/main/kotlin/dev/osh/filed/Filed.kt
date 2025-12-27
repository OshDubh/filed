package dev.osh.filed

import com.sun.net.httpserver.HttpServer
import org.bukkit.plugin.java.JavaPlugin
import java.net.InetSocketAddress

class Filed : JavaPlugin() {

    private lateinit var server: HttpServer

    override fun onEnable() {
        saveDefaultConfig()
        
        val port = config.getInt("port", 9847)
        
        server = HttpServer.create(InetSocketAddress(port), 0)
        
        server.createContext("/health") { exchange ->
            val response = """{"status": "ok"}"""
            
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, response.length.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }
        
        server.start()
        logger.info("Filed API running on port $port")
    }

    override fun onDisable() {
        server.stop(0)
        logger.info("Filed API stopped")
    }
}
