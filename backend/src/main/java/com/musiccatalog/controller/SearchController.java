package com.musiccatalog.controller;

import com.musiccatalog.dto.response.AlbumSearchResult;
import com.musiccatalog.exception.ApiException;
import com.musiccatalog.service.ItunesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final ItunesService itunesService;

    @GetMapping
    public List<AlbumSearchResult> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "album") String type,
            @RequestParam(defaultValue = "25") int limit) {
        if (!"album".equalsIgnoreCase(type)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only type=album is supported");
        }
        if (query.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Query must not be blank");
        }
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return itunesService.searchAlbums(query.trim(), safeLimit);
    }
}
