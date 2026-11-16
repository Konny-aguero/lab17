# Guía para tomar todas las capturas - Laboratorio 17

## Antes de empezar

La programación ya está corregida. Las pruebas reales y sus salidas están guardadas. Esta guía es para obtener imágenes de esas evidencias. Repetir las pruebas de la versión corregida es opcional; las ejecuciones verificadas se realizaron el 14/11/2026 y esta guía se revisó el 16/11/2026.

Carpeta del proyecto:

```text
C:\Users\konny\Desktop\Software IV\laboratorio17
```

No hay archivos `antes.png`, `despues.png` ni `codigo_corregido.png` creados artificialmente. Tú los guardarás. Para el ANTES se usarán los registros históricos; no debes cambiar de versión ni deshacer correcciones. No hay una captura de la antigua alerta XSS: sí hay un registro auténtico que confirma que apareció. No sería correcto fabricar una ventana de alerta ahora.

### 1. Abrir el proyecto

1. Abre Visual Studio Code.
2. En el menú superior selecciona **Archivo > Abrir carpeta**.
3. Pega `C:\Users\konny\Desktop\Software IV\laboratorio17` y selecciona **Seleccionar carpeta**.
4. En el panel izquierdo deben verse `backend`, `frontend`, `docs`, `evidencias` y `scripts`.
5. Para abrir cualquier archivo mencionado abajo, presiona **Ctrl+P**, pega su ruta relativa y pulsa **Enter**. Para ir a una línea, presiona **Ctrl+G**, escribe el número y pulsa **Enter**.

### Ruta recomendada: usar los registros que ya existen

No necesitas arrancar la aplicación ni repetir pruebas para convertir las evidencias válidas en capturas. Sigue el apartado 1 para abrir VS Code y el apartado 4 para guardar imágenes. Después recorre las cinco vulnerabilidades en orden.

Para cada ANTES abre el archivo indicado en su apartado A. Para el DESPUÉS puedes abrir estos registros ya obtenidos:

- SQL: `evidencias/01_SQL_Injection/despues.txt`; busca `BUSQUEDA CON EL ATAQUE ORIGINAL`, HTTP 200 y `[]`.
- IDOR: `evidencias/02_IDOR/despues.txt`; captura por separado el HTTP 403 de la cuenta ajena y el HTTP 403 del débito ajeno.
- XSS: `evidencias/03_Stored_XSS/despues-navegador.txt`; debe verse `alertas: []` e `imagenesInyectadas: 0`. La captura de este registro es evidencia textual del navegador, no una foto antigua de su interfaz.
- BCrypt: `evidencias/04_BCrypt/prueba-maven.txt`; muestra los cinco `BCRYPT_VERIFICADO`, factor 12, `matches=true` y `BUILD SUCCESS`.
- Hardening: `evidencias/05_Hardening/prueba-maven.txt`; muestra `ERROR_500_VERIFICADO`, estado 500 y el mensaje genérico. Agrega una captura de la sección `CONSOLA H2 DESHABILITADA` de `despues.txt`, con HTTP 404.

Guarda cada imagen principal como `despues.png` en su carpeta. Usa `despues_transferencia.png` para el débito IDOR y `despues_h2.png` para H2. No edites los resultados. Los archivos contienen el origen y/o la fecha de las pruebas.

Si quieres capturar la interfaz actual de la aplicación, sigue también los apartados 2 y 3 y las instrucciones de ejecución de cada apartado B. Esas repeticiones no son necesarias para validar lo ya documentado. El ANTES siempre se captura desde los registros preservados.

### 2. Preparar las ventanas (solo para capturas nuevas de la aplicación)

Se necesitan tres terminales PowerShell. Abre una desde el botón Inicio de Windows, escribiendo **PowerShell** y pulsando Enter. Abre otras dos de la misma manera. Las llamaremos A, B y C.

Si los servidores ya están abiertos y puedes entrar a `https://localhost:4200/login` e iniciar sesión, déjalos funcionando y pasa a la terminal C. No arranques otra copia.

En la terminal A pega estas dos líneas y pulsa Enter:

```powershell
cd 'C:\Users\konny\Desktop\Software IV\laboratorio17'
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\iniciar-backend.ps1
```

Espera a ver `Started InsecurePayApplication` y una referencia al puerto 8080 con HTTPS. Deja esta ventana abierta. La primera ejecución puede tardar mientras descarga dependencias.

En la terminal B:

```powershell
cd 'C:\Users\konny\Desktop\Software IV\laboratorio17\frontend'
npm.cmd ci
npm.cmd start
```

Espera a ver `https://localhost:4200/`. Deja esta ventana abierta.

En la terminal C:

```powershell
cd 'C:\Users\konny\Desktop\Software IV\laboratorio17'
```

Todos los comandos de pruebas que aparecen más adelante se ejecutan en C, salvo que se indique lo contrario. No escribas comandos encima de un servidor que sigue ejecutándose.

### 3. Abrir la aplicación

1. Abre Microsoft Edge o Chrome.
2. Escribe `https://localhost:4200/login` en la barra de direcciones.
3. Si aparece una advertencia del certificado, verifica que la dirección sea exactamente **localhost:4200**. Pulsa **Avanzado** y luego **Continuar a localhost (no seguro)**. El nombre puede variar ligeramente entre Edge y Chrome. El aviso se debe al certificado local del laboratorio.
4. En Usuario escribe `carlos_mora`; en Contraseña escribe `carlos123`.
5. Pulsa **Ingresar a la Plataforma**.
6. Debe aparecer Carlos Mora y su cuenta `CR05015202001026458901`. El saldo exacto puede variar por las transferencias de prueba de 1 colón.

### 4. Cómo guardar cada imagen

1. Deja visible el contenido indicado, el nombre del archivo o la dirección de la página y los resultados relevantes. Agranda la ventana; no hace falta mostrar toda la pantalla.
2. Presiona **Windows+Mayús+S** y selecciona con el ratón el área que quieres capturar.
3. Abre la notificación de **Recortes** que aparece después. Si no aparece, abre Recortes desde Inicio y pulsa **Nuevo** para repetir la selección.
4. Pulsa **Ctrl+S**. Navega a la carpeta de evidencia indicada y escribe el nombre exacto, por ejemplo `antes.png`.
5. Si hace falta una segunda imagen porque no cabe todo, usa el nombre adicional indicado. No reemplaces una imagen importante con otra diferente.
6. No edites los resultados, no pegues ventanas falsas y no incluyas tokens. Los comandos preparados ocultan los tokens automáticamente.

Si una prueba muestra `PRUEBA NO COMPLETADA`, un error rojo, o un resultado distinto al esperado, no la presentes como exitosa. Comprueba que A y B sigan abiertos, que la URL use HTTPS y que el usuario sea Carlos. Conserva la salida para revisar el error. Reiniciar el backend restaura saldos y elimina comprobantes creados, por lo que debes repetir únicamente la prueba del DESPUÉS que necesites.

## 1. SQL Injection

### A. Captura del ANTES

1. En VS Code pulsa Ctrl+P, escribe `evidencias/01_SQL_Injection/antes.txt` y pulsa Enter.
2. Este archivo contiene evidencia obtenida antes de las correcciones. No es necesario ejecutar ningún ataque anterior.
3. Busca y deja visible: HTTP 200, la búsqueda con %27+OR+1%3D1+-- y la transacción con id 2, cuentaOrigenId 3 y cuentaDestinoId 4.
4. Toma la captura justo cuando se vean esos datos y el nombre del archivo. Guarda la imagen en `evidencias/01_SQL_Injection/antes.png`.
5. Si la respuesta completa no cabe, captura primero el comando y HTTP 200. Después baja hasta el objeto `"id": 2`, con origen María y destino Juan, y guarda `antes_detalle.png` en la misma carpeta. Es el movimiento ajeno que demuestra el acceso indebido.

### B. Captura del DESPUÉS

**Puedes usar el registro existente indicado al principio de la guía. Los pasos siguientes son la alternativa opcional para obtener una ejecución nueva.**

1. En la terminal C pega este comando y pulsa Enter:

```powershell
python .\scripts\verificar.py SQL
```

2. Espera a que vuelva a aparecer la línea donde puedes escribir. El comando ejecuta pruebas contra el servidor local y guarda la salida real en `evidencias/01_SQL_Injection/despues.txt`.
3. Busca en la terminal la sección **BUSQUEDA CON EL ATAQUE ORIGINAL**. Debe mostrar HTTP 200 y `[]`, que significa que no encontró movimientos con ese texto.
4. Toma la captura cuando se vean el nombre de la prueba, la solicitud con `%27+OR+1%3D1+--`, HTTP 200 y `[]`. Guárdala en `evidencias/01_SQL_Injection/despues.png`.
5. Más abajo, **BUSQUEDA NORMAL** debe mostrar al menos un movimiento de programación. Puedes guardar una segunda imagen como `despues_busqueda_normal.png`.
6. Si la pantalla se desplazó, abre `evidencias/01_SQL_Injection/despues.txt` en VS Code y captura las mismas secciones. No cambies el contenido.

### C. Código corregido

1. En VS Code abre los archivos siguientes con Ctrl+P. Usa Ctrl+G para ir a la primera línea indicada. No modifiques el código.

- Archivo `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/repository/TransaccionRepository.java`. Muestra las líneas **12-22**. Puedes encontrar la zona con Ctrl+F y el texto `@Query(`.
- Archivo `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/TransaccionService.java`. Muestra las líneas **62-68**. Puedes encontrar la zona con Ctrl+F y el texto `public List<TransaccionDto> buscarPorConcepto`.

2. El cambio que debes mostrar es: La consulta JPQL usa :termino, :usuarioId y :auditor con @Param. CAST convierte el CLOB a texto para que LOWER funcione con Hibernate 6.5; el valor de búsqueda sigue vinculado como parámetro. También se limita la búsqueda a movimientos del usuario, salvo los roles de lectura previstos por el laboratorio.
3. Sí, prepara una captura del código para acompañar la bitácora. Si todo cabe, guarda `evidencias/01_SQL_Injection/codigo_corregido.png`.
4. Si son varios archivos, captura el primero como `codigo_corregido.png` y los siguientes como `codigo_corregido_2.png` y `codigo_corregido_3.png` en la misma carpeta. Cada imagen debe mostrar el nombre del archivo, los números de línea y el cambio señalado.
5. También puedes abrir `evidencias/01_SQL_Injection/codigo_corregido.txt`: contiene los fragmentos actuales con sus rutas y números de línea. Si resulta más claro, captura ese archivo sin modificarlo. No necesitas copiar ni programar nada.

## 2. IDOR en cuentas y transferencias

### A. Captura del ANTES

1. En VS Code pulsa Ctrl+P, escribe `evidencias/02_IDOR/antes.txt` y pulsa Enter.
2. Este archivo contiene evidencia obtenida antes de las correcciones. No es necesario ejecutar ningún ataque anterior.
3. Busca y deja visible: HTTP 200 con la cuenta 3 de María y su saldo; en otra captura, HTTP 201 con cuentaOrigenId 3 y monto 1.
4. Toma la captura justo cuando se vean esos datos y el nombre del archivo. Guarda la imagen en `evidencias/02_IDOR/antes.png`.
5. La primera imagen debe mostrar la consulta de María. Luego busca `TRANSFERENCIA DESDE CUENTA AJENA`, deja visible HTTP 201, `cuentaOrigenId: 3`, `cuentaDestinoId: 2` y monto 1; guarda `antes_transferencia.png` en la misma carpeta.

### B. Captura del DESPUÉS

**Puedes usar el registro existente indicado al principio de la guía. Los pasos siguientes son la alternativa opcional para obtener una ejecución nueva.**

1. En la terminal C pega este comando y pulsa Enter:

```powershell
python .\scripts\verificar.py IDOR
```

2. Espera a que vuelva a aparecer la línea donde puedes escribir. El comando ejecuta pruebas contra el servidor local y guarda la salida real en `evidencias/02_IDOR/despues.txt`.
3. La sección **CUENTA PROPIA DE CARLOS** debe mostrar HTTP 200. La sección **CUENTA AJENA DE MARIA** debe mostrar HTTP 403 y `Acceso no autorizado a cuenta ajena.`.
4. Toma la captura de la cuenta ajena con la dirección `/api/cuentas/3/saldo`, HTTP 403 y el mensaje. Guárdala en `evidencias/02_IDOR/despues.png`.
5. Busca **INTENTO DE DEBITAR A MARIA**. Debe mostrar el origen 3, monto 1 y HTTP 403. Guarda otra captura como `despues_transferencia.png`.
6. Al final deben verse los saldos propios antes y después sin cambio. Si no cabe todo, abre `evidencias/02_IDOR/despues.txt` en VS Code para desplazarte con calma.
7. Si también quieres la imagen de la aplicación: vuelve al inicio del navegador, escribe `3` en **ID de Cuenta Bancaria a Consultar** y pulsa **Consultar cuenta**. Debe aparecer el rechazo. Guarda `despues_navegador.png`. No uses admin o auditor para esta captura: esos roles sí pueden consultar por el permiso de lectura del laboratorio.

### C. Código corregido

1. En VS Code abre los archivos siguientes con Ctrl+P. Usa Ctrl+G para ir a la primera línea indicada. No modifiques el código.

- Archivo `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/AccesoService.java`. Muestra las líneas **22-47**. Puedes encontrar la zona con Ctrl+F y el texto `public Usuario autenticar`.
- Archivo `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/CuentaService.java`. Muestra las líneas **33-41**. Puedes encontrar la zona con Ctrl+F y el texto `public CuentaDto obtenerPorId`.
- Archivo `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/TransaccionService.java`. Muestra las líneas **85-88**. Puedes encontrar la zona con Ctrl+F y el texto `acceso.exigirPropietario`.

2. El cambio que debes mostrar es: AccesoService verifica firma y vencimiento del JWT y carga al usuario real desde la base de datos. CuentaService exige propiedad; ADMIN y AUDITOR conservan únicamente la excepción de lectura indicada por el PDF. Toda transferencia exige que el origen pertenezca al usuario, incluso si tiene un rol privilegiado. Se protegen también los listados y los movimientos para evitar rutas alternativas.
3. Sí, prepara una captura del código para acompañar la bitácora. Si todo cabe, guarda `evidencias/02_IDOR/codigo_corregido.png`.
4. Si son varios archivos, captura el primero como `codigo_corregido.png` y los siguientes como `codigo_corregido_2.png` y `codigo_corregido_3.png` en la misma carpeta. Cada imagen debe mostrar el nombre del archivo, los números de línea y el cambio señalado.
5. También puedes abrir `evidencias/02_IDOR/codigo_corregido.txt`: contiene los fragmentos actuales con sus rutas y números de línea. Si resulta más claro, captura ese archivo sin modificarlo. No necesitas copiar ni programar nada.

## 3. Stored XSS en comprobantes

### A. Captura del ANTES

1. En VS Code pulsa Ctrl+P, escribe `evidencias/03_Stored_XSS/antes-navegador.txt` y pulsa Enter.
2. Busca `"fase": "antes"`. Más abajo verás `"alertas"` con `AUDITORIA XSS CONFIRMADA` y `"imagenesInyectadas": 1`.
3. Toma la captura cuando se vean el nombre del archivo, la fecha, la fase y esos dos resultados. Es un registro histórico de Edge; no es una foto de la ventana emergente.
4. Guárdala en `evidencias/03_Stored_XSS/antes.png`.
5. Para mostrar también qué se guardó en la aplicación, abre `evidencias/03_Stored_XSS/antes.txt`. Busca `XSS ALMACENADO`: debe verse HTTP 201 y el concepto con `<img ... onerror=...>`. Guarda esa segunda captura como `antes_transferencia.png` en la misma carpeta.
6. No intentes provocar la antigua alerta en la versión corregida. La foto original de la alerta no existe; su registro auténtico ya es evidencia textual.

### B. Captura del DESPUÉS

**Puedes usar el registro existente indicado al principio de la guía. Los pasos siguientes son la alternativa opcional para obtener una ejecución nueva.**

1. En la terminal C pega este comando y pulsa Enter:

```powershell
python .\scripts\verificar.py XSS
```

2. Espera a que vuelva a aparecer la línea donde puedes escribir. El comando ejecuta pruebas contra el servidor local y guarda la salida real en `evidencias/03_Stored_XSS/despues.txt`.
3. Esta prueba transfiere **1 colón propio** de Carlos a María con un concepto de prueba. Debe devolver HTTP 201 y mostrar `&lt;img` dentro de `conceptoHtml`.
4. Anota el número que aparece después de `comprobante #` al final. Puede ser diferente cada vez; no uses un número tomado de una ejecución antigua.
5. En el navegador, con Carlos conectado, entra a `https://localhost:4200/transacciones`.
6. Pulsa **Restablecer** para cargar los movimientos nuevos. Localiza el número de comprobante que acabas de anotar y pulsa **Ver Comprobante** en esa fila.
7. Baja hasta **Detalle del concepto**. Debe mostrarse texto con el contenido escapado. No debe aparecer una ventana de alerta ni una imagen creada por ese concepto.
8. Toma la captura cuando se vean la URL, el número de transacción y el concepto completo. Guarda `evidencias/03_Stored_XSS/despues.png`. Una foto por sí sola no prueba la ausencia de alertas; acompáñala con `evidencias/03_Stored_XSS/despues-navegador.txt`, que registra la prueba automatizada real con cero alertas y cero imágenes inyectadas.
9. Captura también la terminal con HTTP 201 y `&lt;img`; guarda `despues_terminal.png` en la misma carpeta.
10. Si se abre una alerta, conserva la evidencia y detén la entrega de ese resultado como corregido. Si no aparece el comprobante, comprueba que no hayas reiniciado el backend después del paso 1.

### C. Código corregido

1. En VS Code abre los archivos siguientes con Ctrl+P. Usa Ctrl+G para ir a la primera línea indicada. No modifiques el código.

- Archivo `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/TransaccionService.java`. Muestra las líneas **105-107**. Puedes encontrar la zona con Ctrl+F y el texto `transaccion.setConceptoHtml`.
- Archivo `frontend/src/app/components/transacciones/transacciones.component.html`. Muestra las líneas **93-94**. Puedes encontrar la zona con Ctrl+F y el texto `{{ transaccionSeleccionada.conceptoHtml }}`.

2. El cambio que debes mostrar es: Se eliminó DomSanitizer/SafeHtml y el bypass. La plantilla usa interpolación de texto. El backend aplica HtmlUtils.htmlEscape antes de persistir, como defensa adicional. Por seguir ambas capas pedidas por el laboratorio, las entidades escapadas pueden verse como texto literal en el comprobante.
3. Sí, prepara una captura del código para acompañar la bitácora. Si todo cabe, guarda `evidencias/03_Stored_XSS/codigo_corregido.png`.
4. Si son varios archivos, captura el primero como `codigo_corregido.png` y los siguientes como `codigo_corregido_2.png` y `codigo_corregido_3.png` en la misma carpeta. Cada imagen debe mostrar el nombre del archivo, los números de línea y el cambio señalado.
5. También puedes abrir `evidencias/03_Stored_XSS/codigo_corregido.txt`: contiene los fragmentos actuales con sus rutas y números de línea. Si resulta más claro, captura ese archivo sin modificarlo. No necesitas copiar ni programar nada.

## 4. Contraseñas almacenadas en texto plano

### A. Captura del ANTES

1. En VS Code pulsa Ctrl+P, escribe `evidencias/04_BCrypt/antes.txt` y pulsa Enter.
2. Este archivo contiene evidencia obtenida antes de las correcciones. No es necesario ejecutar ningún ataque anterior.
3. Busca y deja visible: Las cinco contraseñas legibles del archivo data.sql original y la comparación equals en AuthService.
4. Toma la captura justo cuando se vean esos datos y el nombre del archivo. Guarda la imagen en `evidencias/04_BCrypt/antes.png`.
5. Si no caben el archivo de datos y la comparación, guarda primero los usuarios y claves como `antes.png`; después captura `getPassword().equals` como `antes_comparacion.png`. Estas son credenciales ficticias del laboratorio.

### B. Captura del DESPUÉS

**Puedes usar el registro existente indicado al principio de la guía. Los pasos siguientes son la alternativa opcional para obtener una ejecución nueva.**

1. En la terminal C pega este comando y pulsa Enter:

```powershell
python .\scripts\verificar.py BCrypt
```

2. Espera a que vuelva a aparecer la línea donde puedes escribir. El comando ejecuta pruebas contra el servidor local y guarda la salida real en `evidencias/04_BCrypt/despues.txt`.
3. Busca **HASHES ALMACENADOS EN data.sql**. Deben aparecer cinco usuarios con valores que empiezan por `$2a$12$` y `factor=12`. Esos valores son hashes: no son las contraseñas legibles.
4. Captura el título, los cinco usuarios y sus hashes. Guarda `evidencias/04_BCrypt/despues.png`.
5. Al final debe aparecer la confirmación de los cinco logins HTTP 200 y el rechazo de la clave incorrecta con HTTP 401. Captura esa parte como `despues_login.png`.
6. Para mostrar la comprobación real de BCrypt desde Java, abre `evidencias/04_BCrypt/prueba-maven.txt`. Deben verse las cinco líneas `BCRYPT_VERIFICADO`, `matches=true` y `BUILD SUCCESS`. Guarda `despues_prueba_bcrypt.png`.
7. Si deseas repetir esa comprobación en lugar de usar el registro existente, ejecuta en C:

```powershell
Push-Location .\backend
.\mvnw.cmd '-Dtest=SeguridadTests#hashesBcryptYLoginValidos' test
Pop-Location
```

8. Espera a `BUILD SUCCESS`. No captures una ejecución fallida como si hubiera pasado. No es necesario abrir la consola H2, que está deshabilitada.

### C. Código corregido

1. En VS Code abre los archivos siguientes con Ctrl+P. Usa Ctrl+G para ir a la primera línea indicada. No modifiques el código.

- Archivo `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/config/PasswordConfig.java`. Muestra las líneas **8-14**. Puedes encontrar la zona con Ctrl+F y el texto `@Configuration`.
- Archivo `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/AuthService.java`. Muestra las líneas **27-38**. Puedes encontrar la zona con Ctrl+F y el texto `public LoginResponse autenticar`.
- Archivo `backend/src/main/resources/data.sql`. Muestra las líneas **1-6**. Puedes encontrar la zona con Ctrl+F y el texto `INSERT INTO USUARIOS`.

2. El cambio que debes mostrar es: PasswordConfig define BCryptPasswordEncoder(12). Los cinco registros se reemplazaron por hashes con sal independiente. AuthService utiliza exclusivamente passwordEncoder.matches. HTTPS protege las credenciales en tránsito; las contraseñas no se devuelven en las respuestas ni se incluyen en los registros de pruebas. Las credenciales públicas del README corresponden solo a usuarios ficticios del laboratorio.
3. Sí, prepara una captura del código para acompañar la bitácora. Si todo cabe, guarda `evidencias/04_BCrypt/codigo_corregido.png`.
4. Si son varios archivos, captura el primero como `codigo_corregido.png` y los siguientes como `codigo_corregido_2.png` y `codigo_corregido_3.png` en la misma carpeta. Cada imagen debe mostrar el nombre del archivo, los números de línea y el cambio señalado.
5. También puedes abrir `evidencias/04_BCrypt/codigo_corregido.txt`: contiene los fragmentos actuales con sus rutas y números de línea. Si resulta más claro, captura ese archivo sin modificarlo. No necesitas copiar ni programar nada.

## 5. Fuga de trazas y configuración insegura

### A. Captura del ANTES

1. En VS Code abre `evidencias/05_Hardening/antes-traza-legible.txt` con Ctrl+P.
2. Este archivo muestra un extracto del campo `trace` de la respuesta real anterior, con los saltos de línea legibles. La respuesta completa sigue en `evidencias/05_Hardening/antes.txt` y `docs/evidencias/antes-http.txt`.
3. Toma la captura cuando se vean el nombre del archivo, `HTTP 500`, `org.hibernate` y la sentencia `SELECT`.
4. Guarda la imagen en `evidencias/05_Hardening/antes.png`. No ejecutes nuevamente la versión vulnerable.

### B. Captura del DESPUÉS

**Puedes usar el registro existente indicado al principio de la guía. Los pasos siguientes son la alternativa opcional para obtener una ejecución nueva.**

1. En la terminal C pega este comando y pulsa Enter:

```powershell
python .\scripts\verificar.py Hardening
```

2. Espera a que vuelva a aparecer la línea donde puedes escribir. El comando ejecuta pruebas contra el servidor local y guarda la salida real en `evidencias/05_Hardening/despues.txt`.
3. Busca **ERROR CONTROLADO SIN TRAZA**. Debe mostrar HTTP 404, `application/problem+json` y campos `type`, `title`, `status`, `detail` e `instance`. No debe contener `trace`, consultas SQL ni nombres de clases.
4. Toma esa captura y guarda `evidencias/05_Hardening/despues.png`.
5. Busca **CONSOLA H2 DESHABILITADA**. Debe mostrar HTTP 404 para `/h2-console/`. Guarda `despues_h2.png`.
6. Para comprobar específicamente un error 500 sin añadir una falla a la aplicación, ejecuta en C:

```powershell
Push-Location .\backend
.\mvnw.cmd '-Dtest=ErrorHandlingTests' test
Pop-Location
```

7. Espera a `BUILD SUCCESS`. Busca `ERROR_500_VERIFICADO`. Debe mostrar `status:500` y `No fue posible procesar la solicitud.` sin la consulta SQL interna.
8. Toma una captura de esa línea y del resultado exitoso. Guarda `despues_error_500.png`. Si no cabe en pantalla, puedes abrir `evidencias/05_Hardening/prueba-maven.txt`, que conserva la salida real de la ejecución ya realizada.
9. No busques `/fallo-prueba` en el navegador: solo existe dentro de la prueba de Maven. En la aplicación real no se creó un botón ni una ruta para provocar errores.

### C. Código corregido

1. En VS Code abre los archivos siguientes con Ctrl+P. Usa Ctrl+G para ir a la primera línea indicada. No modifiques el código.

- Archivo `backend/src/main/resources/application.properties`. Muestra las líneas **11-21**. Puedes encontrar la zona con Ctrl+F y el texto `spring.h2.console.enabled`.
- Archivo `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/controller/GlobalExceptionHandler.java`. Muestra las líneas **14-33**. Puedes encontrar la zona con Ctrl+F y el texto `@RestControllerAdvice`.
- Archivo `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/util/JwtUtil.java`. Muestra las líneas **17-26**. Puedes encontrar la zona con Ctrl+F y el texto `public JwtUtil`.

2. El cambio que debes mostrar es: Se desactivaron stacktrace, message, binding-errors, exception y la consola H2. @RestControllerAdvice devuelve ProblemDetail con mensajes seguros y application/problem+json. JWT_SECRET es obligatorio, se interpreta como Base64 y exige al menos 32 bytes; se firma con HS256. El script local genera una clave aleatoria si no existe y un certificado temporal no versionado. Backend y frontend quedan ligados a loopback y utilizan HTTPS.
3. Sí, prepara una captura del código para acompañar la bitácora. Si todo cabe, guarda `evidencias/05_Hardening/codigo_corregido.png`.
4. Si son varios archivos, captura el primero como `codigo_corregido.png` y los siguientes como `codigo_corregido_2.png` y `codigo_corregido_3.png` en la misma carpeta. Cada imagen debe mostrar el nombre del archivo, los números de línea y el cambio señalado.
5. También puedes abrir `evidencias/05_Hardening/codigo_corregido.txt`: contiene los fragmentos actuales con sus rutas y números de línea. Si resulta más claro, captura ese archivo sin modificarlo. No necesitas copiar ni programar nada.

## 6. Capturas generales de compilación y funcionamiento

Estas pruebas ya pasaron. Las capturas de sus registros existentes son suficientes como evidencia textual; ejecuta los comandos siguientes solo si deseas repetirlas.

### Backend y pruebas

1. En la terminal C ejecuta:

```powershell
Push-Location .\backend
.\mvnw.cmd clean test
Pop-Location
```

2. Espera a que termine. En la ejecución verificada fueron **18 pruebas, 0 fallos, 0 errores y 0 omitidas**, con `BUILD SUCCESS`.
3. Captura ese resumen y guarda `evidencias/backend_pruebas.png`.
4. Si no deseas repetirlo, abre `evidencias/validacion-maven.txt` y captura el resumen final, indicando que es el registro guardado.

### Angular

1. En C ejecuta:

```powershell
Push-Location .\frontend
npm.cmd run build
Pop-Location
```

2. Espera a `Application bundle generation complete` y a la carpeta `dist` indicada, sin errores de compilación.
3. Guarda la captura como `evidencias/angular_compilacion.png`. También existe el registro real `evidencias/validacion-angular.txt`.

### Inicio de sesión

1. Abre `https://localhost:4200/login`, introduce `carlos_mora` y `carlos123` y pulsa **Ingresar a la Plataforma**.
2. Cuando aparezcan Carlos Mora y su cuenta propia, captura la página sin mostrar la contraseña ni el almacenamiento del navegador.
3. Guarda `evidencias/login.png`.

## 7. Revisión y entrega de las imágenes

1. En VS Code abre la carpeta `evidencias`. Deben existir estas cinco carpetas:

```text
evidencias/
  01_SQL_Injection/
  02_IDOR/
  03_Stored_XSS/
  04_BCrypt/
  05_Hardening/
```

2. Revisa que cada una tenga `antes.png`, `despues.png` y `codigo_corregido.png`, además de las imágenes adicionales necesarias y los textos ya guardados.
3. Abre las imágenes para comprobar que se leen los resultados y que corresponden al archivo correcto. No renombres una imagen de otra prueba para llenar un espacio.
4. Cuando hayas guardado las fotos, puedes incorporarlas sin editar código. En la terminal C ejecuta una sola vez:

```powershell
python -m pip install --user reportlab
```

5. Después ejecuta:

```powershell
python .\scripts\generar_informes.py
```

El comando lee las fotos existentes de las cinco carpetas, reemplaza únicamente sus marcadores pendientes en `docs/BITACORA.md` y regenera `docs/BITACORA.pdf` y `docs/GUIA_CAPTURAS.pdf`. Si una foto todavía no existe, deja el marcador PENDIENTE. No crea ninguna captura. Abre el PDF y verifica que las fotos se lean; el texto editable se conserva en Markdown. Si el comando muestra un error, conserva las imágenes y el Markdown: no afirmes que el PDF se actualizó.

6. La publicación inicial del laboratorio incluye la programación, los registros y la guía. Después de añadir tus fotos, desde C ejecuta:

```powershell
git add evidencias docs/BITACORA.md docs/BITACORA.pdf docs/GUIA_CAPTURAS.pdf
git commit -m "Agregar capturas reales del laboratorio 17"
git push origin main
```

7. Si Git informa que no hay cambios, revisa si guardaste las imágenes en otra carpeta. Si pide iniciar sesión, utiliza tu cuenta de GitHub; no pegues contraseñas ni tokens en documentos o capturas.
8. Abre `https://github.com/Konny-aguero/lab17` y comprueba que se ven las imágenes nuevas. Entrega ese enlace en Mediación Virtual según las instrucciones del curso. No necesitas crear un ZIP.

## Qué queda manual

- Tomar y guardar las capturas auténticas de esta guía.
- Ejecutar el generador de informes después de guardar las fotos, para incorporarlas al Markdown y PDF.
- Subir las capturas y entregar el enlace en Mediación Virtual.

La foto original de la ventana emergente anterior no se recuperó porque no se guardó; utiliza el registro anterior auténtico del navegador. No falta repetir un ataque en una versión vulnerable para completar esa evidencia textual.

La lista exacta de archivos y enlace que debes entregar está en `docs/ENTREGA.md`.
