package com.sonexa.backend.service.audius;

import com.fasterxml.jackson.databind.JsonNode;
import com.sonexa.backend.model.dto.CatalogDtos.ArtistDto;
import com.sonexa.backend.model.dto.CatalogDtos.PlaylistDto;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class AudiusService {

    private static final Logger log = LoggerFactory.getLogger(AudiusService.class);

    private final AudiusClient audiusClient;
    private final AudiusTrackMapper trackMapper;
    private final AudiusArtistMapper artistMapper;
    private final AudiusPlaylistMapper playlistMapper;

    public AudiusService(
            AudiusClient audiusClient,
            AudiusTrackMapper trackMapper,
            AudiusArtistMapper artistMapper,
            AudiusPlaylistMapper playlistMapper
    ) {
        this.audiusClient = audiusClient;
        this.trackMapper = trackMapper;
        this.artistMapper = artistMapper;
        this.playlistMapper = playlistMapper;
    }

    public List<TrackDto> getTrendingTracks(int limit) {
        try {
            JsonNode root = audiusClient.get("/tracks/trending", "limit=" + Math.max(limit, 10));
            if (root != null && root.hasNonNull("data")) {
                return trackMapper.mapTracks(root.get("data"), audiusClient);
            }
        } catch (Exception e) {
            log.error("Failed to fetch trending tracks from Audius: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<TrackDto> searchTracks(String query, int limit) {
        if (query == null || query.isBlank()) {
            return getTrendingTracks(limit);
        }
        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            JsonNode root = audiusClient.get("/tracks/search", "query=" + encoded + "&limit=" + Math.max(limit, 10));
            if (root != null && root.hasNonNull("data")) {
                return trackMapper.mapTracks(root.get("data"), audiusClient);
            }
        } catch (Exception e) {
            log.error("Failed to search tracks from Audius for [{}]: {}", query, e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<ArtistDto> searchArtists(String query, int limit) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            JsonNode root = audiusClient.get("/users/search", "query=" + encoded + "&limit=" + Math.max(limit, 6));
            if (root != null && root.hasNonNull("data")) {
                return artistMapper.mapArtists(root.get("data"));
            }
        } catch (Exception e) {
            log.error("Failed to search artists from Audius for [{}]: {}", query, e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<PlaylistDto> searchPlaylists(String query, int limit) {
        if (query == null || query.isBlank()) {
            return getTrendingPlaylists(limit);
        }
        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            JsonNode root = audiusClient.get("/playlists/search", "query=" + encoded + "&limit=" + Math.max(limit, 6));
            if (root != null && root.hasNonNull("data")) {
                return playlistMapper.mapPlaylists(root.get("data"));
            }
        } catch (Exception e) {
            log.error("Failed to search playlists from Audius for [{}]: {}", query, e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<PlaylistDto> getTrendingPlaylists(int limit) {
        try {
            JsonNode root = audiusClient.get("/playlists/trending", "limit=" + Math.max(limit, 8));
            if (root != null && root.hasNonNull("data")) {
                return playlistMapper.mapPlaylists(root.get("data"));
            }
        } catch (Exception e) {
            log.error("Failed to fetch trending playlists from Audius: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    public TrackDto getTrackById(String id) {
        try {
            JsonNode root = audiusClient.get("/tracks/" + id, "");
            if (root != null && root.hasNonNull("data")) {
                JsonNode trackData = root.get("data");
                String streamUrl = audiusClient.resolveStreamUrl(id);
                return trackMapper.mapTrack(trackData, streamUrl);
            }
        } catch (Exception e) {
            log.error("Failed to fetch track [{}] from Audius: {}", id, e.getMessage());
        }
        return null;
    }

    public ArtistDto getArtistById(String id) {
        try {
            JsonNode root = audiusClient.get("/users/" + id, "");
            if (root != null && root.hasNonNull("data")) {
                return artistMapper.mapArtist(root.get("data"));
            }
        } catch (Exception e) {
            log.error("Failed to fetch artist [{}] from Audius: {}", id, e.getMessage());
        }
        return null;
    }

    public List<TrackDto> getArtistTracks(String artistId, int limit) {
        try {
            JsonNode root = audiusClient.get("/users/" + artistId + "/tracks", "limit=" + Math.max(limit, 20));
            if (root != null && root.hasNonNull("data")) {
                return trackMapper.mapTracks(root.get("data"), audiusClient);
            }
        } catch (Exception e) {
            log.error("Failed to fetch artist tracks [{}] from Audius: {}", artistId, e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<TrackDto> getPlaylistTracks(String playlistId) {
        try {
            JsonNode root = audiusClient.get("/playlists/" + playlistId + "/tracks", "");
            if (root != null && root.hasNonNull("data")) {
                return trackMapper.mapTracks(root.get("data"), audiusClient);
            }
        } catch (Exception e) {
            log.error("Failed to fetch playlist tracks [{}] from Audius: {}", playlistId, e.getMessage());
        }
        return Collections.emptyList();
    }

    public String resolveStreamUrl(String trackId) {
        return audiusClient.resolveStreamUrl(trackId);
    }
}