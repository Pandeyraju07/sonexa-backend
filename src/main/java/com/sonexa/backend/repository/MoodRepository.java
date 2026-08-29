package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.Mood;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MoodRepository extends JpaRepository<Mood, Long> {
    List<Mood> findAllByOrderBySortOrderAsc();
}
