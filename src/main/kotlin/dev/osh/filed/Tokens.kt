/*
 * Tokens.kt
 * manages authorisation token storage and generation
 * created by osh
 *
 * created at 11:46 on Friday, the 2nd of January, 2026
 * last modified at 17:30 on Monday, the 05th of January, 2026
*/

package dev.osh.filed

data class StoredTokens(
    val hash: String,
    val name: String,
    val createdAt: String
)

class Tokens {
	
}
 
