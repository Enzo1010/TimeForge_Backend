package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.AuthChangePasswordRequestDTO;
import br.com.timeforge.timeforge_api.dto.request.AuthLoginRequestDTO;
import br.com.timeforge.timeforge_api.dto.request.AuthProfileUpdateRequestDTO;
import br.com.timeforge.timeforge_api.dto.request.AuthRegisterRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.AuthMeResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.AuthResponseDTO;
import br.com.timeforge.timeforge_api.entity.Role;
import br.com.timeforge.timeforge_api.entity.Usuario;
import br.com.timeforge.timeforge_api.exception.BusinessRuleException;
import br.com.timeforge.timeforge_api.exception.DuplicateResourceException;
import br.com.timeforge.timeforge_api.repository.UsuarioRepository;
import br.com.timeforge.timeforge_api.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

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

    @Test
    void deveRetornarUsuarioAutenticadoNoMe() {
        autenticarComo("admin@test.com", Role.ADMIN);

        Usuario usuario = Usuario.builder()
                .id(10L)
                .nome("Administrador")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .senhaHash("hashed")
                .build();

        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuario));

        AuthMeResponseDTO response = authService.me();

        assertEquals(10L, response.getUsuarioId());
        assertEquals("Administrador", response.getNome());
        assertEquals("admin@test.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void deveAtualizarPerfilComSucesso() {
        autenticarComo("admin@test.com", Role.ADMIN);

        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Administrador")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .senhaHash("hashed")
                .build();

        AuthProfileUpdateRequestDTO request = new AuthProfileUpdateRequestDTO(
                "Admin Atualizado",
                "admin.atualizado@test.com"
        );

        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("admin.atualizado@test.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthMeResponseDTO response = authService.updateProfile(request);

        assertEquals("Admin Atualizado", response.getNome());
        assertEquals("admin.atualizado@test.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void deveLancarConflictAoAtualizarPerfilComEmailDuplicado() {
        autenticarComo("admin@test.com", Role.ADMIN);

        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Administrador")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .senhaHash("hashed")
                .build();

        AuthProfileUpdateRequestDTO request = new AuthProfileUpdateRequestDTO(
                "Admin",
                "ja.existe@test.com"
        );

        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("ja.existe@test.com")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> authService.updateProfile(request)
        );

        assertEquals("Email ja cadastrado.", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveAlterarSenhaComSucesso() {
        autenticarComo("admin@test.com", Role.ADMIN);

        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Administrador")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .senhaHash("senha-antiga-hash")
                .build();

        AuthChangePasswordRequestDTO request = new AuthChangePasswordRequestDTO(
                "senhaAntiga",
                "senhaNova123",
                "senhaNova123"
        );

        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaAntiga", "senha-antiga-hash")).thenReturn(true);
        when(passwordEncoder.matches("senhaNova123", "senha-antiga-hash")).thenReturn(false);
        when(passwordEncoder.encode("senhaNova123")).thenReturn("senha-nova-hash");

        authService.changePassword(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("senha-nova-hash", captor.getValue().getSenhaHash());
    }

    @Test
    void deveLancarUnauthorizedQuandoSenhaAtualInvalidaNoChangePassword() {
        autenticarComo("admin@test.com", Role.ADMIN);

        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Administrador")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .senhaHash("senha-antiga-hash")
                .build();

        AuthChangePasswordRequestDTO request = new AuthChangePasswordRequestDTO(
                "senhaErrada",
                "senhaNova123",
                "senhaNova123"
        );

        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaErrada", "senha-antiga-hash")).thenReturn(false);

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> authService.changePassword(request)
        );

        assertEquals(401, ex.getStatus().value());
        assertEquals("Senha atual invalida.", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarBadRequestQuandoNovaSenhaIgualAtual() {
        autenticarComo("admin@test.com", Role.ADMIN);

        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Administrador")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .senhaHash("senha-antiga-hash")
                .build();

        AuthChangePasswordRequestDTO request = new AuthChangePasswordRequestDTO(
                "senhaAntiga",
                "senhaAntiga",
                "senhaAntiga"
        );

        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaAntiga", "senha-antiga-hash")).thenReturn(true);
        when(passwordEncoder.matches("senhaAntiga", "senha-antiga-hash")).thenReturn(true);

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> authService.changePassword(request)
        );

        assertEquals(400, ex.getStatus().value());
        assertEquals("A nova senha deve ser diferente da senha atual.", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    private void autenticarComo(String email, Role role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
