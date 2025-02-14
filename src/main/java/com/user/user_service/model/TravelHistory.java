package com.user.user_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "travel_history")
@Getter
@Setter
public class TravelHistory {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @Column(name = "source_station", nullable = false)
    private String sourceStation;

    @Column(name = "destination_station", nullable = false)
    private String destinationStation;

    @Column(name = "trip_date")
    private LocalDateTime tripDate;

    private BigDecimal fare;
}

