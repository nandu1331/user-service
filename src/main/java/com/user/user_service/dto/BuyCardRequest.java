package com.user.user_service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Getter
@Setter
public class BuyCardRequest {
    private Long userId;
    private BigDecimal initialBalance;
    // Add additional fields if needed (e.g., cardType, initialBalance)
}
