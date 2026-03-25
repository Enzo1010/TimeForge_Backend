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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponseDTO register(AuthRegisterRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email ja cadastrado.");
        }

        Role role = request.getRole() == null ? Role.VIEWER : request.getRole();
        if (role == Role.ADMIN && !isAdminAuthenticated()) {
            throw new BusinessRuleException(HttpStatus.FORBIDDEN, "Somente admin pode criar outro admin.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senhaHash(passwordEncoder.encode(request.getSenha()))
                .role(role)
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        String token = jwtService.generateToken(salvo);

        return buildResponse(salvo, token);
    }

    public AuthResponseDTO login(AuthLoginRequestDTO request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getSenha()
        );

        try {
            authenticationManager.authenticate(authToken);
        } catch (AuthenticationException ex) {
            throw new BusinessRuleException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas.");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas."));

        String token = jwtService.generateToken(usuario);
        return buildResponse(usuario, token);
    }

    private AuthResponseDTO buildResponse(Usuario usuario, String token) {
        return AuthResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .usuarioId(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .role(usuario.getRole().name())
                .build();
    }

    private boolean isAdminAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
