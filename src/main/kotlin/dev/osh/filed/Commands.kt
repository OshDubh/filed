/*
 * Commands.kt
 * minecraft slash command handling
 * created by osh
 * created at 23:00 on Tuesday, the 06th of January, 2026
 * last modified at 23:22 on Tuesday, the 06th of January, 2026
 */

package dev.osh.filed

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.security.SecureRandom
import java.util.Base64

class Commands(private val tokenAuth: TokenAuth) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("§eusage: /$label generate <name>")
            return true
        }

        when (args[0].lowercase()) {
            "generate" -> generate(sender, args)
            else -> sender.sendMessage("§cunknown subcommand, usage: /$label generate <name>")
        }

        return true
    }

    private fun generate(sender: CommandSender, args: Array<out String>) {
        if (!sender.isOp) {
            sender.sendMessage("§cyou don't have permission to do this.")
            return
        }

        val name = args.getOrNull(1) ?: "default" // get the name provided, else default

        // generate token
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

        tokenAuth.save(token, name)

        sender.sendMessage("§agenerated token '§f$name§a':")
        sender.sendMessage("§e$token")
        sender.sendMessage("§7this is the only time you will see this token, make sure to store it safely now")
    }
}
