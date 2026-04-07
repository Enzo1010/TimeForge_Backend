package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.request.AuthChangePasswordRequestDTO;
import br.com.timeforge.timeforge_api.dto.request.AuthLoginRequestDTO;
import br.com.timeforge.timeforge_api.dto.request.AuthProfileUpdateRequestDTO;
import br.com.timeforge.timeforge_api.dto.request.AuthRegisterRequestDTO;
import br.com.timeforge.timeforge_api.dto.request.ForgotPasswordRequestDTO;
import br.com.timeforge.timeforge_api.dto.request.ResetPasswordRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.AuthMeResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.AuthResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.SimpleMessageResponseDTO;
import br.com.timeforge.timeforge_api.service.AuthService;
import br.com.timeforge.timeforge_api.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO register(@RequestBody @Valid AuthRegisterRequestDTO request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody @Valid AuthLoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public SimpleMessageResponseDTO forgotPassword(@RequestBody @Valid ForgotPasswordRequestDTO request) {
        passwordResetService.requestPasswordReset(request.getEmail());
        return new SimpleMessageResponseDTO("Um link de redefinição foi enviado para o seu email.");
    }

    @PostMapping("/reset-password")
    public SimpleMessageResponseDTO resetPassword(@RequestBody @Valid ResetPasswordRequestDTO request) {
        passwordResetService.resetPassword(request.getToken(), request.getNovaSenha());
        return new SimpleMessageResponseDTO("Senha redefinida com sucesso.");
    }
    
    @GetMapping("/me")
    public AuthMeResponseDTO me() {
        return authService.me();
    }

    @PutMapping("/profile")
    public AuthMeResponseDTO updateProfile(@RequestBody @Valid AuthProfileUpdateRequestDTO request) {
        return authService.updateProfile(request);
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody @Valid AuthChangePasswordRequestDTO request) {
        authService.changePassword(request);
    }
}
