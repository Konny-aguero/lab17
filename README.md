# Laboratorio 17 - InsecurePay

Desarrollo de Software IV (IF0009), Universidad de Costa Rica. Aplicación local de laboratorio basada en [el repositorio original](https://github.com/jonnexusinsights-lab/unsecure_web_app_lab), commit `ef19c14fba95af3668c7184b6070d504c0d4e796`.

Se corrigieron SQL Injection, IDOR, Stored XSS, almacenamiento de contraseñas y configuración de errores/JWT/H2. El historial conserva la versión vulnerable para documentar los hallazgos. Uso exclusivo de laboratorio local.

## Documentación

- [Bitácora pericial](docs/BITACORA.md) y [PDF](docs/BITACORA.pdf).
- [Guía completa para tomar capturas](docs/GUIA_CAPTURAS.md) y [PDF de la guía](docs/GUIA_CAPTURAS.pdf).
- [Lista exacta de entrega](docs/ENTREGA.md).
- [Revisión de la rúbrica y pendientes](docs/REVISION_FINAL.md).
- [Evidencias](evidencias/): resultados reales en texto, organizados en cinco carpetas.

## Iniciar en Windows

Requiere JDK 21 o superior, Node.js/npm y Git. Python 3 se utiliza únicamente para repetir las pruebas HTTP. Se verificó con Java 24.0.2, Node 26.8.1 y el Angular 19 incluido realmente en el repositorio. Maven se ejecuta mediante el wrapper.

Primera terminal, desde la raíz del repositorio:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\iniciar-backend.ps1
```

El script genera un certificado local temporal en `.local/` y establece las variables de entorno para TLS. Si `JWT_SECRET` no existe, genera 32 bytes aleatorios en Base64 para esta ejecución. Si deseas mantener sesiones entre reinicios, define previamente una clave aleatoria propia de al menos 32 bytes codificados en Base64. No la guardes en Git. Una clave corta impide arrancar el servidor.

Segunda terminal:

```powershell
cd frontend
npm.cmd ci
npm.cmd start
```

Abre `https://localhost:4200/login`. El certificado es autofirmado: el navegador puede pedir que aceptes la conexión local. Backend: `https://localhost:8080`. Ambos servidores escuchan solamente en este equipo. La comunicación del frontend al backend usa el proxy HTTPS local; no se utiliza HTTP para enviar credenciales.

También puedes abrir `unsecure_web_app_lab.code-workspace` en VS Code y ejecutar la tarea **Start All (Backend + Frontend)** después de instalar las dependencias del frontend.

## Usuarios ficticios de prueba

| Usuario | Clave de prueba | Cuenta propia |
|---|---|---|
| carlos_mora | carlos123 | ID 2: CR05015202001026458901 |
| maria_rojas | maria123 | ID 3: CR05015202001026458902 |
| juan_perez | juan123 | ID 4: CR05015202001026458903 |
| admin | admin123 | ID 1: CR05015202001026458900 |
| auditor_seguridad | auditor2026 | Sin cuenta propia |

Los valores almacenados en `data.sql` son hashes BCrypt de factor 12. ADMIN y AUDITOR pueden leer otras cuentas según el PDF; nadie puede debitar una cuenta ajena. La base H2 está en memoria: reiniciar el backend restaura los datos iniciales y elimina las transferencias de prueba. La consola H2 está deshabilitada.

## Verificación

Desde `backend`:

```powershell
.\mvnw.cmd clean test
```

Desde `frontend`:

```powershell
npm.cmd run build
```

Con ambos servidores iniciados, desde la raíz:

```powershell
python .\scripts\verificar.py Todo
```

El verificador usa exclusivamente `https://localhost:8080`. Guarda sus salidas por vulnerabilidad y omite tokens. La prueba XSS hace una transferencia propia de 1 CRC; IDOR intenta un débito ajeno que debe rechazarse. El test del error 500 usa un controlador exclusivo de pruebas, no un endpoint agregado al servidor.

La entrega es este repositorio, con su bitácora y evidencias; no necesita un ZIP. Las capturas PNG manuales están pendientes y no se han sustituido por imágenes artificiales. El laboratorio permite también salidas de terminal, que ya están incluidas.
