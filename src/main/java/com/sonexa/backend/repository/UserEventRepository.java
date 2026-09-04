package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
    List<UserEvent> findByUserKeyOrderByTimestampDesc(String userKey);
    List<UserEvent> findTop100ByUserKeyOrderByTimestampDesc(String userKey);
    void deleteByUserKey(String userKey);
}