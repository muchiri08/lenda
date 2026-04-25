package com.ezra.task.product.dto;

import com.ezra.task.common.FeeCalculationType;
import com.ezra.task.common.FeeType;
import com.ezra.task.common.TenureType;
import com.ezra.task.product.entity.Product;
import com.ezra.task.product.entity.ProductFee;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public interface ProductDTOs {
    record ProductRequest(
            @NotBlank(message = "name is required") String name,
            @NotNull(message = "tenure type is required") TenureType tenureType,
            @NotNull(message = "tenure value is required") Integer tenureValue,
            Integer gracePeriod,
            @NotEmpty(message = "at least one product fee must be provided") List<ProductFeeRequest> fees

    ) {
        public Product toProduct() {
            var product = new Product();
            product.setName(name);
            product.setTenureType(tenureType);
            product.setTenureValue(tenureValue);
            product.setGracePeriod(Objects.requireNonNullElse(gracePeriod, 0));

            var productFees = fees.stream().map(ProductFeeRequest::toProductFee).toList();
            productFees.forEach(product::addFee);

            return product;
        }
    }

    record ProductFeeRequest(
            @NotNull(message = "fee type is required") FeeType type,
            @NotNull(message = "fee calculation type is required") FeeCalculationType calculationType,
            @NotNull(message = "amount is require")
            @Positive(message = "amount must be greater than 0") BigDecimal amount
    ) {
        ProductFee toProductFee() {
            var fee = new ProductFee();
            fee.setType(type);
            fee.setCalculationType(calculationType);
            fee.setAmount(amount);
            if (type == FeeType.SERVICE) {
                fee.setApplyAtOrigination(true);
            }
            return fee;
        }
    }

    record ProductResponse(
            Integer id,
            String name,
            TenureType tenureType,
            Integer tenureValue,
            Integer gracePeriod,
            String createdAt,
            List<ProductFeeResponse> fees
    ) {
        public static ProductResponse fromProduct(Product product) {
            return new ProductResponse(
                    product.getId(),
                    product.getName(),
                    product.getTenureType(),
                    product.getTenureValue(),
                    product.getGracePeriod(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC")).format(product.getCreatedAt()),
                    productFeesResponse(product)
            );
        }

        private static List<ProductFeeResponse> productFeesResponse(Product product) {
            return product.getFees().stream().map(ProductFeeResponse::fromProductFee).toList();
        }
    }

    record ProductFeeResponse(
            Integer id,
            FeeType type,
            FeeCalculationType calculationType,
            BigDecimal amount
    ) {
        public static ProductFeeResponse fromProductFee(ProductFee productFee) {
            return new ProductFeeResponse(
                    productFee.getId(),
                    productFee.getType(),
                    productFee.getCalculationType(),
                    productFee.getAmount()
            );
        }
    }
}
