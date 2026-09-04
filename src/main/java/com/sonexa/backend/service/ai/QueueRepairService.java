package com.sonexa.backend.service.ai;

import com.sonexa.backend.model.dto.AiDtos.FixQueueResponse;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QueueRepairService {

    public FixQueueResponse fixQueue(List<TrackDto> originalQueue) {
        if (originalQueue == null || originalQueue.isEmpty()) {
            return new FixQueueResponse(Collections.emptyList(), 0, "Queue is empty");
        }

        Set<String> seenIds = new HashSet<>();
        List<TrackDto> deduplicated = new ArrayList<>();
        int removedDuplicates = 0;

        for (TrackDto t : originalQueue) {
            if (t == null || t.id() == null) continue;
            if (seenIds.add(t.id())) {
                deduplicated.add(t);
            } else {
                removedDuplicates++;
            }
        }

        List<TrackDto> balanced = new ArrayList<>();
        Map<String, List<TrackDto>> byArtist = new LinkedHashMap<>();

        for (TrackDto t : deduplicated) {
            String art = t.artist() != null ? t.artist().trim().toLowerCase(Locale.ROOT) : "unknown";
            byArtist.computeIfAbsent(art, k -> new ArrayList<>()).add(t);
        }

        boolean hasMore = true;
        while (hasMore) {
            hasMore = false;
            for (List<TrackDto> artistTracks : byArtist.values()) {
                if (!artistTracks.isEmpty()) {
                    balanced.add(artistTracks.remove(0));
                    if (!artistTracks.isEmpty()) hasMore = true;
                }
            }
        }

        String summary = String.format("Balanced queue: removed %d duplicate(s) and distributed artists for smooth energy flow.", removedDuplicates);
        return new FixQueueResponse(balanced, removedDuplicates, summary);
    }
}