package com.user.user_service.repository;

import com.user.user_service.model.TravelHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelHistoryRepository extends JpaRepository<TravelHistory, Long> {
    List<TravelHistory> findTop10ByUserIdOrderByTripDateDesc(Long userId);
    void deleteByUserId(Long userId);
}

