package com.ezra.task.product.repository;

import com.ezra.task.product.entity.ProductFee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductFeeRepository extends JpaRepository<ProductFee, Integer> {
}
