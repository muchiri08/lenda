package com.ezra.task.product.controller;

import com.ezra.task.product.dto.ProductDTOs.*;
import com.ezra.task.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Void> createProduct(@RequestBody @Valid ProductRequest productRequest, UriComponentsBuilder uriBuilder) {
        var product = productService.createProduct(productRequest);
        var location = uriBuilder.path("/api/products/{id}")
                .buildAndExpand(product.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProductById(id);
        return ResponseEntity.ok().build();
    }
}
