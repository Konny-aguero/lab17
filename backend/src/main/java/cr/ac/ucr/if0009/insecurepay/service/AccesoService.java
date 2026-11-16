package cr.ac.ucr.if0009.insecurepay.service;

import cr.ac.ucr.if0009.insecurepay.model.Rol;
import cr.ac.ucr.if0009.insecurepay.model.Usuario;
import cr.ac.ucr.if0009.insecurepay.repository.UsuarioRepository;
import cr.ac.ucr.if0009.insecurepay.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccesoService {
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarios;

    public AccesoService(JwtUtil jwtUtil, UsuarioRepository usuarios) {
        this.jwtUtil = jwtUtil;
        this.usuarios = usuarios;
    }

    public Usuario autenticar(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticacion requerida.");
        }
        try {
            String username = jwtUtil.getClaims(header.substring(7)).getSubject();
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException();
            }
            return usuarios.findByUsername(username).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido."));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido o vencido.");
        }
    }

    public boolean puedeAuditar(Usuario usuario) {
        return usuario.getRol() == Rol.ADMIN || usuario.getRol() == Rol.AUDITOR;
    }

    public void exigirPropietario(Usuario usuario, Long propietarioId, boolean permitirAuditoria) {
        if (!usuario.getId().equals(propietarioId) && !(permitirAuditoria && puedeAuditar(usuario))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso no autorizado a cuenta ajena.");
        }
    }
}
