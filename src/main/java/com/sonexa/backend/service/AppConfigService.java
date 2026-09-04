package com.sonexa.backend.service;

import com.sonexa.backend.model.dto.CatalogDtos.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AppConfigService {

    public SplashConfigResponse splash() {
        return new SplashConfigResponse(true, "Zynera", "1.0.0", "1.0.0",
                false, false, "Welcome to Zynera AI Music Platform");
    }

    public OnboardingResponse onboarding() {
        return new OnboardingResponse(true, List.of(
                new OnboardingSlideDto("AI Personal DJ", "Music adapted to your current mood, activity and vibe in real-time"),
                new OnboardingSlideDto("Lossless Audio", "Immerse in studio-quality sound with spatial audio capabilities"),
                new OnboardingSlideDto("Smart Discovery", "Discover emerging tracks before anyone else with Zynera AI curation")
        ));
    }

    public LanguagesCatalogResponse languages() {
        return new LanguagesCatalogResponse(
                true,
                "Choose Music Languages",
                "Select languages you love to listen to",
                1,
                List.of("English", "Hindi"),
                List.of(
                        new LanguageDto("en", "English", "International"),
                        new LanguageDto("hi", "Hindi", "हिंदी"),
                        new LanguageDto("pa", "Punjabi", "ਪੰਜਾਬੀ"),
                        new LanguageDto("ta", "Tamil", "தமிழ்"),
                        new LanguageDto("te", "Telugu", "తెలుగు"),
                        new LanguageDto("es", "Spanish", "Español"),
                        new LanguageDto("ko", "K-Pop", "한국어"),
                        new LanguageDto("fr", "French", "Français"),
                        new LanguageDto("ja", "Japanese", "日本語"),
                        new LanguageDto("de", "German", "Deutsch")
                )
        );
    }

    public AppUpdateResponse appUpdate() {
        return new AppUpdateResponse(true, false, false, "1.0.0",
                "You're on the latest version", "https://play.google.com/store/apps/details?id=com.sonexa.app");
    }

    public PermissionsConfigResponse permissions() {
        return new PermissionsConfigResponse(true,
                Map.of(
                        "title", "Stay Tuned with Alerts",
                        "subtitle", "Get instant notifications for new music releases, trending playlists, and Zynera AI DJ recommendations.",
                        "button", "Enable Notifications",
                        "skip", "Maybe Later",
                        "required", false
                ),
                Map.of(
                        "title", "Offline Listening Storage",
                        "subtitle", "Allow storage access to download high quality audio tracks for seamless offline playback anytime, anywhere.",
                        "button", "Allow Download Access",
                        "skip", "Skip for Now",
                        "required", false
                )
        );
    }
}
