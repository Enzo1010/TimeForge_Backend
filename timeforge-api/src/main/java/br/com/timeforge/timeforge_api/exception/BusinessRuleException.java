package br.com.timeforge.timeforge_api.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends RuntimeException {

    private final HttpStatus status;

    public BusinessRuleException(String message) {
        this(HttpStatus.BAD_REQUEST, message);
    }

    public BusinessRuleException(HttpStatus status, String message) {
        super(message);
        this.status = status == null ? HttpStatus.BAD_REQUEST : status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
