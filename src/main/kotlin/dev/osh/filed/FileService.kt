package dev.osh.filed

import java.nio.file.Path
import kotlin.io.path.*

// tagged union; returns only T and is only one of Success or Error (which we construct here)
sealed class Result<out T> {
    data class Success<T>(val data: T): Result<T>() // T but output only
    data class Error(val message: String, val code: Int) : Result<Nothing>() // Error has no data we want to return
}

// the structured response that we return upon success
sealed class Response {
    data class File(val path: String, val content: String? = null, val range: String? = null): Response()
    data class Directory(val path: String, val files: List<File>): Response()
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

    fun listFiles(dirpath: String, includeContent: Boolean): Result<Response> {
        if (!isAllowed(dirpath)) {
            return Result.Error("path not allowed by existing configuration", 403)
        }
        
        val path = serverRoot.resolve(dirpath) // join the requested path with the local one

        if (!path.exists()) {
            return Result.Error("directory not found", 404)
        }
        
        if (!path.isDirectory()) {
            return Result.Error("not a directory", 400)
        }

        val files = path.listDirectoryEntries()
            .filter {it.isRegularFile()}
            .map {entry -> Response.File(path = entry.fileName.toString(), content = if (includeContent) entry.readText() else null)}

        return Result.Success(Response.Directory(path = Path.of(dirpath).normalize().toString(), files = files))
    }

    fun getFile(filepath: String, includeContent: Boolean): Result<Response> {
        if (!isAllowed(filepath)) {
            return Result.Error("file access not permitted by existing configuration", 403)
        }

        val path = serverRoot.resolve(filepath)

        if (!path.exists()) {
            return Result.Error("file not found", 404)
        }

        if (path.isDirectory()) {
            return Result.Error("path is a directory", 400)
        }

        return Result.Success(Response.File(path = Path.of(filepath).normalize().toString(), content = if (includeContent) path.readText() else null))
    }
}
