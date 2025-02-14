package com.user.user_service.repository;

import com.user.user_service.model.MetroCard;
import com.user.user_service.model.MetroCard.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetroCardRepository extends JpaRepository<MetroCard, Long> {
    Optional<MetroCard> findByIdAndUserIdAndStatus(Long id, Long userId, CardStatus status);
}
