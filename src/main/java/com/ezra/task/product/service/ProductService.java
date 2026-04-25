package com.ezra.task.product.service;

import com.ezra.task.exception.ResourceNotFoundException;
import com.ezra.task.product.dto.ProductDTOs.*;
import com.ezra.task.product.entity.Product;
import com.ezra.task.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product createProduct(ProductRequest productRequest) {
        var product = productRequest.toProduct();
        return productRepository.save(product);
    }

    public ProductResponse getProductById(Integer id) {
        var product = findProductById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product not found")
        );
        return ProductResponse.fromProduct(product);
    }

    public List<ProductResponse> getAllProducts() {
        return findAllProducts().stream().map(ProductResponse::fromProduct).toList();
    }

    @Transactional
    public void deleteProductById(Integer id) {
        var product = findProductById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product not found")
        );
        product.setDeletedAt(Instant.now());
    }

    private Optional<Product> findProductById(Integer productId) {
        return productRepository.findById(productId);
    }

    private List<Product> findAllProducts() {
        return productRepository.findAll();
    }
}
