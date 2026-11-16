# Revisión final de la rúbrica

Revisión documental: 16/11/2026. Las ejecuciones comprobadas corresponden al 14/11/2026; no se repitieron para esta revisión.

Los 100 puntos se distribuyen según el PDF. Esta revisión indica evidencia y pendientes; no otorga una nota.

| Criterio | Puntos | Comprobación realizada | Estado |
|---|---:|---|---|
| Auditoría y PoC | 20 | SQLi con datos ajenos; consulta y débito IDOR; XSS ejecutado en Edge; salidas originales preservadas | Verificado |
| Correcciones defensivas | 20 | JPQL con parámetros; propiedad y JWT; HTTP 403; escape HTML e interpolación; pruebas HTTP, Java y navegador | Verificado |
| BCrypt | 25 | Bean factor 12; cinco hashes; matches contra base cargada; cinco logins válidos; clave errónea rechazada; HTTPS local | Verificado |
| Hardening | 20 | Sin trazas públicas; ProblemDetail; prueba controlada HTTP 500; H2 deshabilitada; clave externa de 256 bits y token inválido/vencido rechazado | Verificado |
| Bitácora y documentación | 15 | Cinco fichas con OWASP/CWE/CVSS, líneas, PoC, impacto, parches, resultados y evidencia textual antes/después; guía de fotos y PDF | Documentado; fotos manuales pendientes |

## Checklist técnico

- [x] `mvnw.cmd clean test`: 18 pruebas, 0 fallos, 0 errores, 0 omitidas, BUILD SUCCESS.
- [x] Backend iniciado por HTTPS en loopback; llamadas reales atendidas.
- [x] `npm.cmd run build`: compilación Angular completada.
- [x] Login real por interfaz y por API.
- [x] Búsqueda manipulada devuelve []; búsqueda normal funciona.
- [x] Cuenta y transferencia ajenas devuelven HTTP 403 para clientes.
- [x] ADMIN/AUDITOR conservan lectura según el PDF; no pueden debitar cuentas ajenas.
- [x] Tokens ausentes, falsificados o vencidos rechazados.
- [x] Transferencia propia y búsqueda/comprobante normal comprobados en navegador.
- [x] XSS anterior confirmado y posterior bloqueado en navegador.
- [x] PasswordEncoder.matches y hashes BCrypt de factor 12 comprobados para cinco usuarios.
- [x] H2 devuelve HTTP 404; error HTTP 500 probado con controlador exclusivo de test.
- [x] Secretos y certificados locales excluidos de Git.
- [x] Registros reales preservados, sin imágenes generadas ni resultados inventados.
- [x] Bitácora en Markdown y PDF; guía con pasos y nombres de captura.
- [ ] Capturas PNG manuales guardadas e incorporadas a la bitácora.
- [ ] PDF actualizado con las fotos, si se desea entregarlas también dentro del PDF.
- [ ] Enlace entregado en Mediación Virtual por el estudiante.

## Evidencia y límites

Las salidas de terminal son una alternativa de evidencia admitida por el enunciado. Las fotos se dejan pendientes por solicitud del estudiante y no se simulan. La evidencia anterior de BCrypt es revisión del código original y login, no una extracción dinámica de H2. La evidencia anterior de XSS es el registro auténtico del navegador; no se guardó una foto de la alerta.

Los IDs y saldos cambian al repetir las transferencias controladas y se restauran al reiniciar la base en memoria. El certificado local es autofirmado. La verificación se limita al laboratorio y no constituye una certificación para producción.

Se verificó la integridad de los 28 registros de texto con `evidencias/SHA256SUMS.txt`: todas las huellas coinciden. No se encontraron archivos PNG en las carpetas de evidencias. La portada usa únicamente el nombre y los datos académicos proporcionados.
