package com.user.user_service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DeductFareRequest {
    private Long metroCardId;
    private BigDecimal fare;
}
