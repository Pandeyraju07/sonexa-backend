package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.UserTasteProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTasteProfileRepository extends JpaRepository<UserTasteProfile, Long> {
    Optional<UserTasteProfile> findByUserKey(String userKey);
    void deleteByUserKey(String userKey);
}