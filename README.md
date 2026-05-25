# Certiva

Plataforma para gestión de eventos formativos: inscripciones, check-in por QR, certificados PDF y panel administrativo.

## Roles del sistema

| Rol | Descripción |
|-----|-------------|
| **Admin** | Dashboard, auditoría, roles, importación CSV |
| **Profesor** | Creación y edición de eventos |
| **Monitor** | Gestión de usuarios, eventos y check-in |
| **Estudiante** | Portal de inscripción a eventos |

## Ciclo de vida del evento (estado operativo)

`PROXIMO` → `EN_CURSO` → `FINALIZADO_POR_TIEMPO` (automático por fechas, scheduler cada 60s).

`FINALIZADO_POR_TIEMPO` → `EN_REVISION` → `CERRADO_Y_CERTIFICADO` (acciones del profesor en **Mi panel**).

`EVENT_CANCELLED` (cancelación soft delete, solo admin).

### API relevante

| Método | Ruta | Rol |
|--------|------|-----|
| POST | `/api/eventos/{id}/iniciar-revision` | Profesor / Admin |
| POST | `/api/eventos/{id}/cerrar-y-certificar` | Profesor / Admin |
| POST | `/api/eventos/{id}/forzar-cierre` | Admin |
| POST | `/api/eventos/{id}/cancelar` | Admin |
| PUT | `/api/inscripciones/{id}/cancelar` | Estudiante (solo evento Próximo) |
| POST | `/api/check-in` | Monitor / Admin |

Auditoría: `EVENT_CREATED`, `ROLE_CHANGE`, `CHECKIN_SUCCESS`, `CHECKIN_DENIED`, `EVENT_CLOSED`, `CERTIFICATE_GENERATED`, etc.

