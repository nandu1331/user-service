package com.user.user_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DeductFareResponse {
    private Long metroCardId;
    private BigDecimal updatedBalance;
    private LocalDateTime updatedAt;
    private String message;
}
