package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.PlaylistTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {
    List<PlaylistTrack> findByPlaylistIdOrderBySortOrderAsc(Long playlistId);
    
    Optional<PlaylistTrack> findByPlaylistIdAndTrackPublicId(Long playlistId, String trackPublicId);
    
    long countByPlaylistId(Long playlistId);

    @Transactional
    @Modifying
    void deleteByPlaylistId(Long playlistId);

    @Transactional
    @Modifying
    void deleteByPlaylistIdAndTrackPublicId(Long playlistId, String trackPublicId);
}
