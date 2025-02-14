package com.user.user_service.service.impl;

import com.user.user_service.dto.*;
import com.user.user_service.exception.EmailAlreadyRegisteredException;
import com.user.user_service.exception.InsufficientBalanceException;
import com.user.user_service.exception.MetroCardNotFoundException;
import com.user.user_service.exception.UserNotFoundException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
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

    @Override
    public User registerUser(UserRegistrationRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new EmailAlreadyRegisteredException("Email already registered");
        });

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
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        MetroCard metroCard = new MetroCard();
        metroCard.setUser(user);
        metroCard.setBalance(request.getInitialBalance());
        metroCard.setPurchaseDate(LocalDateTime.now());
        metroCard.setStatus(CardStatus.ACTIVE);

        MetroCard savedCard = metroCardRepository.save(metroCard);

        // Publish event to Kafka (optional)
        // kafkaProducer.sendMessage("MetroCardPurchased:" + savedCard.getId());

        return savedCard;
    }

    @Override
    public List<TravelHistoryDto> getTravelHistory(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<TravelHistory> histories = travelHistoryRepository.findTop10ByUserIdOrderByTripDateDesc(userId);

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
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        MetroCard metroCard = metroCardRepository.findByIdAndUserIdAndStatus(
                        request.getCardId(), request.getUserId(), CardStatus.ACTIVE)
                .orElseThrow(() -> new MetroCardNotFoundException("Active MetroCard not found for user"));

        metroCard.setStatus(CardStatus.CANCELED);
        metroCardRepository.save(metroCard);
    }

    @Override
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserProfileDto profileDto = new UserProfileDto();
        profileDto.setUserId(user.getId());
        profileDto.setName(user.getName());
        profileDto.setEmail(user.getEmail());
        profileDto.setMobile(user.getMobile());

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
                .orElseThrow(() -> new MetroCardNotFoundException("Metro Card not found"));
        MetroCardBalanceResponse response = new MetroCardBalanceResponse();
        response.setMetroCardId(metroCard.getId());
        response.setBalance(metroCard.getBalance());
        return response;
    }

    @Override
    public DeductFareResponse deductFare(DeductFareRequest request) {
        MetroCard metroCard = metroCardRepository.findById(request.getMetroCardId())
                .orElseThrow(() -> new MetroCardNotFoundException("Metro Card not found"));

        if (metroCard.getBalance().compareTo(request.getFare()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance on metro card");
        }

        metroCard.setBalance(metroCard.getBalance().subtract(request.getFare()));
        metroCardRepository.save(metroCard);

        DeductFareResponse response = new DeductFareResponse();
        response.setMetroCardId(metroCard.getId());
        response.setUpdatedBalance(metroCard.getBalance());
        response.setUpdatedAt(LocalDateTime.now());
        response.setMessage("Fare deducted successfully");
        return response;
    }
}
