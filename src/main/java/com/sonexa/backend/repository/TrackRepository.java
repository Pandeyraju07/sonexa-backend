package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrackRepository extends JpaRepository<Track, Long> {
    List<Track> findByTrendingTrue();
    List<Track> findTop50ByOrderByIdDesc();
    List<Track> findTop20ByOrderByIdDesc();
    List<Track> findByAlbumId(Long albumId);
    List<Track> findByArtistId(Long artistId);
    List<Track> findByTitleContainingIgnoreCaseOrArtistNameContainingIgnoreCase(String title, String artist);
}
