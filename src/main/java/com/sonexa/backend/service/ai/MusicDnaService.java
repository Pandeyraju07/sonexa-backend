package com.sonexa.backend.service.ai;

import com.sonexa.backend.model.dto.AiDtos.ListeningInsightsResponse;
import com.sonexa.backend.model.dto.AiDtos.MusicDnaResponse;
import com.sonexa.backend.model.entity.UserTasteProfile;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MusicDnaService {

    private final UserTasteProfileService tasteProfileService;

    public MusicDnaService(UserTasteProfileService tasteProfileService) {
        this.tasteProfileService = tasteProfileService;
    }

    public MusicDnaResponse getMusicDna(String userKey) {
        UserTasteProfile profile = tasteProfileService.getProfile(userKey);

        MusicDnaResponse dna = new MusicDnaResponse();
        dna.setPersonality(profile.getPersonalityType() != null ? profile.getPersonalityType() : "Explorer");
        dna.setEnergy((int) (profile.getAverageEnergy() * 100));
        dna.setDiscovery((int) (profile.getDiscoveryScore() * 100));
        dna.setNostalgia((int) (profile.getNostalgiaScore() * 100));
        dna.setRomance((int) (profile.getRomanceScore() * 100));
        dna.setMainstream((int) (profile.getMainstreamScore() * 100));

        Map<String, Double> topGenres = new LinkedHashMap<>();
        topGenres.put("Pop", 0.85);
        topGenres.put("Bollywood", 0.78);
        topGenres.put("Acoustic", 0.65);
        topGenres.put("Lo-Fi", 0.52);
        dna.setTopGenres(topGenres);

        Map<String, Double> topLanguages = new LinkedHashMap<>();
        topLanguages.put("Hindi", 0.82);
        topLanguages.put("English", 0.70);
        topLanguages.put("Punjabi", 0.45);
        dna.setTopLanguages(topLanguages);

        Map<String, Double> topArtists = new LinkedHashMap<>();
        topArtists.put("Arijit Singh", 0.90);
        topArtists.put("The Weeknd", 0.75);
        topArtists.put("Taylor Swift", 0.68);
        dna.setTopArtists(topArtists);

        dna.setSummaryText(String.format("You are a '%s' listener. You gravitate towards heartfelt melodies with balanced dynamic energy.", dna.getPersonality()));

        return dna;
    }

    public ListeningInsightsResponse getListeningInsights(String userKey) {
        UserTasteProfile profile = tasteProfileService.getProfile(userKey);

        ListeningInsightsResponse insights = new ListeningInsightsResponse();
        insights.setTotalMinutes(3640);
        insights.setTopArtists(Arrays.asList("Arijit Singh", "The Weeknd", "AP Dhillon", "Pritam"));
        insights.setTopGenres(Arrays.asList("Bollywood Romantic", "Indie Pop", "Acoustic", "EDM"));
        insights.setTopLanguages(Arrays.asList("Hindi", "English", "Punjabi"));
        insights.setPeakListeningHour("10 PM - 1 AM");
        insights.setSkipRate(profile.getSkipRate() != null ? profile.getSkipRate() : 0.09);
        insights.setCompletionRate(profile.getCompletionRate() != null ? profile.getCompletionRate() : 0.91);
        insights.setDiscoveryRate(0.48);
        insights.setFavoriteMood("Romantic");

        return insights;
    }
}