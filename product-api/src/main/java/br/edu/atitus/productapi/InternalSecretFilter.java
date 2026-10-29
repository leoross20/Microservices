package br.edu.atitus.productapi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalSecretFilter extends OncePerRequestFilter {

    private final String internalSecret;

    public InternalSecretFilter(
            @Value("${app.internal.secret}") String internalSecret) {
        this.internalSecret = internalSecret;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/ws/")) {
            String receivedSecret =
                    request.getHeader("X-Internal-Secret");

            if (receivedSecret == null
                    || !MessageDigest.isEqual(
                    internalSecret.getBytes(StandardCharsets.UTF_8),
                    receivedSecret.getBytes(StandardCharsets.UTF_8))) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Acesso permitido somente pelo Gateway");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}