/*
 * Tokens.kt
 * manages authorisation token storage and generation
 *
 * Created at 17:24 on Monday, the 05th of January, 2026 by osh
 */
package dev.osh.filed

data class StoredTokens(
    val hash: String,
    val name: String,
    val createdAt: String
)

class Tokens {
	
}
 
