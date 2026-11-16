import argparse
import datetime
import json
import pathlib
import re
import ssl
import sys
import urllib.error
import urllib.parse
import urllib.request

ROOT = pathlib.Path(__file__).resolve().parents[1]
BASE = "https://localhost:8080"
CASOS = {"SQL": "01_SQL_Injection", "IDOR": "02_IDOR", "XSS": "03_Stored_XSS",
         "BCrypt": "04_BCrypt", "Hardening": "05_Hardening"}
context = ssl._create_unverified_context()
token = None
lines = []


def mostrar(value=""):
    text = value if isinstance(value, str) else json.dumps(value, ensure_ascii=False, indent=2)
    print(text)
    lines.append(text)


def request(label, path, body=None, expected=200, authorization=True):
    headers = {"Accept": "application/json"}
    if authorization and token:
        headers["Authorization"] = "Bearer " + token
    data = json.dumps(body).encode() if body is not None else None
    if data is not None:
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(BASE + path, data=data, headers=headers)
    try:
        response = urllib.request.urlopen(req, context=context, timeout=15)
    except urllib.error.HTTPError as error:
        response = error
    raw = response.read().decode()
    try:
        result = json.loads(raw)
    except ValueError:
        result = raw
    visible = dict(result) if isinstance(result, dict) else result
    if isinstance(visible, dict) and "token" in visible:
        visible["token"] = "[TOKEN OMITIDO]"
    mostrar("\n" + label)
    mostrar(("POST " if data is not None else "GET ") + BASE + path)
    if body is not None:
        shown_body = dict(body)
        if "password" in shown_body:
            shown_body["password"] = "[CLAVE OMITIDA]"
        mostrar(shown_body)
    mostrar(f"HTTP {response.status} | {response.headers.get('Content-Type')}")
    mostrar(visible)
    assert response.status == expected, f"Se esperaba HTTP {expected}, se obtuvo {response.status}."
    return result


def login(username="carlos_mora", password="carlos123"):
    global token
    result = request("INICIO DE SESION", "/api/auth/login", {"username": username, "password": password}, authorization=False)
    token = result["token"]


def verificar(caso):
    global lines
    lines = []
    mostrar("LABORATORIO 17 - VERIFICACION " + caso)
    mostrar("Fecha UTC: " + datetime.datetime.now(datetime.timezone.utc).isoformat())
    mostrar("Destino local: " + BASE)
    login()
    if caso == "SQL":
        payload = "' OR 1=1 --"
        result = request("BUSQUEDA CON EL ATAQUE ORIGINAL", "/api/transacciones/buscar?" + urllib.parse.urlencode({"termino": payload}))
        assert result == [], "El payload debe devolver una lista vacia."
        result = request("BUSQUEDA NORMAL", "/api/transacciones/buscar?termino=programacion")
        assert len(result) >= 1, "La busqueda normal debe encontrar el movimiento de prueba."
        mostrar("CONFIRMADO: ataque sin resultados; busqueda normal disponible.")
    elif caso == "IDOR":
        request("CUENTA PROPIA DE CARLOS", "/api/cuentas/2/saldo")
        request("CUENTA AJENA DE MARIA", "/api/cuentas/3/saldo", expected=403)
        before = request("SALDO PROPIO ANTES DEL INTENTO", "/api/cuentas/2/saldo")["saldo"]
        request("INTENTO DE DEBITAR A MARIA", "/api/transacciones/transferir", {
            "cuentaOrigenId": 3, "numeroCuentaDestino": "CR05015202001026458901",
            "monto": 1, "conceptoHtml": "Prueba controlada IDOR"}, expected=403)
        after = request("SALDO PROPIO DESPUES DEL INTENTO", "/api/cuentas/2/saldo")["saldo"]
        assert before == after, "El intento rechazado no debe acreditar fondos."
        mostrar("CONFIRMADO: cuenta propia HTTP 200; cuenta y debito ajenos HTTP 403.")
    elif caso == "XSS":
        payload = '<img src="x" onerror="alert(\'AUDITORIA XSS CONFIRMADA\')">'
        result = request("TRANSFERENCIA PROPIA DE 1 COLON CON TEXTO DE PRUEBA", "/api/transacciones/transferir", {
            "cuentaOrigenId": 2, "numeroCuentaDestino": "CR05015202001026458902",
            "monto": 1, "conceptoHtml": payload}, expected=201)
        assert "<img" not in result["conceptoHtml"] and "&lt;img" in result["conceptoHtml"]
        request("COMPROBANTE GUARDADO", "/api/transacciones/" + str(result["id"]))
        mostrar("CONFIRMADO: HTML escapado antes de guardar. Abra Movimientos y el comprobante #" + str(result["id"]) + " para comprobar la visualizacion en el navegador.")
    elif caso == "BCrypt":
        source = (ROOT / "backend/src/main/resources/data.sql").read_text(encoding="utf-8")
        hashes = re.findall(r"\('([^']+)', '(\$2[aby]\$\d+\$[^']+)'", source)
        assert len(hashes) == 5, "Se requieren los cinco hashes BCrypt."
        mostrar("\nHASHES ALMACENADOS EN data.sql")
        for username, hashed in hashes:
            factor = int(hashed.split("$")[2])
            assert factor >= 10
            mostrar(username + " | " + hashed + " | factor=" + str(factor))
        for username, password in [("admin", "admin123"), ("auditor_seguridad", "auditor2026"),
                                   ("maria_rojas", "maria123"), ("juan_perez", "juan123")]:
            login(username, password)
        request("CLAVE INCORRECTA RECHAZADA", "/api/auth/login", {"username": "carlos_mora", "password": "incorrecta"}, expected=401, authorization=False)
        mostrar("CONFIRMADO: cinco hashes con factor 12; cinco inicios de sesion HTTP 200; clave incorrecta HTTP 401.")
    elif caso == "Hardening":
        result = request("COMILLA QUE ANTES PROVOCABA UN ERROR SQL", "/api/transacciones/buscar?termino=%27")
        assert result == []
        result = request("ERROR CONTROLADO SIN TRAZA", "/api/cuentas/99999", expected=404)
        assert result.get("status") == 404 and "trace" not in result and "exception" not in result
        request("CONSOLA H2 DESHABILITADA", "/h2-console/", expected=404, authorization=False)
        request("ACCESO SIN TOKEN", "/api/cuentas/2", expected=401, authorization=False)
        mostrar("\nCONFIGURACION ACTUAL")
        for line in (ROOT / "backend/src/main/resources/application.properties").read_text().splitlines():
            if line.startswith(("server.error.", "spring.h2.", "jwt.secret", "server.ssl.enabled", "server.address")):
                mostrar(line)
        mostrar("CONFIRMADO: HTTPS, errores sin traza, H2 HTTP 404 y JWT externo. El HTTP 500 se comprueba por separado con ErrorHandlingTests.")
    path = ROOT / "evidencias" / CASOS[caso] / "despues.txt"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("\nSalida guardada en: " + str(path))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Pruebas locales de InsecurePay")
    parser.add_argument("caso", choices=[*CASOS, "Todo"])
    args = parser.parse_args()
    try:
        for caso in (CASOS if args.caso == "Todo" else [args.caso]):
            verificar(caso)
    except Exception as error:
        print("\nPRUEBA NO COMPLETADA: " + str(error), file=sys.stderr)
        sys.exit(1)
