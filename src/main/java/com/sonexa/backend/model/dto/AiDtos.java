package com.sonexa.backend.model.dto;

import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AiDtos {

    public static class MusicIntent {
        private String intentType = "PLAY_MUSIC"; // PLAY_MUSIC, SEARCH, NEXT, PAUSE, LIKE, ADD_TO_PLAYLIST, CHANGE_VIBE, CREATE_PLAYLIST, START_JOURNEY, FIX_QUEUE, SURPRISE
        private String query = "";
        private String artist;
        private String track;
        private String album;
        private List<String> genres = new ArrayList<>();
        private List<String> languages = new ArrayList<>();
        private List<String> moods = new ArrayList<>();
        private Double energy; // 0.0 to 1.0
        private Integer durationMinutes;
        private String activity; // WORKOUT, STUDY, ROAD_TRIP, SLEEP, PARTY, RELAX
        private String era; // 2010s, 90s, 2020s, RETRO
        private String action; // PLAY, QUEUE, SHUFFLE, FIX
        private Double confidence = 0.90;

        public MusicIntent() {}

        public String getIntentType() { return intentType; }
        public void setIntentType(String intentType) { this.intentType = intentType; }
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public String getArtist() { return artist; }
        public void setArtist(String artist) { this.artist = artist; }
        public String getTrack() { return track; }
        public void setTrack(String track) { this.track = track; }
        public String getAlbum() { return album; }
        public void setAlbum(String album) { this.album = album; }
        public List<String> getGenres() { return genres; }
        public void setGenres(List<String> genres) { this.genres = genres; }
        public List<String> getLanguages() { return languages; }
        public void setLanguages(List<String> languages) { this.languages = languages; }
        public List<String> getMoods() { return moods; }
        public void setMoods(List<String> moods) { this.moods = moods; }
        public Double getEnergy() { return energy; }
        public void setEnergy(Double energy) { this.energy = energy; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public String getActivity() { return activity; }
        public void setActivity(String activity) { this.activity = activity; }
        public String getEra() { return era; }
        public void setEra(String era) { this.era = era; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
    }

    public static class IntentParseRequest {
        private String text;
        private String userKey;
        private String currentTrackId;

        public IntentParseRequest() {}
        public IntentParseRequest(String text) { this.text = text; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getUserKey() { return userKey; }
        public void setUserKey(String userKey) { this.userKey = userKey; }
        public String getCurrentTrackId() { return currentTrackId; }
        public void setCurrentTrackId(String currentTrackId) { this.currentTrackId = currentTrackId; }
    }

    public static class ChangeVibeRequest {
        private String userKey;
        private String vibe; // MORE_ENERGETIC, MORE_RELAXING, MORE_ROMANTIC, MORE_EMOTIONAL, MORE_PARTY, MORE_NEW, MORE_ACOUSTIC
        private List<TrackDto> currentQueue = new ArrayList<>();
        private TrackDto currentTrack;

        public ChangeVibeRequest() {}

        public String getUserKey() { return userKey; }
        public void setUserKey(String userKey) { this.userKey = userKey; }
        public String getVibe() { return vibe; }
        public void setVibe(String vibe) { this.vibe = vibe; }
        public List<TrackDto> getCurrentQueue() { return currentQueue; }
        public void setCurrentQueue(List<TrackDto> currentQueue) { this.currentQueue = currentQueue; }
        public TrackDto getCurrentTrack() { return currentTrack; }
        public void setCurrentTrack(TrackDto currentTrack) { this.currentTrack = currentTrack; }
    }

    public static class ChangeVibeResponse {
        private String newVibe;
        private Double targetEnergy;
        private List<TrackDto> reorderedQueue = new ArrayList<>();
        private String explanation;

        public ChangeVibeResponse() {}
        public ChangeVibeResponse(String newVibe, Double targetEnergy, List<TrackDto> reorderedQueue, String explanation) {
            this.newVibe = newVibe;
            this.targetEnergy = targetEnergy;
            this.reorderedQueue = reorderedQueue;
            this.explanation = explanation;
        }

        public String getNewVibe() { return newVibe; }
        public void setNewVibe(String newVibe) { this.newVibe = newVibe; }
        public Double getTargetEnergy() { return targetEnergy; }
        public void setTargetEnergy(Double targetEnergy) { this.targetEnergy = targetEnergy; }
        public List<TrackDto> getReorderedQueue() { return reorderedQueue; }
        public void setReorderedQueue(List<TrackDto> reorderedQueue) { this.reorderedQueue = reorderedQueue; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }

    public static class FixQueueRequest {
        private String userKey;
        private List<TrackDto> queue = new ArrayList<>();

        public FixQueueRequest() {}
        public String getUserKey() { return userKey; }
        public void setUserKey(String userKey) { this.userKey = userKey; }
        public List<TrackDto> getQueue() { return queue; }
        public void setQueue(List<TrackDto> queue) { this.queue = queue; }
    }

    public static class FixQueueResponse {
        private List<TrackDto> balancedQueue = new ArrayList<>();
        private int removedDuplicatesCount;
        private String balanceSummary;

        public FixQueueResponse() {}
        public FixQueueResponse(List<TrackDto> balancedQueue, int removedDuplicatesCount, String balanceSummary) {
            this.balancedQueue = balancedQueue;
            this.removedDuplicatesCount = removedDuplicatesCount;
            this.balanceSummary = balanceSummary;
        }

        public List<TrackDto> getBalancedQueue() { return balancedQueue; }
        public void setBalancedQueue(List<TrackDto> balancedQueue) { this.balancedQueue = balancedQueue; }
        public int getRemovedDuplicatesCount() { return removedDuplicatesCount; }
        public void setRemovedDuplicatesCount(int removedDuplicatesCount) { this.removedDuplicatesCount = removedDuplicatesCount; }
        public String getBalanceSummary() { return balanceSummary; }
        public void setBalanceSummary(String balanceSummary) { this.balanceSummary = balanceSummary; }
    }

    public static class MusicJourneyPhaseDto {
        private String name; // e.g. Warmup, Build, Peak, Wind Down
        private int startMinute;
        private int endMinute;
        private Double targetEnergy;
        private String mood;
        private List<TrackDto> tracks = new ArrayList<>();

        public MusicJourneyPhaseDto() {}
        public MusicJourneyPhaseDto(String name, int startMinute, int endMinute, Double targetEnergy, String mood, List<TrackDto> tracks) {
            this.name = name;
            this.startMinute = startMinute;
            this.endMinute = endMinute;
            this.targetEnergy = targetEnergy;
            this.mood = mood;
            this.tracks = tracks;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getStartMinute() { return startMinute; }
        public void setStartMinute(int startMinute) { this.startMinute = startMinute; }
        public int getEndMinute() { return endMinute; }
        public void setEndMinute(int endMinute) { this.endMinute = endMinute; }
        public Double getTargetEnergy() { return targetEnergy; }
        public void setTargetEnergy(Double targetEnergy) { this.targetEnergy = targetEnergy; }
        public String getMood() { return mood; }
        public void setMood(String mood) { this.mood = mood; }
        public List<TrackDto> getTracks() { return tracks; }
        public void setTracks(List<TrackDto> tracks) { this.tracks = tracks; }
    }

    public static class MusicJourneyResponse {
        private String title;
        private String theme;
        private int totalDurationMinutes;
        private List<MusicJourneyPhaseDto> phases = new ArrayList<>();
        private List<TrackDto> allTracks = new ArrayList<>();

        public MusicJourneyResponse() {}

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        public int getTotalDurationMinutes() { return totalDurationMinutes; }
        public void setTotalDurationMinutes(int totalDurationMinutes) { this.totalDurationMinutes = totalDurationMinutes; }
        public List<MusicJourneyPhaseDto> getPhases() { return phases; }
        public void setPhases(List<MusicJourneyPhaseDto> phases) { this.phases = phases; }
        public List<TrackDto> getAllTracks() { return allTracks; }
        public void setAllTracks(List<TrackDto> allTracks) { this.allTracks = allTracks; }
    }

    public static class MusicDnaResponse {
        private String personality = "Explorer";
        private int energy = 72;
        private int discovery = 64;
        private int nostalgia = 81;
        private int romance = 58;
        private int mainstream = 42;
        private Map<String, Double> topGenres;
        private Map<String, Double> topLanguages;
        private Map<String, Double> topArtists;
        private String summaryText;

        public MusicDnaResponse() {}

        public String getPersonality() { return personality; }
        public void setPersonality(String personality) { this.personality = personality; }
        public int getEnergy() { return energy; }
        public void setEnergy(int energy) { this.energy = energy; }
        public int getDiscovery() { return discovery; }
        public void setDiscovery(int discovery) { this.discovery = discovery; }
        public int getNostalgia() { return nostalgia; }
        public void setNostalgia(int nostalgia) { this.nostalgia = nostalgia; }
        public int getRomance() { return romance; }
        public void setRomance(int romance) { this.romance = romance; }
        public int getMainstream() { return mainstream; }
        public void setMainstream(int mainstream) { this.mainstream = mainstream; }
        public Map<String, Double> getTopGenres() { return topGenres; }
        public void setTopGenres(Map<String, Double> topGenres) { this.topGenres = topGenres; }
        public Map<String, Double> getTopLanguages() { return topLanguages; }
        public void setTopLanguages(Map<String, Double> topLanguages) { this.topLanguages = topLanguages; }
        public Map<String, Double> getTopArtists() { return topArtists; }
        public void setTopArtists(Map<String, Double> topArtists) { this.topArtists = topArtists; }
        public String getSummaryText() { return summaryText; }
        public void setSummaryText(String summaryText) { this.summaryText = summaryText; }
    }

    public static class ListeningInsightsResponse {
        private int totalMinutes = 4820;
        private List<String> topArtists = new ArrayList<>();
        private List<String> topGenres = new ArrayList<>();
        private List<String> topLanguages = new ArrayList<>();
        private String peakListeningHour = "10 PM - 12 AM";
        private double skipRate = 0.08;
        private double completionRate = 0.92;
        private double discoveryRate = 0.45;
        private String favoriteMood = "Romantic";

        public ListeningInsightsResponse() {}

        public int getTotalMinutes() { return totalMinutes; }
        public void setTotalMinutes(int totalMinutes) { this.totalMinutes = totalMinutes; }
        public List<String> getTopArtists() { return topArtists; }
        public void setTopArtists(List<String> topArtists) { this.topArtists = topArtists; }
        public List<String> getTopGenres() { return topGenres; }
        public void setTopGenres(List<String> topGenres) { this.topGenres = topGenres; }
        public List<String> getTopLanguages() { return topLanguages; }
        public void setTopLanguages(List<String> topLanguages) { this.topLanguages = topLanguages; }
        public String getPeakListeningHour() { return peakListeningHour; }
        public void setPeakListeningHour(String peakListeningHour) { this.peakListeningHour = peakListeningHour; }
        public double getSkipRate() { return skipRate; }
        public void setSkipRate(double skipRate) { this.skipRate = skipRate; }
        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
        public double getDiscoveryRate() { return discoveryRate; }
        public void setDiscoveryRate(double discoveryRate) { this.discoveryRate = discoveryRate; }
        public String getFavoriteMood() { return favoriteMood; }
        public void setFavoriteMood(String favoriteMood) { this.favoriteMood = favoriteMood; }
    }

    public static class PredictionItem {
        private TrackDto track;
        private double matchScore; // 0.0 to 1.0
        private List<String> reasons = new ArrayList<>();

        public PredictionItem() {}
        public PredictionItem(TrackDto track, double matchScore, List<String> reasons) {
            this.track = track;
            this.matchScore = matchScore;
            this.reasons = reasons;
        }

        public TrackDto getTrack() { return track; }
        public void setTrack(TrackDto track) { this.track = track; }
        public double getMatchScore() { return matchScore; }
        public void setMatchScore(double matchScore) { this.matchScore = matchScore; }
        public List<String> getReasons() { return reasons; }
        public void setReasons(List<String> reasons) { this.reasons = reasons; }
    }

    public static class WhyThisSongResponse {
        private String trackId;
        private String trackTitle;
        private List<String> reasons = new ArrayList<>();
        private double affinityScore;

        public WhyThisSongResponse() {}
        public WhyThisSongResponse(String trackId, String trackTitle, List<String> reasons, double affinityScore) {
            this.trackId = trackId;
            this.trackTitle = trackTitle;
            this.reasons = reasons;
            this.affinityScore = affinityScore;
        }

        public String getTrackId() { return trackId; }
        public void setTrackId(String trackId) { this.trackId = trackId; }
        public String getTrackTitle() { return trackTitle; }
        public void setTrackTitle(String trackTitle) { this.trackTitle = trackTitle; }
        public List<String> getReasons() { return reasons; }
        public void setReasons(List<String> reasons) { this.reasons = reasons; }
        public double getAffinityScore() { return affinityScore; }
        public void setAffinityScore(double affinityScore) { this.affinityScore = affinityScore; }
    }

    public static class UserEventDto {
        private String userKey;
        private String eventType;
        private String trackId;
        private String trackTitle;
        private String artist;
        private String genre;
        private String language;
        private String mood;
        private Double energy;
        private String metadataJson;

        public UserEventDto() {}

        public String getUserKey() { return userKey; }
        public void setUserKey(String userKey) { this.userKey = userKey; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getTrackId() { return trackId; }
        public void setTrackId(String trackId) { this.trackId = trackId; }
        public String getTrackTitle() { return trackTitle; }
        public void setTrackTitle(String trackTitle) { this.trackTitle = trackTitle; }
        public String getArtist() { return artist; }
        public void setArtist(String artist) { this.artist = artist; }
        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getMood() { return mood; }
        public void setMood(String mood) { this.mood = mood; }
        public Double getEnergy() { return energy; }
        public void setEnergy(Double energy) { this.energy = energy; }
        public String getMetadataJson() { return metadataJson; }
        public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
    }

    public static class VoiceSearchRequest {
        private String userKey;
        private String transcript;
        private String language;

        public VoiceSearchRequest() {}
        public String getUserKey() { return userKey; }
        public void setUserKey(String userKey) { this.userKey = userKey; }
        public String getTranscript() { return transcript; }
        public void setTranscript(String transcript) { this.transcript = transcript; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    public static class VoiceSearchResponse {
        private String transcript;
        private MusicIntent intent;
        private String feedbackMessage;
        private List<TrackDto> tracks = new ArrayList<>();

        public VoiceSearchResponse() {}
        public VoiceSearchResponse(String transcript, MusicIntent intent, String feedbackMessage, List<TrackDto> tracks) {
            this.transcript = transcript;
            this.intent = intent;
            this.feedbackMessage = feedbackMessage;
            this.tracks = tracks;
        }

        public String getTranscript() { return transcript; }
        public void setTranscript(String transcript) { this.transcript = transcript; }
        public MusicIntent getIntent() { return intent; }
        public void setIntent(MusicIntent intent) { this.intent = intent; }
        public String getFeedbackMessage() { return feedbackMessage; }
        public void setFeedbackMessage(String feedbackMessage) { this.feedbackMessage = feedbackMessage; }
        public List<TrackDto> getTracks() { return tracks; }
        public void setTracks(List<TrackDto> tracks) { this.tracks = tracks; }
    }

    public static class RabbitHoleNode {
        private String id;
        private String name;
        private String type; // ARTIST, GENRE, MOOD, PRODUCER
        private String imageUrl;

        public RabbitHoleNode() {}
        public RabbitHoleNode(String id, String name, String type, String imageUrl) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.imageUrl = imageUrl;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class RabbitHoleResponse {
        private String rootId;
        private String rootType;
        private List<RabbitHoleNode> nodes = new ArrayList<>();
        private List<TrackDto> recommendedTracks = new ArrayList<>();

        public RabbitHoleResponse() {}

        public String getRootId() { return rootId; }
        public void setRootId(String rootId) { this.rootId = rootId; }
        public String getRootType() { return rootType; }
        public void setRootType(String rootType) { this.rootType = rootType; }
        public List<RabbitHoleNode> getNodes() { return nodes; }
        public void setNodes(List<RabbitHoleNode> nodes) { this.nodes = nodes; }
        public List<TrackDto> getRecommendedTracks() { return recommendedTracks; }
        public void setRecommendedTracks(List<TrackDto> recommendedTracks) { this.recommendedTracks = recommendedTracks; }
    }
}