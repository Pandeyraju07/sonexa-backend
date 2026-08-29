package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.CatalogDtos.*;
import com.sonexa.backend.service.CatalogService;
import com.sonexa.backend.service.podcast.PodcastClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/podcasts")
@CrossOrigin(origins = "*")
public class PodcastController {

    private final CatalogService catalogService;
    private final PodcastClient podcastClient;

    public PodcastController(CatalogService catalogService, PodcastClient podcastClient) {
        this.catalogService = catalogService;
        this.podcastClient = podcastClient;
    }

    @GetMapping
    public PodcastListResponse list(
            @RequestParam(value = "category", defaultValue = "hindi") String category,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        String query = category;
        if ("hindi".equalsIgnoreCase(category) || "hindi (हिंदी)".equalsIgnoreCase(category)) {
            query = "the ranveer show hindi audio pitara hindi podcast";
        }
        List<PodcastDto> live = podcastClient.searchPodcasts(query, limit);
        if (!live.isEmpty()) {
            return new PodcastListResponse(true, live);
        }
        return catalogService.podcasts();
    }

    @GetMapping("/categories")
    public List<Map<String, String>> categories() {
        return List.of(
                Map.of("id", "hindi", "name", "Hindi (हिंदी)", "color", "#F97316"),
                Map.of("id", "all", "name", "All", "color", "#7C3AED"),
                Map.of("id", "hindi_stories", "name", "Hindi Stories", "color", "#E11D48"),
                Map.of("id", "technology", "name", "Technology", "color", "#2563EB"),
                Map.of("id", "business", "name", "Business", "color", "#059669"),
                Map.of("id", "comedy", "name", "Comedy", "color", "#D97706"),
                Map.of("id", "true_crime", "name", "True Crime", "color", "#DC2626"),
                Map.of("id", "health", "name", "Health", "color", "#EC4899"),
                Map.of("id", "science", "name", "Science", "color", "#8B5CF6"),
                Map.of("id", "news", "name", "News", "color", "#0EA5E9")
        );
    }

    @GetMapping("/search")
    public PodcastListResponse search(@RequestParam(value = "q", defaultValue = "") String q) {
        if (q.isBlank()) return new PodcastListResponse(true, List.of());
        List<PodcastDto> results = podcastClient.searchPodcasts(q, 20);
        return new PodcastListResponse(true, results);
    }

    @GetMapping("/{id}")
    public PodcastDetailResponse detail(@PathVariable String id) {
        PodcastDetailResponse live = podcastClient.getPodcastDetail(id);
        if (live.success() && live.podcast() != null) {
            return live;
        }
        return catalogService.podcastDetail(id);
    }
}