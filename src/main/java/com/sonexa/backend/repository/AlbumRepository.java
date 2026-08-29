package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByTitleContainingIgnoreCaseOrArtistNameContainingIgnoreCase(String title, String artist);
}
