package br.com.timeforge.timeforge_api.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String requestId = resolveRequestId(request);

        response.setHeader(REQUEST_ID_HEADER, requestId);
        MDC.put(REQUEST_ID_MDC_KEY, requestId);

        String method = request.getMethod();
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = extractClientIp(request);

        log.debug(
                "Requisicao iniciada: metodo={}, path={}, query={}, clientIp={}",
                method,
                path,
                queryString,
                clientIp
        );

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            logCompletion(method, path, queryString, status, durationMs, clientIp);
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    private void logCompletion(
            String method,
            String path,
            String queryString,
            int status,
            long durationMs,
            String clientIp
    ) {
        String message = "Requisicao finalizada: metodo={}, path={}, query={}, status={}, duracaoMs={}, clientIp={}";

        if (status >= 500) {
            log.error(message, method, path, queryString, status, durationMs, clientIp);
            return;
        }

        if (status >= 400) {
            log.warn(message, method, path, queryString, status, durationMs, clientIp);
            return;
        }

        log.info(message, method, path, queryString, status, durationMs, clientIp);
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestId;
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
