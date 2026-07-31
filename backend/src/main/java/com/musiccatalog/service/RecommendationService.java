package com.musiccatalog.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiccatalog.dto.response.AlbumSearchResult;
import com.musiccatalog.dto.response.RecommendationsResponse;
import com.musiccatalog.entity.LibraryItem;
import com.musiccatalog.entity.User;
import com.musiccatalog.repository.LibraryItemRepository;
import com.musiccatalog.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

   

    private final LibraryItemRepository libraryItemRepository;
    private final CurrentUserService currentUserService;
    private final ItunesService itunesService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${app.openai.api-key:}")
    private String openAiApiKey;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String openAiModel;

    @Cacheable(value = "recommendations", key = "#root.target.currentUserId()")
    public RecommendationsResponse getRecommendations() {
        User user = currentUserService.getCurrentUser();
        List<LibraryItem> library = libraryItemRepository.findByUser(user);

        if (library.isEmpty()) {
            return RecommendationsResponse.builder()
                    .recommendations(List.of())
                    .source("empty-library")
                    .build();
        }

        Set<Long> ownedIds = library.stream()
                .map(LibraryItem::getAppleCatalogId)
                .collect(Collectors.toSet());

        if (StringUtils.hasText(openAiApiKey)) {
            try {
                return getLlmRecommendations(library, ownedIds);
            } catch (Exception e) {
                log.warn("LLM recommendations failed, falling back to heuristic: {}", e.getMessage());
            }
        }

        return getHeuristicRecommendations(library, ownedIds);
    }

    public Long currentUserId() {
        return currentUserService.getCurrentUser().getId();
    }

    private RecommendationsResponse getLlmRecommendations(List<LibraryItem> library, Set<Long> ownedIds) throws Exception {
        String prompt = buildPrompt(library);
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", openAiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a music recommendation assistant. Respond ONLY with valid JSON array of 3-5 objects with keys: title, artistName, genre, rationale. No markdown."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        ));

        String response = restClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + openAiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode content = objectMapper.readTree(response)
                .path("choices").path(0).path("message").path("content");
        String json = content.asText().trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("^```json?\\s*", "").replaceAll("```\\s*$", "").trim();
        }

        List<Map<String, String>> raw = objectMapper.readValue(json, new TypeReference<>() {});
        List<RecommendationsResponse.Recommendation> recommendations = new ArrayList<>();

        for (Map<String, String> item : raw) {
            RecommendationsResponse.Recommendation rec = enrichWithItunes(item, ownedIds);
            if (rec != null) {
                recommendations.add(rec);
            }
            if (recommendations.size() >= 5) break;
        }

        return RecommendationsResponse.builder()
                .recommendations(recommendations)
                .source("openai")
                .build();
    }

    private RecommendationsResponse getHeuristicRecommendations(List<LibraryItem> library, Set<Long> ownedIds) {

    List<RecommendationsResponse.Recommendation> recommendations = new ArrayList<>();

    Set<Long> added = new HashSet<>();

    Map<String, Long> genreCount = library.stream()
            .filter(i -> i.getGenre() != null)
            .collect(Collectors.groupingBy(
                    LibraryItem::getGenre,
                    Collectors.counting()));

    List<String> topGenres = genreCount.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .toList();

    Map<String, Long> artistCount = library.stream()
            .collect(Collectors.groupingBy(
                    LibraryItem::getArtistName,
                    Collectors.counting()));

    List<String> topArtists = artistCount.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .toList();

    // Recommend albums from favourite artists

    for (String artist : topArtists) {

        try {

            List<AlbumSearchResult> albums =
                    itunesService.searchAlbums(artist, 10);

            for (AlbumSearchResult album : albums) {

                if (ownedIds.contains(album.getAppleCatalogId()))
                    continue;

                if (!added.add(album.getAppleCatalogId()))
                    continue;

                recommendations.add(
                        RecommendationsResponse.Recommendation.builder()
                                .title(album.getTitle())
                                .artistName(album.getArtistName())
                                .genre(album.getGenre())
                                .appleCatalogId(album.getAppleCatalogId())
                                .artworkUrl(album.getArtworkUrl())
                                .rationale("Because you enjoy albums by " + artist)
                                .build()
                );

                if (recommendations.size() >= 5)
                    break;
            }

        } catch (Exception ignored) {
        }

        if (recommendations.size() >= 5)
            break;
    }

    // Recommend from favourite genres

    if (recommendations.size() < 5) {

        for (String genre : topGenres) {

            try {

                List<AlbumSearchResult> albums =
                        itunesService.searchAlbums(genre, 10);

                for (AlbumSearchResult album : albums) {

                    if (ownedIds.contains(album.getAppleCatalogId()))
                        continue;

                    if (!added.add(album.getAppleCatalogId()))
                        continue;

                    recommendations.add(
                            RecommendationsResponse.Recommendation.builder()
                                    .title(album.getTitle())
                                    .artistName(album.getArtistName())
                                    .genre(album.getGenre())
                                    .appleCatalogId(album.getAppleCatalogId())
                                    .artworkUrl(album.getArtworkUrl())
                                    .rationale("Recommended because you often listen to " + genre)
                                    .build()
                    );

                    if (recommendations.size() >= 5)
                        break;
                }

            } catch (Exception ignored) {
            }

            if (recommendations.size() >= 5)
                break;
        }
    }

    return RecommendationsResponse.builder()
            .recommendations(recommendations)
            .source("heuristic-smart")
            .build();
}

    private String buildPrompt(List<LibraryItem> library) {
        Map<String, Long> genres = library.stream()
                .filter(i -> i.getGenre() != null)
                .collect(Collectors.groupingBy(LibraryItem::getGenre, Collectors.counting()));
        List<String> artists = library.stream()
                .map(LibraryItem::getArtistName)
                .distinct()
                .limit(15)
                .toList();
        List<String> titles = library.stream()
                .map(LibraryItem::getTitle)
                .limit(20)
                .toList();
        List<String> years = library.stream()
                .filter(i -> i.getReleaseDate() != null)
                .map(i -> String.valueOf(i.getReleaseDate().getYear()))
                .distinct()
                .sorted()
                .toList();

        return String.format("""
The user owns these albums:

%s

Favourite genres:

%s

Favourite artists:

%s

Favourite release years:

%s

Recommend five albums that are NOT already owned.

Prefer:
- same artists
- similar artists
- same genres
- similar release era

Each recommendation must include:
title,
artistName,
genre,
rationale.

Return ONLY a JSON array.
""",
titles,
genres,
artists,
years);
    }

    private RecommendationsResponse.Recommendation enrichWithItunes(Map<String, String> item, Set<Long> ownedIds) {
        String query = item.get("title") + " " + item.get("artistName");
        try {
            List<AlbumSearchResult> results = itunesService.searchAlbums(query, 3);
            for (AlbumSearchResult album : results) {
                if (!ownedIds.contains(album.getAppleCatalogId())) {
                    return RecommendationsResponse.Recommendation.builder()
                            .title(album.getTitle())
                            .artistName(album.getArtistName())
                            .genre(album.getGenre() != null ? album.getGenre() : item.get("genre"))
                            .rationale(item.get("rationale"))
                            .appleCatalogId(album.getAppleCatalogId())
                            .artworkUrl(album.getArtworkUrl())
                            .build();
                }
            }
        } catch (Exception e) {
            log.debug("Could not enrich recommendation: {}", e.getMessage());
        }
        return RecommendationsResponse.Recommendation.builder()
                .title(item.get("title"))
                .artistName(item.get("artistName"))
                .genre(item.get("genre"))
                .rationale(item.get("rationale"))
                .build();
    }
}
