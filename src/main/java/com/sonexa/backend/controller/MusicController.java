package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.CatalogDtos.*;
import com.sonexa.backend.service.CatalogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/music")
@CrossOrigin(origins = "*")
public class MusicController {

    private final CatalogService catalogService;

    public MusicController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/home")
    public HomeFeedResponse home() {
        return catalogService.homeFeed();
    }

    @GetMapping("/trending")
    public TrendingResponse trending() {
        return catalogService.trending();
    }

    @GetMapping("/search")
    public SearchResponse search(@RequestParam(value = "q", defaultValue = "") String q) {
        return catalogService.search(q);
    }

    @GetMapping("/tracks/{id}/lyrics")
    public LyricsResponse lyrics(@PathVariable String id) {
        return catalogService.trackLyrics(id);
    }

    @GetMapping("/lyrics")
    public LyricsResponse lyricsByQuery(@RequestParam("trackId") String trackId) {
        return catalogService.trackLyrics(trackId);
    }

    @GetMapping("/tracks/{id}")
    public TrackDetailResponse track(@PathVariable String id) {
        return catalogService.trackDetail(id);
    }
    @GetMapping("/albums/{id}")
    public AlbumDetailResponse album(@PathVariable String id) {
        return catalogService.albumDetail(id);
    }

    @GetMapping("/playlists/{id}")
    public PlaylistDetailResponse playlist(@PathVariable String id) {
        return catalogService.playlistDetail(id);
    }

    @GetMapping("/artists/{id}")
    public ArtistDetailResponse artist(@PathVariable String id) {
        return catalogService.artistDetail(id);
    }

    @GetMapping("/genres")
    public GenreListResponse genres() {
        return catalogService.genres();
    }

    @GetMapping("/artists")
    public ArtistListResponse artists() {
        return catalogService.artists();
    }

    @GetMapping("/moods")
    public MoodListResponse moods() {
        return catalogService.moods();
    }

    @GetMapping("/queue")
    public QueueResponse queue() {
        return catalogService.queue();
    }
}
