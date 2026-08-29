package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.PlaylistTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {
    List<PlaylistTrack> findByPlaylistIdOrderBySortOrderAsc(Long playlistId);
}
