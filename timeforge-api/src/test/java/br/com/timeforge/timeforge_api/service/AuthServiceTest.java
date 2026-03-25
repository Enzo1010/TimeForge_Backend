package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.AuthLoginRequestDTO;
import br.com.timeforge.timeforge_api.dto.request.AuthRegisterRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.AuthResponseDTO;
import br.com.timeforge.timeforge_api.entity.Role;
import br.com.timeforge.timeforge_api.entity.Usuario;
import br.com.timeforge.timeforge_api.exception.BusinessRuleException;
import br.com.timeforge.timeforge_api.exception.DuplicateResourceException;
import br.com.timeforge.timeforge_api.repository.UsuarioRepository;
import br.com.timeforge.timeforge_api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void deveRegistrarUsuarioComSucesso() {
        AuthRegisterRequestDTO request = new AuthRegisterRequestDTO();
        request.setNome("Joao");
        request.setEmail("joao@test.com");
        request.setSenha("senha123");

        when(usuarioRepository.existsByEmail("joao@test.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hashed");

        Usuario salvo = Usuario.builder()
                .id(1L)
                .nome("Joao")
                .email("joao@test.com")
                .senhaHash("hashed")
                .role(Role.VIEWER)
                .build();
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(salvo);
        when(jwtService.generateToken(salvo)).thenReturn("jwt-token");

        AuthResponseDTO response = authService.register(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertEquals(1L, response.getUsuarioId());
        assertEquals("Joao", response.getNome());
        assertEquals("joao@test.com", response.getEmail());
        assertEquals("VIEWER", response.getRole());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals(Role.VIEWER, captor.getValue().getRole());
        assertEquals("hashed", captor.getValue().getSenhaHash());
    }

    @Test
    void deveLancarConflictQuandoEmailJaCadastrado() {
        AuthRegisterRequestDTO request = new AuthRegisterRequestDTO();
        request.setNome("Joao");
        request.setEmail("joao@test.com");
        request.setSenha("senha123");

        when(usuarioRepository.existsByEmail("joao@test.com")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(request)
        );

        assertEquals("Email ja cadastrado.", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLogarComSucesso() {
        AuthLoginRequestDTO request = new AuthLoginRequestDTO();
        request.setEmail("joao@test.com");
        request.setSenha("senha123");

        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Joao")
                .email("joao@test.com")
                .senhaHash("hashed")
                .role(Role.ADMIN)
                .build();

        when(usuarioRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("jwt-token");

        AuthResponseDTO response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("ADMIN", response.getRole());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void deveLancarUnauthorizedQuandoCredenciaisInvalidas() {
        AuthLoginRequestDTO request = new AuthLoginRequestDTO();
        request.setEmail("joao@test.com");
        request.setSenha("errada");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> authService.login(request)
        );

        assertEquals(401, ex.getStatus().value());
        verify(usuarioRepository, never()).findByEmail(any());
    }

    @Test
    void deveLancarUnauthorizedQuandoUsuarioNaoEncontradoAposAutenticacao() {
        AuthLoginRequestDTO request = new AuthLoginRequestDTO();
        request.setEmail("fantasma@test.com");
        request.setSenha("senha123");

        when(usuarioRepository.findByEmail("fantasma@test.com")).thenReturn(Optional.empty());

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> authService.login(request)
        );

        assertEquals(401, ex.getStatus().value());
    }
}
