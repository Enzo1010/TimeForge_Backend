package br.com.timeforge.timeforge_api.security;

import br.com.timeforge.timeforge_api.dto.response.ApiErroResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int    LOGIN_CAPACITY    = 5;
    private static final Duration LOGIN_WINDOW    = Duration.ofMinutes(1);
    private static final int    REGISTER_CAPACITY = 3;
    private static final Duration REGISTER_WINDOW = Duration.ofMinutes(10);

    private final ConcurrentHashMap<String, Bucket> loginBuckets    = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> registerBuckets = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if ("/auth/login".equals(path) || "/auth/register".equals(path)) {
            String clientIp = extractClientIp(request);

            Bucket bucket = "/auth/login".equals(path)
                    ? loginBuckets.computeIfAbsent(clientIp, k -> createBucket(LOGIN_CAPACITY, LOGIN_WINDOW))
                    : registerBuckets.computeIfAbsent(clientIp, k -> createBucket(REGISTER_CAPACITY, REGISTER_WINDOW));

            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit excedido: path={}, clientIp={}", path, clientIp);
                rejectRequest(request, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Bucket createBucket(int capacity, Duration window) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillIntervally(capacity, window)
                        .build())
                .build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void rejectRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ApiErroResponseDTO erro = ApiErroResponseDTO.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .erro("Too Many Requests")
                .mensagem("Limite de tentativas excedido. Tente novamente mais tarde.")
                .path(request.getRequestURI())
                .build();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), erro);
    }
}
