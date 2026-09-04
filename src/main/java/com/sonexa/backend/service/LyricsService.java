package com.sonexa.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonexa.backend.model.dto.CatalogDtos.LyricsLineDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Automatic lyrics lookup similar to Spotify's flow (metadata match → synced LRC).
 * Spotify uses licensed Musixmatch/LyricFind; Zynera uses LRCLIB for free synced lyrics,
 * with optional Musixmatch when {@code sonexa.lyrics.musixmatch-apikey} is set.
 */
@Service
public class LyricsService {

    private static final Logger log = LoggerFactory.getLogger(LyricsService.class);
    private static final Pattern LRC_LINE = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?](.*)");
    private static final String LRCLIB_SEARCH = "https://lrclib.net/api/search";
    private static final String LRCLIB_GET = "https://lrclib.net/api/get";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${sonexa.lyrics.musixmatch-apikey:}")
    private String musixmatchApiKey;

    public record FetchedLyrics(boolean synced, List<LyricsLineDto> lines, String plainText, String source) {}

    public FetchedLyrics fetch(String title, String artist, String album, Long durationMs) {
        if (title == null || title.isBlank()) {
            return empty();
        }
        String safeArtist = artist == null || artist.isBlank() ? "Unknown" : artist;
        String safeAlbum = album == null || album.isBlank() ? title : album;
        int durationSec = durationMs != null && durationMs > 0 ? (int) (durationMs / 1000L) : 0;

        FetchedLyrics fromLrcLib = fetchFromLrcLib(title, safeArtist, safeAlbum, durationSec);
        if (fromLrcLib != null && hasContent(fromLrcLib)) {
            return fromLrcLib;
        }

        FetchedLyrics fromOvh = fetchFromLyricsOvh(title, safeArtist);
        if (fromOvh != null && hasContent(fromOvh)) {
            return fromOvh;
        }

        if (musixmatchApiKey != null && !musixmatchApiKey.isBlank()) {
            FetchedLyrics fromMx = fetchFromMusixmatch(title, safeArtist);
            if (fromMx != null && hasContent(fromMx)) {
                return fromMx;
            }
        }

        return empty();
    }

    public FetchedLyrics parseCachedPayload(String cached) {
        if (cached == null || cached.isBlank()) return empty();
        // Cached as: SONEXA_LYRICS_V1|synced=true|source=lrclib\n<plain or lrc>
        if (cached.startsWith("SONEXA_LYRICS_V1|")) {
            int nl = cached.indexOf('\n');
            String header = nl > 0 ? cached.substring(0, nl) : cached;
            String body = nl > 0 ? cached.substring(nl + 1) : "";
            boolean synced = header.contains("synced=true");
            String source = "cache";
            int srcIdx = header.indexOf("source=");
            if (srcIdx >= 0) {
                source = header.substring(srcIdx + 7).split("\\|")[0].trim();
            }
            if (synced && body.contains("[")) {
                List<LyricsLineDto> lines = parseLrc(body);
                return new FetchedLyrics(true, lines, stripLrc(body), source);
            }
            List<LyricsLineDto> lines = plainToLines(body);
            return new FetchedLyrics(false, lines, body, source);
        }
        if (cached.contains("[") && LRC_LINE.matcher(cached).find()) {
            List<LyricsLineDto> lines = parseLrc(cached);
            return new FetchedLyrics(true, lines, stripLrc(cached), "cache");
        }
        return new FetchedLyrics(false, plainToLines(cached), cached, "cache");
    }

    public String toCachePayload(FetchedLyrics lyrics) {
        if (!hasContent(lyrics)) return null;
        String body = lyrics.synced() && lyrics.lines() != null && !lyrics.lines().isEmpty()
                ? toLrc(lyrics.lines())
                : (lyrics.plainText() != null ? lyrics.plainText() : "");
        return "SONEXA_LYRICS_V1|synced=" + lyrics.synced()
                + "|source=" + (lyrics.source() != null ? lyrics.source() : "unknown")
                + "\n" + body;
    }

    private FetchedLyrics fetchFromLrcLib(String title, String artist, String album, int durationSec) {
        try {
            // 1) Prefer exact get when duration known (skip generic album names)
            boolean usefulAlbum = album != null && !album.isBlank()
                    && !album.equalsIgnoreCase("Singles")
                    && !album.equalsIgnoreCase("Unknown");
            if (durationSec > 0 && usefulAlbum) {
                URI getUri = UriComponentsBuilder.fromHttpUrl(LRCLIB_GET)
                        .queryParam("track_name", title)
                        .queryParam("artist_name", artist)
                        .queryParam("album_name", album)
                        .queryParam("duration", durationSec)
                        .build()
                        .encode()
                        .toUri();
                FetchedLyrics exact = readLrcLibNode(getJson(getUri));
                if (exact != null && hasContent(exact)) return exact;
            }

            // 2) Structured search
            FetchedLyrics fromSearch = searchLrcLib(title, artist);
            if (fromSearch != null && hasContent(fromSearch)) return fromSearch;

            // 3) Broad keyword search
            URI qUri = UriComponentsBuilder.fromHttpUrl(LRCLIB_SEARCH)
                    .queryParam("q", (title + " " + artist).trim())
                    .build()
                    .encode()
                    .toUri();
            FetchedLyrics fromQ = pickBest(getJson(qUri));
            if (fromQ != null && hasContent(fromQ)) return fromQ;

            // 4) Title-only
            URI titleUri = UriComponentsBuilder.fromHttpUrl(LRCLIB_SEARCH)
                    .queryParam("track_name", title)
                    .build()
                    .encode()
                    .toUri();
            return pickBest(getJson(titleUri));
        } catch (Exception e) {
            log.warn("LRCLIB lyrics fetch failed for {} - {}: {}", artist, title, e.getMessage());
        }
        return null;
    }

    private FetchedLyrics searchLrcLib(String title, String artist) throws Exception {
        URI searchUri = UriComponentsBuilder.fromHttpUrl(LRCLIB_SEARCH)
                .queryParam("track_name", title)
                .queryParam("artist_name", artist)
                .build()
                .encode()
                .toUri();
        return pickBest(getJson(searchUri));
    }

    private FetchedLyrics pickBest(JsonNode arr) {
        if (arr == null || !arr.isArray() || arr.isEmpty()) return null;
        JsonNode best = arr.get(0);
        for (JsonNode n : arr) {
            if (n.path("syncedLyrics").asText("").length() > 0) {
                best = n;
                break;
            }
        }
        return readLrcLibNode(best);
    }

    /** Free plain-lyrics fallback (same idea as many open players). */
    private FetchedLyrics fetchFromLyricsOvh(String title, String artist) {
        try {
            String a = java.net.URLEncoder.encode(artist, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");
            String t = java.net.URLEncoder.encode(title, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");
            URI uri = URI.create("https://api.lyrics.ovh/v1/" + a + "/" + t);
            JsonNode node = getJson(uri);
            if (node == null) return null;
            String plain = node.path("lyrics").asText("").trim();
            if (plain.isBlank()) return null;
            plain = plain.replace("\r\n", "\n").trim();
            return new FetchedLyrics(false, plainToLines(plain), plain, "lyrics.ovh");
        } catch (Exception e) {
            log.debug("lyrics.ovh miss for {} - {}: {}", artist, title, e.getMessage());
            return null;
        }
    }

    private FetchedLyrics readLrcLibNode(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        String synced = node.path("syncedLyrics").asText("");
        String plain = node.path("plainLyrics").asText("");
        if (synced != null && !synced.isBlank()) {
            List<LyricsLineDto> lines = parseLrc(synced);
            return new FetchedLyrics(true, lines, plain.isBlank() ? stripLrc(synced) : plain, "lrclib");
        }
        if (plain != null && !plain.isBlank()) {
            return new FetchedLyrics(false, plainToLines(plain), plain, "lrclib");
        }
        if (node.path("instrumental").asBoolean(false)) {
            return new FetchedLyrics(false, List.of(new LyricsLineDto(0, "♪ Instrumental")), "♪ Instrumental", "lrclib");
        }
        return null;
    }

    /**
     * Optional Musixmatch (same family of licensed provider Spotify historically used).
     * Requires free/dev API key: https://developer.musixmatch.com/
     */
    private FetchedLyrics fetchFromMusixmatch(String title, String artist) {
        try {
            URI searchUri = UriComponentsBuilder
                    .fromHttpUrl("https://api.musixmatch.com/ws/1.1/matcher.lyrics.get")
                    .queryParam("apikey", musixmatchApiKey)
                    .queryParam("q_track", title)
                    .queryParam("q_artist", artist)
                    .build()
                    .encode()
                    .toUri();
            JsonNode root = getJson(searchUri);
            if (root == null) return null;
            JsonNode body = root.path("message").path("body").path("lyrics");
            String plain = body.path("lyrics_body").asText("");
            if (plain.isBlank()) return null;
            // Musixmatch free tier appends a commercial notice — keep body usable
            plain = plain.replaceAll("\\*\\*\\*.*", "").trim();
            if (plain.isBlank()) return null;
            return new FetchedLyrics(false, plainToLines(plain), plain, "musixmatch");
        } catch (Exception e) {
            log.warn("Musixmatch lyrics fetch failed for {} - {}: {}", artist, title, e.getMessage());
            return null;
        }
    }

    private JsonNode getJson(URI uri) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "ZyneraApp/2.4 (lyrics; +https://zynera.app)");
        headers.set("Accept", "application/json");
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }
            return objectMapper.readTree(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return null;
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            if (e.getStatusCode().value() == 404) return null;
            throw e;
        }
    }

    public static List<LyricsLineDto> parseLrc(String lrc) {
        List<LyricsLineDto> lines = new ArrayList<>();
        if (lrc == null) return lines;
        for (String raw : lrc.split("\\R")) {
            Matcher m = LRC_LINE.matcher(raw.trim());
            if (!m.find()) continue;
            int min = Integer.parseInt(m.group(1));
            int sec = Integer.parseInt(m.group(2));
            String frac = m.group(3) != null ? m.group(3) : "0";
            // LRC often uses centiseconds (2 digits) or milliseconds (3)
            int msPart = Integer.parseInt(frac);
            if (frac.length() == 1) msPart *= 100;
            else if (frac.length() == 2) msPart *= 10;
            long tMs = min * 60_000L + sec * 1000L + msPart;
            String text = m.group(4) != null ? m.group(4).trim() : "";
            if (!text.isBlank()) {
                lines.add(new LyricsLineDto(tMs, text));
            }
        }
        return lines;
    }

    private static String toLrc(List<LyricsLineDto> lines) {
        StringBuilder sb = new StringBuilder();
        for (LyricsLineDto line : lines) {
            long t = Math.max(0, line.tMs());
            long min = t / 60_000;
            long sec = (t % 60_000) / 1000;
            long cs = (t % 1000) / 10;
            sb.append(String.format(Locale.US, "[%02d:%02d.%02d]%s%n", min, sec, cs, line.text()));
        }
        return sb.toString().trim();
    }

    private static String stripLrc(String lrc) {
        StringBuilder sb = new StringBuilder();
        for (String raw : lrc.split("\\R")) {
            Matcher m = LRC_LINE.matcher(raw.trim());
            if (m.find()) {
                String text = m.group(4) != null ? m.group(4).trim() : "";
                if (!text.isBlank()) sb.append(text).append('\n');
            } else if (!raw.isBlank() && !raw.startsWith("[")) {
                sb.append(raw).append('\n');
            }
        }
        return sb.toString().trim();
    }

    private static List<LyricsLineDto> plainToLines(String plain) {
        List<LyricsLineDto> lines = new ArrayList<>();
        if (plain == null || plain.isBlank()) return lines;
        long t = 0;
        for (String raw : plain.split("\\R")) {
            String text = raw.trim();
            if (text.isBlank()) continue;
            lines.add(new LyricsLineDto(t, text));
            t += 3000;
        }
        return lines;
    }

    private static boolean hasContent(FetchedLyrics lyrics) {
        return lyrics != null && (
                (lyrics.plainText() != null && !lyrics.plainText().isBlank())
                        || (lyrics.lines() != null && !lyrics.lines().isEmpty())
        );
    }

    private static FetchedLyrics empty() {
        return new FetchedLyrics(false, List.of(), "", "none");
    }
}
