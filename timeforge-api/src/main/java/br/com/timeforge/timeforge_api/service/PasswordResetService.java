package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.entity.PasswordResetToken;
import br.com.timeforge.timeforge_api.entity.Usuario;
import br.com.timeforge.timeforge_api.exception.BusinessRuleException;
import br.com.timeforge.timeforge_api.repository.PasswordResetTokenRepository;
import br.com.timeforge.timeforge_api.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:9000}")
    private String frontendUrl;

    @Value("${password-reset.token.expiration-minutes:30}")
    private int expirationMinutes;

    public PasswordResetService(
            UsuarioRepository usuarioRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);
        if (usuarioOptional.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioOptional.get();
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .usuario(usuario)
                .token(token)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        String resetLink = buildResetLink(token);

        String subject = "Redefinição de senha TimeForge";
        String body = "<html><body>"
                + "<div style=\"font-family: Arial, sans-serif; color: #222222;\">"
                + "<h2 style=\"color: #3b82f6;\">Redefinição de senha TimeForge</h2>"
                + "<p>Olá <strong>" + usuario.getNome() + "</strong>,</p>"
                + "<p>Recebemos uma solicitação para redefinir sua senha. Clique no botão abaixo para criar uma nova senha:</p>"
                + "<p><a href=\"" + resetLink + "\" style=\"display: inline-block; padding: 12px 20px; background-color: #3b82f6; color: #ffffff; text-decoration: none; border-radius: 6px;\">Redefinir senha</a></p>"
                + "<p style=\"font-size: 14px; color: #6b7280;\">Se o botão não funcionar, cole este link no seu navegador:</p>"
                + "<p style=\"word-break: break-word;\"><a href=\"" + resetLink + "\">" + resetLink + "</a></p>"
                + "<p style=\"font-size: 14px; color: #6b7280;\">Esse link expira em <strong>" + expirationMinutes + " minutos</strong>.</p>"
                + "<hr style=\"border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;\">"
                + "<p style=\"font-size: 13px; color: #9ca3af;\">Se você não solicitou esta alteração, apenas ignore este e-mail.</p>"
                + "</div></body></html>";

        tokenRepository.save(resetToken);

        try {
            emailService.sendEmail(usuario.getEmail(), subject, body);
        } catch (RuntimeException ex) {
            log.error("Falha ao enviar email de redefinicao para {}", usuario.getEmail(), ex);
            throw new BusinessRuleException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Nao foi possivel enviar o e-mail de redefinicao no momento. Tente novamente."
            );
        }
    }

    @Transactional
    public void resetPassword(String token, String novaSenha) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.BAD_REQUEST, "Token de redefinição de senha inválido ou expirado."));

        if (resetToken.isUsed()) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "Token de redefinição de senha já foi usado.");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "Token de redefinição de senha expirou.");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    private String buildResetLink(String token) {
        String normalizedFrontendUrl = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;

        return normalizedFrontendUrl + "/#/resetar-senha?token=" + token;
    }
}
