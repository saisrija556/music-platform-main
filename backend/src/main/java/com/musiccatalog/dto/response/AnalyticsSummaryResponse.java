package com.musiccatalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryResponse {
    private long totalAlbums;
    private Double averageRating;
    private Map<String, Long> albumsByGenre;
    private Map<String, Double> genreDistributionPercent;
    private Map<String, Long> releasesByYear;
    private List<ArtistCount> topArtists;
    private Map<String, Double> averageRatingByGenre;
    private Map<String, Long> trackCountDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArtistCount {
        private String artistName;
        private long albumCount;
    }
}
