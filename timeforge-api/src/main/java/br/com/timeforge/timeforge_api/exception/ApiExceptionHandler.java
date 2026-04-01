package br.com.timeforge.timeforge_api.exception;

import br.com.timeforge.timeforge_api.dto.response.ApiCampoErroResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ApiErroResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErroResponseDTO> handleEntityNotFoundException(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Entidade não encontrada: path={}, mensagem={}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request,
                List.of()
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErroResponseDTO> handleDuplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request
    ) {
        log.warn("Conflito por recurso duplicado: path={}, mensagem={}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request,
                List.of()
        );
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErroResponseDTO> handleBusinessRuleException(
            BusinessRuleException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Violação de regra de negócio: path={}, status={}, mensagem={}",
                request.getRequestURI(),
                ex.getStatus().value(),
                ex.getMessage()
        );
        return buildResponse(
                ex.getStatus().value(),
                ex.getMessage(),
                request,
                List.of()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErroResponseDTO> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        String mensagem = ex.getReason() == null || ex.getReason().isBlank()
                ? "Erro na requisicao."
                : ex.getReason();
        log.warn(
                "ResponseStatusException: path={}, status={}, mensagem={}",
                request.getRequestURI(),
                ex.getStatusCode().value(),
                mensagem
        );

        return buildResponse(
                ex.getStatusCode().value(),
                mensagem,
                request,
                List.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErroResponseDTO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ApiCampoErroResponseDTO> errosValidacao = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errosValidacao.add(new ApiCampoErroResponseDTO(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            ));
        }

        ex.getBindingResult().getGlobalErrors().forEach(globalError ->
                errosValidacao.add(new ApiCampoErroResponseDTO(
                        globalError.getObjectName(),
                        globalError.getDefaultMessage()
                ))
        );

        log.warn(
                "Falha de validação no corpo da requisição: path={}, totalErros={}",
                request.getRequestURI(),
                errosValidacao.size()
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Falha de validação nos campos da requisição.",
                request,
                errosValidacao
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErroResponseDTO> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ApiCampoErroResponseDTO> errosValidacao = ex.getConstraintViolations()
                .stream()
                .map(violation -> new ApiCampoErroResponseDTO(
                        getUltimoNoCaminho(violation.getPropertyPath().toString()),
                        violation.getMessage()
                ))
                .toList();

        log.warn(
                "Falha de validação em parâmetros: path={}, totalErros={}",
                request.getRequestURI(),
                errosValidacao.size()
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Falha de validação nos parâmetros da requisição.",
                request,
                errosValidacao
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErroResponseDTO> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String mensagem = "Parametro '" + ex.getName() + "' com valor invalido: '" + ex.getValue() + "'.";
        log.warn("Parâmetro inválido: path={}, mensagem={}", request.getRequestURI(), mensagem);

        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                mensagem,
                request,
                List.of()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErroResponseDTO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn("JSON invalido ou mal formatado: path={}", request.getRequestURI());
        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Corpo da requisição invalido ou mal formatado.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErroResponseDTO> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.error(
                "Violacao de integridade de dados: path={}, causa={}",
                request.getRequestURI(),
                ex.getMostSpecificCause() == null ? ex.getMessage() : ex.getMostSpecificCause().getMessage()
        );
        return buildResponse(
                HttpStatus.CONFLICT.value(),
                "Violação de integridade de dados. Verifique duplicidade ou referências em uso.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErroResponseDTO> handleException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Erro interno inesperado: path={}", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno inesperado.",
                request,
                List.of()
        );
    }

    private ResponseEntity<ApiErroResponseDTO> buildResponse(
            int statusCode,
            String mensagem,
            HttpServletRequest request,
            List<ApiCampoErroResponseDTO> errosValidacao
    ) {
        HttpStatus resolvedStatus = HttpStatus.resolve(statusCode);
        String erro = resolvedStatus == null ? "Erro" : resolvedStatus.getReasonPhrase();

        ApiErroResponseDTO response = ApiErroResponseDTO.builder()
                .timestamp(Instant.now())
                .status(statusCode)
                .erro(erro)
                .mensagem(mensagem)
                .path(request.getRequestURI())
                .errosValidacao(errosValidacao)
                .build();

        return ResponseEntity.status(HttpStatusCode.valueOf(statusCode)).body(response);
    }

    private String getUltimoNoCaminho(String path) {
        if (path == null || path.isBlank()) {
            return "parametro";
        }

        int index = path.lastIndexOf('.');
        if (index < 0 || index == path.length() - 1) {
            return path;
        }

        return path.substring(index + 1);
    }
}
