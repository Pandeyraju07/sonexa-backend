package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.CatalogDtos.*;
import com.sonexa.backend.service.CatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile-setup")
@CrossOrigin(origins = "*")
public class ProfileSetupController {

    private final CatalogService catalogService;

    public ProfileSetupController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/avatars")
    public Map<String, Object> avatars() {
        return Map.of(
                "success", true,
                "avatars", List.of(
                        Map.of("id", "av_1", "style", "Neon Purple"),
                        Map.of("id", "av_2", "style", "Cyber Cyan"),
                        Map.of("id", "av_3", "style", "Emerald Wave"),
                        Map.of("id", "av_4", "style", "Sunset Red")
                )
        );
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody ProfileCreateRequest body) {
        SimpleSuccessResponse result = catalogService.createProfile(
                body != null ? body : new ProfileCreateRequest("Zynera Listener", "@zynera_user"));
        return Map.of(
                "success", result.success(),
                "message", result.message(),
                "handle", body != null && body.handle() != null ? body.handle() : "@zynera_user"
        );
    }

    @GetMapping("/genres")
    public GenreListResponse genres() {
        return catalogService.genres();
    }

    @PostMapping("/genres")
    public SaveListResponse saveGenres(@RequestBody Map<String, Object> body) {
        return catalogService.savePrefs("GENRE", extractList(body, "genres", "items"));
    }

    @GetMapping("/artists")
    public ArtistListResponse artists() {
        return catalogService.artists();
    }

    @PostMapping("/artists")
    public SaveListResponse saveArtists(@RequestBody Map<String, Object> body) {
        return catalogService.savePrefs("ARTIST", extractList(body, "artists", "items"));
    }

    @GetMapping("/moods")
    public MoodListResponse moods() {
        return catalogService.moods();
    }

    @PostMapping("/moods")
    public SaveListResponse saveMoods(@RequestBody Map<String, Object> body) {
        return catalogService.savePrefs("MOOD", extractList(body, "moods", "items"));
    }

    @GetMapping("/languages")
    public Map<String, Object> getLanguages() {
        List<String> selected = catalogService.getPrefs("LANGUAGE");
        if (selected.isEmpty()) selected = List.of("English", "Hindi");
        return Map.of("success", true, "languages", selected);
    }

    @PostMapping("/languages")
    public SaveListResponse saveLanguages(@RequestBody LanguagesSaveRequest body) {
        List<String> languages = body != null && body.languages() != null ? body.languages() : List.of();
        SaveListResponse saved = catalogService.savePrefs("LANGUAGE", languages);
        return new SaveListResponse(saved.success(),
                saved.success() ? "Languages saved successfully" : saved.message(),
                saved.items(), saved.count());
    }

    @GetMapping("/permissions")
    public Map<String, Object> getPermissionPrefs() {
        return catalogService.permissionPrefs();
    }

    @PostMapping("/permissions")
    public Map<String, Object> savePermissionPrefs(@RequestBody Map<String, Object> body) {
        boolean notifications = body != null && Boolean.TRUE.equals(body.get("notificationsEnabled"));
        boolean downloads = body != null && Boolean.TRUE.equals(body.get("downloadsEnabled"));
        return catalogService.savePermissionPrefs(notifications, downloads);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractList(Map<String, Object> body, String... keys) {
        if (body == null) return List.of();
        for (String key : keys) {
            Object raw = body.get(key);
            if (raw instanceof List<?> list) {
                List<String> out = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Map<?, ?> map && map.get("name") != null) {
                        out.add(map.get("name").toString());
                    } else if (item != null && !item.toString().isBlank()) {
                        out.add(item.toString().trim());
                    }
                }
                return out;
            }
        }
        return List.of();
    }
}
