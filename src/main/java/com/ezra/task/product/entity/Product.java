package com.ezra.task.product.entity;

import com.ezra.task.common.TenureType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Enumerated(EnumType.STRING)
    private TenureType tenureType;

    private Integer tenureValue;

    private Integer gracePeriod;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductFee> fees = new ArrayList<>();

    @CreationTimestamp
    private Instant createdAt;

    private Instant deletedAt;

    public void addFee(ProductFee fee) {
        this.fees.add(fee);
        fee.setProduct(this);
    }
}
