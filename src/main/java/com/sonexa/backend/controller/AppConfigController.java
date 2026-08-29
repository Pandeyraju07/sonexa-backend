package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.CatalogDtos.*;
import com.sonexa.backend.service.AppConfigService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/config")
@CrossOrigin(origins = "*")
public class AppConfigController {

    private final AppConfigService appConfigService;

    public AppConfigController(AppConfigService appConfigService) {
        this.appConfigService = appConfigService;
    }

    @GetMapping("/splash")
    public SplashConfigResponse splash() {
        return appConfigService.splash();
    }

    @GetMapping("/onboarding")
    public OnboardingResponse onboarding() {
        return appConfigService.onboarding();
    }

    @GetMapping("/languages")
    public LanguagesCatalogResponse languages() {
        return appConfigService.languages();
    }

    @GetMapping("/app-update")
    public AppUpdateResponse appUpdate() {
        return appConfigService.appUpdate();
    }

    @GetMapping("/permissions")
    public PermissionsConfigResponse permissions() {
        return appConfigService.permissions();
    }
}
