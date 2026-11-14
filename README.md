# permission-service

Parte de **Snippet Searcher** (Ingeniería de Sistemas 2026). Guarda quién puede hacer qué sobre cada snippet: `OWNER` o `READ` (US7).

## De qué es dueño

- La relación snippet–usuario–rol, en su propio Postgres.
- Las consultas que todos necesitan ("¿este usuario puede leer o escribir este snippet?" y "¿qué snippets puede ver?") y que ningún otro servicio escribe.

## De qué no es dueño

- De los snippets en sí ni de los usuarios: recibe los ids ya resueltos.
- De la autenticación: no se expone hacia afuera. Solo lo llama `snippet-service` dentro de la red interna, así que no valida JWT.

## Correr en local

```sh
./gradlew bootRun   # levanta en :8081
```

| Variable | Default |
|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `localhost` / `5433` / `permission` / `permission` / `permission` |
| `SERVER_PORT` | `8081` (en Docker, `8080`) |
| `LOG_FORMAT` | Vacío: logs en texto, con el contexto entre corchetes (`[requestId=…, snippetId=…]`). La imagen de Docker trae `ecs`: un JSON por línea (Elastic Common Schema) con `service.name` y el contexto como campos. |

## Logs

Cada cambio de permisos (otorgar, un permiso repetido, revocar los de un snippet borrado) queda en el log en INFO, con `snippetId`, `userId`, `role` o `revoked` en el MDC. Las consultas no se loguean; un permiso que no existe va en DEBUG. Un error inesperado va en ERROR, con el stack trace.

Todos los logs de un pedido llevan su `requestId`: el que manda snippet-service en `X-Request-Id`, que es el mismo de sus propios logs, o uno nuevo si no vino. Vuelve en la respuesta.

## Calidad y CI/CD

Igual que el resto de los servicios: `./gradlew check` (ktlint, tests y 80% de cobertura). La imagen se publica en `ghcr.io/ingsis-2026/permission-service`. Detalle en el README de [`snippet-service`](https://github.com/Ingsis-2026/snippet-service#cicd).
