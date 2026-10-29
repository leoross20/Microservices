package br.edu.atitus.productapi.services;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AuthorizationService {

    public void requireAdmin(Integer userType) {
        if (userType == null || userType != 0) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas administradores podem cadastrar produtos");
        }
    }
}