package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.AiDtos.RabbitHoleResponse;
import com.sonexa.backend.model.dto.ApiResponse;
import com.sonexa.backend.service.ai.RabbitHoleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/discovery")
@CrossOrigin(origins = "*")
public class DiscoveryController {

    private final RabbitHoleService rabbitHoleService;

    public DiscoveryController(RabbitHoleService rabbitHoleService) {
        this.rabbitHoleService = rabbitHoleService;
    }

    @GetMapping("/rabbit-hole/{type}/{id}")
    public ApiResponse<RabbitHoleResponse> getRabbitHole(
            @PathVariable("type") String type,
            @PathVariable("id") String id
    ) {
        return ApiResponse.success(rabbitHoleService.getRabbitHole(type, id));
    }
}