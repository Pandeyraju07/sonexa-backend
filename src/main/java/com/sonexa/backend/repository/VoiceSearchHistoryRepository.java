package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.VoiceSearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoiceSearchHistoryRepository extends JpaRepository<VoiceSearchHistory, Long> {
    List<VoiceSearchHistory> findByUserKeyOrderByTimestampDesc(String userKey);
    void deleteByUserKey(String userKey);
}