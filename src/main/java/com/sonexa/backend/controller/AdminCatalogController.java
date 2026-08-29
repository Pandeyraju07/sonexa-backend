package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.AdminDtos.*;
import com.sonexa.backend.service.AdminCatalogService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;

    public AdminCatalogController(AdminCatalogService adminCatalogService) {
        this.adminCatalogService = adminCatalogService;
    }

    @GetMapping("/stats")
    public AdminStatsResponse stats() {
        return adminCatalogService.stats();
    }

    @GetMapping("/tracks")
    public AdminTrackListResponse listTracks() {
        return adminCatalogService.listTracks();
    }

    @PostMapping("/tracks")
    public AdminTrackResponse createTrack(@RequestBody CreateTrackRequest body) {
        return adminCatalogService.createTrack(body);
    }

    @PostMapping(value = "/tracks/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminTrackResponse uploadTrack(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "cover", required = false) MultipartFile cover,
            @RequestParam("title") String title,
            @RequestParam(value = "artistName", required = false) String artistName,
            @RequestParam(value = "albumTitle", required = false) String albumTitle,
            @RequestParam(value = "durationMs", required = false) Long durationMs,
            @RequestParam(value = "trending", required = false, defaultValue = "false") Boolean trending,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "subgenre", required = false) String subgenre,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "explicit", required = false, defaultValue = "false") Boolean explicit,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "releaseType", required = false) String releaseType,
            @RequestParam(value = "releaseDate", required = false) String releaseDate
    ) {
        return adminCatalogService.uploadTrack(
                file, cover, title, artistName, albumTitle, durationMs, trending,
                genre, subgenre, language, explicit, description, tags, releaseType, releaseDate
        );
    }

    @PutMapping("/tracks/{id}")
    public AdminTrackResponse updateTrack(@PathVariable Long id, @RequestBody UpdateTrackRequest body) {
        return adminCatalogService.updateTrack(id, body);
    }

    @DeleteMapping("/tracks/{id}")
    public SimpleMessageResponse deleteTrack(@PathVariable Long id) {
        return adminCatalogService.deleteTrack(id);
    }

    @GetMapping("/artists")
    public AdminArtistListResponse listArtists() {
        return adminCatalogService.listArtists();
    }

    @PostMapping("/artists")
    public AdminArtistResponse createArtist(@RequestBody CreateArtistRequest body) {
        return adminCatalogService.createArtist(body);
    }

    @GetMapping("/albums")
    public AdminAlbumListResponse listAlbums() {
        return adminCatalogService.listAlbums();
    }

    @PostMapping("/albums")
    public AdminAlbumResponse createAlbum(@RequestBody CreateAlbumRequest body) {
        return adminCatalogService.createAlbum(body);
    }

    @GetMapping("/genres")
    public AdminGenreListResponse listGenres() {
        return adminCatalogService.listGenres();
    }

    @PostMapping("/genres")
    public AdminGenreResponse createGenre(@RequestBody CreateGenreRequest body) {
        return adminCatalogService.createGenre(body);
    }
}
