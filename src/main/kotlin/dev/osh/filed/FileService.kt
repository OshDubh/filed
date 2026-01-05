package dev.osh.filed

import java.nio.file.Path
import kotlin.io.path.*

// tagged union; returns only T and is only one of Success or Error (which we construct here)
sealed class Result<out T> {
    data class Success<T>(val data: T): Result<T>() // T but output only
    data class Error(val error: String, val message: String, val code: Int) : Result<Nothing>() // Error has no data we want to return
}

// the structured response that we return upon success
sealed class Response {
    data class File(val type: String = "file", val path: String, val content: String? = null, val range: String? = null): Response()
    data class Directory(val type: String = "directory", val path: String, val files: List<File>): Response()
}

class FileService(
    private val allowedFiles: List<String>,
    private val allowedDirectories: List<String>,
    private val serverRoot: Path,
    private val maximumAllowedFileSize: Int
) {

    fun read(requestedPath: String, lines: IntRange? = null, bytes: IntRange? = null, includeContent: Boolean = true): Result<Response> {
        // prevent .. escape
        val normalised = Path.of(requestedPath).normalize().toString()
        val path = serverRoot.resolve(normalised)
        val allowed = isAllowed(normalised, path)
        if (allowed != null) return allowed


        // delegate returning the correct response type
        return when {
            !path.exists() -> Result.Error("not_found", "file not found", 404)
            path.isDirectory() -> getFiles(normalised, path, includeContent)
            path.isRegularFile() -> getFile(normalised, path, includeContent)
            else -> Result.Error("invalid_request", "requested path is neither a file nor a directory", 400)
        }
    }

    fun write(requestedPath: String, content: String): Result<Response> {
        val normalised = Path.of(requestedPath).normalize().toString()
        val path = serverRoot.resolve(normalised)
        val allowed = isAllowed(normalised, path)
        if (allowed != null) return allowed


        if (path.isDirectory()) return Result.Error("is_directory", "cannot write to a directory", 400)
        if (!path.parent.isDirectory()) return Result.Error("parent_not_found", "parent directory does not exist", 400)

        val file = path.writeText(content)
        return getFile(normalised, path, true)
    }

    fun delete(requestedPath: String): Result<Response> {
        val normalised = Path.of(requestedPath).normalize().toString()
        val path = serverRoot.resolve(normalised)

        val allowed = isAllowed(normalised, path)
        if (allowed != null) return allowed
        if (!path.exists()) return Result.Error("not_found", "file not found", 404)
        if (path.isDirectory()) return Result.Error("is_directory", "cannot delete directories", 400)

        val content = path.readText()

        return runCatching {path.deleteIfExists()}.fold(
            onSuccess = {Result.Success(Response.File(path = normalised, content = content))},
            onFailure = {Result.Error("delete_failed", "error deleting file: ${it.message}", 500)}
        )
    }

    private fun isAllowed(normalised: String, path: Path): Result<Nothing>? {
        if (normalised.startsWith("..")) {
            return Result.Error("invalid_path", "invalid path", 400)
        }

        // check if allowed by config
        val allowed = normalised in allowedFiles || allowedDirectories.any {dir -> normalised.startsWith("$dir/") || normalised == dir}
        if (!allowed) {
            return Result.Error("forbidden", "path not allowed by existing configuration rules", 403)
        }

        if (path.exists()) {
            if (!path.toRealPath().startsWith(serverRoot.toRealPath())) {
                return Result.Error("forbidden", "file's symlinked path not allowed by existing configuration rules", 403)
            }

            if (path.fileSize() > maximumAllowedFileSize) {
                return Result.Error("too_large", "file exceedes maximum size (${path.fileSize()} bytes)", 413)
            }
        }

        return null
    }
    
    private fun getFiles(normalised: String, path: Path, includeContent: Boolean): Result<Response> {
        // list all the files in the directory, filtering out any directories, and including their contentents if requested
        val files = path.listDirectoryEntries()
            .filter {it.isRegularFile()}
            .map {entry -> Response.File(path = entry.fileName.toString(), content = if (includeContent) entry.readText() else null)}

        return Result.Success(Response.Directory(path = normalised, files = files))
    }

    private fun getFile(normalised: String, path: Path, includeContent: Boolean): Result<Response> {
        // just return both the normalised filepath and the content of the file as text, if requested from the API
        return Result.Success(Response.File(path = normalised, content = if (includeContent) path.readText() else null))
    }
}
