package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.CatalogDtos.LiveEventDetailResponse;
import com.sonexa.backend.model.dto.CatalogDtos.LiveEventsFeedResponse;
import com.sonexa.backend.service.liveevents.LiveEventsClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/live-events")
@CrossOrigin(origins = "*")
public class LiveEventsController {

    private final LiveEventsClient liveEventsClient;

    public LiveEventsController(LiveEventsClient liveEventsClient) {
        this.liveEventsClient = liveEventsClient;
    }

    @GetMapping("/feed")
    public ResponseEntity<LiveEventsFeedResponse> getFeed(
            @RequestParam(required = false, defaultValue = "All") String city,
            @RequestParam(required = false, defaultValue = "All") String category
    ) {
        return ResponseEntity.ok(liveEventsClient.getFeed(city, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LiveEventDetailResponse> getDetail(@PathVariable String id) {
        return ResponseEntity.ok(liveEventsClient.getDetail(id));
    }

    @PostMapping("/{id}/remind")
    public ResponseEntity<Map<String, Object>> toggleReminder(@PathVariable String id) {
        boolean isReminded = liveEventsClient.toggleReminder(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "eventId", id,
                "isReminderSet", isReminded,
                "message", isReminded ? "Reminder set! We will notify you before ticket release." : "Reminder removed."
        ));
    }
}