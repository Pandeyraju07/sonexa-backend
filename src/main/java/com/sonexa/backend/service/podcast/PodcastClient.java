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
            String q = query == null || query.isBlank() ? "top podcasts hindi english" : query;
            String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8);
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
                    String title = node.path("collectionName").asText("Untitled Podcast").replace("?????", "हिंदी");
                    String host = node.path("artistName").asText("Podcast Host");
                    String genre = node.path("primaryGenreName").asText("General");
                    String coverUrl = node.path("artworkUrl600").asText("");
                    if (coverUrl.isBlank()) coverUrl = node.path("artworkUrl100").asText("");
                    String lang = detectLanguage(title + " " + host + " " + q);
                    int trackCount = node.path("trackCount").asInt(25);

                    if (!id.isBlank()) {
                        list.add(new PodcastDto(
                                "pod_" + id,
                                title,
                                host,
                                "Top podcast show in " + genre,
                                coverUrl,
                                genre,
                                lang,
                                "180K",
                                trackCount,
                                false
                        ));
                    }
                }

                // If Hindi query, guarantee The Ranveer Show TRS Hindi is present
                if (q.toLowerCase().contains("hindi") && list.stream().noneMatch(p -> p.id().contains("1542452346"))) {
                    list.add(0, new PodcastDto(
                            "pod_1542452346",
                            "The Ranveer Show (TRS हिंदी)",
                            "BeerBiceps (Ranveer Allahbadia)",
                            "India's biggest Hindi podcast with legendary guests and deep conversations",
                            "https://is1-ssl.mzstatic.com/image/thumb/Podcasts126/v4/4a/12/f9/4a12f915-0557-0a2a-281b-5e60d2ecb3fb/mza_16382103562699898858.jpg/600x600bb.jpg",
                            "Society & Culture",
                            "Hindi",
                            "2.4M",
                            320,
                            true
                    ));
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
            String url = "https://itunes.apple.com/lookup?id=" + rawId + "&entity=podcastEpisode&limit=30";
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
                        String title = node.path("collectionName").asText("Podcast Show").replace("?????", "हिंदी");
                        String host = node.path("artistName").asText("Host");
                        String genre = node.path("primaryGenreName").asText("Podcasts");
                        String coverUrl = node.path("artworkUrl600").asText("");
                        if (coverUrl.isBlank()) coverUrl = node.path("artworkUrl100").asText("");
                        String lang = detectLanguage(title + " " + host);
                        show = new PodcastDto(id, title, host, "Featured Show in " + genre, coverUrl, genre, lang, "250K", 30, false);
                    } else if ("podcastEpisode".equals(wrapper)) {
                        String epId = "ep_" + node.path("trackId").asText(UUID.randomUUID().toString());
                        String epTitle = node.path("trackName").asText("Episode");
                        String epDesc = node.path("description").asText("Listen to full audio episode.");
                        long durationMs = node.path("trackTimeMillis").asLong(1800000L);
                        long mins = durationMs / (1000 * 60);
                        String durLabel = mins > 60 ? (mins / 60) + "h " + (mins % 60) + "m" : mins + " min";
                        String audioUrl = node.path("episodeUrl").asText("");
                        String coverUrl = node.path("artworkUrl600").asText("");
                        if (coverUrl.isBlank()) coverUrl = node.path("artworkUrl160").asText("");
                        int epNum = node.path("trackNumber").asInt(episodes.size() + 1);
                        String pubDate = node.path("releaseDate").asText("Recently added");
                        if (pubDate.length() >= 10) pubDate = pubDate.substring(0, 10);

                        List<PodcastChapterDto> chapters = generateSampleChapters(epTitle, durationMs);

                        if (!audioUrl.isBlank()) {
                            episodes.add(new PodcastEpisodeDto(
                                    epId,
                                    "pod_" + rawId,
                                    epTitle,
                                    epDesc,
                                    durLabel,
                                    durationMs,
                                    audioUrl,
                                    coverUrl,
                                    epNum,
                                    pubDate,
                                    0,
                                    chapters
                            ));
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

    public List<PodcastLanguageDto> getLanguages() {
        return List.of(
                new PodcastLanguageDto("hindi", "Hindi", "हिन्दी", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400", 1420),
                new PodcastLanguageDto("english", "English", "English", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400", 5200),
                new PodcastLanguageDto("tamil", "Tamil", "தமிழ்", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400", 860),
                new PodcastLanguageDto("telugu", "Telugu", "తెలుగు", "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400", 790),
                new PodcastLanguageDto("bengali", "Bengali", "বাংলা", "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=400", 640),
                new PodcastLanguageDto("marathi", "Marathi", "मराठी", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400", 580),
                new PodcastLanguageDto("punjabi", "Punjabi", "ਪੰਜਾਬੀ", "https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=400", 610),
                new PodcastLanguageDto("spanish", "Spanish", "Español", "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400", 1850),
                new PodcastLanguageDto("german", "German", "Deutsch", "https://images.unsplash.com/photo-1445985543469-433ecba627a0?w=400", 920),
                new PodcastLanguageDto("japanese", "Japanese", "日本語", "https://images.unsplash.com/photo-1528164344705-475426879c0d?w=400", 780),
                new PodcastLanguageDto("arabic", "Arabic", "العربية", "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400", 670)
        );
    }

    public List<PodcastCategoryDto> getCategories() {
        return List.of(
                new PodcastCategoryDto("comedy", "Comedy", "🎙️", "#D97706", "#78350F", "#D97706"),
                new PodcastCategoryDto("news", "News", "📰", "#0EA5E9", "#0C4A6E", "#0EA5E9"),
                new PodcastCategoryDto("business", "Business", "💼", "#059669", "#064E3B", "#059669"),
                new PodcastCategoryDto("education", "Education", "🧠", "#8B5CF6", "#4C1D95", "#8B5CF6"),
                new PodcastCategoryDto("technology", "Technology", "🚀", "#2563EB", "#1E3A8A", "#2563EB"),
                new PodcastCategoryDto("relationships", "Relationships", "❤️", "#EC4899", "#831843", "#EC4899"),
                new PodcastCategoryDto("motivation", "Motivation", "🔥", "#F97316", "#7C2D12", "#F97316"),
                new PodcastCategoryDto("true_crime", "True Crime", "🔎", "#DC2626", "#7F1D1D", "#DC2626"),
                new PodcastCategoryDto("stories", "Stories", "📚", "#10B981", "#064E3B", "#10B981"),
                new PodcastCategoryDto("entertainment", "Entertainment", "🎬", "#6366F1", "#312E81", "#6366F1"),
                new PodcastCategoryDto("wellness", "Wellness", "🧘", "#14B8A6", "#134E4A", "#14B8A6"),
                new PodcastCategoryDto("finance", "Finance", "💰", "#F59E0B", "#78350F", "#F59E0B"),
                new PodcastCategoryDto("science", "Science", "🧪", "#A855F7", "#581C87", "#A855F7"),
                new PodcastCategoryDto("sports", "Sports", "🏏", "#3B82F6", "#1D4ED8", "#3B82F6")
        );
    }

    private String detectLanguage(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("hindi") || lower.contains("kahani") || lower.contains("bharat") || lower.contains("desi") || lower.contains("ranveer")) return "Hindi";
        if (lower.contains("tamil") || lower.contains("chennai")) return "Tamil";
        if (lower.contains("telugu") || lower.contains("hyderabad")) return "Telugu";
        if (lower.contains("punjabi") || lower.contains("moose")) return "Punjabi";
        if (lower.contains("marathi") || lower.contains("pune") || lower.contains("mumbai")) return "Marathi";
        if (lower.contains("spanish") || lower.contains("espanol")) return "Spanish";
        if (lower.contains("deutsch") || lower.contains("german")) return "German";
        if (lower.contains("japanese")) return "Japanese";
        return "English";
    }

    private List<PodcastChapterDto> generateSampleChapters(String epTitle, long durationMs) {
        long totalSec = durationMs / 1000;
        if (totalSec <= 120) totalSec = 1800; // default 30m

        long c1 = 0;
        long c2 = Math.min(totalSec / 5, 240);
        long c3 = Math.min(totalSec / 2, 720);
        long c4 = Math.min((totalSec * 3) / 4, 1300);

        return List.of(
                new PodcastChapterDto("00:00 Introduction & Context", c1, c2),
                new PodcastChapterDto("04:00 Deep Dive & Core Discussion", c2, c3),
                new PodcastChapterDto("12:00 Guest Insights & Stories", c3, c4),
                new PodcastChapterDto("22:00 Key Takeaways & Conclusion", c4, totalSec)
        );
    }
}