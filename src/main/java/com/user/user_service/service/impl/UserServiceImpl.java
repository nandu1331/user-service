package com.user.user_service.service.impl;

import com.user.user_service.dto.*;
import com.user.user_service.kafka.KafkaProducer;
import com.user.user_service.model.MetroCard;
import com.user.user_service.model.MetroCard.CardStatus;
import com.user.user_service.model.TravelHistory;
import com.user.user_service.model.User;
import com.user.user_service.repository.MetroCardRepository;
import com.user.user_service.repository.TravelHistoryRepository;
import com.user.user_service.repository.UserRepository;
import com.user.user_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MetroCardRepository metroCardRepository;

    @Autowired
    private TravelHistoryRepository travelHistoryRepository;

    @Autowired
    private KafkaProducer kafkaProducer; // For publishing events (optional)

    @Autowired
    private RestTemplate restTemplate;

    @Value("${metro.service.url}")
    private String metroServiceUrl;

    @Override
    public User registerUser(UserRegistrationRequest request) {
        // Validate if email already exists
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new RuntimeException("Email already registered");
        });

        // Create and save the new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public MetroCard buyMetroCard(BuyCardRequest request) {
        // Validate the user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create a new metro card record
        MetroCard metroCard = new MetroCard();
        metroCard.setUser(user);
        metroCard.setBalance(request.getInitialBalance());
        metroCard.setPurchaseDate(LocalDateTime.now());
        metroCard.setStatus(CardStatus.ACTIVE);

        MetroCard savedCard = metroCardRepository.save(metroCard);

        // Optionally publish an event to Kafka for the new metro card purchase
        kafkaProducer.sendMessage("MetroCardPurchased:" + savedCard.getId());

        return savedCard;
    }

    @Override
    public List<TravelHistoryDto> getTravelHistory(Long userId) {
        // Validate the user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Retrieve the latest 10 travel history records
        List<TravelHistory> histories = travelHistoryRepository.findTop10ByUserIdOrderByTripDateDesc(userId);

        // Map entities to DTOs
        return histories.stream().map(history -> {
            TravelHistoryDto dto = new TravelHistoryDto();
            dto.setTripDate(history.getTripDate());
            dto.setSourceStation(history.getSourceStation());
            dto.setDestinationStation(history.getDestinationStation());
            dto.setFare(history.getFare());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void cancelPass(CancelPassRequest request) {
        // Validate the user exists
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate the metro card exists and is active
        MetroCard metroCard = metroCardRepository.findByIdAndUserIdAndStatus(
                        request.getCardId(), request.getUserId(), CardStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Active MetroCard not found for user"));

        // Update the card status to CANCELED
        metroCard.setStatus(CardStatus.CANCELED);
        metroCardRepository.save(metroCard);
    }

    @Override
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDto profileDto = new UserProfileDto();
        profileDto.setUserId(user.getId());
        profileDto.setName(user.getName());
        profileDto.setEmail(user.getEmail());
        profileDto.setMobile(user.getMobile());

        // Fetch a summary of recent trips
        List<TravelHistory> histories = travelHistoryRepository.findTop10ByUserIdOrderByTripDateDesc(userId);
        List<TravelHistoryDto> historyDtos = histories.stream().map(history -> {
            TravelHistoryDto dto = new TravelHistoryDto();
            dto.setTripDate(history.getTripDate());
            dto.setSourceStation(history.getSourceStation());
            dto.setDestinationStation(history.getDestinationStation());
            dto.setFare(history.getFare());
            return dto;
        }).collect(Collectors.toList());
        profileDto.setRecentTrips(historyDtos);

        return profileDto;
    }

    @Override
    public MetroCardBalanceResponse getMetroCardBalance(Long metroCardId) {
        MetroCard metroCard = metroCardRepository.findById(metroCardId)
                .orElseThrow(() -> new RuntimeException("Metro Card not found"));
        MetroCardBalanceResponse response = new MetroCardBalanceResponse();
        response.setMetroCardId(metroCard.getId());
        response.setBalance(metroCard.getBalance());
        return response;
    }

    @Override
    public DeductFareResponse deductFare(DeductFareRequest request) {
        MetroCard metroCard = metroCardRepository.findById(request.getMetroCardId())
                .orElseThrow(() -> new RuntimeException("Metro Card not found"));

        // Ensure there is sufficient balance.
        if (metroCard.getBalance().compareTo(request.getFare()) < 0) {
            throw new RuntimeException("Insufficient balance on metro card");
        }

        // Deduct the fare.
        metroCard.setBalance(metroCard.getBalance().subtract(request.getFare()));
        metroCardRepository.save(metroCard);

        // Prepare response.
        DeductFareResponse response = new DeductFareResponse();
        response.setMetroCardId(metroCard.getId());
        response.setUpdatedBalance(metroCard.getBalance());
        response.setUpdatedAt(LocalDateTime.now());
        response.setMessage("Fare deducted successfully");
        return response;
    }
}
