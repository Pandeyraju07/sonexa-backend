package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.MusicJourney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicJourneyRepository extends JpaRepository<MusicJourney, Long> {
    List<MusicJourney> findByUserKeyOrderByCreatedAtDesc(String userKey);
}