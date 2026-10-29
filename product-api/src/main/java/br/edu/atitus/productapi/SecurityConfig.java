package br.edu.atitus.productapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        // Consultas públicas de produtos
                        .requestMatchers(
                                HttpMethod.GET,
                                "/products", "/products/**"
                        ).permitAll()

                        // O WsProductController verifica o tipo do usuário
                        .requestMatchers(
                                HttpMethod.POST,
                                "/ws/products"
                        ).permitAll()

                        // O cadastro antigo não será mais utilizado
                        .requestMatchers(
                                HttpMethod.POST,
                                "/products"
                        ).denyAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

                        // Bloqueia qualquer outra rota não prevista
                        .anyRequest().denyAll()
                );

        return http.build();
    }
}