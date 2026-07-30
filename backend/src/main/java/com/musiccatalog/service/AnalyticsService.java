package com.musiccatalog.service;

import com.musiccatalog.dto.response.AnalyticsSummaryResponse;
import com.musiccatalog.entity.User;
import com.musiccatalog.repository.LibraryItemRepository;
import com.musiccatalog.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final LibraryItemRepository libraryItemRepository;
    private final CurrentUserService currentUserService;

    public AnalyticsSummaryResponse getSummary() {
        User user = currentUserService.getCurrentUser();
        long total = libraryItemRepository.findByUser(user).size();

        Map<String, Long> albumsByGenre = toMapLong(libraryItemRepository.countByGenre(user));
        Map<String, Double> genreDistributionPercent = computePercentages(albumsByGenre, total);
        Map<String, Long> releasesByYear = libraryItemRepository.countByReleaseYear(user).stream()
                .collect(Collectors.toMap(
                        row -> String.valueOf(((Number) row[0]).intValue()),
                        row -> ((Number) row[1]).longValue(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<AnalyticsSummaryResponse.ArtistCount> topArtists = libraryItemRepository.countByArtist(user).stream()
                .limit(10)
                .map(row -> AnalyticsSummaryResponse.ArtistCount.builder()
                        .artistName((String) row[0])
                        .albumCount(((Number) row[1]).longValue())
                        .build())
                .toList();

        Map<String, Double> averageRatingByGenre = libraryItemRepository.averageRatingByGenre(user).stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> Math.round(((Number) row[1]).doubleValue() * 10.0) / 10.0,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Long> trackCountDistribution = buildTrackCountBuckets(
                libraryItemRepository.findTrackCounts(user));

        Double averageRating = libraryItemRepository.averageUserRating(user);
        if (averageRating != null) {
            averageRating = Math.round(averageRating * 10.0) / 10.0;
        }

        return AnalyticsSummaryResponse.builder()
                .totalAlbums(total)
                .averageRating(averageRating)
                .albumsByGenre(albumsByGenre)
                .genreDistributionPercent(genreDistributionPercent)
                .releasesByYear(releasesByYear)
                .topArtists(topArtists)
                .averageRatingByGenre(averageRatingByGenre)
                .trackCountDistribution(trackCountDistribution)
                .build();
    }

    private Map<String, Long> toMapLong(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue(),
                (a, b) -> a,
                LinkedHashMap::new
        ));
    }

    private Map<String, Double> computePercentages(Map<String, Long> counts, long total) {
        if (total == 0) {
            return Map.of();
        }
        return counts.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Math.round(e.getValue() * 1000.0 / total) / 10.0,
                (a, b) -> a,
                LinkedHashMap::new
        ));
    }

    private Map<String, Long> buildTrackCountBuckets(List<Integer> trackCounts) {
        Map<String, Long> buckets = new LinkedHashMap<>();
        buckets.put("1-8", 0L);
        buckets.put("9-12", 0L);
        buckets.put("13-16", 0L);
        buckets.put("17+", 0L);
        for (Integer count : trackCounts) {
            if (count == null) continue;
            if (count <= 8) buckets.merge("1-8", 1L, Long::sum);
            else if (count <= 12) buckets.merge("9-12", 1L, Long::sum);
            else if (count <= 16) buckets.merge("13-16", 1L, Long::sum);
            else buckets.merge("17+", 1L, Long::sum);
        }
        return buckets;
    }
}
