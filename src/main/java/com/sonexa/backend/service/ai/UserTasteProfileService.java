package com.sonexa.backend.service.ai;

import com.sonexa.backend.model.dto.AiDtos.UserEventDto;
import com.sonexa.backend.model.entity.UserEvent;
import com.sonexa.backend.model.entity.UserTasteProfile;
import com.sonexa.backend.repository.UserEventRepository;
import com.sonexa.backend.repository.UserTasteProfileRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserTasteProfileService {

    private final UserEventRepository eventRepository;
    private final UserTasteProfileRepository tasteProfileRepository;

    public UserTasteProfileService(UserEventRepository eventRepository, UserTasteProfileRepository tasteProfileRepository) {
        this.eventRepository = eventRepository;
        this.tasteProfileRepository = tasteProfileRepository;
    }

    @Transactional
    public void recordEvent(UserEventDto dto) {
        String key = (dto.getUserKey() != null && !dto.getUserKey().trim().isEmpty()) ? dto.getUserKey() : "guest_user";
        UserEvent event = new UserEvent(
                key,
                dto.getEventType(),
                dto.getTrackId(),
                dto.getTrackTitle(),
                dto.getArtist(),
                dto.getGenre(),
                dto.getLanguage(),
                dto.getMood(),
                dto.getEnergy(),
                dto.getMetadataJson()
        );
        eventRepository.save(event);

        // Update profile in background asynchronously
        updateProfileFromEvents(key);
    }

    @Async
    public void updateProfileFromEvents(String userKey) {
        try {
            UserTasteProfile profile = tasteProfileRepository.findByUserKey(userKey)
                    .orElseGet(() -> new UserTasteProfile(userKey));

            List<UserEvent> events = eventRepository.findTop100ByUserKeyOrderByTimestampDesc(userKey);
            if (events.isEmpty()) return;

            int skips = 0;
            int completions = 0;
            int totalPlays = 0;
            double energySum = 0;
            int energyCount = 0;

            for (UserEvent ev : events) {
                if ("SKIP".equalsIgnoreCase(ev.getEventType())) {
                    skips++;
                } else if ("PLAY_COMPLETED".equalsIgnoreCase(ev.getEventType())) {
                    completions++;
                    totalPlays++;
                } else if ("PLAY_30_SECONDS".equalsIgnoreCase(ev.getEventType()) || "PLAY_STARTED".equalsIgnoreCase(ev.getEventType())) {
                    totalPlays++;
                }
                if (ev.getEnergy() != null) {
                    energySum += ev.getEnergy();
                    energyCount++;
                }
            }

            if (totalPlays > 0) {
                profile.setSkipRate(Math.min(1.0, (double) skips / Math.max(1, totalPlays)));
                profile.setCompletionRate(Math.min(1.0, (double) completions / Math.max(1, totalPlays)));
            }
            if (energyCount > 0) {
                profile.setAverageEnergy(energySum / energyCount);
            }

            // Determine Personality Type
            if (profile.getNoveltyScore() > 0.65) {
                profile.setPersonalityType("Trailblazer");
            } else if (profile.getAverageEnergy() > 0.70) {
                profile.setPersonalityType("Beast Mode");
            } else if (profile.getNostalgiaScore() > 0.70) {
                profile.setPersonalityType("Purist");
            } else {
                profile.setPersonalityType("Explorer");
            }

            profile.setLastUpdated(LocalDateTime.now());
            tasteProfileRepository.save(profile);
        } catch (Exception ignored) {
        }
    }

    public UserTasteProfile getProfile(String userKey) {
        return tasteProfileRepository.findByUserKey(userKey)
                .orElseGet(() -> new UserTasteProfile(userKey));
    }
}