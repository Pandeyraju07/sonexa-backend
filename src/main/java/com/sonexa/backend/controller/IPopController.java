package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.CatalogDtos.IPopArtistDto;
import com.sonexa.backend.model.dto.CatalogDtos.IPopHomeResponse;
import com.sonexa.backend.model.dto.CatalogDtos.IPopPlaylistDto;
import com.sonexa.backend.service.ipop.IPopClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ipop")
@CrossOrigin(origins = "*")
public class IPopController {

    private final IPopClient iPopClient;

    public IPopController(IPopClient iPopClient) {
        this.iPopClient = iPopClient;
    }

    @GetMapping("/feed")
    public ResponseEntity<IPopHomeResponse> getHomeFeed(@RequestParam(required = false, defaultValue = "All") String subgenre) {
        return ResponseEntity.ok(iPopClient.getHomeFeed(subgenre));
    }

    @GetMapping("/playlist/{id}")
    public ResponseEntity<IPopPlaylistDto> getPlaylist(@PathVariable String id) {
        return ResponseEntity.ok(iPopClient.getPlaylist(id));
    }

    @GetMapping("/artists")
    public ResponseEntity<List<IPopArtistDto>> getArtists() {
        return ResponseEntity.ok(iPopClient.getArtists());
    }
}