package dev.osh.filed

import java.nio.file.*

sealed class FileResult {
    data class Success(val files: List<String>): FileResult()
    data class Error(val message: String, val code: Int) : FileResult()
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

    fun listFiles(directoryPath: String): FileResult {
        if (!isAllowed(directoryPath)) {
            return FileResult.Error("path not allowed by existing configuration", 403)
        }

        val path = serverRoot.resolve(directoryPath) // join the requested path with the local one

        if (!Files.exists(path)) {
            return FileResult.Error("directory not found", 404)
        }

        if (!Files.isDirectory(path)) {
            return FileResult.Error("not a directory", 400)
        }

        val files = Files.list(path).use { stream ->
            stream.map { it.fileName.toString() }.toList()
        }

        return FileResult.Success(files)
    }
}
