package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.AuthLoginRequestDTO;
import br.com.timeforge.timeforge_api.dto.request.AuthRegisterRequestDTO;
import br.com.timeforge.timeforge_api.dto.request.AuthProfileUpdateRequestDTO;
import br.com.timeforge.timeforge_api.dto.request.AuthChangePasswordRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.AuthMeResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.AuthResponseDTO;
import br.com.timeforge.timeforge_api.entity.Role;
import br.com.timeforge.timeforge_api.entity.Usuario;
import br.com.timeforge.timeforge_api.exception.BusinessRuleException;
import br.com.timeforge.timeforge_api.exception.DuplicateResourceException;
import br.com.timeforge.timeforge_api.repository.UsuarioRepository;
import br.com.timeforge.timeforge_api.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
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
        log.info("Solicitacao de registro recebida para email={}", request.getEmail());
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email ja cadastrado.");
        }

        Role role = request.getRole() == null ? Role.VIEWER : request.getRole();
        if (role == Role.ADMIN && !isAdminAuthenticated()) {
            log.warn("Tentativa de criar usuario ADMIN sem autenticacao ADMIN: email={}", request.getEmail());
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
        log.info("Usuario registrado com sucesso: id={}, email={}, role={}", salvo.getId(), salvo.getEmail(), salvo.getRole());

        return buildResponse(salvo, token);
    }

    public AuthResponseDTO login(AuthLoginRequestDTO request) {
        log.info("Tentativa de login para email={}", request.getEmail());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getSenha()
        );

        try {
            authenticationManager.authenticate(authToken);
        } catch (AuthenticationException ex) {
            log.warn("Falha de autenticacao para email={}", request.getEmail());
            throw new BusinessRuleException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas.");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas."));

        String token = jwtService.generateToken(usuario);
        log.info("Login realizado com sucesso: usuarioId={}, email={}, role={}", usuario.getId(), usuario.getEmail(), usuario.getRole());
        return buildResponse(usuario, token);
    }

    public AuthMeResponseDTO me() {
        Usuario usuario = getAuthenticatedUsuario();
        return toMeResponse(usuario);
    }

    public AuthMeResponseDTO updateProfile(AuthProfileUpdateRequestDTO request) {
        Usuario usuario = getAuthenticatedUsuario();

        String nome = safeTrim(request.getNome());
        String email = safeTrim(request.getEmail());

        if (!usuario.getEmail().equalsIgnoreCase(email) && usuarioRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email ja cadastrado.");
        }

        usuario.setNome(nome);
        usuario.setEmail(email);

        Usuario atualizado = usuarioRepository.save(usuario);
        return toMeResponse(atualizado);
    }

    public void changePassword(AuthChangePasswordRequestDTO request) {
        Usuario usuario = getAuthenticatedUsuario();

        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenhaHash())) {
            throw new BusinessRuleException(HttpStatus.UNAUTHORIZED, "Senha atual invalida.");
        }

        if (passwordEncoder.matches(request.getNovaSenha(), usuario.getSenhaHash())) {
            throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "A nova senha deve ser diferente da senha atual."
            );
        }

        usuario.setSenhaHash(passwordEncoder.encode(request.getNovaSenha()));
        usuarioRepository.save(usuario);
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

    private AuthMeResponseDTO toMeResponse(Usuario usuario) {
        return AuthMeResponseDTO.builder()
                .usuarioId(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .role(usuario.getRole().name())
                .build();
    }

    private Usuario getAuthenticatedUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessRuleException(HttpStatus.UNAUTHORIZED, "Nao autenticado.");
        }

        Object principal = authentication.getPrincipal();
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = authentication.getName();
        }

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.UNAUTHORIZED, "Usuario autenticado nao encontrado."));
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
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
