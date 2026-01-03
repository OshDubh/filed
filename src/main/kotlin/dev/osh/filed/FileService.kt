package dev.osh.filed

import java.nio.file.*

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

    fun listFiles(directoryPath: String): List<String> {
        val directory = Path.get(directoryPath)
        val stream = Files.newDirectoryStream(directory)
        for (file : stream) {
            // append the list?
        }
    }
}
