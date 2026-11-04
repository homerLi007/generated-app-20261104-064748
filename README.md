# Safe File Deleter

A small full-stack demo application that lets you delete files inside a sandboxed root directory through a web UI. The backend enforces strict path validation (no path traversal, no symlinks, no non-regular files) before performing any deletion.

## Stack

Spring Boot 3.2 / Java 17 / React 18 / Vite 5

## Layout

```
.
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/demo/
│           │   ├── DemoApplication.java
│           │   └── controller/ApiController.java
│           └── resources/application.properties
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.jsx
        ├── App.css
        └── main.jsx
```

## How to run

### Backend

```bash
cd backend
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. Files are deleted relative to `app.allowed-root` (default `./safe-root`), which is resolved relative to the backend's working directory.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server runs on `http://localhost:5173` and proxies `/api` requests to the backend on port `8080`.

## API

### `POST /api/delete`

Deletes one or more files inside the allowed root.

**Request body**

```json
{
  "paths": ["notes.txt", "subdir/report.pdf"]
}
```

Paths are interpreted relative to `app.allowed-root`.

**Response**

```json
{
  "deleted": ["notes.txt"],
  "refused": [
    { "path": "subdir/report.pdf", "reason": "does not exist" }
  ]
}
```

A file is refused (and not deleted) if any of the following apply:

- The resolved path escapes the allowed root (`outside allowed root`).
- The path is a symbolic link (`symlink not allowed`).
- The path does not exist (`does not exist`).
- The path is not a regular file, e.g. a directory (`not a regular file`).
- An I/O or path-parsing error occurs.

## Notes

- **Sandbox only.** Deletion is restricted to `app.allowed-root`. Change it via `app.allowed-root` in `backend/src/main/resources/application.properties` or with `-Dapp.allowed-root=...`.
- **No authentication.** The API is unauthenticated and intended for local/demo use only. Do not expose it on a public network.
- **No CSRF protection / CORS config.** The frontend relies on Vite's dev proxy; for production you would need to serve the built frontend from the backend or configure CORS explicitly.
- **No persistence or audit log.** Deletions are immediate and permanent; there is no undo, trash, or record of what was removed.
- **Single-file granularity.** Directories are refused rather than recursively deleted.
- **Path handling is case-sensitive** on Linux/macOS and inherits the host filesystem's semantics on Windows.
- **Frontend build** (`npm run build`) produces static assets in `frontend/dist`; wiring them into the Spring Boot jar is left as an exercise.