package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByMadeForYouTrue();
    List<Playlist> findByUserKeyOrderByIdDesc(String userKey);
    Optional<Playlist> findByIdAndUserKey(Long id, String userKey);
}
