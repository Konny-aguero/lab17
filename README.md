# Laboratorio 17 - InsecurePay

**Universidad de Costa Rica**  
**Curso:** IF0009 - Desarrollo de Software IV

## Descripción

En este laboratorio se trabajó con la aplicación bancaria InsecurePay, basada en el [proyecto original](https://github.com/jonnexusinsights-lab/unsecure_web_app_lab).

El objetivo fue identificar cinco problemas de seguridad, corregirlos y comprobar que las soluciones funcionaran correctamente.

Los problemas trabajados fueron:
- SQL Injection
- IDOR
- Stored XSS
- Almacenamiento inseguro de contraseñas
- Configuración insegura y exposición de errores

## Ejecución del proyecto

Para ejecutar la aplicación es necesario tener instalados Java 21 o superior y Node.js.

**Backend**

Desde la carpeta principal del proyecto, ejecutar:

```powershell
.\iniciar-backend.ps1
```

**Frontend**

En otra terminal, ejecutar:

```powershell
cd frontend
npm install
npm start
```

Una vez iniciados ambos, se puede acceder a la aplicación desde:

`https://localhost:4200/login`

## Usuarios de prueba

| Usuario | Contraseña |
|---|---|
| carlos_mora | carlos123 |
| maria_rojas | maria123 |
| juan_perez | juan123 |
| admin | admin123 |
| auditor_seguridad | auditor2026 |

## Pruebas

Para comprobar que el backend funciona correctamente, ejecutar desde la carpeta `backend`:

```powershell
.\mvnw.cmd clean test
```

Para comprobar el frontend, ejecutar desde la carpeta `frontend`:

```powershell
npm run build
```

## Documentación y evidencias

La documentación del laboratorio se encuentra en:

- **`docs/BITACORA.pdf`**: contiene la explicación de los problemas encontrados, las pruebas realizadas y las correcciones.
- **`evidencias/`**: contiene las capturas del antes, después y código corregido de cada uno de los cinco problemas.

Las pruebas y los cambios realizados se documentaron en la bitácora.