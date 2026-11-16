package cr.ac.ucr.if0009.insecurepay;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.security.SecureRandom;
import java.util.Base64;

abstract class TestEnvironment {
    static final String SECRET;
    static {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        SECRET = Base64.getEncoder().encodeToString(bytes);
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("jwt.secret", () -> SECRET);
        registry.add("server.ssl.enabled", () -> false);
        registry.add("server.ssl.key-store", () -> "");
        registry.add("server.ssl.key-store-password", () -> "");
    }
}
