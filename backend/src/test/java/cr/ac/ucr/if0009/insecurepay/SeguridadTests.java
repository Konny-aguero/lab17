package cr.ac.ucr.if0009.insecurepay;

import com.fasterxml.jackson.databind.ObjectMapper;
import cr.ac.ucr.if0009.insecurepay.repository.CuentaRepository;
import cr.ac.ucr.if0009.insecurepay.repository.UsuarioRepository;
import cr.ac.ucr.if0009.insecurepay.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadTests extends TestEnvironment {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JwtUtil jwt;
    @Autowired CuentaRepository cuentas;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;

    String auth(String username) {
        var usuario = usuarios.findByUsername(username).orElseThrow();
        return "Bearer " + jwt.generateToken(username, usuario.getRol().name(), usuario.getId());
    }

    String transferencia(long origen, String destino, String concepto) throws Exception {
        return json.writeValueAsString(Map.of("cuentaOrigenId", origen,
                "numeroCuentaDestino", destino, "monto", 1, "conceptoHtml", concepto));
    }

    @Test
    void sqlSeTrataComoTextoYBusquedaLegitimaFunciona() throws Exception {
        mvc.perform(get("/api/transacciones/buscar").param("termino", "' OR 1=1 --")
                .header("Authorization", auth("carlos_mora")))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/api/transacciones/buscar").param("termino", "programacion")
                .header("Authorization", auth("carlos_mora")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/transacciones/buscar").param("termino", "'")
                .header("Authorization", auth("carlos_mora")))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void cuentaPropiaPermitidaYAjenaBloqueada() throws Exception {
        String token = auth("carlos_mora");
        mvc.perform(get("/api/cuentas/2/saldo").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.saldo").value(250000));
        for (String path : new String[]{"/api/cuentas/3", "/api/cuentas/3/saldo", "/api/cuentas/usuario/4", "/api/transacciones/cuenta/3", "/api/transacciones/2"}) {
            mvc.perform(get(path).header("Authorization", token)).andExpect(status().isForbidden())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        }
    }

    @Test
    void listadosYBusquedaNoEludenAutorizacion() throws Exception {
        String token = auth("carlos_mora");
        mvc.perform(get("/api/cuentas").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
        mvc.perform(get("/api/transacciones").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].id", not(hasItem(2))));
        mvc.perform(get("/api/transacciones/buscar").param("termino", "COMPLETADA").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].id", not(hasItem(2))));
    }

    @Test
    void transferenciaAjenaNoModificaSaldos() throws Exception {
        var antes = cuentas.findById(3L).orElseThrow().getSaldo();
        mvc.perform(post("/api/transacciones/transferir").header("Authorization", auth("carlos_mora"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(transferencia(3, "CR05015202001026458901", "IDOR")))
                .andExpect(status().isForbidden());
        assertEquals(antes, cuentas.findById(3L).orElseThrow().getSaldo());
    }

    @Test
    void transferenciaPropiaEscapaHtmlYPreservaSaldoTotal() throws Exception {
        var origen = cuentas.findById(2L).orElseThrow().getSaldo();
        var destino = cuentas.findById(3L).orElseThrow().getSaldo();
        mvc.perform(post("/api/transacciones/transferir").header("Authorization", auth("carlos_mora"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(transferencia(2, "CR05015202001026458902", "<img src=x onerror=alert(1)>")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.conceptoHtml").value("&lt;img src=x onerror=alert(1)&gt;"));
        assertEquals(origen.subtract(java.math.BigDecimal.ONE), cuentas.findById(2L).orElseThrow().getSaldo());
        assertEquals(destino.add(java.math.BigDecimal.ONE), cuentas.findById(3L).orElseThrow().getSaldo());
    }

    @ParameterizedTest
    @CsvSource({"admin,admin123", "auditor_seguridad,auditor2026", "carlos_mora,carlos123", "maria_rojas,maria123", "juan_perez,juan123"})
    void hashesBcryptYLoginValidos(String username, String password) throws Exception {
        String hash = usuarios.findByUsername(username).orElseThrow().getPassword();
        assertTrue(hash.startsWith("$2a$12$"));
        assertNotEquals(password, hash);
        assertTrue(encoder.matches(password, hash));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.password").doesNotExist());
        System.out.println("BCRYPT_VERIFICADO: " + username + " | factor 12 | matches=true | login HTTP 200");
    }

    @Test
    void credencialesIncorrectasDevuelvenErrorGenerico() throws Exception {
        for (String username : new String[]{"carlos_mora", "desconocido"}) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(Map.of("username", username, "password", "incorrecta"))))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.detail").value("Credenciales invalidas."))
                    .andExpect(jsonPath("$.trace").doesNotExist());
        }
    }

    @Test
    void tokensAusentesInvalidosYVencidosNoAutorizan() throws Exception {
        mvc.perform(get("/api/cuentas/2")).andExpect(status().isUnauthorized());
        String expired = Jwts.builder().subject("carlos_mora").expiration(new Date(0))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)), Jwts.SIG.HS256).compact();
        String forged = Jwts.builder().subject("admin").signWith(Jwts.SIG.HS256.key().build()).compact();
        for (String token : new String[]{"invalido", expired, forged}) {
            mvc.perform(get("/api/cuentas/3").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.trace").doesNotExist());
        }
    }

    @ParameterizedTest
    @CsvSource({"admin", "auditor_seguridad"})
    void auditoriaPermiteLecturaPeroNoDebitosAjenos(String username) throws Exception {
        mvc.perform(get("/api/cuentas/3/saldo").header("Authorization", auth(username))).andExpect(status().isOk());
        mvc.perform(post("/api/transacciones/transferir").header("Authorization", auth(username))
                .contentType(MediaType.APPLICATION_JSON)
                .content(transferencia(3, "CR05015202001026458901", "IDOR"))).andExpect(status().isForbidden());
    }

    @Test
    void datosInvalidosYH2NoExponenDetalles() throws Exception {
        mvc.perform(post("/api/transacciones/transferir").header("Authorization", auth("carlos_mora"))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.trace").doesNotExist());
        mvc.perform(get("/h2-console/")).andExpect(status().isNotFound());
    }

    @Test
    void secretoCortoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("YWJj", 1000));
    }
}
