# permission-service

Parte de **Snippet Searcher** (Ingeniería de Sistemas 2026). Guarda quién puede hacer qué sobre cada snippet: `OWNER` o `READ` (US7).

## De qué es dueño

- La relación snippet–usuario–rol, en su propio Postgres.
- Las consultas que todos necesitan ("¿este usuario puede leer o escribir este snippet?" y "¿qué snippets puede ver?") y que ningún otro servicio escribe.

## De qué no es dueño

- De los snippets en sí ni de los usuarios: recibe los ids ya resueltos.
- De la autenticación de los usuarios: no se expone hacia afuera y no sabe quién usa la UI. Solo lo llama `snippet-service`, con un access token machine-to-machine de Auth0 para la API interna, y este servicio lo valida: sin él, cualquier container de la red interna podría darse `OWNER` de cualquier snippet.

## Correr en local

```sh
./gradlew bootRun   # levanta en :8081
```

| Variable | Default |
|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `localhost` / `5433` / `permission` / `permission` / `permission` |
| `SERVER_PORT` | `8081` (en Docker, `8080`) |
| `AUTH0_DOMAIN` / `AUTH0_INTERNAL_AUDIENCE` | `dev-2fvmkxju8g70kqvs.us.auth0.com` / `https://snippet-searcher/internal`. El tenant que emite el token de `snippet-service` y el identifier de la API interna. No son secretos. |
| `AUTH_ENABLED` | `true`. En `false` no pide token: para correrlo en tu máquina sin la aplicación machine-to-machine de Auth0. En las VMs no se toca. |

## Calidad y CI/CD

Igual que el resto de los servicios: `./gradlew check` (ktlint, tests y 80% de cobertura). La imagen se publica en `ghcr.io/ingsis-2026/permission-service`. Detalle en el README de [`snippet-service`](https://github.com/Ingsis-2026/snippet-service#cicd).
