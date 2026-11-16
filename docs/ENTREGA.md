# Entrega del Laboratorio 17

**Estudiante:** Konny Suseth Agüero Díaz.  
**Curso:** IF0009 - Desarrollo de Software IV, Universidad de Costa Rica.

## Qué entregar en Mediación Virtual

1. **Enlace al repositorio:** https://github.com/Konny-aguero/lab17
2. **Informe:** `docs/BITACORA.pdf`. Adjunta este archivo si la actividad permite o solicita adjuntar el informe; en cualquier caso debe quedar dentro del repositorio.
3. **Código, pruebas y evidencias dentro de ese repositorio:** carpetas `backend/`, `frontend/`, `scripts/`, `docs/` y `evidencias/`, además del README y los archivos de arranque/configuración del proyecto.

El PDF del profesor exige un repositorio y el documento con la bitácora, pero no especifica el formulario exacto de Mediación Virtual. Si el formulario solo admite un enlace, entrega el enlace del repositorio: allí estarán el informe y las evidencias. Si tiene un espacio para adjuntos, agrega también el PDF. No hace falta crear un ZIP.

## Qué debe quedar dentro del repositorio

- Código corregido y sus pruebas automatizadas.
- `README.md`, con instrucciones y usuarios ficticios de prueba.
- `docs/BITACORA.md`: versión editable del informe.
- `docs/BITACORA.pdf`: versión para lectura y entrega.
- `evidencias/01_SQL_Injection`, `02_IDOR`, `03_Stored_XSS`, `04_BCrypt` y `05_Hardening`: salidas reales y las fotos que tomes.
- `docs/evidencias/`: registros originales preservados.
- `docs/REVISION_FINAL.md`: revisión de los criterios que suman 100 puntos.
- `docs/GUIA_CAPTURAS.md` y `.pdf`: guía de apoyo para preparar las fotos.

No se incluyen `node_modules`, `target`, certificados, claves privadas ni secretos de ejecución.

## Lo que todavía debes hacer personalmente

1. Tomar las capturas reales que deseas agregar siguiendo `docs/GUIA_CAPTURAS.md`. Ya hay evidencia textual válida: no es necesario repetir las pruebas anteriores.
2. Guardarlas con los nombres indicados. No hay ninguna foto PNG fabricada para rellenar estos espacios.
3. Ejecutar `python scripts/generar_informes.py` para incorporar las fotos existentes y actualizar los PDFs; la guía incluye la instalación de su única dependencia.
4. Revisar visualmente el PDF, guardar los cambios en Git y subirlos, según los comandos de la guía.
5. Abrir el repositorio en GitHub, comprobar que contiene los archivos y pegar el enlace en la actividad de Mediación Virtual. Adjuntar el PDF cuando el formulario lo permita o lo pida.

## Estado verificado

Las pruebas se ejecutaron el 14/11/2026: 18 pruebas de backend aprobadas, Angular compilado, login real, protección SQLi/IDOR/XSS, BCrypt y errores seguros comprobados, además de transferencia legítima, búsqueda y comprobante. El 16/11/2026 se revisaron la documentación y la integridad de los registros sin repetir esas pruebas. La rúbrica está cubierta con la evidencia textual existente; las capturas manuales y la entrega en la plataforma permanecen pendientes. La nota final la asigna el profesor.
