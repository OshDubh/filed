# Filed
Filed is a PaperMC plugin that exposes server files over a REST API with token auth and path whitelisting. 

Read, create, delete and modify explicitly allowed server files via an API endpoint hosted by your server. Access is gated, and requires an API key that you generate to be included with all requests.

## Installation

2. Place the JAR in your server's `plugins/` directory`
3. Restart the server.
4. Configure `plugins/Filed/config.yml`.

## Configuration
Example config:
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

Paths are relative to the server root (the same directory where `server.properties` lives).

## Authentication

Generate a token in-game or via console:

```
/filed generate <name>
```

Requires op permissions. The token is shown once and cannot be retrieved later.

Use the token in requests by including `Bearer <token>` as the `Authorization` header. 

## API

### Read

```
GET /files?path=<path>&content=<true|false>
```

Example response for files:
```json
{
  "type": "file",
  "path": "server.properties",
  "content": "..."
}
```

Example response for directories: [does not include directories nor sub-directory files]
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

### Write

```
PUT /files?path=<path>
Content-Type: text/plain

<file content>
```

Creates or overwrites the file. Parent directory must exist. Returns the file contents of the modified file. Only files can be written to.

### Delete

```
DELETE /files?path=<path>
```

Returns the deleted file's content in the response. Only files can be deleted, not directories.

### Health Check

```
GET /health
```
This endpoint can be checked without authorisation, and can be used just to make sure that the API endpoint is up and running. Returns `{"message": "ok"}` if it's all good.


## Errors
Example unsuccessful response:
```json
{
  "error": "forbidden",
  "message": "path not allowed by existing configuration rules"
}
```

The full list of all error codes and their meaning:
| Error | Code | Meaning |
|-------|------|---------|
| `unauthorized` | 401 | Missing or invalid token |
| `forbidden` | 403 | Path not in whitelist or symlink escape |
| `not_found` | 404 | File doesn't exist |
| `invalid_path` | 400 | Provided path isn't valid |
| `is_directory` | 400 | Can't write/delete directories |
| `parent_not_found` | 400 | Parent directory doesn't exist (for writes) |
| `too_large` | 413 | File exceeds size limit |

## Security
- Paths are normalized and checked against the whitelist before any operation, escaping any `..` traversal.
- Symlinks that resolve outside the server root are rejected.
- Tokens are SHA-256 hashed before storage, and used just to verify access.
- Configurable file size limits prevent memory exhaustion.
- Sub-directory traversal is not permitted. If a directory is requested, only the files included in that directory are returned.
