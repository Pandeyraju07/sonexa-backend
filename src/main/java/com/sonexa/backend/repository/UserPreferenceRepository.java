package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    List<UserPreference> findByUserKeyAndPrefType(String userKey, String prefType);

    List<UserPreference> findByUserKeyAndPrefTypeStartingWith(String userKey, String prefTypePrefix);

    @Modifying
    @Transactional
    void deleteByUserKeyAndPrefType(String userKey, String prefType);
}
