package com.musiccatalog.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationServiceCuratedDataTest {

    @Test
    void curatedGenreMapContainsExpectedGenres() throws Exception {
        var field = RecommendationService.class.getDeclaredField("CURATED_BY_GENRE");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, List<String>> curated = (Map<String, List<String>>) field.get(null);

        assertTrue(curated.containsKey("Pop"));
        assertTrue(curated.containsKey("Rock"));
        assertFalse(curated.get("Pop").isEmpty());
    }
}
