package br.com.timeforge.timeforge_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErroResponseDTO {

    private Instant timestamp;
    private Integer status;
    private String erro;
    private String mensagem;
    private String path;

    @Builder.Default
    private List<ApiCampoErroResponseDTO> errosValidacao = new ArrayList<>();
}
