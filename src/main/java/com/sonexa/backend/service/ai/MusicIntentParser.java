package com.sonexa.backend.service.ai;

import com.sonexa.backend.model.dto.AiDtos.MusicIntent;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MusicIntentParser {

    private static final Map<String, List<String>> MOOD_DICTIONARY = new HashMap<>();
    private static final Map<String, List<String>> LANGUAGE_DICTIONARY = new HashMap<>();
    private static final Map<String, List<String>> GENRE_DICTIONARY = new HashMap<>();
    private static final Map<String, List<String>> ACTIVITY_DICTIONARY = new HashMap<>();

    static {
        // Moods
        MOOD_DICTIONARY.put("ROMANTIC", Arrays.asList("romantic", "love", "pyaar", "ishq", "dil", "mohabbat", "valentine", "couples"));
        MOOD_DICTIONARY.put("CALM", Arrays.asList("calm", "relaxing", "peaceful", "soothing", "chill", "sukoon", "shanti", "meditation", "stress relief"));
        MOOD_DICTIONARY.put("ENERGETIC", Arrays.asList("energetic", "hype", "pump", "josh", "high energy", "powerful", "banger", "fast"));
        MOOD_DICTIONARY.put("EMOTIONAL", Arrays.asList("emotional", "sad", "dard", "cry", "heartbreak", "breakup", "lonely", "alone", "judaai"));
        MOOD_DICTIONARY.put("PARTY", Arrays.asList("party", "dance", "club", "dj", "nacho", "celebration", "dhamaka", "remix"));
        MOOD_DICTIONARY.put("NOSTALGIC", Arrays.asList("nostalgic", "old", "purane", "classic", "retro", "90s", "80s", "2000s", "vintage", "golden era"));

        // Languages
        LANGUAGE_DICTIONARY.put("Hindi", Arrays.asList("hindi", "bollywood", "desi"));
        LANGUAGE_DICTIONARY.put("Punjabi", Arrays.asList("punjabi", "punjab", "bhangra"));
        LANGUAGE_DICTIONARY.put("English", Arrays.asList("english", "hollywood", "international", "pop", "western"));
        LANGUAGE_DICTIONARY.put("Bhojpuri", Arrays.asList("bhojpuri"));
        LANGUAGE_DICTIONARY.put("Tamil", Arrays.asList("tamil", "kollywood"));
        LANGUAGE_DICTIONARY.put("Telugu", Arrays.asList("telugu", "tollywood"));
        LANGUAGE_DICTIONARY.put("Bengali", Arrays.asList("bengali", "bangla"));
        LANGUAGE_DICTIONARY.put("Marathi", Arrays.asList("marathi"));
        LANGUAGE_DICTIONARY.put("Gujarati", Arrays.asList("gujarati", "garba"));
        LANGUAGE_DICTIONARY.put("Malayalam", Arrays.asList("malayalam"));
        LANGUAGE_DICTIONARY.put("Kannada", Arrays.asList("kannada"));
        LANGUAGE_DICTIONARY.put("Urdu", Arrays.asList("urdu", "ghazal", "qawwali", "sufi"));

        // Genres
        GENRE_DICTIONARY.put("Hip-Hop", Arrays.asList("hip hop", "hip-hop", "rap", "trap", "drill"));
        GENRE_DICTIONARY.put("Pop", Arrays.asList("pop", "dance pop", "synth"));
        GENRE_DICTIONARY.put("Electronic", Arrays.asList("edm", "electronic", "house", "techno", "dubstep"));
        GENRE_DICTIONARY.put("Rock", Arrays.asList("rock", "metal", "alt rock"));
        GENRE_DICTIONARY.put("Acoustic", Arrays.asList("acoustic", "unplugged", "guitar", "piano"));
        GENRE_DICTIONARY.put("Sufi", Arrays.asList("sufi", "qawwali", "spiritual"));
        GENRE_DICTIONARY.put("Lo-Fi", Arrays.asList("lo-fi", "lofi", "study beats"));

        // Activities
        ACTIVITY_DICTIONARY.put("WORKOUT", Arrays.asList("workout", "gym", "running", "jogging", "hiit", "fitness", "training"));
        ACTIVITY_DICTIONARY.put("STUDY", Arrays.asList("study", "studying", "focus", "concentration", "work", "reading"));
        ACTIVITY_DICTIONARY.put("SLEEP", Arrays.asList("sleep", "sleeping", "bedtime", "night", "deep sleep"));
        ACTIVITY_DICTIONARY.put("ROAD_TRIP", Arrays.asList("road trip", "driving", "drive", "car", "travel", "journey"));
    }

    public MusicIntent parse(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            MusicIntent empty = new MusicIntent();
            empty.setConfidence(0.50);
            return empty;
        }

        String text = rawText.trim().toLowerCase(Locale.ROOT);
        MusicIntent intent = new MusicIntent();
        intent.setQuery(rawText.trim());

        // 1. Direct playback control commands
        if (text.equals("next") || text.contains("next song") || text.contains("skip this") || text.contains("skip")) {
            intent.setIntentType("NEXT");
            intent.setAction("NEXT");
            intent.setConfidence(0.98);
            return intent;
        }
        if (text.equals("pause") || text.contains("stop music") || text.contains("pause song") || text.equals("ruk")) {
            intent.setIntentType("PAUSE");
            intent.setAction("PAUSE");
            intent.setConfidence(0.98);
            return intent;
        }
        if (text.equals("resume") || text.contains("resume music") || text.contains("play again") || text.equals("chalao")) {
            intent.setIntentType("PLAY_MUSIC");
            intent.setAction("PLAY");
            intent.setConfidence(0.98);
            return intent;
        }
        if (text.contains("like this") || text.contains("add to favorites") || text.contains("favorite this")) {
            intent.setIntentType("LIKE");
            intent.setAction("LIKE");
            intent.setConfidence(0.98);
            return intent;
        }
        if (text.contains("change vibe") || text.contains("change the vibe") || text.contains("more energetic") || text.contains("more relaxing")) {
            intent.setIntentType("CHANGE_VIBE");
            intent.setAction("CHANGE_VIBE");
            if (text.contains("energetic") || text.contains("fast")) intent.setEnergy(0.85);
            if (text.contains("relax") || text.contains("calm")) intent.setEnergy(0.30);
            intent.setConfidence(0.95);
            return intent;
        }
        if (text.contains("fix queue") || text.contains("balance queue") || text.contains("repair queue")) {
            intent.setIntentType("FIX_QUEUE");
            intent.setAction("FIX");
            intent.setConfidence(0.95);
            return intent;
        }
        if (text.contains("surprise me") || text.contains("something new") || text.contains("discover new")) {
            intent.setIntentType("SURPRISE");
            intent.setAction("SURPRISE");
            intent.setConfidence(0.95);
            return intent;
        }
        if (text.contains("create playlist") || text.contains("make playlist") || text.contains("playlist for")) {
            intent.setIntentType("CREATE_PLAYLIST");
            intent.setAction("CREATE_PLAYLIST");
        } else {
            intent.setIntentType("PLAY_MUSIC");
            intent.setAction("PLAY");
        }

        // 2. Extract Languages
        for (Map.Entry<String, List<String>> entry : LANGUAGE_DICTIONARY.entrySet()) {
            for (String kw : entry.getValue()) {
                if (text.contains(kw)) {
                    if (!intent.getLanguages().contains(entry.getKey())) {
                        intent.getLanguages().add(entry.getKey());
                    }
                }
            }
        }

        // 3. Extract Moods
        for (Map.Entry<String, List<String>> entry : MOOD_DICTIONARY.entrySet()) {
            for (String kw : entry.getValue()) {
                if (text.contains(kw)) {
                    if (!intent.getMoods().contains(entry.getKey())) {
                        intent.getMoods().add(entry.getKey());
                    }
                }
            }
        }

        // 4. Extract Genres
        for (Map.Entry<String, List<String>> entry : GENRE_DICTIONARY.entrySet()) {
            for (String kw : entry.getValue()) {
                if (text.contains(kw)) {
                    if (!intent.getGenres().contains(entry.getKey())) {
                        intent.getGenres().add(entry.getKey());
                    }
                }
            }
        }

        // 5. Extract Activities
        for (Map.Entry<String, List<String>> entry : ACTIVITY_DICTIONARY.entrySet()) {
            for (String kw : entry.getValue()) {
                if (text.contains(kw)) {
                    intent.setActivity(entry.getKey());
                    if (entry.getKey().equals("WORKOUT")) intent.setEnergy(0.85);
                    if (entry.getKey().equals("SLEEP")) intent.setEnergy(0.20);
                    if (entry.getKey().equals("STUDY")) intent.setEnergy(0.40);
                    break;
                }
            }
        }

        // 6. Extract duration if present (e.g. "30 minute", "45 mins", "1 hour")
        Pattern durationPattern = Pattern.compile("(\\d+)\\s*(min|minute|minutes|hr|hour|hours)");
        Matcher durationMatcher = durationPattern.matcher(text);
        if (durationMatcher.find()) {
            int val = Integer.parseInt(durationMatcher.group(1));
            String unit = durationMatcher.group(2);
            if (unit.startsWith("hr") || unit.startsWith("hour")) {
                intent.setDurationMinutes(val * 60);
            } else {
                intent.setDurationMinutes(val);
            }
        }

        // 7. Extract Artist Name if phrased like "play songs by [artist]" or "songs like [artist]"
        Pattern artistPattern = Pattern.compile("(by|like|of|singer)\\s+([a-zA-Z\\s]{3,30})");
        Matcher artistMatcher = artistPattern.matcher(text);
        if (artistMatcher.find()) {
            String candidate = artistMatcher.group(2).trim();
            // clean up common noise words
            candidate = candidate.replaceAll("(songs|music|tracks|playlist|please)", "").trim();
            if (candidate.length() > 2) {
                intent.setArtist(candidate);
            }
        }

        // 8. Confidence calculation
        double conf = 0.80;
        if (!intent.getLanguages().isEmpty()) conf += 0.05;
        if (!intent.getMoods().isEmpty()) conf += 0.05;
        if (intent.getActivity() != null) conf += 0.05;
        if (intent.getArtist() != null) conf += 0.05;
        intent.setConfidence(Math.min(0.98, conf));

        return intent;
    }
}