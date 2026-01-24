# Filed

Paper plugin that exposes server files over a REST API with token auth and path whitelisting.

## Installation

1. Download the latest JAR from [Releases](../../releases)
2. Place in your server's `plugins/` directory
3. Restart the server
4. Configure `plugins/Filed/config.yml`

## Configuration

```yaml
port: 9847
maximum_allowed_file_size: 1000000
auth_token_file: "tokens.json"

allowed:
  files:
    - "server.properties"
  directories:
    - "world/stats"
```

| Option | Description |
|--------|-------------|
| `port` | HTTP port for the API |
| `maximum_allowed_file_size` | Max file size in bytes (default 1MB) |
| `auth_token_file` | Where hashed tokens are stored |
| `allowed.files` | Exact file paths that can be accessed |
| `allowed.directories` | Directories (and their contents) that can be accessed |

Paths are relative to the server root (where `server.properties` lives).

## Authentication

Generate a token in-game or via console:

```
/filed generate <name>
```

Requires operator permissions. The token is shown once and cannot be retrieved later. Store it immediately.

Use the token in requests:

```
Authorization: Bearer <token>
```

## API

All file endpoints require authentication. Paths are passed as query parameters.

### Health Check

```
GET /health
```

No auth required. Returns `{"message": "ok"}` if the API service is running and can be connected to.

### Read File or Directory

```
GET /files?path=<path>&content=<true|false>
```

For files:
```json
{
  "type": "file",
  "path": "server.properties",
  "content": "..."
}
```

For directories: [does not include directories nor sub-directory files]
```json
{
  "type": "directory",
  "path": "world/stats",
  "files": [
    {"type": "file", "path": "player.json", "content": "..."}
  ]
}
```

Set `content=false` to omit file contents.

### Write File

```
PUT /files?path=<path>
Content-Type: text/plain

<file content>
```

Creates or overwrites the file. Parent directory must exist.

### Delete File

```
DELETE /files?path=<path>
```

Returns the deleted file's content in the response.

## Errors

```json
{
  "error": "forbidden",
  "message": "path not allowed by existing configuration rules"
}
```

| Error | Code | Meaning |
|-------|------|---------|
| `unauthorized` | 401 | Missing or invalid token |
| `forbidden` | 403 | Path not in whitelist or symlink escape |
| `not_found` | 404 | File doesn't exist |
| `invalid_path` | 400 | Path traversal attempt |
| `is_directory` | 400 | Can't write/delete directories |
| `parent_not_found` | 400 | Parent directory doesn't exist (for writes) |
| `too_large` | 413 | File exceeds size limit |

## Security Considerations

- Paths are normalized and checked against the whitelist before any operation
- `..` traversal is blocked
- Symlinks that resolve outside the server root are rejected
- Tokens are SHA-256 hashed before storage
- File size limits prevent memory exhaustion

## Building

```
./gradlew build
```

JAR is output to `build/libs/filed-<version>.jar`.
