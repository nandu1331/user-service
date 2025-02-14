package com.user.user_service.service;

import com.user.user_service.dto.TravelHistoryDto;
import com.user.user_service.model.TravelHistory;
import com.user.user_service.model.User;
import com.user.user_service.repository.TravelHistoryRepository;
import com.user.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TravelHistorySyncService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TravelHistoryRepository travelHistoryRepository;

    @Autowired
    private UserRepository userRepository;  // To fetch the User entity

    // Base URL of the Metro Service, e.g., http://localhost:8081
    @Value("${metro.service.url}")
    private String metroServiceUrl;

    /**
     * Synchronizes travel history for a given user by fetching data from the Metro Service.
     * Uses the journeyId from Metro Service as the primary key in the local travel_history table.
     *
     * @param userId The ID of the user whose travel history is to be synced.
     * @return List of TravelHistory records saved in the User Service DB.
     */
    public List<TravelHistory> syncTravelHistory(Long userId) {
        // Build the URL for the Metro Service endpoint
        String url = metroServiceUrl + "/metro/travel-history/" + userId;

        // Make the REST call to Metro Service to fetch travel history data
        ResponseEntity<TravelHistoryDto[]> responseEntity = restTemplate.getForEntity(url, TravelHistoryDto[].class);
        TravelHistoryDto[] dtos = responseEntity.getBody();

        if (dtos == null) {
            throw new RuntimeException("No travel history data received from Metro Service");
        }

        // Fetch the User entity from the local database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found in User Service with ID: " + userId));

        // Map each received DTO into a TravelHistory entity
        List<TravelHistory> histories = Arrays.stream(dtos)
                .map(dto -> {
                    TravelHistory th = new TravelHistory();
                    // Set the journey ID from Metro Service as the primary key
                    th.setId(dto.getId());
                    // Associate this travel history record with the fetched user
                    th.setUser(user);
                    th.setSourceStation(dto.getSourceStation());
                    th.setDestinationStation(dto.getDestinationStation());
                    th.setTripDate(dto.getTripDate());
                    th.setFare(dto.getFare());
                    return th;
                })
                .collect(Collectors.toList());

        // Save the mapped travel history records in the local database
        travelHistoryRepository.saveAll(histories);
        return histories;
    }
}
