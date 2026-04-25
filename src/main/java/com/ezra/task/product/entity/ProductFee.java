package com.ezra.task.product.entity;

import com.ezra.task.common.FeeCalculationType;
import com.ezra.task.common.FeeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Table(name = "product_fees")
@Getter
@Setter
public class ProductFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private FeeType type;

    @Enumerated(EnumType.STRING)
    private FeeCalculationType calculationType;

    private BigDecimal amount;

    private Boolean applyAtOrigination = false;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @CreationTimestamp
    private Instant createdAt;

    private Instant deletedAt;

    public BigDecimal calculate(BigDecimal principal) {
        return switch (calculationType) {
            case FIXED -> amount != null ? amount : BigDecimal.ZERO;
            case PERCENTAGE -> {
                var percentage = amount;
                yield principal.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
        };
    }

}
