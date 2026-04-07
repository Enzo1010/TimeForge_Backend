package br.com.timeforge.timeforge_api.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthChangePasswordRequestDTO {

    @NotBlank(message = "Senha atual e obrigatoria")
    @Size(min = 6, message = "Senha atual deve ter no minimo 6 caracteres")
    private String senhaAtual;

    @NotBlank(message = "Nova senha e obrigatoria")
    @Size(min = 6, message = "Nova senha deve ter no minimo 6 caracteres")
    private String novaSenha;

    @NotBlank(message = "Confirmacao de senha e obrigatoria")
    @Size(min = 6, message = "Confirmacao de senha deve ter no minimo 6 caracteres")
    private String confirmacaoSenha;

    @AssertTrue(message = "Confirmacao de senha nao confere")
    public boolean isConfirmacaoValida() {
        if (novaSenha == null || confirmacaoSenha == null) {
            return true;
        }
        return novaSenha.equals(confirmacaoSenha);
    }
}

