package com.user.user_service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CancelPassRequest {
    private Long userId;
    private Long cardId;
}
