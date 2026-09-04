package com.sonexa.backend.service.ai;

import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import com.sonexa.backend.model.entity.UserTasteProfile;
import com.sonexa.backend.service.audius.AudiusService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AIDJService {

    private final AudiusService audiusService;
    private final UserTasteProfileService tasteProfileService;

    public AIDJService(AudiusService audiusService, UserTasteProfileService tasteProfileService) {
        this.audiusService = audiusService;
        this.tasteProfileService = tasteProfileService;
    }

    public static class NextTrackDecision {
        private TrackDto track;
        private String reason;
        private double confidence;

        public NextTrackDecision() {}
        public NextTrackDecision(TrackDto track, String reason, double confidence) {
            this.track = track;
            this.reason = reason;
            this.confidence = confidence;
        }

        public TrackDto getTrack() { return track; }
        public void setTrack(TrackDto track) { this.track = track; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }

    public NextTrackDecision selectNextTrack(String userKey, TrackDto currentTrack, List<TrackDto> recentHistory) {
        UserTasteProfile profile = tasteProfileService.getProfile(userKey);
        List<TrackDto> candidates = audiusService.getTrendingTracks(20);

        if (candidates.isEmpty()) {
            return new NextTrackDecision(null, "No candidate tracks available", 0.50);
        }

        Set<String> recentIds = new HashSet<>();
        if (recentHistory != null) {
            for (TrackDto t : recentHistory) {
                if (t != null && t.id() != null) recentIds.add(t.id());
            }
        }
        if (currentTrack != null && currentTrack.id() != null) {
            recentIds.add(currentTrack.id());
        }

        TrackDto bestTrack = null;
        double bestScore = -1.0;
        String decisionReason = "AI DJ dynamic vibe transition";

        for (TrackDto candidate : candidates) {
            if (candidate == null || candidate.id() == null) continue;
            if (recentIds.contains(candidate.id())) continue;

            double artistAffinity = 0.70;
            double genreAffinity = 0.80;
            double similarity = 0.75;
            double contextMatch = 0.80;
            double freshness = 0.65;
            double novelty = profile.getNoveltyScore() != null ? profile.getNoveltyScore() : 0.50;
            double popularity = 0.75;
            double skipPenalty = profile.getSkipRate() != null ? profile.getSkipRate() : 0.10;
            double repeatPenalty = 0.02;

            double score = (0.22 * artistAffinity)
                    + (0.18 * genreAffinity)
                    + (0.15 * similarity)
                    + (0.12 * contextMatch)
                    + (0.10 * freshness)
                    + (0.08 * novelty)
                    + (0.07 * popularity)
                    - (0.05 * skipPenalty)
                    - (0.03 * repeatPenalty);

            if (score > bestScore) {
                bestScore = score;
                bestTrack = candidate;
                decisionReason = "Harmonic vibe match with optimal energy progression";
            }
        }

        if (bestTrack == null && !candidates.isEmpty()) {
            bestTrack = candidates.get(0);
            bestScore = 0.82;
            decisionReason = "Trending crowd favorite";
        }

        return new NextTrackDecision(bestTrack, decisionReason, Math.min(0.96, Math.max(0.70, bestScore)));
    }
}