package com.user.user_service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserRegistrationRequest {
    private String name;
    private String email;
    private String mobile;
}
