package com.sonexa.backend.service.audius;

import com.fasterxml.jackson.databind.JsonNode;
import com.sonexa.backend.model.dto.CatalogDtos.PlaylistDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AudiusPlaylistMapper {

    public PlaylistDto mapPlaylist(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        String id = node.path("id").asText("");
        if (id.isBlank()) {
            id = String.valueOf(node.path("playlist_id").asLong(0));
        }

        String title = node.path("playlist_name").asText("Trending Playlist").trim();
        String description = node.path("description").asText("Curated with TuneFlow").trim();

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
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500";
        }

        return new PlaylistDto(
                id,
                title,
                description,
                "gradient",
                coverUrl
        );
    }

    public List<PlaylistDto> mapPlaylists(JsonNode dataNode) {
        List<PlaylistDto> list = new ArrayList<>();
        if (dataNode == null || !dataNode.isArray()) {
            return list;
        }

        for (JsonNode item : dataNode) {
            PlaylistDto dto = mapPlaylist(item);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }
}