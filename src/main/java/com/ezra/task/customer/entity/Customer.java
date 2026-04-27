package com.ezra.task.customer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String fullName;

    private String email;

    private String phone;

    private Integer creditScore;

    @CreationTimestamp
    private Instant createdAt;

    private Instant deletedAt;

    public Customer(String fullName, String email, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    @PrePersist
    public void assignCreditScore() {
        if (this.creditScore == null) {
            this.creditScore = ThreadLocalRandom.current().nextInt(0, 101);
        }
    }
}
