package com.user.user_service.service;

import com.user.user_service.dto.*;
import com.user.user_service.model.MetroCard;
import com.user.user_service.model.User;

import java.util.List;

public interface UserService {
    MetroCard buyMetroCard(BuyCardRequest request);
    List<TravelHistoryDto> getTravelHistory(Long userId);
    void cancelPass(CancelPassRequest request);
    UserProfileDto getUserProfile(Long userId);
    User registerUser(UserRegistrationRequest request);
//    List<TravelHistoryDto> syncTravelHistory(Long userId);
    MetroCardBalanceResponse getMetroCardBalance(Long metroCardId);
    DeductFareResponse deductFare(DeductFareRequest request);
}
