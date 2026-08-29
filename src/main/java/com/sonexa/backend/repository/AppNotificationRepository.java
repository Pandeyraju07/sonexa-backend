package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findByUserKeyOrderByCreatedAtDesc(String userKey);
}
