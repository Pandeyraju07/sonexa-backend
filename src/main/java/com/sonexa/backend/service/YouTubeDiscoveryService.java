package com.sonexa.backend.service;

import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Backend YouTube Discovery Service using official YouTube Data API v3.
 * Strictly metadata discovery only (no scraping, no stream extraction, no DRM bypassing).
 */
@Service
public class YouTubeDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(YouTubeDiscoveryService.class);
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";

    private final RestTemplate restTemplate;

    @Value("${youtube.api.key:}")
    private String apiKey;

    @Value("${sonexa.youtube.enabled:true}")
    private boolean enabled;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 15 * 60 * 1000L; // 15 minutes compliant TTL

    private record CacheEntry(long timestamp, List<TrackDto> results) {}

    public YouTubeDiscoveryService(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();
    }

    @SuppressWarnings("unchecked")
    public List<TrackDto> searchYouTubeMusic(String query, int maxResults) {
        if (!enabled || query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        String effectiveKey = (apiKey != null && !apiKey.isBlank()) ? apiKey.trim() : System.getenv("YOUTUBE_API_KEY");
        if (effectiveKey == null || effectiveKey.isBlank()) {
            log.debug("event=YOUTUBE_SEARCH_SKIPPED reason=no_api_key");
            return Collections.emptyList();
        }

        String cacheKey = query.trim().toLowerCase();
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && (System.currentTimeMillis() - cached.timestamp()) < CACHE_TTL_MS) {
            return cached.results();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(YOUTUBE_SEARCH_URL)
                    .queryParam("part", "snippet")
                    .queryParam("q", query)
                    .queryParam("type", "video")
                    .queryParam("videoCategoryId", "10") // Music category
                    .queryParam("videoEmbeddable", "true") // Must be embeddable for official player
                    .queryParam("videoSyndicated", "true")
                    .queryParam("maxResults", Math.min(maxResults, 25))
                    .queryParam("key", effectiveKey)
                    .build(false)
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("items")) {
                return Collections.emptyList();
            }

            List<?> items = (List<?>) response.get("items");
            List<TrackDto> tracks = new ArrayList<>();

            for (Object itemObj : items) {
                if (!(itemObj instanceof Map<?, ?> rawItem)) continue;
                Map<String, Object> itemMap = (Map<String, Object>) rawItem;

                Map<String, Object> idMap = (Map<String, Object>) itemMap.get("id");
                String videoId = idMap != null ? (String) idMap.get("videoId") : null;
                if (videoId == null || videoId.isBlank()) continue;

                Map<String, Object> snippet = (Map<String, Object>) itemMap.get("snippet");
                if (snippet == null) continue;

                String rawTitle = (String) snippet.getOrDefault("title", "Unknown Track");
                String channelTitle = (String) snippet.getOrDefault("channelTitle", "YouTube Artist");
                String cleanTitle = cleanVideoTitle(rawTitle);

                String coverUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
                Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                if (thumbnails != null) {
                    Map<String, Object> high = (Map<String, Object>) thumbnails.get("high");
                    if (high != null && high.containsKey("url")) {
                        coverUrl = (String) high.get("url");
                    }
                }

                boolean isOfficial = channelTitle.endsWith("- Topic") || channelTitle.toLowerCase().contains("official") || rawTitle.toLowerCase().contains("official");

                TrackDto dto = new TrackDto(
                        "yt_" + videoId,
                        cleanTitle,
                        cleanArtist(channelTitle),
                        "YouTube Music",
                        0L,
                        "https://www.youtube.com/watch?v=" + videoId,
                        coverUrl,
                        "YouTube",
                        false,
                        "YOUTUBE",
                        videoId,
                        videoId,
                        "https://www.youtube.com/watch?v=" + videoId,
                        true,
                        "OFFICIAL_IFRAME_PLAYER",
                        channelTitle,
                        isOfficial
                );
                tracks.add(dto);
            }

            cache.put(cacheKey, new CacheEntry(System.currentTimeMillis(), tracks));
            return tracks;
        } catch (Exception e) {
            log.warn("event=YOUTUBE_SEARCH_ERROR query={} msg={}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String cleanVideoTitle(String raw) {
        if (raw == null) return "Unknown Title";
        return raw.replaceAll("(?i)\\s*\\(Official Video\\)", "")
                .replaceAll("(?i)\\s*\\[Official Video\\]", "")
                .replaceAll("(?i)\\s*\\(Official Music Video\\)", "")
                .replaceAll("(?i)\\s*\\[Official Music Video\\]", "")
                .replaceAll("(?i)\\s*\\(Official Audio\\)", "")
                .replaceAll("(?i)\\s*\\[Official Audio\\]", "")
                .replaceAll("(?i)\\s*\\(Lyrics\\)", "")
                .replaceAll("(?i)\\s*\\[Lyrics\\]", "")
                .replaceAll("(?i)\\s*\\(Audio\\)", "")
                .replaceAll("(?i)\\s*\\(Visualizer\\)", "")
                .trim();
    }

    private String cleanArtist(String raw) {
        if (raw == null) return "YouTube Artist";
        return raw.replaceAll("(?i)\\s*-\\s*Topic$", "").trim();
    }
}