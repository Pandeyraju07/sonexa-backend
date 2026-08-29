package com.sonexa.backend.service.podcast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonexa.backend.model.dto.CatalogDtos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Component
public class PodcastClient {

    private static final Logger log = LoggerFactory.getLogger(PodcastClient.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PodcastClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<PodcastDto> searchPodcasts(String query, int limit) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://itunes.apple.com/search?term=" + encoded + "&media=podcast&entity=podcast&limit=" + limit;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "TuneFlow-Podcast/1.0")
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(resp.body());
                JsonNode results = root.path("results");
                List<PodcastDto> list = new ArrayList<>();
                for (JsonNode node : results) {
                    String id = node.path("collectionId").asText("");
                    String title = node.path("collectionName").asText("Untitled Podcast");
                    String host = node.path("artistName").asText("Podcast Host");
                    String genre = node.path("primaryGenreName").asText("General");
                    String coverUrl = node.path("artworkUrl600").asText("");
                    if (coverUrl.isBlank()) coverUrl = node.path("artworkUrl100").asText("");
                    if (!id.isBlank()) {
                        list.add(new PodcastDto("pod_" + id, title, host, "Top show in " + genre, coverUrl, genre));
                    }
                }
                return list;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch podcasts for query {}: {}", query, e.getMessage());
        }
        return Collections.emptyList();
    }

    public PodcastDetailResponse getPodcastDetail(String podcastId) {
        try {
            String rawId = podcastId.startsWith("pod_") ? podcastId.substring(4) : podcastId;
            String url = "https://itunes.apple.com/lookup?id=" + rawId + "&entity=podcastEpisode&limit=25";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "TuneFlow-Podcast/1.0")
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(resp.body());
                JsonNode results = root.path("results");

                PodcastDto show = null;
                List<PodcastEpisodeDto> episodes = new ArrayList<>();

                for (JsonNode node : results) {
                    String wrapper = node.path("wrapperType").asText("");
                    if ("track".equals(wrapper) && show == null) {
                        String id = "pod_" + node.path("collectionId").asText(rawId);
                        String title = node.path("collectionName").asText("Podcast Show");
                        String host = node.path("artistName").asText("Host");
                        String genre = node.path("primaryGenreName").asText("Podcasts");
                        String coverUrl = node.path("artworkUrl600").asText("");
                        if (coverUrl.isBlank()) coverUrl = node.path("artworkUrl100").asText("");
                        show = new PodcastDto(id, title, host, "Featured Show in " + genre, coverUrl, genre);
                    } else if ("podcastEpisode".equals(wrapper)) {
                        String epId = "ep_" + node.path("trackId").asText(UUID.randomUUID().toString());
                        String epTitle = node.path("trackName").asText("Episode");
                        String epDesc = node.path("description").asText("");
                        long durationMs = node.path("trackTimeMillis").asLong(0);
                        long mins = durationMs / (1000 * 60);
                        String durLabel = mins > 60 ? (mins / 60) + "h " + (mins % 60) + "m" : mins + " min";
                        String audioUrl = node.path("episodeUrl").asText("");
                        int epNum = node.path("trackNumber").asInt(episodes.size() + 1);

                        if (!audioUrl.isBlank()) {
                            episodes.add(new PodcastEpisodeDto(epId, epTitle, epDesc, durLabel, audioUrl, epNum));
                        }
                    }
                }

                if (show != null) {
                    return new PodcastDetailResponse(true, show, episodes);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get podcast detail for {}: {}", podcastId, e.getMessage());
        }
        return new PodcastDetailResponse(false, null, Collections.emptyList());
    }
}