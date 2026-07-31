package by.nikiforova.userservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-test-secret-key-test-secret-key";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    @Test
    @DisplayName("validateToken - success")
    void validateTokenWhenTokenValidShouldReturnTrue() {
        String token = createToken(1L, "USER", "liza");

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("validateToken - invalid token")
    void validateTokenWhenTokenInvalidShouldReturnFalse() {

        assertFalse(jwtService.validateToken("not-a-token"));
    }

    @Test
    @DisplayName("parseToken - success")
    void parseTokenWhenTokenValidShouldReturnClaims() {
        String token = createToken(1L, "USER", "liza");

        Claims claims = jwtService.parseToken(token);

        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals("USER", claims.get("role", String.class));
        assertEquals("liza", claims.getSubject());
    }

    private String createToken(Long userId, String role, String login) {
        return Jwts.builder()
                .subject(login)
                .claim("userId", userId)
                .claim("role", role)
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
