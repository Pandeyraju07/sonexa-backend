package com.sonexa.backend.service.ai;

import com.sonexa.backend.model.dto.AiDtos.RabbitHoleNode;
import com.sonexa.backend.model.dto.AiDtos.RabbitHoleResponse;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import com.sonexa.backend.service.audius.AudiusService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RabbitHoleService {

    private final AudiusService audiusService;

    public RabbitHoleService(AudiusService audiusService) {
        this.audiusService = audiusService;
    }

    public RabbitHoleResponse getRabbitHole(String type, String id) {
        RabbitHoleResponse response = new RabbitHoleResponse();
        response.setRootId(id);
        response.setRootType(type);

        List<RabbitHoleNode> nodes = new ArrayList<>();
        nodes.add(new RabbitHoleNode(id, "Origin Artist", "ARTIST", ""));
        nodes.add(new RabbitHoleNode("node_genre", "Indie Pop & Electronic", "GENRE", ""));
        nodes.add(new RabbitHoleNode("node_collab", "Frequent Collaborators", "ARTIST", ""));
        nodes.add(new RabbitHoleNode("node_producer", "Signature Synth Producers", "PRODUCER", ""));
        nodes.add(new RabbitHoleNode("node_next_gen", "Emerging Lo-Fi Wave", "GENRE", ""));
        response.setNodes(nodes);

        List<TrackDto> tracks = audiusService.getTrendingTracks(10);
        response.setRecommendedTracks(tracks);

        return response;
    }
}