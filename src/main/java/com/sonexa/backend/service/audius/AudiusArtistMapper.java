package com.sonexa.backend.service.audius;

import com.fasterxml.jackson.databind.JsonNode;
import com.sonexa.backend.model.dto.CatalogDtos.ArtistDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AudiusArtistMapper {

    public ArtistDto mapArtist(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        String id = node.path("id").asText("");
        if (id.isBlank()) {
            id = String.valueOf(node.path("user_id").asLong(0));
        }

        String name = node.path("name").asText("").trim();
        if (name.isBlank()) {
            name = node.path("handle").asText("Audius Artist");
        }

        String bio = node.path("bio").asText("Official TuneFlow Creator");
        boolean verified = node.path("is_verified").asBoolean(false);
        int followersCount = node.path("follower_count").asInt(0);

        String imageUrl = "";
        JsonNode profilePic = node.path("profile_picture");
        if (profilePic != null && !profilePic.isNull()) {
            if (profilePic.hasNonNull("480x480")) {
                imageUrl = profilePic.path("480x480").asText("");
            } else if (profilePic.hasNonNull("1000x1000")) {
                imageUrl = profilePic.path("1000x1000").asText("");
            } else if (profilePic.hasNonNull("150x150")) {
                imageUrl = profilePic.path("150x150").asText("");
            }
        }

        if (imageUrl.isBlank()) {
            imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500";
        }

        return new ArtistDto(
                id,
                name,
                "Audius Verified",
                bio,
                imageUrl,
                "#7C3AED",
                "#EC4899",
                followersCount,
                verified
        );
    }

    public List<ArtistDto> mapArtists(JsonNode dataNode) {
        List<ArtistDto> list = new ArrayList<>();
        if (dataNode == null || !dataNode.isArray()) {
            return list;
        }

        for (JsonNode item : dataNode) {
            ArtistDto dto = mapArtist(item);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }
}