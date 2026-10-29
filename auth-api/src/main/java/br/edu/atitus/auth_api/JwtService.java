package br.edu.atitus.auth_api;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Service
public class JwtService {

    private final JwtEncoder encoder;

    @Value("${jwt.expiration-seconds}")
    private long expirationSeconds;

    public JwtService(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secret);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    public String generateToken(Long userId, String email, String role) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("auth-api")
                .issuedAt(now)
                .expiresAt(now.plus(expirationSeconds, ChronoUnit.SECONDS))
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .build();

        return encoder.encode(
                        JwtEncoderParameters.from(
                                org.springframework.security.oauth2.jwt.JwsHeader
                                        .with(MacAlgorithm.HS256).build(),
                                claims))
                .getTokenValue();
    }
}