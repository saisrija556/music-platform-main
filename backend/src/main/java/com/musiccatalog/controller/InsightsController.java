package com.musiccatalog.controller;

import com.musiccatalog.dto.response.RecommendationsResponse;
import com.musiccatalog.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightsController {

    private final RecommendationService recommendationService;

    @GetMapping("/recommendations")
    public RecommendationsResponse getRecommendations() {
        return recommendationService.getRecommendations();
    }
}
