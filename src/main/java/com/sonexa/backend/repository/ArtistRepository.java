package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Artist> findByNameIgnoreCase(String name);
    List<Artist> findByNameContainingIgnoreCase(String q);
}
