package com.musiccatalog.service;

import com.musiccatalog.dto.request.CreateLibraryItemRequest;
import com.musiccatalog.dto.request.UpdateLibraryItemRequest;
import com.musiccatalog.dto.response.AlbumSearchResult;
import com.musiccatalog.dto.response.LibraryItemResponse;
import com.musiccatalog.entity.LibraryItem;
import com.musiccatalog.entity.User;
import com.musiccatalog.exception.ApiException;
import com.musiccatalog.repository.LibraryItemRepository;
import com.musiccatalog.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryItemRepository libraryItemRepository;
    private final ItunesService itunesService;
    private final CurrentUserService currentUserService;

    public Page<LibraryItemResponse> getLibrary(Pageable pageable) {
        User user = currentUserService.getCurrentUser();
        return libraryItemRepository.findByUser(user, pageable).map(this::toResponse);
    }

    @Transactional
    public LibraryItemResponse addToLibrary(CreateLibraryItemRequest request) {
        User user = currentUserService.getCurrentUser();
        if (libraryItemRepository.existsByAppleCatalogIdAndUser(request.getAppleCatalogId(), user)) {
            throw new ApiException(HttpStatus.CONFLICT, "Album already in library");
        }

        AlbumSearchResult album = resolveAlbumMetadata(request);
        LibraryItem item = LibraryItem.builder()
                .appleCatalogId(album.getAppleCatalogId())
                .title(album.getTitle())
                .artistName(album.getArtistName())
                .genre(album.getGenre())
                .releaseDate(parseDate(album.getReleaseDate()))
                .trackCount(album.getTrackCount())
                .artworkUrl(album.getArtworkUrl())
                .user(user)
                .build();
        return toResponse(libraryItemRepository.save(item));
    }

    @Transactional
    public LibraryItemResponse updateItem(Long id, UpdateLibraryItemRequest request) {
        User user = currentUserService.getCurrentUser();
        LibraryItem item = libraryItemRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Library item not found"));
        if (request.getUserRating() != null) {
            item.setUserRating(request.getUserRating());
        }
        if (request.getUserNotes() != null) {
            item.setUserNotes(request.getUserNotes());
        }
        return toResponse(libraryItemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long id) {
        User user = currentUserService.getCurrentUser();
        LibraryItem item = libraryItemRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Library item not found"));
        libraryItemRepository.delete(item);
    }

    private AlbumSearchResult resolveAlbumMetadata(CreateLibraryItemRequest request) {
        if (StringUtils.hasText(request.getTitle()) && StringUtils.hasText(request.getArtistName())) {
            return AlbumSearchResult.builder()
                    .appleCatalogId(request.getAppleCatalogId())
                    .title(request.getTitle())
                    .artistName(request.getArtistName())
                    .genre(request.getGenre())
                    .releaseDate(request.getReleaseDate())
                    .trackCount(request.getTrackCount())
                    .artworkUrl(request.getArtworkUrl())
                    .build();
        }
        return itunesService.lookupAlbum(request.getAppleCatalogId());
    }

    private LocalDate parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.substring(0, Math.min(10, dateStr.length())));
        } catch (Exception e) {
            return null;
        }
    }

    private LibraryItemResponse toResponse(LibraryItem item) {
        return LibraryItemResponse.builder()
                .id(item.getId())
                .appleCatalogId(item.getAppleCatalogId())
                .title(item.getTitle())
                .artistName(item.getArtistName())
                .genre(item.getGenre())
                .releaseDate(item.getReleaseDate())
                .trackCount(item.getTrackCount())
                .artworkUrl(item.getArtworkUrl())
                .userRating(item.getUserRating())
                .userNotes(item.getUserNotes())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
