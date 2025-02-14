package com.user.user_service.controller;

import com.user.user_service.dto.*;
import com.user.user_service.model.MetroCard;
import com.user.user_service.model.TravelHistory;
import com.user.user_service.model.User;
import com.user.user_service.service.TravelHistorySyncService;
import com.user.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TravelHistorySyncService syncService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRegistrationRequest request) {
        User savedUser = userService.registerUser(request);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/buy-card")
    public ResponseEntity<MetroCard> buyCard(@RequestBody BuyCardRequest request) {
        MetroCard newCard = userService.buyMetroCard(request);
        return ResponseEntity.ok(newCard);
    }

    @GetMapping("/travel-history/{userId}")
    public ResponseEntity<List<TravelHistoryDto>> getTravelHistory(@PathVariable Long userId) {
        List<TravelHistoryDto> history = userService.getTravelHistory(userId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/cancel-pass")
    public ResponseEntity<String> cancelPass(@RequestBody CancelPassRequest request) {
        userService.cancelPass(request);
        return ResponseEntity.ok("Pass canceled successfully");
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable Long userId) {
        UserProfileDto profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/sync-travel-history/{userId}")
    public ResponseEntity<List<TravelHistory>> syncTravelHistory(@PathVariable Long userId) {
        List<TravelHistory> histories = syncService.syncTravelHistory(userId);
        return ResponseEntity.ok(histories);
    }

    // Endpoint to get the current balance of a metro card.
    @GetMapping("metro-card/balance/{metroCardId}")
    public ResponseEntity<MetroCardBalanceResponse> getBalance(@PathVariable Long metroCardId) {
        MetroCardBalanceResponse response = userService.getMetroCardBalance(metroCardId);
        return ResponseEntity.ok(response);
    }

    // Endpoint to deduct fare from the metro card.
    @PostMapping("metro-card/deduct")
    public ResponseEntity<DeductFareResponse> deductFare(@RequestBody DeductFareRequest request) {
        DeductFareResponse response = userService.deductFare(request);
        return ResponseEntity.ok(response);
    }
}

