package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.EmotionCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmotionCheckInRepository extends JpaRepository<EmotionCheckIn, Long> {
    List<EmotionCheckIn> findTop10ByUserKeyOrderByTimestampDesc(String userKey);
}