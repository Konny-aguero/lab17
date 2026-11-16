# Laboratorio 17

## Universidad de Costa Rica

**Curso:** IF0009 - Desarrollo de Software IV

**Auditoría Forense de Software, Detección OWASP Top 10 y Remediación Defensiva en InsecurePay**

**Estudiante:** Konny Suseth Agüero Díaz

**Pruebas realizadas:** 14 de noviembre de 2026

**Revisión documental:** 16 de noviembre de 2026

Informe con evidencia textual real. Las capturas manuales pendientes se identifican expresamente.

<!-- SALTO DE PAGINA -->

# Bitácora Pericial Forense

**Curso:** IF0009 - Desarrollo de Software IV, Universidad de Costa Rica.  
**Aplicación:** InsecurePay.  
**Fecha de ejecución:** 14 de noviembre de 2026 (Costa Rica, UTC-6).  
**Alcance:** copia local; pruebas controladas sobre cuentas ficticias. No se contactaron objetivos externos para realizar ataques.

## Método y custodia

Se revisó el código original, se ejecutaron pruebas HTTP y de navegador, se guardaron las salidas y después se aplicaron las correcciones. La referencia original es el commit `ef19c14fba95af3668c7184b6070d504c0d4e796` del repositorio docente. Las líneas afectadas que siguen corresponden a ese commit; las líneas del parche corresponden a la entrega actual.

Las evidencias iniciales se conservaron en `docs/evidencias/antes-http.txt`, `antes-codigo.txt` y `antes-xss-navegador.txt`. Las carpetas `evidencias/01_SQL_Injection` a `05_Hardening` incluyen extractos identificados y pruebas posteriores. Los tokens de sesión se omitieron. `evidencias/SHA256SUMS.txt` permite comprobar la integridad de los archivos de texto capturados en esta entrega; si se repite una prueba, la nueva salida tendrá otra huella.

No se revirtieron las correcciones para repetir ataques iniciales. No existen capturas PNG del estado anterior: existen salidas reales de terminal y de la automatización del navegador. Se dejan marcadas las capturas manuales pendientes. Una captura de esos archivos muestra evidencia histórica, no una nueva ejecución de la versión vulnerable.

## Diferencias entre el PDF y el repositorio

El código real usa el paquete `cr.ac.ucr.if0009.insecurepay`, Angular 19, la columna `concepto_html` y el campo `fecha`. Carlos tiene usuario ID 3 y cuenta ID 2; María, usuario ID 4 y cuenta ID 3, con saldo inicial de 4 850 000 CRC. La clave de Carlos es `carlos123`. El endpoint `/api/cuentas/{id}/saldo` no existía originalmente: la evidencia anterior usa `/api/cuentas/3`, que sí revelaba el saldo. Se agregó el endpoint de saldo protegido solicitado en la guía. Se adaptaron las consultas y pruebas a estos datos reales.

## Criterio de severidad

Las puntuaciones son estimaciones CVSS v3.1 para los vectores indicados, no calificaciones emitidas por un escáner ni notas del curso. Se explicitan las condiciones asumidas y no se suman los puntajes de distintas vulnerabilidades.


## HALLAZGO-01: SQL Injection

**Identificador:** HALLAZGO-01.  
**Clasificación OWASP:** A03:2021 - Injection.  
**CWE:** CWE-89.  
**CVSS v3.1:** 7.5 (Alta).  
**Vector:** `CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N`.  
**Archivo y línea original afectados:** `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/TransaccionService.java:55`.

### Descripción del problema

La búsqueda concatenaba el término recibido dentro de una sentencia SQL. Una comilla permitía cambiar la condición y consultar movimientos de otros clientes.

### Prueba realizada e impacto

Se envió GET /api/transacciones/buscar?termino=%27+OR+1%3D1+--. La respuesta HTTP 200 incluyó las cuatro transacciones iniciales, incluida la transacción 2 de María a Juan, ajena a Carlos.

Exposición de movimientos bancarios, nombres, cuentas y montos de terceros. La puntuación considera únicamente la pérdida de confidencialidad demostrada.

### Corrección aplicada y justificación

La consulta JPQL usa :termino, :usuarioId y :auditor con @Param. CAST convierte el CLOB a texto para que LOWER funcione con Hibernate 6.5; el valor de búsqueda sigue vinculado como parámetro. También se limita la búsqueda a movimientos del usuario, salvo los roles de lectura previstos por el laboratorio.

### Resultado de verificación

El mismo payload devolvió HTTP 200 con []. La búsqueda normal de programacion encontró su movimiento. Los listados y búsquedas no incluyeron la transacción ajena 2.

La prueba posterior se puede repetir desde la raíz con `python scripts/verificar.py SQL`. Las verificaciones adicionales de base de datos, firma/vencimiento de tokens y error 500 están en `backend/src/test/java/cr/ac/ucr/if0009/insecurepay/`.

### Evidencias

- Antes: [salida y/o código original](../evidencias/01_SQL_Injection/antes.txt).
- Después: [salida HTTP real](../evidencias/01_SQL_Injection/despues.txt).
- Parche: [extractos numerados del código actual](../evidencias/01_SQL_Injection/codigo_corregido.txt).

### Extractos de evidencia real

Los siguientes fragmentos proceden de los registros enlazados. Se omiten partes no relevantes; los originales completos se conservan sin cambios. Las salidas de pruebas automatizadas se identifican como tales.

**Antes:**

```text
SQLi
GET /api/transacciones/buscar?termino=%27+OR+1%3D1+--
HTTP 200
Content-Type: application/json
[Extracto de la transacción ajena incluida en la respuesta]
  {
    "id": 2,
    "cuentaOrigenId": 3,
    "numeroCuentaOrigen": "CR05015202001026458902",
    "titularOrigen": "María Rojas Soto",
    "cuentaDestinoId": 4,
    "numeroCuentaDestino": "CR05015202001026458903",
    "titularDestino": "Juan Pérez Delgado",
    "monto": 120000.0,
    "conceptoHtml": "<p>Cancelacion cuota de alquiler oficina central. Factura electronica #8849.</p>",
    "fecha": "2026-03-05T14:30:00",
    "estado": "COMPLETADA"
  }
```

**Después:**

```text
SQLi
GET /api/transacciones/buscar?termino=%27+OR+1%3D1+--
HTTP 200
Content-Type: application/json
[]
```

### Código corregido

`backend/src/main/java/cr/ac/ucr/if0009/insecurepay/repository/TransaccionRepository.java`, líneas 12-22:

```text
12:     @Query("""
13:             SELECT t FROM Transaccion t
14:             WHERE (LOWER(CAST(t.conceptoHtml AS string)) LIKE LOWER(CONCAT('%', :termino, '%'))
15:                 OR LOWER(t.estado) LIKE LOWER(CONCAT('%', :termino, '%')))
16:               AND (:auditor = true OR t.cuentaOrigen.usuario.id = :usuarioId
17:                 OR t.cuentaDestino.usuario.id = :usuarioId)
18:             ORDER BY t.fecha DESC
19:             """)
20:     List<Transaccion> buscarPorTerminoSeguro(@Param("termino") String termino,
21:             @Param("usuarioId") Long usuarioId, @Param("auditor") boolean auditor);
22: }
```

`backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/TransaccionService.java`, líneas 62-68:

```text
62:     public List<TransaccionDto> buscarPorConcepto(String termino, String auth) {
63:         Usuario usuario = acceso.autenticar(auth);
64:         if (termino == null || termino.isBlank()) return List.of();
65:         return transaccionRepository.buscarPorTerminoSeguro(termino.trim(), usuario.getId(),
66:                 acceso.puedeAuditar(usuario)).stream().map(this::convertirADto).toList();
67:     }
68: 
```

### Capturas manuales

- Antes (`evidencias/01_SQL_Injection/antes.png`): PENDIENTE: INSERTAR CAPTURA REAL.

- Después (`evidencias/01_SQL_Injection/despues.png`): PENDIENTE: INSERTAR CAPTURA REAL.

- Código (`evidencias/01_SQL_Injection/codigo_corregido.png`): PENDIENTE: INSERTAR CAPTURA REAL.

Las salidas reales ya incluidas cumplen la alternativa de evidencia textual descrita por el laboratorio. La guía explica cómo obtener las imágenes sin reproducir el estado vulnerable.

## HALLAZGO-02: IDOR en cuentas y transferencias

**Identificador:** HALLAZGO-02.  
**Clasificación OWASP:** A01:2021 - Broken Access Control.  
**CWE:** CWE-639 / CWE-862.  
**CVSS v3.1:** 9.1 (Crítica).  
**Vector:** `CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:N`.  
**Archivo y línea original afectados:** `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/CuentaService.java:30`; `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/TransaccionService.java:71`.

### Descripción del problema

El servidor aceptaba el ID enviado por el cliente sin comprobar la firma del token ni la propiedad de la cuenta. También permitía debitar desde una cuenta ajena.

### Prueba realizada e impacto

Con una sesión de Carlos se consultó GET /api/cuentas/3 y se envió POST /api/transacciones/transferir con origen 3, destino CR05015202001026458901 y monto 1. Se obtuvo HTTP 200 con el saldo de María de 4 850 000 CRC y HTTP 201 para el débito ajeno. El acceso sin token también era aceptado.

Revelación de saldos y transferencia de fondos sin autorización del titular. La puntuación considera que la versión original tampoco exigía autenticación.

### Corrección aplicada y justificación

AccesoService verifica firma y vencimiento del JWT y carga al usuario real desde la base de datos. CuentaService exige propiedad; ADMIN y AUDITOR conservan únicamente la excepción de lectura indicada por el PDF. Toda transferencia exige que el origen pertenezca al usuario, incluso si tiene un rol privilegiado. Se protegen también los listados y los movimientos para evitar rutas alternativas.

### Resultado de verificación

Cuenta propia: HTTP 200. Cuenta ajena, saldo ajeno y débito ajeno: HTTP 403. Token ausente, inválido, vencido o firmado con otra clave: HTTP 401. Las pruebas comprobaron que un intento rechazado no cambia el saldo y que una transferencia propia sí debita y acredita 1 CRC.

La prueba posterior se puede repetir desde la raíz con `python scripts/verificar.py IDOR`. Las verificaciones adicionales de base de datos, firma/vencimiento de tokens y error 500 están en `backend/src/test/java/cr/ac/ucr/if0009/insecurepay/`.

### Evidencias

- Antes: [salida y/o código original](../evidencias/02_IDOR/antes.txt).
- Después: [salida HTTP real](../evidencias/02_IDOR/despues.txt).
- Parche: [extractos numerados del código actual](../evidencias/02_IDOR/codigo_corregido.txt).

### Extractos de evidencia real

Los siguientes fragmentos proceden de los registros enlazados. Se omiten partes no relevantes; los originales completos se conservan sin cambios. Las salidas de pruebas automatizadas se identifican como tales.

**Antes:**

```text
CUENTA AJENA CON SALDO
GET /api/cuentas/3
HTTP 200
Content-Type: application/json
{
  "id": 3,
  "usuarioId": 4,
  "titular": "María Rojas Soto",
  "numeroCuenta": "CR05015202001026458902",
  "tipoCuenta": "CORRIENTE",
  "saldo": 4850000.0,
  "moneda": "CRC",
  "activa": true
}

TRANSFERENCIA DESDE CUENTA AJENA
POST /api/transacciones/transferir
HTTP 201
Content-Type: application/json
{
  "id": 5,
  "cuentaOrigenId": 3,
  "numeroCuentaOrigen": "CR05015202001026458902",
  "titularOrigen": "María Rojas Soto",
  "cuentaDestinoId": 2,
  "numeroCuentaDestino": "CR05015202001026458901",
  "titularDestino": "Carlos Mora Morales",
  "monto": 1,
  "conceptoHtml": "PoC IDOR controlada",
  "fecha": "2026-11-14T15:59:39.1387199",
  "estado": "COMPLETADA"
}
```

**Después:**

```text
CUENTA AJENA CON SALDO
GET /api/cuentas/3
HTTP 403
Content-Type: application/problem+json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "Acceso no autorizado a cuenta ajena.",
  "instance": "/api/cuentas/3"
}

TRANSFERENCIA DESDE CUENTA AJENA
POST /api/transacciones/transferir
HTTP 403
Content-Type: application/problem+json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "Acceso no autorizado a cuenta ajena.",
  "instance": "/api/transacciones/transferir"
}
```

### Código corregido

`backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/AccesoService.java`, líneas 22-47:

```text
22:     public Usuario autenticar(String header) {
23:         if (header == null || !header.startsWith("Bearer ")) {
24:             throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticacion requerida.");
25:         }
26:         try {
27:             String username = jwtUtil.getClaims(header.substring(7)).getSubject();
28:             if (username == null || username.isBlank()) {
29:                 throw new IllegalArgumentException();
30:             }
31:             return usuarios.findByUsername(username).orElseThrow(() ->
32:                     new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido."));
33:         } catch (JwtException | IllegalArgumentException ex) {
34:             throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido o vencido.");
35:         }
36:     }
37: 
38:     public boolean puedeAuditar(Usuario usuario) {
39:         return usuario.getRol() == Rol.ADMIN || usuario.getRol() == Rol.AUDITOR;
40:     }
41: 
42:     public void exigirPropietario(Usuario usuario, Long propietarioId, boolean permitirAuditoria) {
43:         if (!usuario.getId().equals(propietarioId) && !(permitirAuditoria && puedeAuditar(usuario))) {
44:             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso no autorizado a cuenta ajena.");
45:         }
46:     }
47: }
```

`backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/CuentaService.java`, líneas 33-41:

```text
33:     public CuentaDto obtenerPorId(Long id, String auth) {
34:         Usuario usuario = acceso.autenticar(auth);
35:         CuentaBancaria cuenta = cuentaRepository.findById(id)
36:                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta bancaria no encontrada."));
37:         acceso.exigirPropietario(usuario, cuenta.getUsuario().getId(), true);
38:         return convertirADto(cuenta);
39:     }
40: 
41:     public List<CuentaDto> obtenerPorUsuario(Long usuarioId, String auth) {
```

`backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/TransaccionService.java`, líneas 85-88:

```text
85:         acceso.exigirPropietario(usuario, cuentaOrigen.getUsuario().getId(), false);
86:         CuentaBancaria cuentaDestino = cuentaRepository.findByNumeroCuenta(dto.getNumeroCuentaDestino())
87:                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta de destino no encontrada."));
88: 
```

### Capturas manuales

- Antes (`evidencias/02_IDOR/antes.png`): PENDIENTE: INSERTAR CAPTURA REAL.

- Después (`evidencias/02_IDOR/despues.png`): PENDIENTE: INSERTAR CAPTURA REAL.

- Código (`evidencias/02_IDOR/codigo_corregido.png`): PENDIENTE: INSERTAR CAPTURA REAL.

Las salidas reales ya incluidas cumplen la alternativa de evidencia textual descrita por el laboratorio. La guía explica cómo obtener las imágenes sin reproducir el estado vulnerable.

## HALLAZGO-03: Stored XSS en comprobantes

**Identificador:** HALLAZGO-03.  
**Clasificación OWASP:** A03:2021 - Injection.  
**CWE:** CWE-79.  
**CVSS v3.1:** 6.1 (Media).  
**Vector:** `CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:C/C:L/I:L/A:N`.  
**Archivo y línea original afectados:** `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/TransaccionService.java:98`; `frontend/src/app/components/transacciones/transacciones.component.ts:82`.

### Descripción del problema

El backend guardaba HTML sin tratamiento y la vista desactivaba el filtro de Angular con bypassSecurityTrustHtml. Al abrir un comprobante se ejecutaba el contenido almacenado.

### Prueba realizada e impacto

Se transfirió 1 CRC desde la cuenta propia de Carlos a María con el concepto <img src="x" onerror="alert('AUDITORIA XSS CONFIRMADA')">. En Microsoft Edge, al abrir el comprobante 6 de la ejecución original, se registró la alerta y una imagen inyectada en el documento.

Ejecución de JavaScript en la sesión de quien abre el comprobante. Puede permitir lectura de datos accesibles al navegador o manipulación de la interfaz. La prueba fue una alerta local, sin extracción de tokens ni comunicación externa; la severidad usa impactos limitados y requiere que alguien abra el comprobante.

### Corrección aplicada y justificación

Se eliminó DomSanitizer/SafeHtml y el bypass. La plantilla usa interpolación de texto. El backend aplica HtmlUtils.htmlEscape antes de persistir, como defensa adicional. Por seguir ambas capas pedidas por el laboratorio, las entidades escapadas pueden verse como texto literal en el comprobante.

### Resultado de verificación

La transferencia propia devolvió HTTP 201 y el concepto almacenado contenía &lt;img. En Edge se abrió el comprobante 5 de la ejecución corregida: cero alertas y cero imágenes inyectadas. El flujo normal de transferencia, búsqueda y comprobante también funcionó.

La prueba posterior se puede repetir desde la raíz con `python scripts/verificar.py XSS`. Las verificaciones adicionales de base de datos, firma/vencimiento de tokens y error 500 están en `backend/src/test/java/cr/ac/ucr/if0009/insecurepay/`.

### Evidencias

- Antes: [salida y/o código original](../evidencias/03_Stored_XSS/antes.txt).
- Después: [salida HTTP real](../evidencias/03_Stored_XSS/despues.txt).
- Parche: [extractos numerados del código actual](../evidencias/03_Stored_XSS/codigo_corregido.txt).
- Navegador: [antes](../evidencias/03_Stored_XSS/antes-navegador.txt) y [después](../evidencias/03_Stored_XSS/despues-navegador.txt), con alerta y recuento real de imágenes.

### Extractos de evidencia real

Los siguientes fragmentos proceden de los registros enlazados. Se omiten partes no relevantes; los originales completos se conservan sin cambios. Las salidas de pruebas automatizadas se identifican como tales.

**Antes:**

```text
{
  "fechaUTC": "2026-11-14T22:00:55.384Z",
  "fase": "antes",
  "navegador": "Microsoft Edge (Playwright)",
  "loginInterfaz": "correcto",
  "transaccion": 6,
  "payload": "<img src=\"x\" onerror=\"alert('AUDITORIA XSS CONFIRMADA')\">",
  "alertas": [
    "AUDITORIA XSS CONFIRMADA"
  ],
  "imagenesInyectadas": 1,
  "resultado": "JavaScript almacenado ejecutado"
}
```

**Después:**

```text
{
  "fechaUTC": "2026-11-14T23:18:24.136Z",
  "fase": "despues",
  "navegador": "Microsoft Edge (Playwright)",
  "loginInterfaz": "correcto",
  "transaccion": 5,
  "payload": "&lt;img src=&quot;x&quot; onerror=&quot;alert(&#39;AUDITORIA XSS CONFIRMADA&#39;)&quot;&gt;",
  "alertas": [],
  "imagenesInyectadas": 0,
  "resultado": "Concepto mostrado como texto; JavaScript bloqueado"
}
```

### Código corregido

`backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/TransaccionService.java`, líneas 105-107:

```text
105:         transaccion.setConceptoHtml(HtmlUtils.htmlEscape(dto.getConceptoHtml() == null ? "" : dto.getConceptoHtml()));
106:         transaccion.setEstado("COMPLETADA");
107: 
```

`frontend/src/app/components/transacciones/transacciones.component.html`, líneas 93-94:

```text
93:            >{{ transaccionSeleccionada.conceptoHtml }}
94:       </div>
```

### Capturas manuales

- Antes (`evidencias/03_Stored_XSS/antes.png`): PENDIENTE: INSERTAR CAPTURA REAL.

- Después (`evidencias/03_Stored_XSS/despues.png`): PENDIENTE: INSERTAR CAPTURA REAL.

- Código (`evidencias/03_Stored_XSS/codigo_corregido.png`): PENDIENTE: INSERTAR CAPTURA REAL.

Las salidas reales ya incluidas cumplen la alternativa de evidencia textual descrita por el laboratorio. La guía explica cómo obtener las imágenes sin reproducir el estado vulnerable.

## HALLAZGO-04: Contraseñas almacenadas en texto plano

**Identificador:** HALLAZGO-04.  
**Clasificación OWASP:** A02:2021 - Cryptographic Failures.  
**CWE:** CWE-256.  
**CVSS v3.1:** 5.5 (Media).  
**Vector:** `CVSS:3.1/AV:L/AC:L/PR:L/UI:N/S:U/C:H/I:N/A:N`.  
**Archivo y línea original afectados:** `backend/src/main/resources/data.sql:7`; `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/AuthService.java:27`.

### Descripción del problema

data.sql incluía contraseñas legibles y AuthService las comparaba directamente con equals. Una copia del archivo o lectura de la base revelaba inmediatamente las credenciales.

### Prueba realizada e impacto

Se conservaron las líneas originales de data.sql y AuthService antes de corregirlas. El login original de Carlos devolvió HTTP 200. Esta evidencia es de revisión de código y de funcionamiento del login; no se afirma haber extraído la tabla por la consola H2.

Exposición de las contraseñas de usuarios ante acceso al almacenamiento. El vector CVSS asume lectura local del archivo/base con privilegios bajos; no asigna al hash por sí solo los efectos de otras vulnerabilidades.

### Corrección aplicada y justificación

PasswordConfig define BCryptPasswordEncoder(12). Los cinco registros se reemplazaron por hashes con sal independiente. AuthService utiliza exclusivamente passwordEncoder.matches. HTTPS protege las credenciales en tránsito; las contraseñas no se devuelven en las respuestas ni se incluyen en los registros de pruebas. Las credenciales públicas del README corresponden solo a usuarios ficticios del laboratorio.

### Resultado de verificación

Se comprobaron los cinco hashes con prefijo $2a$12$, la comparación BCrypt contra los valores persistidos y los cinco logins HTTP 200. Una clave incorrecta devuelve HTTP 401 con un mensaje genérico. Las pruebas no dependen de un secreto JWT fijo.

La prueba posterior se puede repetir desde la raíz con `python scripts/verificar.py BCrypt`. Las verificaciones adicionales de base de datos, firma/vencimiento de tokens y error 500 están en `backend/src/test/java/cr/ac/ucr/if0009/insecurepay/`.

### Evidencias

- Antes: [salida y/o código original](../evidencias/04_BCrypt/antes.txt).
- Después: [salida HTTP real](../evidencias/04_BCrypt/despues.txt).
- Parche: [extractos numerados del código actual](../evidencias/04_BCrypt/codigo_corregido.txt).
- Prueba adicional de Maven: [salida real](../evidencias/04_BCrypt/prueba-maven.txt); registro completo en `evidencias/validacion-maven.txt`.

### Extractos de evidencia real

Los siguientes fragmentos proceden de los registros enlazados. Se omiten partes no relevantes; los originales completos se conservan sin cambios. Las salidas de pruebas automatizadas se identifican como tales.

**Antes:**

```text
7: ('admin', 'admin123', 'Administrador General de Sistemas', 'admin@insecurepay.fi.cr', 'ADMIN'),
8: ('auditor_seguridad', 'auditor2026', 'Lic. Esteban Solano Chaves', 'esolano@insecurepay.fi.cr', 'AUDITOR'),
9: ('carlos_mora', 'carlos123', 'Carlos Mora Morales', 'cmora@correo.cr', 'CLIENTE'),
10: ('maria_rojas', 'maria123', 'María Rojas Soto', 'mrojas@empresa.cr', 'CLIENTE'),
11: ('juan_perez', 'juan123', 'Juan Pérez Delgado', 'jperez@correo.cr', 'CLIENTE');
12: 
27:         if (!usuario.getPassword().equals(request.getPassword())) {
28:             throw new RuntimeException("Credenciales invalidas para el usuario: " + request.getUsername());
29:         }
30: 
31:         String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().name(), usuario.getId());
32:
```

**Después:**

```text
BCRYPT_VERIFICADO: admin | factor 12 | matches=true | login HTTP 200
BCRYPT_VERIFICADO: auditor_seguridad | factor 12 | matches=true | login HTTP 200
BCRYPT_VERIFICADO: carlos_mora | factor 12 | matches=true | login HTTP 200
BCRYPT_VERIFICADO: maria_rojas | factor 12 | matches=true | login HTTP 200
BCRYPT_VERIFICADO: juan_perez | factor 12 | matches=true | login HTTP 200
[INFO] BUILD SUCCESS
```

### Código corregido

`backend/src/main/java/cr/ac/ucr/if0009/insecurepay/config/PasswordConfig.java`, líneas 8-14:

```text
8: @Configuration
9: public class PasswordConfig {
10:     @Bean
11:     public PasswordEncoder passwordEncoder() {
12:         return new BCryptPasswordEncoder(12);
13:     }
14: }
```

`backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/AuthService.java`, líneas 27-38:

```text
27:     public LoginResponse autenticar(LoginRequest request) {
28:         Usuario usuario = usuarioRepository.findByUsername(request.getUsername()).orElse(null);
29:         boolean coincide = passwordEncoder.matches(request.getPassword(),
30:                 usuario == null ? dummyHash : usuario.getPassword());
31:         if (!coincide || usuario == null) {
32:             throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas.");
33:         }
34:         String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().name(), usuario.getId());
35:         return new LoginResponse(token, usuario.getUsername(), usuario.getNombreCompleto(),
36:                 usuario.getRol().name(), usuario.getId());
37:     }
38: }
```

`backend/src/main/resources/data.sql`, líneas 1-6:

```text
1: INSERT INTO USUARIOS (username, password, nombre_completo, email, rol) VALUES
2: ('admin', '$2a$12$fn28LWqh2.Ce2.wLjsgmyeoGZaSVlpvA4MARp9hnDracpObn.nHLC', 'Administrador General de Sistemas', 'admin@insecurepay.fi.cr', 'ADMIN'),
3: ('auditor_seguridad', '$2a$12$.yEGP49D4TXW3GAlrcXpgudKIgDJS8r/zwCKi0w6UaIxc91nOQSSG', 'Lic. Esteban Solano Chaves', 'esolano@insecurepay.fi.cr', 'AUDITOR'),
4: ('carlos_mora', '$2a$12$rb8CKgWlUuY8iQP22lxnqeyDBchxA1gambjKWWEQoXEXkLKGCjTQS', 'Carlos Mora Morales', 'cmora@correo.cr', 'CLIENTE'),
5: ('maria_rojas', '$2a$12$I2vRCzYKtoPRdswA3z2B1.YNJ8khBNApozBuNdh9rbWtSyDHC9A0.', 'María Rojas Soto', 'mrojas@empresa.cr', 'CLIENTE'),
6: ('juan_perez', '$2a$12$dAQwV2spycHpb0rKblSnoeWIa.etfmf3SowgIQAtT0RdekwKfZxKC', 'Juan Pérez Delgado', 'jperez@correo.cr', 'CLIENTE');
```

### Capturas manuales

- Antes (`evidencias/04_BCrypt/antes.png`): PENDIENTE: INSERTAR CAPTURA REAL.

- Después (`evidencias/04_BCrypt/despues.png`): PENDIENTE: INSERTAR CAPTURA REAL.

- Código (`evidencias/04_BCrypt/codigo_corregido.png`): PENDIENTE: INSERTAR CAPTURA REAL.

Las salidas reales ya incluidas cumplen la alternativa de evidencia textual descrita por el laboratorio. La guía explica cómo obtener las imágenes sin reproducir el estado vulnerable.

## HALLAZGO-05: Fuga de trazas y configuración insegura

**Identificador:** HALLAZGO-05.  
**Clasificación OWASP:** A05:2021 - Security Misconfiguration.  
**CWE:** CWE-209 / CWE-798.  
**CVSS v3.1:** 5.3 (Media).  
**Vector:** `CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:L/I:N/A:N`.  
**Archivo y línea original afectados:** `backend/src/main/resources/application.properties:28`; `backend/src/main/resources/application.properties:15`; `backend/src/main/resources/application.properties:34`.

### Descripción del problema

Los errores mostraban trazas del framework y consultas SQL. La consola H2 estaba habilitada para otros equipos y la clave JWT estaba escrita en la configuración versionada.

### Prueba realizada e impacto

La búsqueda de una comilla aislada devolvió HTTP 500 con trace, clases de Hibernate y una sentencia SELECT. Se preservaron la respuesta íntegra y la configuración original. La ruta /h2-console/ respondía HTTP 200; no se afirma haber iniciado sesión en ella.

Divulgación de la estructura interna de la aplicación y de la base de datos, útil para preparar nuevos ataques. El puntaje base corresponde a la fuga de información; la clave fija y H2 se tratan como factores adicionales corregidos.

### Corrección aplicada y justificación

Se desactivaron stacktrace, message, binding-errors, exception y la consola H2. @RestControllerAdvice devuelve ProblemDetail con mensajes seguros y application/problem+json. JWT_SECRET es obligatorio, se interpreta como Base64 y exige al menos 32 bytes; se firma con HS256. El script local genera una clave aleatoria si no existe y un certificado temporal no versionado. Backend y frontend quedan ligados a loopback y utilizan HTTPS.

### Resultado de verificación

La comilla aislada devuelve HTTP 200 con []. Un recurso inexistente devuelve HTTP 404 con ProblemDetail, sin trace. H2 devuelve HTTP 404. El test ErrorHandlingTests provoca deliberadamente una excepción en un controlador exclusivo de pruebas y obtuvo HTTP 500 con detalle genérico, sin SQL ni traza. No existe un endpoint de fallo agregado a la aplicación.

La prueba posterior se puede repetir desde la raíz con `python scripts/verificar.py Hardening`. Las verificaciones adicionales de base de datos, firma/vencimiento de tokens y error 500 están en `backend/src/test/java/cr/ac/ucr/if0009/insecurepay/`.

### Evidencias

- Antes: [salida y/o código original](../evidencias/05_Hardening/antes.txt).
- Después: [salida HTTP real](../evidencias/05_Hardening/despues.txt).
- Parche: [extractos numerados del código actual](../evidencias/05_Hardening/codigo_corregido.txt).
- Prueba adicional de Maven: [salida real](../evidencias/05_Hardening/prueba-maven.txt); registro completo en `evidencias/validacion-maven.txt`.

### Extractos de evidencia real

Los siguientes fragmentos proceden de los registros enlazados. Se omiten partes no relevantes; los originales completos se conservan sin cambios. Las salidas de pruebas automatizadas se identifican como tales.

**Antes:**

```text
EXTRACTO DE RESPUESTA REAL; campo trace decodificado desde JSON.
Fuente completa: docs/evidencias/antes-http.txt

CONSULTA QUE PRODUCIA ERROR SQL
GET /api/transacciones/buscar?termino=%27
HTTP 500
Content-Type: application/json

org.hibernate.exception.DataException: could not prepare statement [Valor Invalido "CHARACTER VARYING(1)" para el parametro "MOD argument"
Invalid value "CHARACTER VARYING(1)" for parameter "MOD argument"; SQL statement:
SELECT * FROM TRANSACCIONES WHERE concepto_html LIKE '%'%' OR estado LIKE '%'%' [90008-224]] [SELECT * FROM TRANSACCIONES WHERE concepto_html LIKE '%'%' OR estado LIKE '%'%']
	at org.hibernate.exception.internal.SQLExceptionTypeDelegate.convert(SQLExceptionTypeDelegate.java:55)
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58)
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:108)
	at org.hibernate.engine.jdbc.internal.StatementPreparerImpl$StatementPreparationTemplate.prepareStatement(StatementPreparerImpl.java:194)
	at org.hibernate.engine.jdbc.internal.StatementPreparerImpl.prepareQueryStatement(StatementPreparerImpl.java:155)
	at org.hibernate.sql.exec.spi.JdbcSelectExecutor.lambda$list$0(JdbcSelectExecutor.java:85)
	at org.hibernate.sql.results.jdbc.internal.DeferredResultSetAccess.executeQuery(DeferredResultSetAccess.java:231)
	at org.hibernate.sql.results.jdbc.internal.DeferredResultSetAccess.getResultSet(DeferredResultSetAccess.java:167)
	at org.hibernate.sql.results.jdbc.internal.AbstractResultSetAccess.getMetaData(AbstractResultSetAccess.java:36)
	at org.hibernate.sql.results.jdbc.internal.AbstractResultSetAccess.getColumnCount(AbstractResultSetAccess.java:52)
	at org.hibernate.query.results.ResultSetMappingImpl.resolve(ResultSetMappingImpl.java:193)
```

**Después:**

```text
ERROR_500_VERIFICADO: {"type":"about:blank","title":"Internal Server Error","status":500,"detail":"No fue posible procesar la solicitud.","instance":"/fallo-prueba"}
[INFO] BUILD SUCCESS
```

### Código corregido

`backend/src/main/resources/application.properties`, líneas 11-21:

```text
11: spring.h2.console.enabled=false
12: spring.h2.console.settings.web-allow-others=false
13: spring.jpa.hibernate.ddl-auto=none
14: spring.sql.init.mode=always
15: spring.jpa.show-sql=false
16: server.error.include-message=never
17: server.error.include-stacktrace=never
18: server.error.include-binding-errors=never
19: server.error.include-exception=false
20: jwt.secret=${JWT_SECRET}
21: jwt.expiration-ms=3600000
```

`backend/src/main/java/cr/ac/ucr/if0009/insecurepay/controller/GlobalExceptionHandler.java`, líneas 14-33:

```text
14: @RestControllerAdvice
15: public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
16:     @ExceptionHandler(ResponseStatusException.class)
17:     public ProblemDetail acceso(ResponseStatusException ex) {
18:         return ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason());
19:     }
20: 
21:     @ExceptionHandler(Exception.class)
22:     public ProblemDetail inesperado(Exception ex) {
23:         return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
24:                 "No fue posible procesar la solicitud.");
25:     }
26: 
27:     @Override
28:     protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
29:             HttpHeaders headers, HttpStatusCode status, WebRequest request) {
30:         return super.handleExceptionInternal(ex,
31:                 ProblemDetail.forStatusAndDetail(status, "Solicitud no valida."), headers, status, request);
32:     }
33: }
```

`backend/src/main/java/cr/ac/ucr/if0009/insecurepay/util/JwtUtil.java`, líneas 17-26:

```text
17:     public JwtUtil(@Value("${jwt.secret}") String secret,
18:                    @Value("${jwt.expiration-ms}") long expirationMs) {
19:         byte[] decoded = Decoders.BASE64.decode(secret);
20:         if (decoded.length < 32) {
21:             throw new IllegalArgumentException("JWT_SECRET requiere al menos 256 bits en Base64.");
22:         }
23:         this.key = Keys.hmacShaKeyFor(decoded);
24:         this.expirationMs = expirationMs;
25:     }
26: 
```

### Capturas manuales

- Antes (`evidencias/05_Hardening/antes.png`): PENDIENTE: INSERTAR CAPTURA REAL.

- Después (`evidencias/05_Hardening/despues.png`): PENDIENTE: INSERTAR CAPTURA REAL.

- Código (`evidencias/05_Hardening/codigo_corregido.png`): PENDIENTE: INSERTAR CAPTURA REAL.

Las salidas reales ya incluidas cumplen la alternativa de evidencia textual descrita por el laboratorio. La guía explica cómo obtener las imágenes sin reproducir el estado vulnerable.

## Verificación general y límites

`mvnw.cmd clean test` terminó con 18 pruebas, 0 fallos, 0 errores y 0 omitidas. `npm.cmd run build` terminó correctamente. Se verificaron por navegador el login, la consulta propia, el bloqueo de una cuenta ajena, una transferencia propia de 1 CRC, la búsqueda normal y el comprobante. Los resultados se guardaron en `evidencias/validacion-maven.txt`, `validacion-angular.txt` y `flujo-normal-navegador.txt`.

Las transferencias de prueba modificaron únicamente la base local en memoria. Los IDs de comprobante y saldos posteriores dependen del orden de ejecución y se reinician al detener el backend. No se presenta este laboratorio como una auditoría de seguridad completa para producción.

Pendiente manual: tomar las capturas reales indicadas, incorporarlas a la documentación y al repositorio y entregar el enlace en Mediación Virtual. La lista exacta de entrega está en `docs/ENTREGA.md`. La calificación final corresponde al profesor; la revisión documental cubre los cinco criterios que suman 100 puntos.

## Referencias

- Enunciado `laboratorio_17.pdf`, páginas 3-13, facilitado para esta tarea.
- [Repositorio docente InsecurePay](https://github.com/jonnexusinsights-lab/unsecure_web_app_lab).
- [OWASP Top 10:2021](https://owasp.org/Top10/2021/).
- [CVSS v3.1, especificación FIRST](https://www.first.org/cvss/v3.1/specification-document).
- [Spring Security: almacenamiento de contraseñas](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html).
