/*
 * TokenAuth.kt
 * manages authorisation token storage and generation
 * created by osh
 *
 * created at 11:46 on Friday, the 2nd of January, 2026
 * last modified at 23:45 on Monday, the 05th of January, 2026
*/

package dev.osh.filed

import java.nio.file.Path
import java.security.MessageDigest
import java.util.Base64
import kotlin.io.path.*
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.readValue

data class HashedToken(
    val hash: String,
    val name: String,
    val createdAt: Long
)

class TokenAuth(private val dataFile: Path) {
    private var hashedTokens: MutableMap<String, HashedToken> = mutableMapOf()

    // create our mapper to save and load our HashedToken data class
    private val mapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .build()

    init {
        load()
    }

    // takes a string and hashes it
    private fun hash(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    // creates/loads the tokens from the token JSON for use by the rest of this file
    fun load(): MutableMap<String, HashedToken> {
        if (!dataFile.exists()) {
            val json = mapper.writeValueAsString(hashedTokens)
            dataFile.writeText(json)
            return hashedTokens
        }

        val json = dataFile.readText()
        hashedTokens = mapper.readValue(json)
        return hashedTokens
    }

    // saves a token's hashed details to a file
    fun save(token: String, name: String): HashedToken {
        val hash = hash(token)
        val hashedToken = HashedToken(hash, name, java.time.Instant.now().toEpochMilli())
        hashedTokens.put(hash, hashedToken)

        val json = mapper.writeValueAsString(hashedTokens)
        dataFile.writeText(json)

        return hashedToken
    }

    // checks if a given token is stored in our hashedTokens map
    fun isValid(token: String): Boolean {
        val hashed = hash(token)
        return hashedTokens.containsKey(hashed)
    }
}
