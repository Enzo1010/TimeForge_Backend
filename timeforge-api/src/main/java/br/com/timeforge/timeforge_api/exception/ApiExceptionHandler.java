package br.com.timeforge.timeforge_api.exception;

import br.com.timeforge.timeforge_api.dto.response.ApiCampoErroResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ApiErroResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErroResponseDTO> handleEntityNotFoundException(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
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

        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Falha de validacao nos campos da requisicao.",
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

        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Falha de validacao nos parametros da requisicao.",
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
        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Corpo da requisicao invalido ou mal formatado.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErroResponseDTO> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT.value(),
                "Violacao de integridade de dados. Verifique duplicidade ou referencias em uso.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErroResponseDTO> handleException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Erro nao tratado em {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

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
