package com.user.user_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "metro_card")
@Getter
@Setter
public class MetroCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @Column(precision = 10, scale = 2)
    private BigDecimal balance;

    public enum CardStatus {
        ACTIVE,
        CANCELED
    }
}

