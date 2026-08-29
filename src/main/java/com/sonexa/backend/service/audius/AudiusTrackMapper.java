package com.sonexa.backend.service.audius;

import com.fasterxml.jackson.databind.JsonNode;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AudiusTrackMapper {

    public TrackDto mapTrack(JsonNode node, String streamUrl) {
        if (node == null || node.isNull()) {
            return null;
        }

        String id = node.path("id").asText("");
        if (id.isBlank()) {
            id = String.valueOf(node.path("track_id").asLong(0));
        }

        String title = node.path("title").asText("Untitled Track").trim();
        String genre = node.path("genre").asText("Music");
        long durationSec = node.path("duration").asLong(180);
        long durationMs = durationSec * 1000L;
        long playCount = node.path("play_count").asLong(0);
        String playsCountFormatted = playCount > 0 ? formatPlays(playCount) : "10K+";

        JsonNode userNode = node.path("user");
        String artist = "Audius Artist";
        if (userNode != null && !userNode.isNull()) {
            String name = userNode.path("name").asText("").trim();
            if (!name.isBlank()) {
                artist = name;
            } else {
                artist = userNode.path("handle").asText("Audius Artist");
            }
        }

        String coverUrl = "";
        JsonNode artwork = node.path("artwork");
        if (artwork != null && !artwork.isNull()) {
            if (artwork.hasNonNull("480x480")) {
                coverUrl = artwork.path("480x480").asText("");
            } else if (artwork.hasNonNull("1000x1000")) {
                coverUrl = artwork.path("1000x1000").asText("");
            } else if (artwork.hasNonNull("150x150")) {
                coverUrl = artwork.path("150x150").asText("");
            }
        }

        if (coverUrl.isBlank()) {
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500";
        }

        String effectiveAudioUrl = (streamUrl != null && !streamUrl.isBlank())
                ? streamUrl
                : "https://discoveryprovider.audius.co/v1/tracks/" + id + "/stream?app_name=TuneFlow";

        boolean isStreamable = node.path("is_streamable").asBoolean(true);

        return new TrackDto(
                id,
                title,
                artist,
                genre,
                durationMs,
                effectiveAudioUrl,
                coverUrl,
                playsCountFormatted,
                false,
                "audius",
                id,
                null,
                effectiveAudioUrl,
                isStreamable,
                "AUDIO",
                artist,
                true
        );
    }

    public List<TrackDto> mapTracks(JsonNode dataNode, AudiusClient audiusClient) {
        List<TrackDto> list = new ArrayList<>();
        if (dataNode == null || !dataNode.isArray()) {
            return list;
        }

        for (JsonNode item : dataNode) {
            String id = item.path("id").asText("");
            if (id.isBlank()) {
                id = String.valueOf(item.path("track_id").asLong(0));
            }
            String streamUrl = audiusClient.resolveStreamUrl(id);
            TrackDto dto = mapTrack(item, streamUrl);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }

    private String formatPlays(long plays) {
        if (plays >= 1_000_000) {
            return String.format("%.1fM", plays / 1_000_000.0);
        } else if (plays >= 1_000) {
            return String.format("%.1fK", plays / 1_000.0);
        }
        return String.valueOf(plays);
    }
}