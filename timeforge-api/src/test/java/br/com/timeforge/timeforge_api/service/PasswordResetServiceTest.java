package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.entity.PasswordResetToken;
import br.com.timeforge.timeforge_api.entity.Role;
import br.com.timeforge.timeforge_api.entity.Usuario;
import br.com.timeforge.timeforge_api.repository.PasswordResetTokenRepository;
import br.com.timeforge.timeforge_api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:9000");
    }

    @Test
    void deveEnviarEmailQuandoUsuarioExistir() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Lucas")
                .email("lucas@example.com")
                .senhaHash("hash")
                .role(Role.VIEWER)
                .build();

        when(usuarioRepository.findByEmail("lucas@example.com")).thenReturn(Optional.of(usuario));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        passwordResetService.requestPasswordReset("lucas@example.com");

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendEmail(
                eq("lucas@example.com"),
                eq("Redefinição de senha TimeForge"),
                contains("resetar-senha?token=")
        );
    }

    @Test
    void naoDeveEnviarEmailQuandoUsuarioNaoExistir() {
        when(usuarioRepository.findByEmail("vazio@example.com")).thenReturn(Optional.empty());

        passwordResetService.requestPasswordReset("vazio@example.com");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }
}
