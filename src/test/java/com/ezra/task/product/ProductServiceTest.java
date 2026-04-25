package com.ezra.task.product;

import com.ezra.task.exception.ResourceNotFoundException;
import com.ezra.task.product.dto.ProductDTOs.*;
import com.ezra.task.product.entity.Product;
import com.ezra.task.product.repository.ProductRepository;
import com.ezra.task.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void should_create_product() {
        var product = new Product();
        var request = mock(ProductRequest.class);

        when(request.toProduct()).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);

        var result = productService.createProduct(request);

        assertNotNull(result);
        verify(productRepository).save(product);
    }

    @Test
    void should_get_product_by_id() {
        var product = new Product();
        product.setId(1);
        product.setCreatedAt(Instant.now());

        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        var response = productService.getProductById(1);

        assertNotNull(response);
    }

    @Test
    void should_throw_when_product_not_found() {
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(1));
    }

    @Test
    void should_soft_delete_product() {
        var product = new Product();

        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        productService.deleteProductById(1);

        assertNotNull(product.getDeletedAt());
    }

    @Test
    void should_throw_when_deleting_nonexistent_product() {
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProductById(1));
    }

}
