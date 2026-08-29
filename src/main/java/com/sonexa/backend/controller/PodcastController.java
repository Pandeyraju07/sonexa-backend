package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.CatalogDtos.*;
import com.sonexa.backend.service.CatalogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/podcasts")
@CrossOrigin(origins = "*")
public class PodcastController {

    private final CatalogService catalogService;

    public PodcastController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public PodcastListResponse list() {
        return catalogService.podcasts();
    }

    @GetMapping("/{id}")
    public PodcastDetailResponse detail(@PathVariable String id) {
        return catalogService.podcastDetail(id);
    }
}
