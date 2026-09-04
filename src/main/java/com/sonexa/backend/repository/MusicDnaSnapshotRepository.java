package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.MusicDnaSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MusicDnaSnapshotRepository extends JpaRepository<MusicDnaSnapshot, Long> {
    Optional<MusicDnaSnapshot> findTopByUserKeyOrderByCreatedAtDesc(String userKey);
}