package br.com.timeforge.timeforge_api.security;

import br.com.timeforge.timeforge_api.entity.Role;
import br.com.timeforge.timeforge_api.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "uma-chave-secreta-com-pelo-menos-32-caracteres",
                120
        );
    }

    private Usuario usuario(String email, Role role) {
        return Usuario.builder()
                .id(1L)
                .nome("Test User")
                .email(email)
                .senhaHash("hashed")
                .role(role)
                .build();
    }

    @Test
    void deveGerarTokenValido() {
        Usuario user = usuario("admin@test.com", Role.ADMIN);

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void deveExtrairUsernameDoToken() {
        Usuario user = usuario("admin@test.com", Role.ADMIN);

        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);

        assertEquals("admin@test.com", username);
    }

    @Test
    void deveValidarTokenCorretamente() {
        Usuario user = usuario("admin@test.com", Role.ADMIN);

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void deveInvalidarTokenComUsuarioDiferente() {
        Usuario user1 = usuario("admin@test.com", Role.ADMIN);
        Usuario user2 = usuario("outro@test.com", Role.VIEWER);

        String token = jwtService.generateToken(user1);

        assertFalse(jwtService.isTokenValid(token, user2));
    }

    @Test
    void deveInvalidarTokenExpirado() {
        JwtService shortLivedService = new JwtService(
                "uma-chave-secreta-com-pelo-menos-32-caracteres",
                0
        );

        Usuario user = usuario("admin@test.com", Role.ADMIN);
        String token = shortLivedService.generateToken(user);

        // Token com expiração 0 minutos lança ExpiredJwtException ao ser parseado
        assertThrows(ExpiredJwtException.class, () -> shortLivedService.isTokenValid(token, user));
    }
}
