package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.UserLibraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserLibraryItemRepository extends JpaRepository<UserLibraryItem, Long> {
    List<UserLibraryItem> findByUserKeyAndLikedTrue(String userKey);
    Optional<UserLibraryItem> findByUserKeyAndTrackPublicId(String userKey, String trackPublicId);
}
