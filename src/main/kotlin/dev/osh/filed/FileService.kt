package dev.osh.filed

import java.nio.file.*

// tagged union; returns only T and is only one of Success or Error (which we construct here)
sealed class Result<out T> {
    data class Success<T>(val data: T): Result<T>() // T but output only
    data class Error(val message: String, val code: Int) : Result<Nothing>() // Error has no data we want to return
}

class FileService(
    private val allowedFiles: List<String>,
    private val allowedDirectories: List<String>,
    private val serverRoot: Path
) {
    
    fun isAllowed(path: String): Boolean {
        // prevent .. escape
        val normalised = Path.of(path).normalize().toString()
        if (normalised.startsWith("..")) {
            return false
        }

        // if it's a file, just check it directly
        if (normalised in allowedFiles) {
            return true
        }

        // check both if it's in a directory that's allowed or it is just the directory specified
        for (dir in allowedDirectories) {
            if (normalised.startsWith("$dir/") || normalised == dir) {
                return true
            }
        }

        return false
    }

    fun listFiles(directoryPath: String): Result<List<String>> {
        if (!isAllowed(directoryPath)) {
            return Result.Error("path not allowed by existing configuration", 403)
        }
        
        val path = serverRoot.resolve(directoryPath) // join the requested path with the local one

        if (!Files.exists(path)) {
            return Result.Error("directory not found", 404)
        }
        
        if (!Files.isDirectory(path)) {
            return Result.Error("not a directory", 400)
        }

        val files = Files.list(path).use { stream ->
            stream.map { it.fileName.toString() }.toList()
        }

        return Result.Success(files)
    }
}
