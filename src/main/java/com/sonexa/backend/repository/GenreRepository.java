package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    List<Genre> findAllByOrderBySortOrderAsc();
}
