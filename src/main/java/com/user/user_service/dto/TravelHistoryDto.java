package com.user.user_service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class TravelHistoryDto {
    private Long id;
    private LocalDateTime tripDate;
    private String sourceStation;
    private String destinationStation;
    private BigDecimal fare;
}
