package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.Podcast;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PodcastRepository extends JpaRepository<Podcast, Long> {}
