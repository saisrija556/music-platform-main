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

    private static final Map<String, List<String>> CURATED_BY_GENRE = Map.of(
            "Pop", List.of("1989 Taylor Swift", "Future Nostalgia Dua Lipa", "Thriller Michael Jackson"),
            "Rock", List.of("Abbey Road Beatles", "Nevermind Nirvana", "The Dark Side of the Moon Pink Floyd"),
            "Hip-Hop/Rap", List.of("To Pimp a Butterfly Kendrick Lamar", "Illmatic Nas", "My Beautiful Dark Twisted Fantasy Kanye West"),
            "R&B/Soul", List.of("Songs in A Minor Alicia Keys", "Channel Orange Frank Ocean", "What's Going On Marvin Gaye"),
            "Alternative", List.of("OK Computer Radiohead", "In the Aeroplane Over the Sea Neutral Milk Hotel", "Funeral Arcade Fire"),
            "Jazz", List.of("Kind of Blue Miles Davis", "A Love Supreme John Coltrane", "Time Out Dave Brubeck"),
            "Classical", List.of("The Four Seasons Vivaldi", "Symphony No. 9 Beethoven", "Goldberg Variations Bach"),
            "Country", List.of("Golden Hour Kacey Musgraves", "Red Taylor Swift", "No Fences Garth Brooks"),
            "Electronic", List.of("Random Access Memories Daft Punk", "Discovery Daft Punk", "Immunity Jon Hopkins")
    );

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
        String topGenre = library.stream()
                .filter(i -> i.getGenre() != null)
                .collect(Collectors.groupingBy(LibraryItem::getGenre, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Pop");

        List<String> seeds = CURATED_BY_GENRE.getOrDefault(topGenre,
                CURATED_BY_GENRE.get("Pop"));

        List<RecommendationsResponse.Recommendation> recommendations = new ArrayList<>();
        for (String seed : seeds) {
            if (recommendations.size() >= 5) break;
            try {
                List<AlbumSearchResult> results = itunesService.searchAlbums(seed, 5);
                for (AlbumSearchResult album : results) {
                    if (ownedIds.contains(album.getAppleCatalogId())) continue;
                    recommendations.add(RecommendationsResponse.Recommendation.builder()
                            .title(album.getTitle())
                            .artistName(album.getArtistName())
                            .genre(album.getGenre())
                            .rationale("Based on your interest in " + topGenre + " — a well-loved album in that genre.")
                            .appleCatalogId(album.getAppleCatalogId())
                            .artworkUrl(album.getArtworkUrl())
                            .build());
                    break;
                }
            } catch (Exception e) {
                log.debug("Heuristic search failed for seed {}: {}", seed, e.getMessage());
            }
        }

        return RecommendationsResponse.builder()
                .recommendations(recommendations)
                .source("heuristic")
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
                The user already owns these albums (do NOT recommend any of these):
                %s

                Their library profile:
                - Top genres: %s
                - Artists they collect: %s
                - Release eras: %s

                Recommend 3-5 albums they do NOT already own. Return JSON array only.
                """, titles, genres, artists, years);
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
