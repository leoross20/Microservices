package br.edu.atitus.productapi.controllers;

import br.edu.atitus.productapi.dtos.ProductRequest;
import br.edu.atitus.productapi.entities.ProductEntity;
import br.edu.atitus.productapi.services.AuthorizationService;
import br.edu.atitus.productapi.services.ProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ws/products")
public class WsProductController {

    private final ProductService productService;
    private final AuthorizationService authorizationService;

    public WsProductController(
            @Qualifier("productServiceJpa") ProductService productService,
            AuthorizationService authorizationService) {
        this.productService = productService;
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<ProductEntity> create(
            @RequestBody ProductRequest request,
            @RequestHeader(value = "X-User-Type", required = false)
            Integer userType) throws Exception {

        authorizationService.requireAdmin(userType);

        ProductEntity response = productService.save(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}