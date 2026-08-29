package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.CatalogDtos.*;
import com.sonexa.backend.service.CatalogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = "*")
public class AiSignatureController {

    private final CatalogService catalogService;

    public AiSignatureController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping("/signature")
    public AiSignatureResponse signature(@RequestBody(required = false) AiSignatureRequest body) {
        return catalogService.aiSignature(body != null ? body : new AiSignatureRequest("", "", ""));
    }

    @PostMapping("/chat")
    public AiChatResponse chat(@RequestBody(required = false) AiChatRequest body) {
        return catalogService.aiChat(body != null ? body : new AiChatRequest(""));
    }
}
