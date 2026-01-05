/*
 * Tokens.kt
 * manages authorisation token storage and generation
 * created by osh
 *
 * created at 11:46 on Friday, the 2nd of January, 2026
 * last modified at 19:35 on Monday, the 05th of January, 2026
*/

package dev.osh.filed

import java.nio.file.Path
import kotlin.io.path.*oimport java.security.MessageDigest
import java.util.Base64


data class StoredToken(
    val hash: String,
    val name: String,
    val createdAt: Long
)

class Tokens(private val dataFolder: Path) {
    private var tokens: MutableList<StoredToken> = mutableListOf()

    private fun hash(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}
