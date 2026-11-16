package cr.ac.ucr.if0009.insecurepay.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        byte[] decoded = Decoders.BASE64.decode(secret);
        if (decoded.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET requiere al menos 256 bits en Base64.");
        }
        this.key = Keys.hmacShaKeyFor(decoded);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, String rol, Long usuarioId) {
        return Jwts.builder().subject(username)
                .claim("rol", rol).claim("usuarioId", usuarioId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, Jwts.SIG.HS256).compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
