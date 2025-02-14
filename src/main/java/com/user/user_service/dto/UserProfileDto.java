package com.user.user_service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class UserProfileDto {
    private Long userId;
    private String name;
    private String email;
    private String mobile;
    private List<TravelHistoryDto> recentTrips;
}

