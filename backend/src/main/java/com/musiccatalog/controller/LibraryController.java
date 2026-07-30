package com.musiccatalog.controller;

import com.musiccatalog.dto.request.CreateLibraryItemRequest;
import com.musiccatalog.dto.request.UpdateLibraryItemRequest;
import com.musiccatalog.dto.response.LibraryItemResponse;
import com.musiccatalog.service.LibraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping
    public Page<LibraryItemResponse> getLibrary(
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return libraryService.getLibrary(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryItemResponse addToLibrary(@Valid @RequestBody CreateLibraryItemRequest request) {
        return libraryService.addToLibrary(request);
    }

    @PutMapping("/{id}")
    public LibraryItemResponse updateItem(@PathVariable Long id, @Valid @RequestBody UpdateLibraryItemRequest request) {
        return libraryService.updateItem(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long id) {
        libraryService.deleteItem(id);
    }
}
