package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.CatalogDtos.*;
import com.sonexa.backend.service.CatalogService;
import com.sonexa.backend.service.podcast.PodcastClient;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/podcasts")
@CrossOrigin(origins = "*")
public class PodcastController {

    private final CatalogService catalogService;
    private final PodcastClient podcastClient;
    private final Set<String> followedPodcasts = Collections.synchronizedSet(new HashSet<>());

    public PodcastController(CatalogService catalogService, PodcastClient podcastClient) {
        this.catalogService = catalogService;
        this.podcastClient = podcastClient;
        // Default popular follows
        followedPodcasts.add("pod_1542452346");
    }

    @GetMapping("/home")
    public PodcastHomeResponse home() {
        List<PodcastDto> hindiShows = podcastClient.searchPodcasts("the ranveer show hindi audio pitara", 8);
        List<PodcastDto> popular = podcastClient.searchPodcasts("top popular podcasts", 8);
        List<PodcastDto> trending = new ArrayList<>(hindiShows);
        trending.addAll(popular);

        List<PodcastEpisodeDto> continueListening = new ArrayList<>();
        if (!hindiShows.isEmpty()) {
            PodcastDetailResponse det = podcastClient.getPodcastDetail(hindiShows.get(0).id());
            if (det.success() && !det.episodes().isEmpty()) {
                continueListening.add(det.episodes().get(0));
            }
        }

        return new PodcastHomeResponse(
                true,
                continueListening,
                podcastClient.getLanguages(),
                trending.stream().distinct().limit(10).toList(),
                hindiShows,
                popular,
                podcastClient.getCategories()
        );
    }

    @GetMapping
    public PodcastListResponse list(
            @RequestParam(value = "category", defaultValue = "hindi") String category,
            @RequestParam(value = "limit", defaultValue = "25") int limit
    ) {
        List<PodcastDto> live = podcastClient.searchPodcasts(category, limit);
        if (!live.isEmpty()) {
            return new PodcastListResponse(true, live);
        }
        return catalogService.podcasts();
    }

    @GetMapping("/languages")
    public List<PodcastLanguageDto> languages() {
        return podcastClient.getLanguages();
    }

    @GetMapping("/language/{code}")
    public PodcastListResponse byLanguage(
            @PathVariable String code,
            @RequestParam(value = "limit", defaultValue = "25") int limit
    ) {
        String q = code + " podcast";
        List<PodcastDto> live = podcastClient.searchPodcasts(q, limit);
        return new PodcastListResponse(true, live);
    }

    @GetMapping("/categories")
    public List<PodcastCategoryDto> categories() {
        return podcastClient.getCategories();
    }

    @GetMapping("/category/{category}")
    public PodcastListResponse byCategory(
            @PathVariable String category,
            @RequestParam(value = "limit", defaultValue = "25") int limit
    ) {
        List<PodcastDto> live = podcastClient.searchPodcasts(category, limit);
        return new PodcastListResponse(true, live);
    }

    @GetMapping("/search")
    public PodcastListResponse search(@RequestParam(value = "q", defaultValue = "") String q) {
        if (q.isBlank()) return new PodcastListResponse(true, List.of());
        List<PodcastDto> results = podcastClient.searchPodcasts(q, 25);
        return new PodcastListResponse(true, results);
    }

    @GetMapping("/{id}")
    public PodcastDetailResponse detail(@PathVariable String id) {
        PodcastDetailResponse live = podcastClient.getPodcastDetail(id);
        if (live.success() && live.podcast() != null) {
            boolean isFollowed = followedPodcasts.contains(live.podcast().id());
            PodcastDto updatedShow = new PodcastDto(
                    live.podcast().id(),
                    live.podcast().title(),
                    live.podcast().host(),
                    live.podcast().description(),
                    live.podcast().coverUrl(),
                    live.podcast().category(),
                    live.podcast().language(),
                    live.podcast().followerCount(),
                    live.podcast().episodeCount(),
                    isFollowed
            );
            return new PodcastDetailResponse(true, updatedShow, live.episodes());
        }
        return catalogService.podcastDetail(id);
    }

    @PostMapping("/{id}/follow")
    public SimpleSuccessResponse follow(@PathVariable String id) {
        followedPodcasts.add(id);
        return new SimpleSuccessResponse(true, "Followed podcast successfully");
    }

    @DeleteMapping("/{id}/follow")
    public SimpleSuccessResponse unfollow(@PathVariable String id) {
        followedPodcasts.remove(id);
        return new SimpleSuccessResponse(true, "Unfollowed podcast successfully");
    }

    @PostMapping("/episodes/{id}/progress")
    public SimpleSuccessResponse updateProgress(
            @PathVariable String id,
            @RequestParam(value = "positionMs", defaultValue = "0") long positionMs,
            @RequestParam(value = "completed", defaultValue = "false") boolean completed
    ) {
        return new SimpleSuccessResponse(true, "Progress updated");
    }
}