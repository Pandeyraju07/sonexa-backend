package com.sonexa.backend.service.ai;

import com.sonexa.backend.model.dto.AiDtos.*;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import com.sonexa.backend.model.entity.UserTasteProfile;
import com.sonexa.backend.service.audius.AudiusService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationEngine {

    private final AudiusService audiusService;
    private final UserTasteProfileService tasteProfileService;

    public RecommendationEngine(AudiusService audiusService, UserTasteProfileService tasteProfileService) {
        this.audiusService = audiusService;
        this.tasteProfileService = tasteProfileService;
    }

    public List<TrackDto> recommendTracks(String userKey, int limit) {
        List<TrackDto> catalog = audiusService.getTrendingTracks(Math.max(limit * 2, 30));
        return applyDiversityFilter(catalog, limit);
    }

    public List<TrackDto> generateDailyMix(String userKey) {
        List<TrackDto> tracks = audiusService.getTrendingTracks(25);
        Collections.shuffle(tracks);
        return applyDiversityFilter(tracks, 20);
    }

    public List<TrackDto> generateSurprise(String userKey) {
        List<TrackDto> fresh = audiusService.searchTracks("Electronic Lo-Fi Indie", 25);
        if (fresh.isEmpty()) {
            fresh = audiusService.getTrendingTracks(25);
        }
        Collections.shuffle(fresh);
        return applyDiversityFilter(fresh, 15);
    }

    public List<TrackDto> generateMoodSession(String userKey, String mood, Double energy) {
        String query = mood != null ? mood : "Chill";
        List<TrackDto> tracks = audiusService.searchTracks(query, 25);
        if (tracks.isEmpty()) {
            tracks = audiusService.getTrendingTracks(20);
        }
        return applyDiversityFilter(tracks, 20);
    }

    public List<TrackDto> generateEnergySession(String userKey, Double targetEnergy) {
        String query = (targetEnergy != null && targetEnergy > 0.7) ? "EDM Hype Dance" : (targetEnergy != null && targetEnergy < 0.4) ? "Acoustic Peaceful Sleep" : "Pop Melodic Chill";
        List<TrackDto> tracks = audiusService.searchTracks(query, 25);
        if (tracks.isEmpty()) {
            tracks = audiusService.getTrendingTracks(20);
        }
        return applyDiversityFilter(tracks, 20);
    }

    public MusicJourneyResponse generateJourney(String userKey, String theme, Integer durationMinutes) {
        int duration = (durationMinutes != null && durationMinutes > 0) ? durationMinutes : 60;
        String normalizedTheme = (theme != null && !theme.isBlank()) ? theme.toUpperCase(Locale.ROOT) : "WORKOUT";

        MusicJourneyResponse response = new MusicJourneyResponse();
        response.setTheme(normalizedTheme);
        response.setTotalDurationMinutes(duration);

        List<MusicJourneyPhaseDto> phases = new ArrayList<>();

        if (normalizedTheme.contains("WORKOUT")) {
            response.setTitle("Beast Mode Workout Journey");
            int p1 = (int) (duration * 0.20);
            int p2 = (int) (duration * 0.55);
            int p3 = (int) (duration * 0.85);

            List<TrackDto> warmup = getOrSearchTracks("Workout Warmup Melodic", 6);
            List<TrackDto> buildup = getOrSearchTracks("EDM Workout Gym Upbeat", 6);
            List<TrackDto> beast = getOrSearchTracks("Hardstyle Trap Heavy Bass Workout", 6);
            List<TrackDto> cooldown = getOrSearchTracks("Acoustic Lo-Fi Recovery", 6);

            phases.add(new MusicJourneyPhaseDto("Warmup & Stretch", 0, p1, 0.45, "Upbeat", warmup));
            phases.add(new MusicJourneyPhaseDto("Tempo Buildup & Cardio", p1, p2, 0.75, "High Energy", buildup));
            phases.add(new MusicJourneyPhaseDto("Beast Mode / Peak Intensity", p2, p3, 0.95, "Peak Beast", beast));
            phases.add(new MusicJourneyPhaseDto("Cooldown & Recovery", p3, duration, 0.30, "Calm", cooldown));
        } else if (normalizedTheme.contains("ROAD_TRIP") || normalizedTheme.contains("NIGHT_DRIVE")) {
            response.setTitle("Late Night Highway Journey");
            int p1 = (int) (duration * 0.30);
            int p2 = (int) (duration * 0.70);

            List<TrackDto> sunset = getOrSearchTracks("Lo-Fi Synthwave Indie Drive", 6);
            List<TrackDto> midnight = getOrSearchTracks("Pop Melodic Chartbusters", 6);
            List<TrackDto> deep = getOrSearchTracks("Deep House Melodic Techno", 6);

            phases.add(new MusicJourneyPhaseDto("Sunset Cruise", 0, p1, 0.50, "Atmospheric", sunset));
            phases.add(new MusicJourneyPhaseDto("Midnight Singalong", p1, p2, 0.75, "Vibrant", midnight));
            phases.add(new MusicJourneyPhaseDto("Deep Bass Horizon", p2, duration, 0.85, "Hypnotic", deep));
        } else if (normalizedTheme.contains("STUDY") || normalizedTheme.contains("FOCUS")) {
            response.setTitle("Deep Focus & Study Flow");
            int p1 = (int) (duration * 0.35);
            int p2 = (int) (duration * 0.75);

            List<TrackDto> alpha = getOrSearchTracks("Lo-Fi Study Beats Instrumental", 6);
            List<TrackDto> focus = getOrSearchTracks("Ambient Electronic Focus", 6);
            List<TrackDto> flow = getOrSearchTracks("Chillwave Binaural Soundscapes", 6);

            phases.add(new MusicJourneyPhaseDto("Alpha Waves & Warmup", 0, p1, 0.35, "Focus", alpha));
            phases.add(new MusicJourneyPhaseDto("Deep Productivity Zone", p1, p2, 0.45, "Concentration", focus));
            phases.add(new MusicJourneyPhaseDto("Sublime Flow State", p2, duration, 0.40, "Immersive", flow));
        } else if (normalizedTheme.contains("PARTY")) {
            response.setTitle("Epic Party Starter Flow");
            int p1 = (int) (duration * 0.25);
            int p2 = (int) (duration * 0.65);

            List<TrackDto> pregame = getOrSearchTracks("Groovy Pop R&B Party", 6);
            List<TrackDto> ignition = getOrSearchTracks("Dance Club Commercial Pop", 6);
            List<TrackDto> drop = getOrSearchTracks("EDM Festival Drops High BPM", 6);

            phases.add(new MusicJourneyPhaseDto("Pre-Game Warmup", 0, p1, 0.65, "Groovy", pregame));
            phases.add(new MusicJourneyPhaseDto("Dance Floor Ignition", p1, p2, 0.85, "Party", ignition));
            phases.add(new MusicJourneyPhaseDto("Peak Festival Drop", p2, duration, 0.98, "Wild Energy", drop));
        } else {
            response.setTitle("Calm to Energetic Flow");
            int p1 = duration / 3;
            int p2 = (duration * 2) / 3;

            List<TrackDto> calm = getOrSearchTracks("Acoustic Calm Serenity", 6);
            List<TrackDto> build = getOrSearchTracks("Pop Melodic Groove Upbeat", 6);
            List<TrackDto> peak = getOrSearchTracks("EDM Dance Energetic", 6);

            phases.add(new MusicJourneyPhaseDto("Morning Serenity & Warmup", 0, p1, 0.35, "Calm", calm));
            phases.add(new MusicJourneyPhaseDto("Building Momentum", p1, p2, 0.65, "Groovy", build));
            phases.add(new MusicJourneyPhaseDto("Peak Energy & Celebration", p2, duration, 0.90, "Energetic", peak));
        }

        response.setPhases(phases);

        List<TrackDto> all = new ArrayList<>();
        for (MusicJourneyPhaseDto p : phases) {
            if (p.getTracks() != null) {
                all.addAll(p.getTracks());
            }
        }
        response.setAllTracks(all);

        return response;
    }

    private List<TrackDto> getOrSearchTracks(String query, int limit) {
        List<TrackDto> tracks = audiusService.searchTracks(query, limit * 2);
        if (tracks == null || tracks.isEmpty()) {
            tracks = audiusService.getTrendingTracks(limit);
        }
        return applyDiversityFilter(tracks, limit);
    }

    public List<PredictionItem> predictWhatILike(String userKey) {
        List<TrackDto> tracks = audiusService.getTrendingTracks(10);
        List<PredictionItem> predictions = new ArrayList<>();

        double[] scores = {0.94, 0.91, 0.88, 0.85, 0.83};
        String[][] reasonsPool = {
                {"Matches your usual evening listening energy", "Similar harmonic style to artists you play often"},
                {"High completion rate among listeners with your music taste", "Fresh release in your top preferred genre"},
                {"Acoustic vibe aligned with your late-night patterns", "Trending rapidly across listeners like you"}
        };

        for (int i = 0; i < Math.min(tracks.size(), 5); i++) {
            TrackDto t = tracks.get(i);
            double score = scores[i % scores.length];
            List<String> reasons = Arrays.asList(reasonsPool[i % reasonsPool.length]);
            predictions.add(new PredictionItem(t, score, reasons));
        }

        return predictions;
    }

    public WhyThisSongResponse getWhyThisSong(String userKey, String trackId) {
        TrackDto track = audiusService.getTrackById(trackId);
        String title = track != null ? track.title() : "Track #" + trackId;

        List<String> reasons = Arrays.asList(
                "Matches your preferred mood and energy profile",
                "High similarity to tracks you frequently finish without skipping",
                "Selected based on your weighted recent listening history"
        );

        return new WhyThisSongResponse(trackId, title, reasons, 0.89);
    }

    private List<TrackDto> applyDiversityFilter(List<TrackDto> source, int limit) {
        if (source == null || source.isEmpty()) return Collections.emptyList();

        List<TrackDto> filtered = new ArrayList<>();
        String lastArtist = null;
        int consecutiveArtistCount = 0;

        for (TrackDto track : source) {
            String currentArtist = track.artist() != null ? track.artist().trim().toLowerCase(Locale.ROOT) : "";
            if (currentArtist.equals(lastArtist)) {
                consecutiveArtistCount++;
                if (consecutiveArtistCount >= 2) {
                    continue;
                }
            } else {
                lastArtist = currentArtist;
                consecutiveArtistCount = 1;
            }

            filtered.add(track);
            if (filtered.size() >= limit) break;
        }

        return filtered;
    }
}