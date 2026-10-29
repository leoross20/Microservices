
package br.edu.atitus.apigateway;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final String internalSecret;

    public AuthFilter(
            JwtDecoder jwtDecoder,
            @Value("${app.internal.secret}") String internalSecret) {
        this.jwtDecoder = jwtDecoder;
        this.internalSecret = internalSecret;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (!path.startsWith(contextPath + "/ws/")
                && !path.startsWith("/ws/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token JWT ausente ou inválido");
            return;
        }

        try {
            String token = authorization.substring(7);
            Jwt jwt = jwtDecoder.decode(token);

            Object userId = jwt.getClaim("userId");
            String role = jwt.getClaimAsString("role");
            String email = jwt.getSubject();

            if (userId == null || role == null || email == null) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Token sem os dados necessários do usuário");
                return;
            }

            String userType;

            if ("ADMIN".equals(role)) {
                userType = "0";
            } else if ("USER".equals(role)) {
                userType = "1";
            } else {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Tipo de usuário inválido");
                return;
            }

            Map<String, String> trustedHeaders = new HashMap<>();
            trustedHeaders.put("X-User-Id", userId.toString());
            trustedHeaders.put("X-User-Type", userType);
            trustedHeaders.put("X-User-Email", email);
            trustedHeaders.put("X-Internal-Secret", internalSecret);

            HttpServletRequest wrappedRequest =
                    new IdentityHeadersRequest(request, trustedHeaders);

            filterChain.doFilter(wrappedRequest, response);

        } catch (JwtException | IllegalArgumentException exception) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token JWT inválido ou expirado");
        }
    }

    private static class IdentityHeadersRequest
            extends HttpServletRequestWrapper {

        private final Map<String, String> trustedHeaders;

        IdentityHeadersRequest(
                HttpServletRequest request,
                Map<String, String> trustedHeaders) {
            super(request);
            this.trustedHeaders = trustedHeaders;
        }

        private String getTrustedHeader(String name) {
            for (Map.Entry<String, String> entry
                    : trustedHeaders.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        @Override
        public String getHeader(String name) {
            if (isIdentityHeader(name)) {
                return getTrustedHeader(name);
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (isIdentityHeader(name)) {
                String value = getTrustedHeader(name);

                return Collections.enumeration(
                        value == null
                                ? Collections.emptyList()
                                : Collections.singletonList(value));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Map<String, String> headers = new HashMap<>();

            Enumeration<String> originalNames =
                    super.getHeaderNames();

            while (originalNames.hasMoreElements()) {
                String name = originalNames.nextElement();

                if (!isIdentityHeader(name)) {
                    headers.put(name, name);
                }
            }

            trustedHeaders.keySet().forEach(
                    name -> headers.put(name, name));

            return Collections.enumeration(headers.keySet());
        }

        private boolean isIdentityHeader(String name) {
            return name != null
                    && (name.equalsIgnoreCase("X-User-Id")
                    || name.equalsIgnoreCase("X-User-Type")
                    || name.equalsIgnoreCase("X-User-Email")
                    || name.equalsIgnoreCase("X-Internal-Secret"));
        }
    }
}
