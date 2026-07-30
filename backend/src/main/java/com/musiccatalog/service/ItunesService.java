package com.musiccatalog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiccatalog.dto.response.AlbumSearchResult;
import com.musiccatalog.exception.ItunesApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItunesService {

    private static final String SEARCH_URL = "https://itunes.apple.com/search";
    private static final String LOOKUP_URL = "https://itunes.apple.com/lookup";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Cacheable(value = "itunesSearch", key = "#query + '-' + #limit")
    public List<AlbumSearchResult> searchAlbums(String query, int limit) {
        String url = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
                .queryParam("term", query)
                .queryParam("entity", "album")
                .queryParam("limit", limit)
                .build()
                .toUriString();
        return fetchAndParseResults(url);
    }

    @Cacheable(value = "itunesLookup", key = "#collectionId")
    public AlbumSearchResult lookupAlbum(Long collectionId) {
        String url = UriComponentsBuilder.fromHttpUrl(LOOKUP_URL)
                .queryParam("id", collectionId)
                .queryParam("entity", "album")
                .build()
                .toUriString();
        List<AlbumSearchResult> results = fetchAndParseResults(url);
        if (results.isEmpty()) {
            throw new ItunesApiException("Album not found for id: " + collectionId);
        }
        return results.get(0);
    }

    private List<AlbumSearchResult> fetchAndParseResults(String url) {
        try {
            String body = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(body);
            JsonNode results = root.path("results");
            List<AlbumSearchResult> albums = new ArrayList<>();
            if (results.isArray()) {
                for (JsonNode node : results) {
                    if (!"collection".equals(node.path("wrapperType").asText())) {
                        continue;
                    }
                    albums.add(mapToAlbum(node));
                }
            }
            return albums;
        } catch (ItunesApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("iTunes API error for url {}: {}", url, e.getMessage());
            throw new ItunesApiException("Failed to fetch from iTunes", e);
        }
    }

    private AlbumSearchResult mapToAlbum(JsonNode node) {
        String artwork = node.path("artworkUrl100").asText(null);
        if (artwork != null) {
            artwork = artwork.replace("100x100bb", "300x300bb");
        }
        String releaseDateStr = node.path("releaseDate").asText(null);
        String releaseDate = null;
        if (releaseDateStr != null && releaseDateStr.length() >= 10) {
            releaseDate = releaseDateStr.substring(0, 10);
        }
        return AlbumSearchResult.builder()
                .appleCatalogId(node.path("collectionId").asLong())
                .title(node.path("collectionName").asText(null))
                .artistName(node.path("artistName").asText(null))
                .genre(node.path("primaryGenreName").asText(null))
                .releaseDate(releaseDate)
                .trackCount(node.has("trackCount") ? node.path("trackCount").asInt() : null)
                .artworkUrl(artwork)
                .price(node.has("collectionPrice") ? node.path("collectionPrice").asDouble() : null)
                .build();
    }
}
