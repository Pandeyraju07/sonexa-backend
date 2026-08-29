package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.PodcastEpisode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PodcastEpisodeRepository extends JpaRepository<PodcastEpisode, Long> {
    List<PodcastEpisode> findByPodcastIdOrderByEpisodeNumberAsc(Long podcastId);
}
