package com.user.user_service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MetroCardBalanceResponse {
    private Long metroCardId;
    private BigDecimal balance;
}
