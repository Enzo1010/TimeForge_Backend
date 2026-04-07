package br.com.timeforge.timeforge_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthProfileUpdateRequestDTO {

    @NotBlank(message = "Nome e obrigatorio")
    private String nome;

    @NotBlank(message = "Email e obrigatorio")
    @Email(message = "Email invalido")
    private String email;
}

